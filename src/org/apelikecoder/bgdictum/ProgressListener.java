package org.apelikecoder.bgdictum;

import android.content.Context;

interface ProgressListener {

    public void onComplete(boolean success);
    public void onProgress(String msg);
    public Context getContext();
}
