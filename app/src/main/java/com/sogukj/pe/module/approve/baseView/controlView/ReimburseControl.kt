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
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.isNullOrEmpty
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.module.approve.baseView.BaseControl
import com.sogukj.pe.module.approve.baseView.ControlFactory
import com.sogukj.pe.module.approve.baseView.viewBean.ControlBean
import com.sogukj.pe.module.approve.utils.NumberToCN
import kotlinx.android.synthetic.main.layout_control_reimburse.view.*
import kotlinx.android.synthetic.main.layout_control_reimburse_list_footer.view.*
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dip
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

    override fun getContentResId(): Int = R.layout.layout_control_reimburse

    private var group: MutableList<ControlBean> by Delegates.observable(mutableListOf(), { property, oldValue, newValue ->
        var totalMoney = 0.0
        (newValue.size > 0).yes {
            group.forEach {
                it.children!!.filter { it.control == 9 }.filterNot { it.value.isNullOrEmpty() }.forEach {
                    it.value!!.isNotEmpty().yes {
                        val singMoney = (it.value!![0] as String).toDouble()
                        totalMoney += singMoney
                    }
                }
            }
            inflate.totalAmount.text = totalMoney.toString()
            inflate.CNMoney.text =  NumberToCN.money2CNUnit(totalMoney.toString())
        }
    })

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
                    it.add(resetValues(it[0].copy()))
                    detailAdapter.notifyDataSetChanged()
                }
            }
        }
    }


    inner class DetailAdapter(data: List<ControlBean>) : BaseQuickAdapter<ControlBean, BaseViewHolder>(R.layout.item_control_reimburse_detail, data) {
        override fun convert(helper: BaseViewHolder, item: ControlBean) {
            //"报销明细"的控件组(包含提示文字,金额输入,报销类型选择,费用明细填写)
            val detailItem = helper.getView<LinearLayout>(R.id.detailsItem)
            val delete = helper.getView<ImageView>(R.id.deleteItem)
            val factory = ControlFactory(activity)
            item.children?.let {
                it.forEach {
                    val control = when (it.control) {
                        8 -> {
                            it.name = "报销明细${helper.adapterPosition + 1}"
                            factory.createControl(NoticText::class.java, it)
                        }
                        9 -> factory.createControl(MoneyInput::class.java, it)
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