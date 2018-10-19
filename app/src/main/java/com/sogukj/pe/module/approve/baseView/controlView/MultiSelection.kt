package com.sogukj.pe.module.approve.baseView.controlView

import android.content.Context
import android.util.AttributeSet
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.amap.api.mapcore.util.it
import com.google.gson.internal.LinkedTreeMap
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.module.approve.baseView.BaseControl
import com.sogukj.pe.module.approve.baseView.viewBean.ApproveValueBean
import kotlinx.android.synthetic.main.layout_control_multi_selection.view.*

/**
 * 多选框
 * Created by admin on 2018/10/10.
 */
class MultiSelection @JvmOverloads constructor(

        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {
    override fun getContentResId(): Int = R.layout.layout_control_multi_selection
    override fun bindContentView() {
        hasInit.yes {
            inflate.star.setVisible(isMust)
            inflate.multiSelectionTitle.text = controlBean.name
            controlBean.value?.let {values->
                values.isNotEmpty().yes {
                    val beans = mutableListOf<ApproveValueBean>()
                    values.forEach { map ->
                        val treeMap = map as LinkedTreeMap<String, Any>
                        beans.add(ApproveValueBean(name = treeMap["name"] as String))
                    }
                    controlBean.value?.clear()
                    controlBean.value?.addAll(beans)
                    inflate.multiTv.text = beans.joinToString(",") { it.name }
                    inflate.multiTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }.otherWise {
                    inflate.multiTv.hint = controlBean.placeholder
                    inflate.multiTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right, 0)
                }
            }
            inflate.clickWithTrigger {
                controlBean.options?.let {
                    val position = arrayListOf<Int>()
                    it.map { it.name }.forEachIndexed { index, s ->
                        (controlBean.value as  MutableList<ApproveValueBean>).map { it.name }.contains(s).yes {
                            position.add(index)
                        }
                    }
                    MaterialDialog.Builder(activity)
                            .theme(Theme.LIGHT)
                            .title(controlBean.name!!)
                            .items(it.map { it.name })
                            .itemsCallbackMultiChoice(position.toTypedArray()){dialog, which, text ->
                                controlBean.value?.clear()
                                which.forEach { i->
                                    controlBean.value?.add(ApproveValueBean(name = it[i].name))
                                }
                                inflate.multiTv.text = text.joinToString(",")
                                inflate.multiTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                                true
                            }
                            .positiveText("确定")
                            .negativeText("取消")
                            .show()
                }

            }
        }
    }
}