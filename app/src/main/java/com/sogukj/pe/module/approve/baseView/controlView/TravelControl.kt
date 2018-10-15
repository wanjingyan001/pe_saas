package com.sogukj.pe.module.approve.baseView.controlView

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.module.approve.baseView.BaseControl
import com.sogukj.pe.module.approve.baseView.ControlFactory
import com.sogukj.pe.module.approve.baseView.viewBean.ControlBean
import kotlinx.android.synthetic.main.layout_control_reimburse_list_footer.view.*
import kotlinx.android.synthetic.main.layout_control_travel.view.*
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.dip

/**
 * 出差套件
 * Created by admin on 2018/10/12.
 */
class TravelControl @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {

    private lateinit var travelAdapter: TravelAdapter

    override fun getContentResId(): Int = R.layout.layout_control_travel

    override fun bindContentView() {
        val groupList = controlBean.children!! //第一层级(包含出差事由,行程详情列表,增加行程,出差总天数,出差备注,同行人)
        hasInit.yes {
            //出差事由输入框
            groupList[0].value?.let {
                it.isNotEmpty().yes {
                    inflate.travelReasonEdt.setText(it[0] as String)
                }.otherWise {
                    inflate.travelReasonEdt.hint = groupList[0].placeholder
                }
            }
            //行程行情列表
            groupList[1].children?.let {
                travelAdapter = TravelAdapter(it)
                val footer = LayoutInflater.from(activity).inflate(R.layout.layout_control_reimburse_list_footer, null)
                footer.copyDetail.text = groupList[2].name
                travelAdapter.addFooterView(footer)
                inflate.travelList.apply {
                    layoutManager = LinearLayoutManager(activity)
                    adapter = travelAdapter
                }
                footer.copyDetail.clickWithTrigger { _ ->
                    it.add(resetValues(it[0].copy()))
                    travelAdapter.notifyDataSetChanged()
                }
            }
            //出差总天数
            groupList[3].let {
                inflate.totalDays.setVisible(it.is_show ?: true)
                inflate.totalDaysTitle.setVisible(it.is_show ?: true)
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


    inner class TravelAdapter(data: List<ControlBean>) : BaseQuickAdapter<ControlBean, BaseViewHolder>(R.layout.item_control_reimburse_detail, data) {
        override fun convert(helper: BaseViewHolder, item: ControlBean) {
            //行程
            val detailItem = helper.getView<LinearLayout>(R.id.detailsItem)
            val delete = helper.getView<ImageView>(R.id.deleteItem)
            val factory = ControlFactory(activity)
            item.children?.let {
                it.forEach {
                    val control = when (it.control) {
                        8 -> {
                            it.name = "行程${helper.adapterPosition + 1}"
                            factory.createControl(NoticText::class.java, it)
                        }
                        17 -> factory.createControl(CheckBoxControl::class.java, it)
                        16 -> factory.createControl(RadioControl::class.java, it)
                        25 -> factory.createControl(CitySelection::class.java, it)
                        22 -> factory.createControl(DateRangeControl::class.java, it)
                        else -> throw Exception("")
                    }
                    detailItem.addView(getDividerView(1))
                    detailItem.addView(control)
                }
            }
        }
    }
}