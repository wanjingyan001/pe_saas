package com.sogukj.pe.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.lcodecore.tkrefreshlayout.IHeaderView
import com.lcodecore.tkrefreshlayout.OnAnimEndListener
import com.scwang.smartrefresh.layout.api.RefreshHeader
import com.scwang.smartrefresh.layout.api.RefreshKernel
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.constant.SpinnerStyle
import com.sogukj.pe.R
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find
import kotlin.properties.Delegates
/**
 * Created by admin on 2018/3/27.
 */
class ArrangeHeaderView : FrameLayout, RefreshHeader,AnkoLogger {
    private lateinit var headerTv: TextView
    private lateinit var view: View
    private var originalHeight: Int by Delegates.notNull()
    private var originalSize: Float by Delegates.notNull()

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr) {
        init()
    }

    fun init() {
        view = LayoutInflater.from(context).inflate(R.layout.layout_arrange_list_fh, null)
        headerTv = view.find(R.id.footOrHead)
        viewTreeObserver.addOnGlobalLayoutListener {
            if (viewTreeObserver.isAlive) {
                viewTreeObserver.removeOnGlobalLayoutListener {}
                originalHeight = measuredHeight
                originalSize = headerTv.textSize
            }
        }
        addView(view)
    }

    override fun getView() = this

    override fun getSpinnerStyle(): SpinnerStyle = SpinnerStyle.Translate

    override fun onFinish(refreshLayout: RefreshLayout, success: Boolean): Int = 200

    override fun onInitialized(kernel: RefreshKernel, height: Int, extendHeight: Int) {
    }

    override fun onHorizontalDrag(percentX: Float, offsetX: Int, offsetMax: Int) {
    }

    override fun onReleased(refreshLayout: RefreshLayout?, height: Int, extendHeight: Int) {
    }

    override fun onPulling(percent: Float, offset: Int, height: Int, extendHeight: Int) {
    }

    override fun setPrimaryColors(vararg colors: Int) {
    }

    override fun onReleasing(percent: Float, offset: Int, height: Int, extendHeight: Int) {
    }

    override fun onStartAnimator(refreshLayout: RefreshLayout, height: Int, extendHeight: Int) {
    }

    override fun onStateChanged(refreshLayout: RefreshLayout?, oldState: RefreshState?, newState: RefreshState?) {
    }

    override fun isSupportHorizontalDrag(): Boolean  = false

}