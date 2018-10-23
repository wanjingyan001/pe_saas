package com.sogukj.pe.module.approve.baseView.controlView

import android.content.Context
import android.util.AttributeSet
import com.jakewharton.rxbinding2.widget.RxTextView
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.module.approve.baseView.BaseControl
import kotlinx.android.synthetic.main.layout_control_singline_edt.view.*

/**
 * 审批单行输入框
 * Created by admin on 2018/9/27.
 */
class SingLineInput @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {

    override fun getContentResId(): Int {
        return R.layout.layout_control_singline_edt
    }

    override fun bindContentView() {
        hasInit.yes {
            inflate.star.setVisible(isMust)
            inflate.singLineTitle.text = controlBean.name
            controlBean.value?.let {
                it.isNotEmpty().yes {
                    inflate.singLineEdt.setText(controlBean.value!![0] as String)
                }.otherWise {
                    inflate.singLineEdt.hint = controlBean.placeholder
                }
            }
            RxTextView.textChanges(inflate.singLineEdt).skipInitialValue().map { input ->
                input.toString().trimStart().trimEnd()
            }.subscribe { input ->
                controlBean.value?.clear()
                controlBean.value?.add(input)
            }
        }
    }
}