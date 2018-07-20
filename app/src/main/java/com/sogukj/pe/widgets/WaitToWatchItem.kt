package com.sogukj.pe.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.sogukj.pe.R
import kotlinx.android.synthetic.main.item_wait_to_watch.view.*
import org.jetbrains.anko.textColor

/**
 * Created by sogubaby on 2017/12/6.
 */
class WaitToWatchItem : LinearLayout {

    constructor(context: Context?) : super(context) {
        init(context, null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    fun init(context: Context?, attrs: AttributeSet?) {
        LayoutInflater.from(context).inflate(R.layout.item_wait_to_watch, this)

        var ta = context?.obtainStyledAttributes(attrs, R.styleable.title)
        var name = ta?.getString(R.styleable.title_name)
        des.text = name
        ta?.recycle()
    }

    fun setClick(flag: Boolean) {
        if (flag) {
            tag_item.visibility = View.VISIBLE
            des.textColor = resources.getColor(R.color.colorPrimary)
        } else {
            tag_item.visibility = View.INVISIBLE
            des.textColor = resources.getColor(R.color.text_3)
        }
        invalidate()
    }
}