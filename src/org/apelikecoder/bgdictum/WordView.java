package org.apelikecoder.bgdictum;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.View.OnTouchListener;
import android.widget.ScrollView;
import android.widget.TextView;

public class WordView extends TextView implements OnTouchListener {

    private String infoWord;

    private static class Type1Span extends ClickableSpan {
        String word;
        public Type1Span(String word) {
            this.word = word;
        }
        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(touchedInstance == this);
        }
        @Override
        public void onClick(View widget) {
            Uri uri = Uri.parse("bgdictum://" + word);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            widget.getContext().startActivity(intent);
        }
    };

    private static class Type2Span extends Type1Span {
        public Type2Span(String word) {
            super(word);
        }
        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setTypeface(Typeface.DEFAULT_BOLD);
        }
    }

    static Type1Span touchedInstance;

    private int touchX, touchY;
    private ScrollView parent;

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
        setOnTouchListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ViewParent p = getParent();
        if (p instanceof ScrollView)
            parent = (ScrollView) p;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return event.getAction() == MotionEvent.ACTION_DOWN ?
                true : super.onTouchEvent(event);
    }

    public void setWordInfo(String word, String text) {
        infoWord = word;
        setText(infoWord.trim().toUpperCase() + "\n\n" + text);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        Integer currentType = LinksFinder.getType(infoWord != null ? infoWord : ""); //XXX
        ArrayList<LinksFinder.LinkSpec> links = LinksFinder.getLinks(text.toString());
        if (links == null) {
            super.setText(text, type);
            return;
        }
        SpannableString ss = new SpannableString(text);
        int links_length = links.size();
        for (int i = 0; i < links_length; ++i) {
            LinksFinder.LinkSpec l = links.get(i);
            Type1Span span;
            if (currentType == l.type)
                span = new Type2Span(l.url);
            else
                span = new Type1Span(l.url);
            ss.setSpan(span, l.start, l.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        super.setText(ss, type);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN
                || event.getAction() == MotionEvent.ACTION_MOVE) {

            int x = (int) event.getX();
            int y = (int) event.getY();

            touchX = x;
            touchY = y;

            x -= getTotalPaddingLeft();
            y -= getTotalPaddingTop();

            x += getScrollX();
            y += getScrollY();

            Layout layout = getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            Type1Span[] candidates = ((Spannable) getText()).getSpans(off, off, Type1Span.class);
            if (candidates.length > 0) {
                touchedInstance = candidates[0];
            } else {
                touchedInstance = null;
            }
            invalidate();
        } else {
            if (touchedInstance != null) {
                touchedInstance = null;
                invalidate();
            }
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (touchedInstance != null) {
            RectF rf = textBoundsF;
            Rect r = getTextSize(touchedInstance.word, textPaint);
            rf.set(r);
            int hw = r.width()/2;
            int offsetX = 0;
            int offsetY = 0;
            int tx = getRealPosX(touchX);
            int ty = getRealPosY(touchY);
            if (ty - 40 > 0) {
                offsetY -= 40;
            } else {
                if (tx > getWidth()/2)
                    offsetX -= hw;
                else
                    offsetX += hw;
            }
            if (tx + hw > getWidth()) {
                offsetX = - hw;
            } else if (tx - hw < 0) {
                offsetX = hw;
            }
            rf.offset(touchX + offsetX - hw, touchY + offsetY);
            float br = 4.0f;
            canvas.drawRoundRect(rf, br, br, bgPaint);
            canvas.drawText(touchedInstance.word, rf.left + padding/2, rf.bottom - r.height()/3, textPaint);
            rf.inset(1.0f, 1.0f);
            canvas.drawRoundRect(rf, br, br, borderPaint);
        }
    }

    private static Rect getTextSize(String word, Paint textPaint) {
        Rect rect = textBounds;
        rect.setEmpty();
        textPaint.getTextBounds(word, 0, word.length(), rect);
        rect.right += padding;
        rect.bottom += padding;
        return rect;
    }

    private int getRealPosX(int val) {
        return parent == null ? val : val - parent.getScrollX();
    }

    private int getRealPosY(int val) {
        return parent == null ? val : val - parent.getScrollY();
    }

    static int padding = 20;
    static Paint bgPaint = new Paint();
    static Paint textPaint = new Paint();
    static Paint borderPaint = new Paint();
    static Rect textBounds = new Rect();
    static RectF textBoundsF = new RectF();
    static {
        bgPaint.setColor(0xee6f0000);
        bgPaint.setAntiAlias(true);
        textPaint.setColor(0xeeffffff);
        textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        textPaint.setTextSize(20);
        textPaint.setAntiAlias(true);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1.4f);
    }
}
