package com.sogukj.pe.module.approve.baseView.controlView

import android.content.Context
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.View
import com.amap.api.mapcore.util.it
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.module.approve.baseView.BaseControl
import com.sogukj.pe.module.approve.baseView.viewBean.ApproveValueBean
import kotlinx.android.synthetic.main.item_control_seal_list.view.*
import kotlinx.android.synthetic.main.layout_control_seal_selection.view.*
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.textChangedListener
import kotlin.properties.Delegates

/**
 * 用印选择
 * Created by admin on 2018/10/8.
 */
class SealSelection @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {
    private lateinit var sealAdapter: RecyclerAdapter<ApproveValueBean>

    override fun getContentResId(): Int = R.layout.layout_control_seal_selection

    override fun bindContentView() {
        hasInit.yes {
            inflate.star.setVisible(isMust)
            inflate.sealSelectionTitle.text = controlBean.name
            sealAdapter = RecyclerAdapter(context) { _adapter, parent, _ ->
                SealHolder(_adapter.getView(R.layout.item_control_seal_list, parent))
            }
            val values = mutableListOf<ApproveValueBean>()
            controlBean.value?.let {
                it.forEach {
                    val treeMap = it as LinkedTreeMap<*, *>
                    val number = ((treeMap["value"] as? Number) ?: 0).toInt()
                    values.add(ApproveValueBean(name = treeMap["name"] as String, value = number))
                }
            }
            controlBean.value?.clear()
            controlBean.value?.addAll(values)
            (controlBean.value as MutableList<ApproveValueBean>).let {
                sealAdapter.dataList.addAll(it)
            }
            inflate.sealList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = sealAdapter
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            }
        }
    }

    inner class SealHolder(itemView: View) : RecyclerHolder<ApproveValueBean>(itemView) {
        var sealValue by Delegates.observable(0, { _, _, newValue ->
            itemView.sealTitle.isChecked = newValue > 0
        })

        override fun setData(view: View, data: ApproveValueBean, position: Int) {
            sealValue = data.value ?: 0
            view.sealTitle.text = data.name
            view.sealTitle.isChecked = data.value != 0
            view.sealNum.setText(sealValue.toString())
            view.sealTitle.onCheckedChange { _, isChecked ->
                isChecked.no {
                    sealValue = 0
                }.otherWise {
                    sealValue = 1
                }
                view.sealNum.setText(sealValue.toString())
            }

            view.sealNum.textChangedListener {
                onTextChanged { _, _, _, _ ->
                    val num = view.sealNum.textStr
                    num.isNotEmpty().yes {
                        (controlBean.value?.get(position) as ApproveValueBean).value = num.toInt()
                        view.sealTitle.isChecked = num.toInt() > 0
                    }
                }
            }
            view.minus.onClick {
                (sealValue > 0).yes {
                    sealValue--
                    view.sealNum.setText(sealValue.toString())
                }.otherWise {
                    sealValue = 0
                    view.sealNum.setText(sealValue.toString())
                }
            }
            view.plus.onClick {
                sealValue++
                view.sealNum.setText(sealValue.toString())
            }
        }
    }
}