package com.sogukj.pe.baselibrary.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ListView


class ListViewCompat : ListView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val expandSpec = View.MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE shr 2, View.MeasureSpec.AT_MOST)

        setMeasuredDimension(widthMeasureSpec, expandSpec)
        super.onMeasure(widthMeasureSpec, expandSpec)
    }
}
