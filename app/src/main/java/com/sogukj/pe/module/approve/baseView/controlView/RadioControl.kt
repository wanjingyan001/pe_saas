package com.sogukj.pe.module.approve.baseView.controlView

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.util.AttributeSet
import android.view.View
import com.amap.api.mapcore.util.it
import com.google.gson.internal.LinkedTreeMap
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.ifNotNull
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.module.approve.baseView.BaseControl
import com.sogukj.pe.module.approve.baseView.viewBean.ApproveValueBean
import com.sogukj.pe.module.approve.baseView.viewBean.OptionBean
import kotlinx.android.synthetic.main.item_control_radio.view.*
import kotlinx.android.synthetic.main.layout_control_radio.view.*

/**
 * 横向页面单选
 * Created by admin on 2018/10/8.
 */
class RadioControl @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {
    private lateinit var radioAdapter: RecyclerAdapter<OptionBean>

    override fun getContentResId(): Int = R.layout.layout_control_radio

    override fun bindContentView() {
        hasInit.yes {
            radioAdapter = RecyclerAdapter(context) { _adapter, parent, _ ->
                RadioHolder(_adapter.getView(R.layout.item_control_radio, parent))
            }
            inflate.radioList.apply {
                layoutManager = GridLayoutManager(context, 2)
                adapter = radioAdapter
            }
            controlBean.options?.let {
                radioAdapter.refreshData(it)
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
            radioAdapter.onItemClick = { v, position ->
                controlBean.value?.clear()
                controlBean.value?.add(radioAdapter.dataList[position])
                radioAdapter.selectedPosition = position
            }
            ifNotNull(controlBean.value, controlBean.options) { value1, value2 ->
                controlBean.value!!.isNotEmpty().yes {
                    val bean = controlBean.value!![0] as ApproveValueBean
                    radioAdapter.selectedPosition =  controlBean.options!!.map { it.name }.indexOf(bean.name)
                }
            }
        }
    }

    inner class RadioHolder(itemView: View) : RecyclerHolder<OptionBean>(itemView) {
        override fun setData(view: View, data: OptionBean, position: Int) {
            view.radioItem.text = data.name
            view.radioItem.isSelected = radioAdapter.selectedPosition == position
        }
    }
}