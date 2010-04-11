package org.apelikecoder.bgdictum;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.os.AsyncTask;

class ExtractTask extends AsyncTask<Void, Integer, Integer> {

    private BufferedInputStream bis = null;
    private BufferedOutputStream bos = null;
    
    private String fileToExtract;
    private String outputFile;

    private String error;
    private ProgressListener listener;
    
    public ExtractTask(ProgressListener listener, String fileToExtract, String outputFile) {
        setProgressListener(listener);
        this.fileToExtract = fileToExtract;
        this.outputFile = outputFile;
    }
    
    public void setProgressListener(ProgressListener listener) {
        this.listener = listener;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        int res = 0;
        try {
            publishProgress(0);
            ZipEntry ze;
            ZipInputStream in2 = new ZipInputStream(new FileInputStream(fileToExtract));

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
            error = e.getMessage();
            e.printStackTrace();
            res = 1;
        } finally {
            cleanup();
        }
        return res;
    }

    protected void onProgressUpdate(Integer... progress) {
        if (listener != null)
            listener.onProgress(
                    String.format(listener.getContext().getString(R.string.extracting), progress[0]));
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        new File(fileToExtract).delete();
        if (listener != null)
            listener.onComplete(error == null);
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
    
    public String getError() {
        return error;
    }
}