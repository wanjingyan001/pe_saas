package com.sogukj.pe.module.approve.baseView.controlView

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.util.AttributeSet
import android.view.View
import com.google.gson.internal.LinkedTreeMap
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.module.approve.baseView.BaseControl
import com.sogukj.pe.module.approve.baseView.viewBean.ApproveValueBean
import com.sogukj.pe.module.approve.baseView.viewBean.OptionBean
import kotlinx.android.synthetic.main.item_control_checkbox_option.view.*
import kotlinx.android.synthetic.main.layout_control_checkbox.view.*

/**
 * 横向页面多选
 * Created by admin on 2018/10/8.
 */
class CheckBoxControl @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {
    private lateinit var optionAdapter: RecyclerAdapter<OptionBean>
    override fun getContentResId(): Int = R.layout.layout_control_checkbox

    override fun bindContentView() {
        hasInit.yes {
            optionAdapter = RecyclerAdapter(context) { _adapter, parent, _ ->
                OptionHolder(_adapter.getView(R.layout.item_control_checkbox_option, parent))
            }
            controlBean.options?.let {
                optionAdapter.refreshData(it)
            }
            controlBean.value?.let { values ->
                val beans = mutableListOf<ApproveValueBean>()
                values.forEach { map ->
                    val treeMap = map as LinkedTreeMap<String, Any>
                    beans.add(ApproveValueBean(name = treeMap["name"] as String))
                }
                controlBean.value?.clear()
                controlBean.value?.addAll(beans)
            }
            inflate.optionList.apply {
                layoutManager = GridLayoutManager(context, 2)
                adapter = optionAdapter
            }
            optionAdapter.onItemClick = { v, p ->
                controlBean.value?.let {
                    it.contains(optionAdapter.dataList[p]).yes {
                        it.removeAt(p)
                    }.otherWise {
                        it.add(optionAdapter.dataList[p])
                    }
                    optionAdapter.notifyItemChanged(p)
                }
            }
        }
    }

    inner class OptionHolder(itemView: View) : RecyclerHolder<OptionBean>(itemView) {
        override fun setData(view: View, data: OptionBean, position: Int) {
            view.checkboxOption.text = data.name
            controlBean.value?.let {
                it.forEach { a ->
                    a as ApproveValueBean
                    view.checkboxOption.isChecked = a.name == data.name
                }
            }
        }
    }
}