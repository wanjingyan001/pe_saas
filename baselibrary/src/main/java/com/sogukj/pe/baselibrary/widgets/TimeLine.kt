package com.sogukj.pe.baselibrary.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.TypedValue
import android.view.View

/**
 * Created by qinfei on 17/8/18.
 */
class TimeLine(context: Context?) : View(context) {
    fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).toInt()
    }

    val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        paint.strokeWidth = dpToPx(2).toFloat()
        paint.color = Color.BLUE
    }

    override fun onDraw(canvas: Canvas) {
        val x0 = dpToPx(2).toFloat()
        val y0 = 0f
        val x1 = dpToPx(4).toFloat()
        val y1 = height.toFloat()
        canvas.drawLine(x0, y0, x1, y1, paint)
        val cx = dpToPx(3).toFloat()
        val cy = y1 / 2
        canvas.drawCircle(cx, cy, dpToPx(1).toFloat(), paint)
        super.onDraw(canvas)
    }
}