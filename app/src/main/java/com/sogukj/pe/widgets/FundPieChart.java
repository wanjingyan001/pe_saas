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
 * 基金台账的饼状图
 *
 * @author admin
 * @date 2017/11/27
 */

public class FundPieChart extends View {
    private int radius;
    private int[] color = {R.color.fund_light_yellow, R.color.fund_deep_yellow};
    private Paint raisePaint;
    private Paint freePaint;

    private Paint linePaint;

    private Paint textPaint;
    private float[] datas = new float[2];
    private RectF rectF;
    private Context context;


    public FundPieChart(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public FundPieChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        radius = Utils.dpToPx(context, 85);
        raisePaint = new Paint();
        raisePaint.setAntiAlias(true);
        raisePaint.setStyle(Paint.Style.FILL);
        raisePaint.setColor(getResources().getColor(color[0]));

        freePaint = new Paint();
        freePaint.setAntiAlias(true);
        freePaint.setStyle(Paint.Style.FILL);
        freePaint.setColor(getResources().getColor(color[1]));

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(2);
        linePaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint();
        textPaint.setColor(getResources().getColor(R.color.text_1));
        textPaint.setTextSize(Utils.dpToPx(context, 14));
        textPaint.setStyle(Paint.Style.STROKE);

    }

    public void setDatas(float[] datas) {
        this.datas = datas;
        rectF = new RectF(getPaddingLeft(), 0, radius * 2 + getPaddingLeft(), radius * 2);
        invalidate();
    }

    public void setColor(int[] newColor){
        this.color[0] = newColor[0];
        this.color[1] = newColor[1];
        raisePaint.setColor(getResources().getColor(color[0]));
        freePaint.setColor(getResources().getColor(color[1]));

    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float sum = datas[0] + datas[1];
        if (sum == 0) {
            return;
        }
        float sweepAngle = (datas[0] / sum) * 360;
        canvas.drawArc(rectF, 0, sweepAngle, true, raisePaint);
        canvas.drawArc(rectF, 0, -(360 - sweepAngle), true, freePaint);

        linePaint.setColor(getResources().getColor(color[1]));
        Path path1 = new Path();
        //对边长(x轴偏移量)
        float a = (float) Math.sin(Math.abs(sweepAngle / 2 - 90)) * (radius / 2);
        //夹边长(y轴偏移量)
        float b = (float) Math.sqrt((Math.pow((radius / 2), 2) - Math.pow(a, 2)));
        if (sweepAngle / 2 < 90) {
            path1.moveTo(radius + a + getPaddingLeft(), radius + b);
        } else {
            path1.moveTo(radius - a + getPaddingLeft(), radius + b);
        }
        path1.lineTo(radius + (float) (radius / Math.sqrt(2)) + getPaddingLeft(), radius + (float) (radius / Math.sqrt(2)));
        path1.lineTo(radius + (float) (radius / Math.sqrt(2)) + 200 + getPaddingLeft(), radius + (float) (radius / Math.sqrt(2)));
        canvas.drawPath(path1, linePaint);
        canvas.drawText(String.valueOf(datas[0]), radius + (float) (radius / Math.sqrt(2)) + getPaddingLeft(),radius + (float) (radius / Math.sqrt(2))-35,textPaint);

        linePaint.setColor(getResources().getColor(color[0]));
        Path path2 = new Path();
        //对边长(x轴偏移量)
        float a1 = (float) Math.sin(Math.abs(sweepAngle / 2 - 90)) * (radius / 2);
        //夹边长(y轴偏移量)
        float b1 = (float) Math.sqrt((Math.pow((radius / 2), 2) - Math.pow(a, 2)));
        if ((360 - sweepAngle) / 2 < 90) {
            path2.moveTo(radius + a1 + getPaddingLeft(), radius - b1);
        } else {
            path2.moveTo(radius - a1 + getPaddingLeft(), radius - b1);
        }
        path2.lineTo(radius + (float) (radius / Math.sqrt(2)) + getPaddingLeft(), radius - (float) (radius / Math.sqrt(2)));
        path2.lineTo(radius + (float) (radius / Math.sqrt(2)) + 200 + getPaddingLeft(), radius -(float) (radius / Math.sqrt(2)));
        canvas.drawPath(path2, linePaint);
        canvas.drawText(String.valueOf(datas[1]), radius + (float) (radius / Math.sqrt(2)) + getPaddingLeft(),radius - (float) (radius / Math.sqrt(2)) -35,textPaint);
    }
}
