package com.sogukj.pe.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.sogukj.pe.R;


/**
 * Created by sogubaby on 2018/2/28.
 */

public class MyProgressBar extends View {

    public MyProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        txtBg = BitmapFactory.decodeResource(getResources(), R.drawable.bg_txt);
        point = BitmapFactory.decodeResource(getResources(), R.drawable.point_mark);
    }

    public MyProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        txtBg = BitmapFactory.decodeResource(getResources(), R.drawable.bg_txt);
        point = BitmapFactory.decodeResource(getResources(), R.drawable.point_mark);
    }

    private Paint mPaintFore = new Paint();
    private Paint mPaintBg = new Paint();
    private Paint mPaintTxt = new Paint();
    private Paint mPaintBitmap = new Paint();

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        //当mode=AT_MOST时，设置默认的宽高
        if (widthSpecMode == MeasureSpec.AT_MOST
                && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(dp2px(25), dp2px(25));
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(dp2px(25), heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, dp2px(25));
        }
    }

    private int dp2px(int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal,
                getResources().getDisplayMetrics());
    }

    private int sp2px(int spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spVal,
                getResources().getDisplayMetrics());
    }

    public void setProgress(int pro) {
        progress = pro;
        invalidate();
    }

    private int progress = 0;
    private Bitmap txtBg, point;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float rx = dp2px(6);
        float ry = dp2px(6);
        int height = getHeight();
        int width = getWidth();

        //background
        mPaintBg.setColor(Color.parseColor("#f7f7f7"));
        mPaintBg.setStyle(Paint.Style.FILL_AND_STROKE);
        RectF rectF_bg = new RectF(0, height - dp2px(8), width, height - dp2px(2));
        canvas.drawRoundRect(rectF_bg, rx, ry, mPaintBg);

        //foreground
        mPaintFore.setColor(Color.parseColor("#5ba7fc"));
        mPaintFore.setStyle(Paint.Style.FILL_AND_STROKE);
        RectF rectF_fore = new RectF(0, height - dp2px(8), width * progress / 100, height - dp2px(2));
        canvas.drawRoundRect(rectF_fore, rx, ry, mPaintFore);

        //bg_txt
        Rect txtRect = new Rect();
        mPaintTxt.getTextBounds(progress + "%", 0, (progress + "%").length(), txtRect);
        Rect mSrcRect = new Rect(0, 0, txtBg.getWidth(), txtBg.getHeight());
        int left = width * progress / 100 - txtRect.width() / 2 - dp2px(2);
        int top = height - dp2px(8 + 4 + 2) - txtRect.height();
        int right = width * progress / 100 + txtRect.width() / 2 + dp2px(2);
        int bottom = height - dp2px(8 + 2);
        Rect mDestRect = new Rect(left, top, right, bottom);
        canvas.drawBitmap(txtBg, mSrcRect, mDestRect, mPaintBitmap);

        //text
        mPaintTxt.setColor(Color.parseColor("#90959b"));
        mPaintTxt.setTextSize(sp2px(12));
        mPaintTxt.measureText(progress + "%");
        float x = width * progress / 100 - txtRect.width() / 2;
        float y = height - dp2px(8 + 4);
        canvas.drawText(progress + "%", x, y, mPaintTxt);

        //bitmap
        Rect mSrcRect1 = new Rect(0, 0, point.getWidth(), point.getHeight());
        int left1 = width * progress / 100 - dp2px(6);
        int top1 = height - dp2px(10);
        int right1 = width * progress / 100 + dp2px(6);
        int bottom1 = height;
        Rect mDestRect1 = new Rect(left1, top1, right1, bottom1);
        canvas.drawBitmap(point, mSrcRect1, mDestRect1, mPaintBitmap);
    }
}
