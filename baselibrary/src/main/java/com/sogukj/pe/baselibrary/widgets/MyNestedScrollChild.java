package com.sogukj.pe.baselibrary.widgets;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.support.v4.view.NestedScrollingChild;
import android.widget.TextView;

/**
 * Created by sogubaby on 2018/1/12.
 */

public class MyNestedScrollChild extends LinearLayout implements NestedScrollingChild {
    private NestedScrollingChildHelper mNestedScrollingChildHelper;
    private final int[] offset = new int[2]; //偏移量
    private final int[] consumed = new int[2]; //消费
    private int lastY;
    private int showHeight;

    public MyNestedScrollChild(Context context) {
        super(context);
    }

    public MyNestedScrollChild(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyNestedScrollChild(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        //当mode=AT_MOST时，设置默认的宽高.全是match_parent
        if (widthSpecMode == MeasureSpec.AT_MOST
                && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(800, 800);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(800, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, 800);
        }
        showHeight = heightSpecSize;
    }

    //@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            //按下
            case MotionEvent.ACTION_DOWN:
                lastY = (int) event.getRawY();
                break;
            //移动
            case MotionEvent.ACTION_MOVE:
                int y = (int) (event.getRawY());
                int dy = y - lastY;
                lastY = y;
                if (startNestedScroll(ViewCompat.SCROLL_AXIS_HORIZONTAL)
                        && dispatchNestedPreScroll(0, dy, consumed, offset)) //如果找到了支持嵌套滑动的父类,父类进行了一系列的滑动
                {
                    //获取滑动距离
                    int remain = dy - consumed[1];
                    if (remain != 0) {
                        scrollBy(0, -remain);
                    }

                } else {
                    scrollBy(0, -dy);
                }
                break;
        }
        return true;
    }

    //限制滚动范围
    @Override
    public void scrollTo(int x, int y) {
        int maxY = getMeasuredHeight() - showHeight;
        if (y > maxY) {
            y = maxY;
        }
        if (y < 0) {
            y = 0;
        }
        super.scrollTo(x, y);
    }

    //初始化helper对象
    private NestedScrollingChildHelper getScrollingChildHelper() {
        if (mNestedScrollingChildHelper == null) {
            mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
            mNestedScrollingChildHelper.setNestedScrollingEnabled(true);
        }
        return mNestedScrollingChildHelper;
    }

    @Override
    public boolean startNestedScroll(int axes) {
        Log.e("child", "startNestedScroll" + axes + "-----");
        return getScrollingChildHelper().startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        Log.e("child", "stopNestedScroll");
        getScrollingChildHelper().stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        Log.e("child", "hasNestedScrollingParent");
        return getScrollingChildHelper().hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow) {
        Log.e("child", "dispatchNestedScroll");
        return getScrollingChildHelper().dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] offsetInWindow, @Nullable int[] ints1) {
        Log.e("child", "dispatchNestedPreScroll");
        return getScrollingChildHelper().dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

}
