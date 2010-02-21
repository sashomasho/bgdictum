package org.apelikecoder.bgdictum;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.widget.Toast;

public class Downloader extends ProgressDialog implements OnClickListener {

    private Context context;
    private String in;
    private String downloadMsg;
    private String extractMsg;
    private String outputFile;
    private String downloadFile;
    private String errorMessage;
    private DownloadingTask downloadTask;
    private ExtractTask extractTask;
    private boolean result = false;
    private boolean use_external = false;

    public Downloader(Context context, String in, String outputFile) {
        super(context);
        this.context = context;
        this.in = in;
        this.outputFile = outputFile;
        try {
            downloadFile = File.createTempFile("download", null,
                new File(outputFile).getParentFile()).getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            downloadFile = new File(outputFile).getParent() + File.separatorChar + "download.tmp";
        }
        downloadMsg = context.getString(R.string.downloading);
        extractMsg = context.getString(R.string.extracting);
        setButton(context.getString(android.R.string.cancel), this);
        setCancelable(false);
        setMessage(String.format(downloadMsg, 0));
        show();
    }

    private void startExtract() {
        extractTask = new ExtractTask();
        extractTask.execute();
    }

    public void start() {
        File f = new File("/sdcard/bgdictum.db.zip");
        if (f.exists()) {
            use_external = true;
            downloadFile = f.getAbsolutePath();
            startExtract();
            return;
        }
        downloadTask = new DownloadingTask();
        downloadTask.execute();
    }

    public boolean getResult() {
        return result;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dismiss();
        if (extractTask != null) {
            extractTask.cancel(true);
            extractTask.cleanup();
            return;
        }
        if (downloadTask != null) {
            downloadTask.cancel(true);
            downloadTask.cleanup();
            return;
        }
    }

    private class DownloadingTask extends AsyncTask<Void, Integer, Integer> {
        private BufferedInputStream bis;
        private BufferedOutputStream bos;
    
        private int count;
        private int size = -1;

        URLConnection conn;

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                URL url = new URL(in);
                conn = url.openConnection();
                BufferedInputStream in = bis = new BufferedInputStream(conn.getInputStream());
                BufferedOutputStream out = bos = new BufferedOutputStream(
                                                    new FileOutputStream(downloadFile));
                int length = size = conn.getContentLength();
                System.out.println("SIZE IS " + length);
                setIndeterminate(length == -1);
                publishProgress(0);
                int c = 0;
                int current = 0;
                while ((current = in.read()) != -1) {
                    out.write(current);
                    if (++c % 1000 == 0)
                        publishProgress((c * 100) / length);
                }
                count = c;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                errorMessage = e.getMessage();
                e.printStackTrace();
            } finally {
                cleanup();
            }
            return count;
        }

        public void cleanup() {
            try {
                if (bis != null)
                    bis.close();
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        protected void onProgressUpdate(Integer... progress) {
            setMessage(String.format(downloadMsg, progress[0]));
        }

        protected void onPostExecute(Integer result) {
            if (errorMessage != null) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                return;
            }
            if (size > 0 && size != count)
                return;
            downloadTask = null;
            startExtract();
        }
    }

    private class ExtractTask extends AsyncTask<Void, Integer, Integer> {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        @Override
        protected Integer doInBackground(Void... params) {
            int res = 0;
            try {
                publishProgress(0);
                ZipEntry ze;
                ZipInputStream in2 = new ZipInputStream(new FileInputStream(downloadFile));
                
                BufferedInputStream in = bis = new BufferedInputStream(in2); 
                BufferedOutputStream out = bos = new BufferedOutputStream(new FileOutputStream(outputFile));
                ze = in2.getNextEntry();
                int size = (int)ze.getSize();
                int x;
                int count = 0;
                while ((x = in.read()) != -1) {
                    out.write(x);
                    if (++count % 10000 == 0)
                        publishProgress((count * 100)/size);
                }
            } catch (IOException e) {
                errorMessage = e.getMessage();
                e.printStackTrace();
                res = 1;
            } finally {
            }
            return res;
        }
        
        protected void onProgressUpdate(Integer... progress) {
            setMessage(String.format(extractMsg, progress[0]));
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (errorMessage != null) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                return;
            }
            if (!use_external)
                new File(downloadFile).delete();
            if (result == 0)
                Downloader.this.result = true;
            dismiss();
        }
        
        public void cleanup() {
            try {
                if (bis != null)
                    bis.close();
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
