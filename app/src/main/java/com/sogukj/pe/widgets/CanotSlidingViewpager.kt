package com.sogukj.pe.widgets

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import com.sogukj.pe.module.main.GuideActivity
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info


/**
 * Created by admin on 2018/7/13.
 */
class CanotSlidingViewpager : ViewPager {
    /**
     * 上一次x坐标
     */
    private var beforeX: Float = 0.toFloat()

    /**
     * 设置 是否可以滑动
     * @param isCanScroll
     */
    var isScrollble = false
    var toNextPage: (() -> Unit)? = null

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)


    constructor(context: Context) : super(context)


    private var motionValue: Float = 0f

    //-----禁止左滑-------左滑：上一次坐标 > 当前坐标
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (isScrollble) {
            return super.dispatchTouchEvent(ev)
        } else {
            when (ev.action) {
            //按下如果‘仅’作为‘上次坐标’，不妥，因为可能存在左滑，motionValue大于0的情况（来回滑，只要停止坐标在按下坐标的右边，左滑仍然能滑过去）
                MotionEvent.ACTION_DOWN -> beforeX = ev.x
                MotionEvent.ACTION_MOVE -> {
                    motionValue = ev.x - beforeX
                    if (motionValue > 0) {//禁止左滑
                        return true
                    }
                    beforeX = ev.x//手指移动时，再把当前的坐标作为下一次的‘上次坐标’，解决上述问题
                }
                MotionEvent.ACTION_UP -> {
                    AnkoLogger("WJY").info { motionValue }
                    val pagerAdapter = adapter as GuideActivity.GuidePagerAdapter
                    if (pagerAdapter.currentPosition == pagerAdapter.imgs.size - 1 && toNextPage != null) {
                        toNextPage?.invoke()
                        return true
                    }
                }
                else -> {
                }
            }
            return super.dispatchTouchEvent(ev)
        }

    }
}