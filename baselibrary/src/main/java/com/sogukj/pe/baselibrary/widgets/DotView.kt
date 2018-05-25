package com.sogukj.pe.baselibrary.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.widget.ImageView
import com.sogukj.pe.baselibrary.utils.Utils

/**
 * Created by sogubaby on 2017/11/30.
 */
class DotView : ImageView {
    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    var upFlag: Boolean = false
    var lowFlag: Boolean = false
    var mImportant: Boolean = false
    val mPaint = Paint()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var lineWidth = Utils.dpToPx(context, 2)//线条宽度，单位px
        var radius = Utils.dpToPx(context, 7) / 2 //半径
        var dot_pos = Utils.dpToPx(context, 20)//点最上面的距离
        var center_pos = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 23.5f, context.resources.displayMetrics).toInt()
        mPaint.isAntiAlias = false
        if (upFlag) {
            mPaint.color = Color.parseColor("#f1f1f1")
            var rect = Rect(width / 2 - lineWidth / 2, 0, width / 2 + lineWidth / 2, dot_pos)
            canvas.drawRect(rect, mPaint)
        }
        if (lowFlag) {
            mPaint.color = Color.parseColor("#f1f1f1")
            var rect = Rect(width / 2 - lineWidth / 2, dot_pos + radius * 2, width / 2 + lineWidth / 2, height)
            canvas.drawRect(rect, mPaint)
        }
        if (mImportant) {
            mPaint.color = Color.parseColor("#ff5858")
            canvas.drawCircle(width.toFloat() / 2, center_pos.toFloat(), radius.toFloat(), mPaint)
        } else {
            mPaint.color = Color.parseColor("#608cf8")
            canvas.drawCircle(width.toFloat() / 2, center_pos.toFloat(), radius.toFloat(), mPaint)
        }
    }

    fun setUp(upFlag: Boolean) {
        this.upFlag = upFlag
        invalidate()
    }

    fun setLow(lowFlag: Boolean) {
        this.lowFlag = lowFlag
        invalidate()
    }

    fun setImportant(isImportant: Boolean) {
        this.mImportant = isImportant
        invalidate()
    }
}