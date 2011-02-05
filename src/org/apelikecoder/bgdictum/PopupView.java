package org.apelikecoder.bgdictum;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class PopupView extends View {

    public PopupView(Context context, AttributeSet attrs) {
        super(context, attrs);
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

    private static final int DEFAULT_OFFSET = 40;
    private static final int PADDING = 20;
    private String mText;
    private int posX, posY;
    private Paint bgPaint = new Paint();
    private Paint textPaint = new Paint();
    private Paint borderPaint = new Paint();
    private Rect textBounds = new Rect();
    private RectF textBoundsF = new RectF();

    public void setTypeFace(Typeface typeface) {
        textPaint.setTypeface(typeface);
    }

    public void setPopupText(int x, int y, String text) {
        this.posX = x;
        this.posY = y;
        mText = text;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mText != null) {
            RectF rf = textBoundsF;
            Rect r = getTextSize(mText, textPaint);
            rf.set(r);
            int hw = r.width() / 2;
            int offsetX = 0;
            int offsetY = 0;
            int tx = posX;
            int ty = posY;
            if (ty - DEFAULT_OFFSET > 0) {
                offsetY -= DEFAULT_OFFSET;
            } else {
                if (tx > getWidth() / 2)
                    offsetX -= hw;
                else
                    offsetX += hw;
            }
            if (tx + hw > getWidth()) {
                offsetX = -hw;
            } else if (tx - hw < 0) {
                offsetX = hw;
            }
            rf.offset(tx - hw, ty + offsetY);
            float br = 4.0f;
            canvas.drawRoundRect(rf, br, br, bgPaint);
            canvas.drawText(mText, rf.left + PADDING / 2, rf.bottom - r.height() / 3, textPaint);
            rf.inset(1.0f, 1.0f);
            canvas.drawRoundRect(rf, br, br, borderPaint);
        }
    }
    
    public void clear() {
        mText = null;
        invalidate();
    }
    

    private Rect getTextSize(String word, Paint textPaint) {
        Rect rect = textBounds;
        rect.setEmpty();
        textPaint.getTextBounds(word, 0, word.length(), rect);
        rect.right += PADDING;
        rect.bottom += PADDING;
        return rect;
    }

}
