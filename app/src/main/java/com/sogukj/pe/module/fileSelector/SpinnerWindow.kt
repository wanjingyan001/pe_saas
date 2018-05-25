package com.sogukj.pe.module.fileSelector

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ListView
import android.widget.PopupWindow
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.utils.Utils
import org.jetbrains.anko.find

/**
 * Created by admin on 2018/3/5.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class SpinnerWindow(val context: Context, val listener: AdapterView.OnItemClickListener) : PopupWindow(context) {
    private lateinit var inflate: View

    init {
        initView()
    }

    private fun initView() {
        inflate = LayoutInflater.from(context).inflate(R.layout.layout_spinner_window, null)
        val spinnerList = inflate.find<ListView>(R.id.spinner_list)
        spinnerList.adapter = SpinnerAdapter(context, context.resources.getStringArray(R.array.spinner_select_file))
        spinnerList.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            if (isShowing) {
                dismiss()
            }
            listener.onItemClick(parent, view, position, id)
        }
        contentView = inflate
        width = Utils.dpToPx(context, 145)
        height = WindowManager.LayoutParams.WRAP_CONTENT
        isFocusable = true
        isOutsideTouchable = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            elevation = Utils.dpToPx(context, 5).toFloat()
        }
        setBackgroundDrawable(ColorDrawable(0x00000000))
    }
}