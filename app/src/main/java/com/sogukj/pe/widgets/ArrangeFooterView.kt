package com.sogukj.pe.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.lcodecore.tkrefreshlayout.IBottomView
import com.sogukj.pe.R
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find
import kotlin.properties.Delegates


/**
 * Created by admin on 2018/3/27.
 */
class ArrangeFooterView : FrameLayout, IBottomView, AnkoLogger {
    private lateinit var footerTv: TextView
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
        footerTv = view.find(R.id.footOrHead)
        viewTreeObserver.addOnGlobalLayoutListener {
            if (viewTreeObserver.isAlive) {
                viewTreeObserver.removeOnGlobalLayoutListener {}
                originalHeight = measuredHeight
                originalSize = footerTv.textSize
            }
        }
        footerTv.text = "上拉查看上周工作安排"
        addView(view)
    }

    override fun onFinish() {

    }

    override fun onPullingUp(fraction: Float, maxBottomHeight: Float, bottomHeight: Float) {
//        val params = footerTv.layoutParams as LinearLayout.LayoutParams
//        val height = params.height
//        if (height < bottomHeight) {
//            footerTv.textSize = 14 * (1 - fraction)
//            if (footerTv.textSize > 80f){
//                footerTv.textSize = 80f
//            }
//            info { "onPullingUp  size::${footerTv.textSize}" }
//        }
    }

    override fun getView() = this

    override fun onPullReleasing(fraction: Float, maxBottomHeight: Float, bottomHeight: Float) {
//        footerTv.textSize = 14 * (1 + fraction)
//        if (footerTv.textSize > 80f){
//            footerTv.textSize = 80f
//        }
//        info { "onPullReleasing  size::${footerTv.textSize}" }
    }

    override fun reset() {

    }

    override fun startAnim(maxBottomHeight: Float, bottomHeight: Float) {
    }
}