package com.sogukj.pe.module.dataSource

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sogukj.pe.R
import com.sogukj.pe.bean.PatentItem

/**
 * Created by admin on 2018/9/6.
 */
class PatentAdapter(data: MutableList<PatentItem>) : BaseQuickAdapter<PatentItem, BaseViewHolder>(R.layout.item_patent_list, data) {
    override fun convert(helper: BaseViewHolder, item: PatentItem) {
        helper.getView<TextView>(R.id.num).text = helper.adapterPosition.toString()
        helper.getView<TextView>(R.id.patentName).text = "${item.name}, ${item.number}"
        helper.getView<TextView>(R.id.applicantName).text = "申请人:${item.author}"
        helper.getView<TextView>(R.id.timeTv).text = "申请日期:${item.date}"
    }
}