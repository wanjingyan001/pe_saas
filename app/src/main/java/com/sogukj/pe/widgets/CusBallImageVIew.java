package com.sogukj.pe.widgets;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by CH-ZH on 2018/9/13.
 */
public class CusBallImageVIew extends android.support.v7.widget.AppCompatImageView {
    private int parentHeight;
    private int parentWidth;
    private Context context;
    public CusBallImageVIew(Context context) {
        this(context,null);
    }

    public CusBallImageVIew(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public CusBallImageVIew(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    private int lastX;
    private int lastY;
    private boolean isDrag;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int rawX = (int) event.getRawX();
        int rawY = (int) event.getRawY();
          switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN :
                    isDrag = false;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    lastX = rawX;
                    lastY = rawY;
                    ViewGroup parent;
                    if (getParent() != null) {
                        parent = (ViewGroup) getParent();
                        parentHeight = parent.getHeight();
                        parentWidth = parent.getWidth();
                    }
                    break;
                case MotionEvent.ACTION_MOVE :
                    if (parentHeight <= 0 || parentWidth == 0) {
                        isDrag = false;
                        break;
                    } else {
                        isDrag = true;
                    }
                    int dx = rawX - lastX;
                    int dy = rawY - lastY;
                    //这里修复一些华为手机无法触发点击事件
                    int distance = (int) Math.sqrt(dx * dx + dy * dy);
                    if (distance == 0) {
                        isDrag = false;
                        break;
                    }
                    float x = getX() + dx;
                    float y = getY() + dy;
                    //检测是否到达边缘 左上右下
                    x = x < 0 ? 0 : x > parentWidth - getWidth() ? parentWidth - getWidth() : x;
                    y = getY() < 0 ? 0 : getY() + getHeight() > parentHeight ? parentHeight - getHeight() : y;
                    setX(x);
                    setY(y);
                    lastX = rawX;
                    lastY = rawY;
                    break;
                case MotionEvent.ACTION_UP :
                        if (rawX >= parentWidth / 2) {
                            //靠右吸附
                            animate().setInterpolator(new DecelerateInterpolator())
                                    .setDuration(500)
                                    .xBy(parentWidth - getWidth() - getX())
                                    .start();
                        } else {
                            //靠左吸附
                            ObjectAnimator oa = ObjectAnimator.ofFloat(this, "x", getX(), 0);
                            oa.setInterpolator(new DecelerateInterpolator());
                            oa.setDuration(500);
                            oa.start();
                        }
                    break;
            }

        return !isNotDrag() || super.onTouchEvent(event);
    }
    private boolean isNotDrag() {
        return !isDrag && (getX() == 0 || (getX() == parentWidth - getWidth()));
    }
}
