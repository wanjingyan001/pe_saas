package com.sogukj.pe.module.approve.baseView.controlView

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.widget.RadioButton
import com.google.gson.internal.LinkedTreeMap
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.module.approve.baseView.BaseControl
import com.sogukj.pe.module.approve.baseView.ControlFactory
import com.sogukj.pe.module.approve.baseView.viewBean.ApproveValueBean
import kotlinx.android.synthetic.main.layout_control_fund_seal.view.*

/**
 * 基金用印套件
 * Created by admin on 2018/10/12.
 */
class FundSealControl @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {
    var block: ((fundId: String?, projectId: String?) -> Unit)? = null
    override fun getContentResId(): Int = R.layout.layout_control_fund_seal

    override fun bindContentView() {
        hasInit.yes {
            val group = controlBean.children!!
            val factory = ControlFactory(activity)
            arrayOf(group[0], group[1]).forEach {
                val control = when (it.control) {
                    21 -> {
                        val fap = factory.createControl(FAPControl::class.java, it)
                        (fap as FAPControl).block = { v1, v2 ->
                            block?.invoke(v1, v2)
                        }
                        fap
                    }
                    10 -> factory.createControl(AttachmentSelection::class.java, it)
                    else -> throw Exception("")
                }
                inflate.controlLayout1.addView(getDividerView())
                inflate.controlLayout1.addView(control)
            }
            //是否需要律师意见
            group[2].let { radio ->
                inflate.lawyerTitle.text = radio.name
                radio.value?.let { values ->
                    values.isNotEmpty().yes {
                        val beans = mutableListOf<ApproveValueBean>()
                        values.forEach { map ->
                            val treeMap = map as LinkedTreeMap<*, *>
                            beans.add(ApproveValueBean(name = treeMap["name"] as String))
                        }
                        radio.value?.clear()
                        radio.value?.addAll(beans)
                    }
                }
                radio.options?.let {
                    it.forEachIndexed { index, optionBean ->
                        val button = inflate.optionGroup.getChildAt(index) as RadioButton
                        button.text = optionBean.name
                        radio.value?.let { beans ->
                            beans.isNotEmpty().yes {
                                beans as MutableList<ApproveValueBean>
                                button.isChecked = optionBean.name == beans[0].name
                                inflate.controlLayout2.setVisible(beans[0].name == "是")
                            }
                        }
                    }
                    inflate.optionGroup.setOnCheckedChangeListener { group, checkedId ->
                        when (checkedId) {
                            R.id.yes -> inflate.controlLayout2.setVisible(true)
                            R.id.no -> inflate.controlLayout2.setVisible(false)
                        }
                        radio.value?.clear()
                        radio.value?.add(ApproveValueBean(name = if (checkedId == R.id.yes) "是" else "否"))
                    }
                }
            }
            //律师意见文件和意见输入框
            arrayOf(group[3], group[4]).forEach {
                val control = when (it.control) {
                    1 -> factory.createControl(SingLineInput::class.java, it)
                    7 -> factory.createControl(PhotoSelection::class.java, it)
                    else -> throw Exception("")
                }
                inflate.controlLayout2.addView(getDividerView())
                inflate.controlLayout2.addView(control)
            }
            //是否短信通知审批人
            group[5].let {
                it.is_show!!.yes {
                    inflate.controlLayout3.addView(getDividerView())
                    inflate.controlLayout2.addView(factory.createControl(SMSNotification::class.java, it))
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        (0 until inflate.controlLayout2.childCount).forEach {
            if (inflate.controlLayout2.getChildAt(it) is PhotoSelection) {
                (inflate.controlLayout2.getChildAt(it) as PhotoSelection).onActivityResult(requestCode, resultCode, data)
            }
        }
    }
}