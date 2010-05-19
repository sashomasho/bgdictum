package org.apelikecoder.bgdictum;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.Toast;

public class Setup extends Activity implements DB, ProgressListener, OnCancelListener {

    private class InstanceState {
        private DownloadingTask downloader;
        private ExtractTask extractor;
        private String downloadFile;
    }

    private static final int DLG_CONFIRM_DOWNLOAD = 0;
    private static final int DLG_PROGRESS = 1;
    private App app;
    private InstanceState state;
    private ProgressDialog progress;
    private PowerManager.WakeLock mLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PowerManager mgr = (PowerManager) getSystemService(POWER_SERVICE);
        mLock = mgr.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "bgdictum");
        mLock.acquire();
        app = (App) getApplication();
        state = (InstanceState) getLastNonConfigurationInstance();
        if (state == null) {
            state = new InstanceState();
            showDialog(DLG_CONFIRM_DOWNLOAD);
        } else {
            if (state.downloader != null) {
                state.downloader.setProgressListener(this);
            }
            if (state.extractor != null)
                state.extractor.setProgressListener(this);
        }
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dlg = null;
        switch (id) {
            case DLG_CONFIRM_DOWNLOAD:
                dlg = new AlertDialog.Builder(this)
                    .setMessage(R.string.download_question)
                    .setPositiveButton(R.string.download, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startDownload();
                            showDialog(DLG_PROGRESS);
                        }
                    })
                    .setNegativeButton(R.string.quit, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Setup.this.finish();
                        }
                    })
                    .create();
                break;
            case DLG_PROGRESS:
                dlg = progress = new ProgressDialog(this);
                progress.setMessage(getString(R.string.please_wait));
                dlg.setCancelable(true);
                dlg.setOnCancelListener(this);
                break;
        }
        return dlg;
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        if (state.downloader != null)
            state.downloader.setProgressListener(null);
        if (state.extractor != null)
            state.extractor.setProgressListener(null);
        return state;
    }

    private void startDownload() {
        try {
            state.downloadFile = File.createTempFile("download", null,
                new File(app.getDataPath())).getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.unable_to_create_file, Toast.LENGTH_LONG);
            return;
        }
        state.downloader = new DownloadingTask(this, getString(R.string.dictionary_url), state.downloadFile);
        state.downloader.execute();
    }

    private void finish(String reason) {
        Toast.makeText(this, reason, Toast.LENGTH_LONG).show();
        finish();
    }

    public Context getContext() {
        return this;
    }

    public void onComplete(boolean success) {
        if (!success) {
            String errorMsg = state.extractor != null 
                    ? state.extractor.getError() : state.downloader.getError();
            if (errorMsg == null)
                errorMsg = "unknown error";
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG);
            finish();
            finish(errorMsg);
            return;
        }

        if (state.extractor == null) {
            state.extractor = new ExtractTask(this, state.downloadFile, app.getDataPath() + File.separatorChar + DATABASE);
            state.extractor.execute();
        } else {
            int msgId;
            if (app.setupDB()) {
                startActivity(new Intent(Setup.this, BGDictum.class));
                msgId = R.string.database_installed_successfully;
            } else
                msgId = R.string.unknown_error_occured;
            finish(getString(msgId));
        }
    }

    public void onProgress(String msg) {
        if (progress == null) return;
        progress.setMessage(msg);
    }

    public void onCancel(DialogInterface dialog) {
        if (state.downloader != null) {
            state.downloader.setProgressListener(null);
            state.downloader.cancel(true);
            state.downloader = null;
        }
        if (state.extractor != null) {
            state.extractor.setProgressListener(null);
            state.extractor.cancel(true);
            state.extractor = null;
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        mLock.release();
    }
}
