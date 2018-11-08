package com.sogukj.pe.baselibrary.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by sogubaby on 2018/1/18.
 */

public class ProgressView extends View {

    private Paint mProgessPaint;
    private Paint mBlurPaint;

    public ProgressView(Context context) {
        super(context);
        init();
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mProgessPaint = new Paint();
        mProgessPaint.setStyle(Paint.Style.FILL);//实心矩形框
        mProgessPaint.setColor(Color.parseColor("#ffb7b7"));  //颜色为红色

        mBlurPaint = new Paint();
        mBlurPaint.setStyle(Paint.Style.FILL);//实心矩形框
        mBlurPaint.setColor(Color.parseColor("#ebf1fa"));  //颜色为红色
    }

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
            setMeasuredDimension(200, 200);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(200, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, 200);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight() * percent / 100;
//        canvas.drawRect(0, 0, getWidth(), height, mProgessPaint);
//        canvas.drawRect(0, height, getWidth(), getHeight(), mBlurPaint);

        canvas.drawRect(0, getHeight(), getWidth(), 0, mBlurPaint);
        canvas.drawRect(0, getHeight(), getWidth(), getHeight() - height, mProgessPaint);
    }

    private int percent = 0;

    //60%传60
    public void setPercent(int percent) {
        this.percent = percent;
        invalidate();
    }
}
