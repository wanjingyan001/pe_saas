package com.sogukj.pe.module.approve.baseView.controlView

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.forEach
import com.amap.api.mapcore.util.it
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.utils.RxBus
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.module.approve.baseView.BaseControl
import com.sogukj.pe.module.approve.baseView.ControlFactory
import com.sogukj.pe.module.approve.baseView.viewBean.ControlBean
import com.sogukj.pe.module.approve.baseView.viewBean.copyWithoutValue
import com.sogukj.pe.module.approve.utils.NumberToCN
import kotlinx.android.synthetic.main.layout_control_money_input.view.*
import kotlinx.android.synthetic.main.layout_control_reimburse.view.*
import kotlinx.android.synthetic.main.layout_control_reimburse_list_footer.view.*
import kotlinx.android.synthetic.main.layout_control_travel.view.*
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dip
import org.jetbrains.anko.info
import kotlin.properties.Delegates

/**
 * 报销套件
 * Created by admin on 2018/10/11.
 */
class ReimburseControl @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {

    private lateinit var grouplist: ControlBean //"control": -1000 可以通过点击"添加明细"自我复制的控件组+

    private lateinit var detailAdapter: DetailAdapter

    private var totals = mutableListOf<Double>()

    override fun getContentResId(): Int = R.layout.layout_control_reimburse

    private var group = mutableListOf<ControlBean>()

    override fun bindContentView() {
        hasInit.yes {
            grouplist = controlBean.children!![0]
            group = grouplist.children!!
            group.let {
                detailAdapter = DetailAdapter(it)
                val footer = LayoutInflater.from(activity).inflate(R.layout.layout_control_reimburse_list_footer, null)
                detailAdapter.addFooterView(footer)
                inflate.reimburseDetailList.apply {
                    layoutManager = LinearLayoutManager(activity)
                    adapter = detailAdapter
                }
                footer.copyDetail.clickWithTrigger { _ ->
                    val copy = it.last().copyWithoutValue(isDelete = grouplist.is_delete)
                    detailAdapter.addData(it.size, copy)
                }
            }
            //总报销金额
            controlBean.children!![2].let {
                it.value?.let { totalNum ->
                    totalNum.isNotEmpty().yes {
                        detailAdapter.moneyNum = totalNum[0].toString().toDouble()
                        inflate.totalAmount.text = totalNum[0].toString()
                    }.otherWise {
                        inflate.totalAmount.hint = it.placeholder
                    }
                }
                it.extras?.let {
                    it.value.isNotEmpty().yes {
                        inflate.CNMoney.text = it.value
                    }
                }
            }
        }
    }


    inner class DetailAdapter(data: List<ControlBean>)
        : BaseQuickAdapter<ControlBean, BaseViewHolder>(R.layout.item_control_reimburse_detail, data) {
        var moneyNum = 0.0

        override fun convert(helper: BaseViewHolder, item: ControlBean) {
            val position = helper.adapterPosition
            //"报销明细"的控件组(包含提示文字,金额输入,报销类型选择,费用明细填写)
            val detailItem = helper.getView<LinearLayout>(R.id.detailsItem)
            val delete = helper.getView<ImageView>(R.id.deleteItem)
            delete.setVisible(item.is_delete == true && position > 0)
            val factory = ControlFactory(activity)
            item.children?.let {
                it.forEach {
                    val control = when (it.control) {
                        8 -> {
                            it.name = "报销明细${position + 1}"
                            factory.createControl(NoticText::class.java, it)
                        }
                        9 -> {
                            val input = factory.createControl(MoneyInput::class.java, it)
                            (input as MoneyInput).block = { money ->
                                val value = grouplist.children!![position].children!![1].value
                                value?.clear()
                                value?.add(money.toString())
                                calculateAgain()
                            }
                            input
                        }
                        4 -> factory.createControl(SingSelection::class.java, it)
                        2 -> factory.createControl(MultiLineInput::class.java, it)
                        else -> throw  Exception("类型错误")
                    }
                    val view = View(activity)
                    val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dip(1))
                    view.layoutParams = lp
                    view.backgroundColorResource = R.color.divider2
                    detailItem.addView(view)
                    detailItem.addView(control)
                }
            }
            delete.clickWithTrigger {
                detailAdapter.remove(position)
                calculateAgain()
            }
        }

        private fun calculateAgain() {
            var totalNum = 0.0
            grouplist.children?.forEach {
                it.children!![1].value?.let {
                    it.isNotEmpty().yes {
                        info { it[0].toString() }
                        totalNum += it[0].toString().toDouble()
                    }
                }
            }
            inflate.totalAmount.text = totalNum.toString()
            inflate.CNMoney.text = NumberToCN.money2CNUnit(totalNum.toString())
            controlBean.children!![2].value?.clear()
            controlBean.children!![2].value?.add(totalNum.toString())
            controlBean.children!![2].extras?.value = NumberToCN.money2CNUnit(totalNum.toString())
        }
    }
}