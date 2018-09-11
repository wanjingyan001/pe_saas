package com.sogukj.pe.widgets;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by CH-ZH on 2018/9/11.
 */
public class SpotCircleLoadingView extends View {

    public static final int DEFAULT_SIZE = 45;

    Paint mPaint;

    BallSpinFadeLoaderIndicator mIndicatorController;

    private boolean mHasAnimation;


    public static final String TAG = SpotCircleLoadingView.class.getSimpleName();

    public SpotCircleLoadingView(Context context) {
        super(context);
        init(null, 0);
    }

    public SpotCircleLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SpotCircleLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SpotCircleLoadingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyle) {
        mPaint = new Paint();
        mPaint.setColor(Color.parseColor("#1787fb"));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mIndicatorController = new BallSpinFadeLoaderIndicator();
        mIndicatorController.setTarget(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width  = measureDimension(dp2px(DEFAULT_SIZE), widthMeasureSpec);
        int height = measureDimension(dp2px(DEFAULT_SIZE), heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private int measureDimension(int defaultSize, int measureSpec) {
        int result   = defaultSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(defaultSize, specSize);
        } else {
            result = defaultSize;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawIndicator(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!mHasAnimation) {
            mHasAnimation = true;
            applyAnimation();
        }
    }

    @Override
    public void setVisibility(int v) {
        if (getVisibility() != v) {
            super.setVisibility(v);
            if (v == GONE || v == INVISIBLE) {
                mIndicatorController.setAnimationStatus(BallSpinFadeLoaderIndicator.AnimStatus.END);
            } else {
                mIndicatorController.setAnimationStatus(BallSpinFadeLoaderIndicator.AnimStatus.START);
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mHasAnimation) {
            mIndicatorController.setAnimationStatus(BallSpinFadeLoaderIndicator.AnimStatus.START);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIndicatorController.setAnimationStatus(BallSpinFadeLoaderIndicator.AnimStatus.CANCEL);
    }

    void drawIndicator(Canvas canvas) {
        mIndicatorController.draw(canvas, mPaint);
    }

    void applyAnimation() {
        mIndicatorController.initAnimation();
    }

    private int dp2px(int dpValue) {
        return (int) getContext().getResources().getDisplayMetrics().density * dpValue;
    }

    static class BallSpinFadeLoaderIndicator {

        private WeakReference<View> mTarget;

        private List<Animator> mAnimators;


        public void setTarget(View target){
            this.mTarget=new WeakReference<>(target);
        }

        public View getTarget(){
            return mTarget!=null?mTarget.get():null;
        }


        public int getWidth(){
            return getTarget()!=null?getTarget().getWidth():0;
        }

        public int getHeight(){
            return getTarget()!=null?getTarget().getHeight():0;
        }

        public void postInvalidate(){
            if (getTarget()!=null){
                getTarget().postInvalidate();
            }
        }


        public void initAnimation(){
            mAnimators=createAnimation();
        }

        /**
         * make animation to start or end when target
         * view was be Visible or Gone or Invisible.
         * make animation to cancel when target view
         * be onDetachedFromWindow.
         * @param animStatus
         */
        public void setAnimationStatus(AnimStatus animStatus){
            if (mAnimators==null){
                return;
            }
            int count=mAnimators.size();
            for (int i = 0; i < count; i++) {
                Animator animator=mAnimators.get(i);
                boolean isRunning=animator.isRunning();
                switch (animStatus){
                    case START:
                        if (!isRunning){
                            animator.start();
                        }
                        break;
                    case END:
                        if (isRunning){
                            animator.end();
                        }
                        break;
                    case CANCEL:
                        if (isRunning){
                            animator.cancel();
                        }
                        break;
                }
            }
        }


        public enum AnimStatus{
            START,END,CANCEL
        }
        public static final float SCALE=1.0f;

        float[] scaleFloats=new float[]{SCALE,
                SCALE,
                SCALE,
                SCALE,
                SCALE,};

        public void draw(Canvas canvas, Paint paint) {
            float translateX=getWidth()/9;
            float translateY=getHeight()/2;
            for (int i = 0; i < 4; i++) {
                canvas.save();
                canvas.translate((2 + i * 2) * translateX - translateX / 2, translateY);
                canvas.scale(scaleFloats[i], scaleFloats[i]);
                RectF rectF=new RectF(-translateX/2,-getHeight()/2.5f,translateX/2,getHeight()/2.5f);
                canvas.drawRoundRect(rectF,5,5,paint);
                canvas.restore();
            }
        }

        public List<Animator> createAnimation() {
            List<Animator> animators=new ArrayList<>();
            long[] durations=new long[]{1260, 430, 1010, 730};
            long[] delays=new long[]{770, 290, 280, 740};
            for (int i = 0; i < 4; i++) {
                final int index=i;
                ValueAnimator scaleAnim=ValueAnimator.ofFloat(1, 0.4f, 1);
                scaleAnim.setDuration(durations[i]);
                scaleAnim.setRepeatCount(-1);
                scaleAnim.setStartDelay(delays[i]);
                scaleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        scaleFloats[index] = (float) animation.getAnimatedValue();
                        postInvalidate();
                    }
                });
                scaleAnim.start();
                animators.add(scaleAnim);
            }
            return animators;
        }

    }
    public void setLoadingAnim() {
        setVisibility(View.VISIBLE);
    }

    public void clearLoadingAnim() {
        setVisibility(View.INVISIBLE);
    }
}

