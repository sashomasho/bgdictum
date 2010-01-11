package org.apelikecoder.bgdictum;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipInputStream;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;

public class Downloader extends ProgressDialog implements OnClickListener {

    private InputStream is;
    private BufferedInputStream bis;
    private FileOutputStream fos;
    private BufferedOutputStream bos;
    private String in;
    private String outputPath;
    private String msg;
    private String finishMsg;
    private int count;
    private int size = -1;
    private String extractFileName;
    private DownloadingTask task;
    private ExtractTask extractTask;
    private boolean result = false;
    
    public Downloader(Context context, String in, String extractFileName) {
        super(context);
        this.in = in;
        outputPath = extractFileName + ".tmp";
        msg = context.getString(R.string.downloading) + " ";
        this.extractFileName = extractFileName;
        finishMsg = context.getString(R.string.completed);
        setButton(context.getString(android.R.string.cancel), this);
        setCancelable(false);
        setMessage(msg + "0%");
        show();
    }

    public void start() {
        task = new DownloadingTask();
        task.execute();
    }
    
    private void cleanup() {
        try {
            if (is != null) is.close();
            if (bis != null) bis.close();
            if (fos != null) fos.close();
            if (bos != null) bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class DownloadingTask extends AsyncTask<Void, Integer, Integer> {
        URLConnection conn;

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                URL url = new URL(in);
                conn = url.openConnection();
                
                int current = 0;
                is = conn.getInputStream();
                size = conn.getContentLength();
                setIndeterminate(size == -1);
                publishProgress(0);
                
                bis = new BufferedInputStream(is); 
                fos = new FileOutputStream(outputPath);
                bos = new BufferedOutputStream(fos);
                while((current = bis.read()) != -1) {
                    bos.write(current);
                    if (++count % 1000 == 0)
                        publishProgress((count * 100)/size);
                }
                dismiss();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                cleanup();
            }
            return count;
        }
        
        protected void onProgressUpdate(Integer... progress) {
            setMessage(msg + progress[0] + '%');
        }
        
        protected void onPostExecute(Integer result) {
            setMessage(finishMsg);
            if (extractFileName != null) {
                setMessage("Extracting...");
                extractTask = new ExtractTask();
                extractTask.execute();
                task = null;
            } else {
                dismiss();
            }
        }
    }
    
    public boolean getResult() {
        return result;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (extractTask != null)
            return;
        if (task != null) {
            task.cancel(true);
            cleanup();
            dismiss();
            System.out.println("dismiss");
            return;
        }
    }
    
    
    private class ExtractTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            try {
                int current = 0;
                InputStream is = new ZipInputStream(new FileInputStream(new File(outputPath)));
                OutputStream os = new FileOutputStream(new File(extractFileName)); 
                while ((current = is.read()) != -1) {
                    os.write(current);
                }
                is.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }
        
        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            Downloader.this.result = true;
            dismiss();
        }
    }
}
