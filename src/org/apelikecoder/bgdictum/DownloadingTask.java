package org.apelikecoder.bgdictum;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;

class DownloadingTask extends AsyncTask<Void, Integer, Void> {

    private BufferedInputStream bis;
    private BufferedOutputStream bos;

    private int count;
    private int size = -1;

    private URLConnection conn;
    
    private String urlPath;
    private String downloadFile;
    
    private String error;
    
    private ProgressListener progressListener;
    
    public DownloadingTask(ProgressListener listener, String url, String downloadFile) {
        this.urlPath = url;
        this.downloadFile = downloadFile;
        setProgressListener(listener);
    }
    
    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }
    
    @Override
    protected Void doInBackground(Void... params) {
        try {
            URL url = new URL(urlPath);
            conn = url.openConnection();
            BufferedInputStream in = bis = new BufferedInputStream(conn.getInputStream());
            BufferedOutputStream out = bos = new BufferedOutputStream(
                                                new FileOutputStream(downloadFile));
            int length = size = conn.getContentLength();
            System.out.println("SIZE IS " + length);
            //setIndeterminate(length == -1);
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
            error = e.getMessage();
            e.printStackTrace();
        } finally {
            cleanup();
        }
        return null;
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
    //    setMessage(String.format(downloadMsg, progress[0])); //TODO
        if (progressListener != null)
            progressListener.onProgress(
                    String.format(progressListener.getContext().getString(R.string.downloading),  progress[0]));
    }

    protected void onPostExecute(Void result) {
        if (progressListener != null)
            progressListener.onComplete(error == null && size > 0 && size == count);
        /*
        if (error != null) {
            //Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
            return;
        }
        if (size > 0 && size != count)
            return;
        //downloadTask = null; //TODO
        //startExtract();
        */
    }
    
    public String getError() {
        return error;
    }
}

