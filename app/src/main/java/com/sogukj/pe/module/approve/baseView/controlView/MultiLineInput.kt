package com.sogukj.pe.module.approve.baseView.controlView

import android.content.Context
import android.util.AttributeSet
import com.jakewharton.rxbinding2.widget.RxTextView
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.isNullOrEmpty
import com.sogukj.pe.baselibrary.Extended.no
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.module.approve.baseView.BaseControl
import kotlinx.android.synthetic.main.layout_control_multi_line_edt.view.*

/**
 * 审批多行输入框
 * Created by admin on 2018/9/27.
 */
class MultiLineInput @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {
    override fun getContentResId(): Int  = R.layout.layout_control_multi_line_edt

    override fun bindContentView() {
       hasInit.yes{
           inflate.multiLineTitle.text = controlBean.name
           controlBean.value?.let {
               it.isNotEmpty().yes {
                   inflate.multiLineEdt.setText(controlBean.value!![0] as String)
               }.otherWise {
                   inflate.multiLineEdt.hint = controlBean.placeholder
               }
           }
           RxTextView.textChanges(inflate.multiLineTitle).skipInitialValue().map { input->
               input.toString().trimStart().trimEnd()
           }.subscribe { input ->
               if (controlBean.value.isNullOrEmpty()) {
                   controlBean.value?.add(input)
               } else {
                   controlBean.value?.let {
                       it[0] = input
                   }
               }
           }
       }
    }
}