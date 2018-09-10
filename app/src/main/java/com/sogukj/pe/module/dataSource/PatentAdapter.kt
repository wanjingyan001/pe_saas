package com.sogukj.pe.module.dataSource

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.bean.PatentItem

/**
 * Created by admin on 2018/9/6.
 */
class PatentAdapter(data: MutableList<PatentItem>) : BaseQuickAdapter<PatentItem, BaseViewHolder>(R.layout.item_patent_list, data) {
    var searchKey: String? = null
    override fun convert(helper: BaseViewHolder, item: PatentItem) {

        helper.getView<TextView>(R.id.patentName).text = replaceText("${item.name}, ${item.number}")
        helper.getView<TextView>(R.id.applicantName).text = "申请人：${item.author}"
        helper.getView<TextView>(R.id.timeTv).text = "申请日期：${item.date}"
        helper.getView<TextView>(R.id.summaryTv).text = replaceText("【摘要】：${item.summary}")

    }

    private fun replaceText(str: String): SpannableString {
        val spannable = SpannableString(str)
        searchKey?.let {
            str.contains(it).yes {
                spannable.setSpan(ForegroundColorSpan(Color.parseColor("#1787FB"))
                        , str.indexOf(it), str.indexOf(it) + it.length,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            }
        }
        return spannable
    }
}