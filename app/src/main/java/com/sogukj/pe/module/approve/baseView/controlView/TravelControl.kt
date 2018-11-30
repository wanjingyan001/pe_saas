package com.sogukj.pe.module.approve.baseView.controlView

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import com.amap.api.mapcore.util.it
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.jakewharton.rxbinding2.widget.RxTextView
import com.sogukj.pe.R
import com.sogukj.pe.R.drawable.copy
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.module.approve.baseView.BaseControl
import com.sogukj.pe.module.approve.baseView.ControlFactory
import com.sogukj.pe.module.approve.baseView.viewBean.ControlBean
import com.sogukj.pe.module.approve.baseView.viewBean.copyWithoutValue
import kotlinx.android.synthetic.main.layout_control_reimburse_list_footer.view.*
import kotlinx.android.synthetic.main.layout_control_travel.view.*
import org.jetbrains.anko.info
import kotlin.properties.Delegates

/**
 * 出差套件
 * Created by admin on 2018/10/12.
 */
class TravelControl @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {

    private lateinit var travelAdapter: TravelAdapter

    override fun getContentResId(): Int = R.layout.layout_control_travel

    private lateinit var groupList: MutableList<ControlBean>

    override fun bindContentView() {
        groupList = controlBean.children!! //第一层级(包含出差事由,行程详情列表,增加行程,出差总天数,出差备注,同行人)
        hasInit.yes {
            //出差事由输入框
            groupList[0].value?.let {
                it.isNotEmpty().yes {
                    inflate.travelReasonEdt.setText(it[0] as String)
                }.otherWise {
                    inflate.travelReasonEdt.hint = groupList[0].placeholder
                }
                RxTextView.textChanges(inflate.travelReasonEdt).skipInitialValue().map { input ->
                    input.toString().trimStart().trimEnd()
                }.subscribe { input ->
                    it.clear()
                    it.add(input)
                }
            }
            //行程行情列表
            val group = groupList[1].children!!
            group.let {
                travelAdapter = TravelAdapter(it)
                val footer = LayoutInflater.from(activity).inflate(R.layout.layout_control_reimburse_list_footer, null)
                footer.copyDetail.text = groupList[2].name
                travelAdapter.addFooterView(footer)
                inflate.travelList.apply {
                    layoutManager = LinearLayoutManager(activity)
                    adapter = travelAdapter
                }
                footer.copyDetail.clickWithTrigger { _ ->
                    val copy = it.last().copyWithoutValue(isDelete = groupList[1].is_delete)
                    travelAdapter.addData(it.size, copy)
                }
            }
            //出差总天数
            groupList[3].let {
                inflate.totalDays.setVisible(it.is_show != false)
                inflate.totalDaysTitle.setVisible(it.is_show != false)
                it.value?.let { value ->
                    value.isNotEmpty().yes {
                        inflate.totalDays.text = value[0] as String
                    }.otherWise {
                        inflate.totalDays.hint = it.placeholder
                    }
                }
            }
            val factory = ControlFactory(activity)
            //出差备注
            groupList[4].let {
                inflate.controlLayout.addView(getDividerView())
                inflate.controlLayout.addView(factory.createControl(MultiLineInput::class.java, it))
            }
            //同行人
            groupList[5].let {
                if (it.is_show!!) {
                    inflate.controlLayout.addView(getDividerView())
                    inflate.controlLayout.addView(factory.createControl(ContactSelection::class.java, it))
                }
            }
        }
    }


    inner class TravelAdapter(data: List<ControlBean>)
        : BaseQuickAdapter<ControlBean, BaseViewHolder>(R.layout.item_control_reimburse_detail, data) {
        var days = 0.0
        var timeUnit = "小时"

        @SuppressLint("SetTextI18n")
        override fun convert(helper: BaseViewHolder, item: ControlBean) {
            var itemDays: Pair<Double, String> by Delegates.observable(0.0 to "") { _, oldValue, newValue ->
                days = days + newValue.first - oldValue.first
                inflate.totalDays.text = "$days${newValue.second} "
                controlBean.children!![3].value?.let {
                    it.isNotEmpty().yes {
                        it[0] = "$days${newValue.second}"
                    }.otherWise {
                        it.add("$days${newValue.second}")
                    }
                }
            }

            //行程
            val detailItem = helper.getView<LinearLayout>(R.id.detailsItem)
            val delete = helper.getView<ImageView>(R.id.deleteItem)
            val position = helper.adapterPosition
            delete.setVisible(item.is_delete == true && position > 0)
            val factory = ControlFactory(activity)
            item.children?.let {
                it.forEach {
                    val control = when (it.control) {
                        8 -> {
                            it.name = "行程${position + 1}"
                            factory.createControl(NoticText::class.java, it)
                        }
                        17 -> factory.createControl(CheckBoxControl::class.java, it)
                        16 -> factory.createControl(RadioControl::class.java, it)
                        25 -> factory.createControl(CitySelection::class.java, it)
                        22 -> {
                            val dateRange = factory.createControl(DateRangeControl::class.java, it)
                            (dateRange as DateRangeControl).block = { days, unit ->
                                itemDays = days to (unit ?: "")
                                timeUnit = unit ?: ""
                            }
                            dateRange
                        }
                        else -> throw Exception("类型错误")
                    }
                    detailItem.addView(getDividerView(1))
                    detailItem.addView(control)
                }
            }
            delete.clickWithTrigger {
                travelAdapter.remove(position)
                calculateAgain()
            }
        }

        private fun calculateAgain() {
            var newTotal = 0.0
            controlBean.children!![1].children?.forEach {
                it.children!![5].value?.let {
                    if (it.size == 3) {
                        newTotal += it[2].toString().toDouble()
                    }
                }
            }
            inflate.totalDays.text = "$newTotal${travelAdapter.timeUnit}"
        }
    }
}