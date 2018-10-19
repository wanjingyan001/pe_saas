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
import kotlinx.android.synthetic.main.layout_control_sing_selection.view.*

/**
 * 单选框
 * Created by admin on 2018/10/10.
 */
class SingSelection @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {
    override fun getContentResId(): Int = R.layout.layout_control_sing_selection

    override fun bindContentView() {
        hasInit.yes {
            inflate.star.setVisible(isMust)
            inflate.singSelectionTitle.text = controlBean.name
            controlBean.value?.let { values->
                values.isNotEmpty().yes {
                    val beans = mutableListOf<ApproveValueBean>()
                    values.forEach { map ->
                        val treeMap = map as LinkedTreeMap<String, Any>
                        beans.add(ApproveValueBean(name = treeMap["name"] as String))
                    }
                    controlBean.value?.clear()
                    controlBean.value?.addAll(beans)
                    inflate.singTv.text = (controlBean.value!![0] as ApproveValueBean).name
                    inflate.singTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }.otherWise {
                    inflate.singTv.hint = controlBean.placeholder
                    inflate.singTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right, 0)
                }
                inflate.clickWithTrigger {
                    controlBean.options?.let {
                        var index = -1
                        controlBean.value?.let { bean ->
                            bean.isNotEmpty().yes {
                                index = it.map { it.name }.indexOf((bean[0] as ApproveValueBean).name)
                            }
                        }
                        MaterialDialog.Builder(activity)
                                .title(controlBean.name!!)
                                .theme(Theme.LIGHT)
                                .items(it.map { it.name })
                                .itemsCallbackSingleChoice(index) { dialog, itemView, which, text ->
                                    inflate.singTv.text = it[which].name
                                    controlBean.value?.clear()
                                    controlBean.value?.add(ApproveValueBean(it[which].name))
                                    dialog.dismiss()
                                    true
                                }
                                .show()
                    }

                }
            }
        }
    }
}