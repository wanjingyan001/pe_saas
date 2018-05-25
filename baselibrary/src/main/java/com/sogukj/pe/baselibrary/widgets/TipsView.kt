package com.sogukj.pe.baselibrary.widgets

/**
 * Created by qff on 2016/3/24.
 */

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.TextView
import com.sogukj.pe.baselibrary.utils.Utils


class TipsView : android.support.v7.widget.AppCompatTextView {
    private var number = 0
    private var color = Color.RED

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (number > 0) {
            val textPaint = Paint()
            val textRect = Rect()
            val text = number.toString() + ""
            textPaint.color = color
            textPaint.isAntiAlias = true
            textPaint.textSize = Utils.dpToPx(context, if (text.length == 1) 10 else if (text.length == 2) 9 else 8).toFloat()
            textPaint.typeface = Typeface.SANS_SERIF
            textPaint.getTextBounds(text, 0, text.length, textRect)
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(text, (width - textRect.width()).toFloat(), textRect.height().toFloat(), textPaint)
        }
    }


    fun display(number: Int, color: Int) {
        this.number = number
        this.color = color
        invalidate()
    }
    
}