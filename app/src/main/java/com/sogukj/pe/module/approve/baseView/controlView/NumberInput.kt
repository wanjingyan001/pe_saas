package com.sogukj.pe.module.approve.baseView.controlView

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import com.jakewharton.rxbinding2.widget.RxTextView
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.module.approve.baseView.BaseControl
import kotlinx.android.synthetic.main.layout_control_number_edt.view.*
import org.jetbrains.anko.sdk25.coroutines.textChangedListener
import java.text.DecimalFormat
import java.text.NumberFormat

/**
 * 数字输入框
 * Created by admin on 2018/9/27.
 */
class NumberInput @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {
    private val decimalDigits: Int = 4// 小数的位数
    private val integerDigits: Int = 11// 整数的位数

    override fun getContentResId(): Int = R.layout.layout_control_number_edt

    override fun bindContentView() {
        hasInit.yes {
            inflate.star.setVisible(isMust)
            inflate.numberEdtTitle.text = controlBean.name
            inflate.numberEdt.textChangedListener {
                afterTextChanged { editable ->
                    editable?.let {
                        var s = editable.toString()
                        if (s.contains(".")) {
                            inflate.numberEdt.filters = arrayOf(InputFilter.LengthFilter(integerDigits + decimalDigits + 1))
                            if (s.length - 1 - s.indexOf(".") > decimalDigits) {
                                s = s.substring(0,
                                        s.indexOf(".") + decimalDigits + 1)
                                editable.replace(0, editable.length, s.trim())//不输入超出位数的数字
                            }
                        } else {
                            inflate.numberEdt.filters = arrayOf(InputFilter.LengthFilter(integerDigits + 1))
                            if (s.length > integerDigits) {
                                s = s.substring(0, integerDigits)
                                editable.replace(0, editable.length, s.trim())
                            }

                        }
                        if (s.trim() == ".") {//小数点开头，小数点前补0
                            s = "0" + s
                            editable.replace(0, editable.length, s.trim())
                        }
                        if (s.startsWith("0") && s.trim().length > 1) {//多个0开头，只输入一个0
                            if (s.substring(1, 2) != ".") {
                                editable.replace(0, editable.length, "0")
                            }
                        }
                    }
                }
            }
            controlBean.value?.let {
                it.isNotEmpty().yes {
                    inflate.numberEdt.setText(controlBean.value!![0] as String)
                }.otherWise {
                    inflate.numberEdt.hint = controlBean.placeholder
                }
            }
            RxTextView.textChanges(inflate.numberEdt).skipInitialValue().map { input ->
                input.toString().trimStart().trimEnd()
            }.subscribe { input ->
                controlBean.value?.clear()
                controlBean.value?.add(input)
            }
        }
    }
}