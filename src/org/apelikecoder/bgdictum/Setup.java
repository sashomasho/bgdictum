package org.apelikecoder.bgdictum;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.widget.Toast;

public class Setup extends Activity implements DB {

    private static final int DLG_CONFIRM_DOWNLOAD = 0;
    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (App) getApplication();
        showDialog(DLG_CONFIRM_DOWNLOAD);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dlg = null;
        switch (id) {
            case DLG_CONFIRM_DOWNLOAD:
                dlg = new AlertDialog.Builder(this)
                    .setMessage(R.string.download_question)
                    .setPositiveButton(R.string.download, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startDownload();
                        }
                    })
                    .setNegativeButton(R.string.quit, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Setup.this.finish();
                        }
                    })
                    .create();
        }
        return dlg;
    }
    
    private void startDownload() {
        final Downloader dl = new Downloader(this, getString(R.string.dictionary_url),
                app.getDataPath() + File.separatorChar + DATABASE);
        dl.start();
        dl.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (!dl.getResult()) {
                    finish("Download unsuccessfall");
                } else {
                    if (app.setupDB()) {
                        startActivity(new Intent(Setup.this, BGDictum.class));
                        finish();
                    }
                }
            }
        });
    }

    private void finish(String reason) {
        Toast.makeText(this, reason, Toast.LENGTH_LONG).show();
        finish();
    }
}
