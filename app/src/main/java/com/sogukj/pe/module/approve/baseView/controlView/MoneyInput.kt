package com.sogukj.pe.module.approve.baseView.controlView

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.module.approve.baseView.BaseControl
import com.sogukj.pe.module.approve.utils.NumberToCN
import com.sogukj.pe.widgets.MoneyFilter
import kotlinx.android.synthetic.main.layout_control_money_input.view.*
import org.jetbrains.anko.sdk25.coroutines.onFocusChange
import org.jetbrains.anko.sdk25.coroutines.textChangedListener
import java.util.regex.Pattern

/**
 * 金额输入框
 * Created by admin on 2018/9/27.
 */
class MoneyInput @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {
    var block: ((money: Double) -> Unit)? = null

    override fun getContentResId(): Int = R.layout.layout_control_money_input

    override fun bindContentView() {
        hasInit.yes {
            inflate.star.setVisible(isMust)
            inflate.moneyTitle.text = controlBean.name
            inflate.moneyEdt.filters = arrayOf(MoneyFilter())

            controlBean.value?.let {
                it.isNotEmpty().yes {
                    it[0].toString().isNotEmpty().yes {
                        inflate.moneyEdt.setText(it[0].toString())
                        block?.invoke(inflate.moneyEdt.textStr.toDouble())
                    }
                }
            }
            inflate.moneyEdt.onFocusChange { _, hasFocus ->
                if (!hasFocus && inflate.moneyEdt.textStr.isNotEmpty()) {
                    isConformRules(inflate.moneyEdt.textStr).no {
                        showErrorToast("输入的金额有误,请检查")
                        inflate.moneyEdt.requestFocus()
                        inflate.moneyEdt.setText("")
                    }
                }
            }
            inflate.moneyCapitalTv.setVisible(controlBean.is_show ?: false)
            controlBean.is_show?.yes {
                inflate.moneyEdt.textChangedListener {
                    afterTextChanged {
                        val moneyStr = inflate.moneyEdt.textStr
                        moneyStr.isNotEmpty().yes {
                            moneyCapitalTv.text = NumberToCN.money2CNUnit(moneyStr)
                            block?.invoke(moneyStr.toDouble())
                            controlBean.value?.clear()
                            controlBean.value?.add(moneyStr)
                            controlBean.extras?.value = NumberToCN.money2CNUnit(moneyStr)
                        }
                    }
                }
            }
        }
    }


    /**
     * 输入内容是否符合规则
     * @return
     */
    private fun isConformRules(result: String): Boolean {
        if (TextUtils.isEmpty(result)) {
            return false
        } else if (result.contains(".")) {
            if (result.startsWith(".")) {
                return false
            } else if (result.startsWith("0")) {
                val indexZero = result.indexOf("0")
                val indexPoint = result.indexOf(".")
                if (indexPoint - indexZero != 1) {
                    return false
                } else if (TextUtils.equals("0.", result)) {
                    return false
                }
            }
        } else if (!result.contains(".")) {
            if (result.startsWith("0")) {
                return false
            }
        }
        return true
    }

}