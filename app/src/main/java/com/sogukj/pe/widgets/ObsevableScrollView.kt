package com.sogukj.pe.widgets

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView
import com.sogukj.pe.interf.ICallback


class ObsevableScrollView : ScrollView {

    internal var shouldIntercept = true
    internal var scrollY: Int = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun computeScrollDeltaToGetChildRectOnScreen(rect: Rect): Int {
        return 0
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return shouldIntercept && super.onInterceptTouchEvent(ev)
    }

    fun setTouchMode(shouldIntercept: Boolean) {
        //        Log.e(TAG, "setTouchMode = " + shouldIntercept);
        this.shouldIntercept = shouldIntercept
    }

    class ScrollStatus {
        internal var l: Int = 0
        internal var t: Int = 0
        internal var oldl: Int = 0
        internal var oldt: Int = 0

        constructor()

        constructor(l: Int, t: Int, oldl: Int, oldt: Int) {
            this.l = l
            this.t = t
            this.oldl = oldl
            this.oldt = oldt
        }
    }

    internal var scrollStatusCallback: ICallback<ScrollStatus>? = null

    fun setScrollStatusCallback(scrollStatusCallback: ICallback<ScrollStatus>) {
        this.scrollStatusCallback = scrollStatusCallback
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (scrollStatusCallback != null) {
            val scrollStatus = ScrollStatus(l, t, oldl, oldt)
            scrollStatusCallback!!.callback(scrollStatus)
        }
    }

    companion object {

        private val TAG = ObsevableScrollView::class.java.simpleName
    }
}
