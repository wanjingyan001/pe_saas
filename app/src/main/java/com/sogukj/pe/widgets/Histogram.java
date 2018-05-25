package com.sogukj.pe.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.sogukj.pe.baselibrary.R;
import com.sogukj.pe.baselibrary.utils.Utils;


/**
 * 基金台账的柱状图
 *
 * @author admin
 * @date 2017/11/24
 */

public class Histogram extends View {
    private int[] valueX = new int[3];
    private float[] valueY = new float[3];
    private int[] colors = {R.color.fund_deep_red, R.color.fund_light_red, R.color.fund_pink};
    private float[] datas = new float[3];
    //item宽度
    private int ITEM_WIDTH;
    //基金总规模画笔
    private Paint totalPaint;
    //基金认缴规模画笔
    private Paint contributePaint;
    //基金实缴规模画笔
    private Paint actualPaint;

    private Paint linePaint;

    private Paint textPaint;
    private RectF rectF1;
    private RectF rectF2;
    private RectF rectF3;

    private Context context;


    public Histogram(Context context) {
        super(context);
        this.context = context;
        intiPaint();
    }

    public Histogram(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        intiPaint();
    }

    private void intiPaint() {
        ITEM_WIDTH = Utils.dpToPx(context, 25);
        totalPaint = new Paint();
        totalPaint.setAntiAlias(true);
        totalPaint.setStyle(Paint.Style.FILL);
        totalPaint.setColor(getResources().getColor(colors[0]));
        totalPaint.setStrokeWidth(ITEM_WIDTH);

        contributePaint = new Paint();
        contributePaint.setAntiAlias(true);
        contributePaint.setStyle(Paint.Style.FILL);
        contributePaint.setColor(getResources().getColor(colors[1]));
        contributePaint.setStrokeWidth(ITEM_WIDTH);

        actualPaint = new Paint();
        actualPaint.setAntiAlias(true);
        actualPaint.setStyle(Paint.Style.FILL);
        actualPaint.setColor(getResources().getColor(colors[2]));
        actualPaint.setStrokeWidth(ITEM_WIDTH);

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(2);
        linePaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint();
        textPaint.setColor(getResources().getColor(R.color.text_1));
        textPaint.setTextSize(Utils.dpToPx(context, 14));
        textPaint.setStyle(Paint.Style.STROKE);
    }

    public void setData(float[] data) {
        datas = data;
        valueX[0] = Utils.dpToPx(context, 15) + getPaddingLeft();
        valueX[1] = Utils.dpToPx(context, 50) + getPaddingLeft();
        valueX[2] = Utils.dpToPx(context, 85) + getPaddingLeft();
        valueY[0] = Utils.dpToPx(context, 130);
        valueY[1] = (datas[1] / datas[0]) * Utils.dpToPx(context, 130);
        valueY[2] = (datas[2] / datas[0]) * Utils.dpToPx(context, 130);
        if (valueY[1]<100){
            valueY[1] = 75;
        }
        if (valueY[2]<100){
            valueY[2] = 75;
        }
        int top = Utils.dpToPx(context, 35);
        rectF1 = new RectF(valueX[0], top, valueX[0] + ITEM_WIDTH, valueY[0] + top);
        rectF2 = new RectF(valueX[1], valueY[0] - valueY[1] + top,
                valueX[1] + ITEM_WIDTH, valueY[0] + top);
        rectF3 = new RectF(valueX[2], valueY[0] - valueY[2] + top,
                valueX[2] + ITEM_WIDTH, valueY[0] + top);
        invalidate();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (datas[0] == 0) {
            return;
        }
        canvas.drawRect(rectF1, totalPaint);
        canvas.drawRect(rectF2, contributePaint);
        canvas.drawRect(rectF3, actualPaint);

        linePaint.setColor(getResources().getColor(colors[0]));
        Path path1 = new Path();
        path1.moveTo((rectF1.left + rectF1.right) / 2, rectF1.top);
        path1.lineTo(((rectF1.left + rectF1.right) / 2 + 50), rectF1.top - 50);
        path1.lineTo(((rectF1.left + rectF1.right) / 2 + 300), rectF1.top - 50);
        canvas.drawPath(path1, linePaint);
        canvas.drawText(String.valueOf(datas[0]), ((rectF1.left + rectF1.right) / 2 + 50), rectF1.top - 70, textPaint);

        linePaint.setColor(getResources().getColor(colors[1]));
        Path path2 = new Path();
        path2.moveTo((rectF2.left + rectF2.right) / 2, rectF2.top);
        int i = valueY[1] - valueY[2] > 0 ? 50 : (int) Math.abs(valueY[1] - valueY[2]) + 90;
        path2.lineTo((rectF2.left + rectF2.right) / 2 + 50, rectF2.top - i);
        path2.lineTo((rectF2.left + rectF2.right) / 2 + 300, rectF2.top - i);
        canvas.drawPath(path2, linePaint);
        canvas.drawText(String.valueOf(datas[1]), (rectF2.left + rectF2.right) / 2 + 50, rectF2.top - i - 20, textPaint);

        linePaint.setColor(getResources().getColor(colors[2]));
        Path path3 = new Path();
        path3.moveTo((rectF3.left + rectF3.right) / 2, rectF3.top);
        path3.lineTo((rectF3.left + rectF3.right) / 2 + 50, rectF3.top - 50);
        path3.lineTo((rectF3.left + rectF3.right) / 2 + 300, rectF3.top - 50);
        canvas.drawPath(path3, linePaint);
        canvas.drawText(String.valueOf(datas[2]), (rectF3.left + rectF3.right) / 2 + 50, rectF3.top - 70, textPaint);

    }
}
