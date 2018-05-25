package com.sogukj.pe.widgets;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.sogukj.pe.baselibrary.R;
import com.sogukj.pe.baselibrary.utils.Utils;


/**
 * Created by admin on 2017/11/27.
 */

public class FundProgress3 extends View {
    private static final int HEIGHT = 36;
    private int[] colors = {R.color.fund_light_green, R.color.fund_deep_green};
    private Paint quitSectionPaint;
    private Paint quitAllPaint;
    private Paint investedPaint;
    private Paint slashPaint;
    private Paint linePaint;
    private Paint textPaint;
    private Context context;
    private float[] datas = new float[3];
    private RectF rectF;
    private RectF rectF1;
    private RectF rectF2;
    private int[] property;
    private int width;


    public FundProgress3(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public FundProgress3(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        property = Utils.getAndroidScreenProperty(context);
        width = Utils.dpToPx(context, (property[0] - 2 * 20));

        quitSectionPaint = new Paint();
        quitSectionPaint.setStyle(Paint.Style.FILL);
        quitSectionPaint.setAntiAlias(true);
        quitSectionPaint.setColor(getResources().getColor(colors[0]));

        quitAllPaint = new Paint();
        quitAllPaint.setStyle(Paint.Style.FILL);
        quitAllPaint.setAntiAlias(true);
        quitAllPaint.setColor(Color.rgb(203, 255, 146));

        investedPaint = new Paint();
        investedPaint.setStyle(Paint.Style.FILL);
        investedPaint.setAntiAlias(true);
        investedPaint.setColor(getResources().getColor(colors[1]));

        slashPaint = new Paint();
        slashPaint.setStrokeWidth(2);
        slashPaint.setColor(Color.rgb(146, 255, 255));
        slashPaint.setStyle(Paint.Style.STROKE);
        slashPaint.setAntiAlias(true);

        linePaint = new Paint();
        linePaint.setStrokeWidth(2);
        linePaint.setColor(Color.parseColor("#f1f1f1"));
        linePaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#808080"));
        textPaint.setTextSize(Utils.dpToPx(context, 12));
        textPaint.setAntiAlias(true);
    }

    public void setData(float[] data) {
        datas = data;
        float sectionWidth = width * (datas[0] / datas[2]);
        float allWidth = width * (datas[1] / datas[2]);
        if (datas[2] - datas[0] - datas[1] < 0) {
            return;
        }
        float investWidth = width * ((datas[2] - datas[0] - datas[1]) / datas[2]);
        int top = (getHeight() - Utils.dpToPx(context, HEIGHT)) / 2;
        int left = Utils.dpToPx(context, 20);
        rectF = new RectF(left, top, sectionWidth + left, Utils.dpToPx(context, HEIGHT) + top);
        rectF1 = new RectF(rectF.right, top, rectF.right + allWidth, Utils.dpToPx(context, HEIGHT) + top);
        rectF2 = new RectF(rectF1.right, top, rectF1.right + investWidth, Utils.dpToPx(context, HEIGHT) + top);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (rectF == null || rectF1 == null || rectF2 == null) {
            return;
        }
        canvas.drawRect(rectF, quitSectionPaint);
        canvas.drawRect(rectF1, quitAllPaint);
        canvas.drawRect(rectF2, investedPaint);
        //绘制斜线
        for (int i = 0; i < (int) (rectF1.right - rectF1.left) / 20; i++) {
            canvas.drawLine(rectF1.left + 20 * i, rectF1.top, rectF1.right, rectF1.bottom - 20 * i, slashPaint);
        }
        for (int i = 0; i < (int) (rectF1.bottom - rectF1.top) / 20; i++) {
            canvas.drawLine(rectF1.left, rectF1.top + 20 * i, rectF1.right - 20 * i, rectF1.bottom, slashPaint);
        }

        canvas.drawLine(rectF.left, rectF.top - Utils.dpToPx(context, 15), rectF.left, rectF.top - Utils.dpToPx(context, 5), linePaint);
        canvas.drawLine(rectF.left, rectF.top - Utils.dpToPx(context, 8), rectF.right, rectF.top - Utils.dpToPx(context, 8), linePaint);
        canvas.drawLine(rectF.right, rectF.top - Utils.dpToPx(context, 15), rectF.right, rectF.top - Utils.dpToPx(context, 5), linePaint);
        canvas.drawLine(rectF1.left, rectF1.top - Utils.dpToPx(context, 8), rectF1.right, rectF1.top - Utils.dpToPx(context, 8), linePaint);
        canvas.drawLine(rectF1.right, rectF1.top - Utils.dpToPx(context, 15), rectF1.right, rectF1.top - Utils.dpToPx(context, 5), linePaint);
        canvas.drawLine(rectF.left, rectF.bottom + Utils.dpToPx(context, 15), rectF.left, rectF.bottom + Utils.dpToPx(context, 5), linePaint);
        canvas.drawLine(rectF.left, rectF.bottom + Utils.dpToPx(context, 8), rectF2.right, rectF.bottom + Utils.dpToPx(context, 8), linePaint);
        canvas.drawLine(rectF2.right, rectF.bottom + Utils.dpToPx(context, 15), rectF2.right, rectF.bottom + Utils.dpToPx(context, 5), linePaint);

        canvas.drawText(String.valueOf(datas[0]), (rectF.left + rectF.right) / 2 - 30, rectF.top - Utils.dpToPx(context, 17), textPaint);
        canvas.drawText(String.valueOf(datas[1]), (rectF1.left + rectF1.right) / 2 - 30, rectF.top - Utils.dpToPx(context, 17), textPaint);
        canvas.drawText(String.valueOf(datas[0] + datas[1] + datas[2]),
                (rectF.left + rectF2.right) / 2 - 30, rectF.bottom + Utils.dpToPx(context, 21), textPaint);

        canvas.drawText("单位：个", rectF2.right - Utils.dpToPx(context, 64), rectF.top - Utils.dpToPx(context, 17), textPaint);
    }
}
