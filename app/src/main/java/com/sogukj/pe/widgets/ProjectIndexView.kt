package com.sogukj.pe.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.sogukj.pe.R

/**
 * Created by CH-ZH on 2018/9/19.
 */
class ProjectIndexView : LinearLayout {
    private var mContext : Context ? = null
    private var mRootView : View? = null
    constructor(context: Context) : super(context){
        mContext = context
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mContext = context
        initView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mContext = context
        initView()
    }

    private fun initView() {
        mRootView = View.inflate(mContext, R.layout.layout_project_index,this)
    }
}