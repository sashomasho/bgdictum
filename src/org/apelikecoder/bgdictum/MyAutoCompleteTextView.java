package org.apelikecoder.bgdictum;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

public class MyAutoCompleteTextView extends AutoCompleteTextView {

    boolean mBlocking;

    public MyAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public MyAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setText(String text, boolean filter) {
        mBlocking = !filter;
        super.setText(text);
        mBlocking = false;
    }

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        if (!mBlocking)
            super.performFiltering(text, keyCode);
    }
}
