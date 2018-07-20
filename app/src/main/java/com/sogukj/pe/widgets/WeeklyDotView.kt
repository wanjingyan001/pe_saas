package com.sogukj.pe.widgets

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
class WeeklyDotView : ImageView {
    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    val mPaint = Paint()
    //var txt: String = "12:14"

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var lineWidth = Utils.dpToPx(context, 1)//线条宽度，单位px
        var radius = Utils.dpToPx(context, 5) / 2 //半径
        var dot_pos = Utils.dpToPx(context, 34)
        var txtSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12.toFloat(), context.resources.displayMetrics)//12sp
        var space = Utils.dpToPx(context, 5)

        mPaint.isAntiAlias = false

        mPaint.color = Color.parseColor("#f1f1f1")
        var rect = Rect(width / 2 - lineWidth / 2, dot_pos, width / 2 + lineWidth / 2, height - Utils.dpToPx(context, 10))
        canvas.drawRect(rect, mPaint)

        mPaint.color = Color.parseColor("#1787FB")
        canvas.drawCircle(width.toFloat() / 2, dot_pos.toFloat(), radius.toFloat(), mPaint)

//        var textRect = Rect()
//        mPaint.textSize = txtSize
//        mPaint.getTextBounds(txt, 0, txt.length, textRect)
//        var bottom = mPaint.fontMetrics.bottom
//        canvas.drawText(txt, width.toFloat() / 2 - textRect.width() / 2, space + dot_pos + radius.toFloat() + textRect.height(), mPaint)

//        var rect = Rect(width / 2 - lineWidth / 2, dot_pos + radius + textRect.height() + space * 2, width / 2 + lineWidth / 2, height - Utils.dpToPx(context, 15))
//        canvas.drawRect(rect, mPaint)
    }

//    fun setTime(time: String) {
//        txt = time
//        invalidate()
//    }
}