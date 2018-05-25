package com.sogukj.pe.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.sogukj.pe.R;
import com.sogukj.pe.baselibrary.utils.Utils;

import java.text.DecimalFormat;

/**
 * Created by sogubaby on 2017/12/13.
 */

public class TotalCircleScoreBoard extends View {

    public TotalCircleScoreBoard(Context context) {
        super(context);
        init(context);
    }

    public TotalCircleScoreBoard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TotalCircleScoreBoard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private Paint paint;
    private Bitmap bitmap;
    private Context ctx;

    private Paint outpaint;

    private void init(Context context) {
        this.paint = new Paint();
        this.paint.setAntiAlias(true); //消除锯齿
        this.paint.setStyle(Paint.Style.STROKE); //绘制空心圆
        ctx = context;
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg_red);

        outpaint = new Paint();
        outpaint.setColor(Color.parseColor("#EA5847"));
        outpaint.setStyle(Paint.Style.STROKE);//设置画笔类型为STROKE类型（个人感觉是描边的意思）
        outpaint.setStrokeWidth(Utils.dpToPx(ctx, 4));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == MeasureSpec.AT_MOST
                && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(300, 300);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(300, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, 300);
        }
    }

    //宽高250dp
    //最外层有进度的宽4dp
    //图片宽高230dp

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawOut(canvas);
        drawInnerImage(canvas);
        drawTxt(canvas);
//        if (tag == "FINISH") {
//            drawOut(canvas);
//            drawInnerImage(canvas);
//            drawTxt(canvas);
//        } else if (tag == "UNFINISH") {
//
//        }
        if (tag == "UNFINISH") {
            try {
                Thread.sleep(10);
                step++;
                invalidate();
            } catch (Exception e) {
            }
        }
    }

    private void drawOut(Canvas canvas) {
        /***********配置画笔*************/
        paint.setColor(Color.parseColor("#ff9b8f"));
        paint.setStyle(Paint.Style.STROKE);//设置画笔类型为STROKE类型（个人感觉是描边的意思）
        paint.setStrokeWidth(Utils.dpToPx(ctx, 4));
        /***********绘制圆弧*************/
        float pad = Utils.dpToPx(ctx, 2);
        RectF rectf_head = new RectF(pad, pad, getWidth() - pad, getHeight() - pad);//确定外切矩形范围

        paint.setColor(Color.parseColor("#ff9b8f"));
        paint.setStyle(Paint.Style.STROKE);//设置画笔类型为STROKE类型（个人感觉是描边的意思）
        paint.setStrokeWidth(Utils.dpToPx(ctx, 4));

        canvas.drawArc(rectf_head, 120, 300, false, outpaint);//绘制圆弧，不含圆心

        if (tag == "FINISH") {
            canvas.drawArc(rectf_head, 120, 3 * percent * step, false, paint);//绘制圆弧，不含圆心
            //一共300，分100次，每次3
        } else if (tag == "UNFINISH") {
            int angle = 3 * step;
            int yushu = angle / 300;
            if (yushu % 2 == 1) {
                angle = 300 * (yushu + 1) - angle;
            } else {
                angle = angle - 300 * yushu;
            }
            canvas.drawArc(rectf_head, 120, angle, false, paint);//绘制圆弧，不含圆心
        }
    }

    private void drawInnerImage(Canvas canvas) {
        int center = getWidth() / 2;
        Rect rect_src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Rect rect_dst = new Rect(center - Utils.dpToPx(ctx, 230 / 2), center - Utils.dpToPx(ctx, 230 / 2),
                center + Utils.dpToPx(ctx, 230 / 2), center + Utils.dpToPx(ctx, 230 / 2));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawBitmap(bitmap, rect_src, rect_dst, paint);
    }

    private void drawTxt(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
        paint.setColor(Color.parseColor("#411a1a"));
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 50, ctx.getResources().getDisplayMetrics()));
        String str = df.format(step * txt);

        if (str.substring(str.length() - 2).equals("00")) {
            str = str.substring(0, str.length() - 3);
        }

        int center = getWidth() / 2;

        if (tag == "FINISH") {
            Rect textRect = new Rect();
            paint.getTextBounds(str, 0, str.length(), textRect);
            canvas.drawText(str, center - textRect.width() / 2, center + textRect.height() / 2, paint);
        } else if (tag == "UNFINISH") {
            str = "计算中\n...";

            Rect textRect = new Rect();
            paint.getTextBounds("计算中", 0, "计算中".length(), textRect);
            canvas.drawText("计算中", center - textRect.width() / 2, center + textRect.height() / 2, paint);

            Rect textRect1 = new Rect();
            paint.getTextBounds("...", 0, "...".length(), textRect1);
            canvas.drawText("...", center - textRect1.width() / 2, center + textRect.height() / 2 + textRect1.height() + Utils.dpToPx(ctx, 12), paint);
        }
    }

    private DecimalFormat df = new DecimalFormat("#####0.00");

    private int step = 0;
    private double txt = 0.00;
    private float percent = 0.00f;

    public void setDate(int step, double txt, float percent) {
        this.step = 100 - step;
        this.txt = txt;
        this.percent = percent;
        invalidate();
    }

    private String tag = "FINISH";

    public void setTag() {
        this.tag = "UNFINISH";
        invalidate();
    }
}
