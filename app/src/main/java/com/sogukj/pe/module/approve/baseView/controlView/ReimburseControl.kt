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
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.module.approve.baseView.BaseControl
import com.sogukj.pe.module.approve.baseView.ControlFactory
import com.sogukj.pe.module.approve.baseView.viewBean.ControlBean
import com.sogukj.pe.module.approve.utils.NumberToCN
import kotlinx.android.synthetic.main.layout_control_money_input.view.*
import kotlinx.android.synthetic.main.layout_control_reimburse.view.*
import kotlinx.android.synthetic.main.layout_control_reimburse_list_footer.view.*
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
            //	"control": 0
            group = grouplist.children!!
            group.let {
                detailAdapter = DetailAdapter(it)
                val footer = LayoutInflater.from(activity).inflate(R.layout.layout_control_reimburse_list_footer, null)
                detailAdapter.addFooterView(footer)
                inflate.reimburseDetailList.apply {
                    layoutManager = LinearLayoutManager(activity)
                    adapter = detailAdapter
                    //                    addItemDecoration(SpaceItemDecoration(dip(12)))
                }
                footer.copyDetail.clickWithTrigger { _ ->
                    val element = resetValues(it[0].copy())
                    element?.let {
                        group.add(it)
                        detailAdapter.notifyItemInserted(group.size - 1)
                    }
                }
            }
        }
    }


    inner class DetailAdapter(data: List<ControlBean>) : BaseQuickAdapter<ControlBean, BaseViewHolder>(R.layout.item_control_reimburse_detail, data) {
        var moneyNum = 0.0

        override fun convert(helper: BaseViewHolder, item: ControlBean) {
            var itemMoney: Double by Delegates.observable(0.0, { property, oldValue, newValue ->
                moneyNum = moneyNum + newValue - oldValue
                totals.add(moneyNum)
                totals.forEach {
                    info { it }
                }
                helper.itemView.postDelayed({
                    val d = totals[totals.size - 1]
                    totals.clear()
                    totals.add(d)
                    inflate.totalAmount.text = d.toString()
                    inflate.CNMoney.text = NumberToCN.money2CNUnit(d.toString())
                }, 1000)

            })
            //"报销明细"的控件组(包含提示文字,金额输入,报销类型选择,费用明细填写)
            val detailItem = helper.getView<LinearLayout>(R.id.detailsItem)
            val delete = helper.getView<ImageView>(R.id.deleteItem)
            delete.setVisible(item.is_delete ?: false)
            val factory = ControlFactory(activity)
            item.children?.let {
                it.forEach {
                    val control = when (it.control) {
                        8 -> {
                            it.name = "报销明细${helper.adapterPosition + 1}"
                            factory.createControl(NoticText::class.java, it)
                        }
                        9 -> {
                            val input = factory.createControl(MoneyInput::class.java, it)
                            (input as MoneyInput).block = { money ->
                                itemMoney = money
                            }
                            input
                        }
                        4 -> factory.createControl(SingSelection::class.java, it)
                        2 -> factory.createControl(MultiLineInput::class.java, it)
                        else -> throw  Exception("")
                    }
                    val view = View(activity)
                    val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dip(1))
                    view.layoutParams = lp
                    view.backgroundColorResource = R.color.divider2
                    detailItem.addView(view)
                    detailItem.addView(control)
                }
            }
        }

    }
}