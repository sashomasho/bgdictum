package org.apelikecoder.bgdictum;

import java.util.ArrayList;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

public class WordView extends TextView {

    public WordView(Context context) {
        super(context);
        init();
    }

    public WordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public WordView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            System.out.println(getSelectionStart());
            return true;
        }
        return super.onTouchEvent(event);
    }
    
    private class MyUrlSpan extends URLSpan {
        public MyUrlSpan(String url) {
            super(url);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }
    };
    
    @Override
    public void setText(CharSequence text, BufferType type) {
        ArrayList<LinksFinder.LinkSpec> links = LinksFinder.getLinks(text.toString());
        if (links == null) {
            super.setText(text, type);
            return;
        }
        SpannableString ss = new SpannableString(text);
        int links_length = links.size();
        for (int i = 0; i < links_length; ++i) {
            LinksFinder.LinkSpec l = links.get(i);
            ss.setSpan(new MyUrlSpan(l.url), l.start, l.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        super.setText(ss, type);
    }
}
