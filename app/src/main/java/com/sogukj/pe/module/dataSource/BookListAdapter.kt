package com.sogukj.pe.module.dataSource

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.gson.Gson
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.bean.PdfBook
import java.nio.charset.Charset

/**
 * Created by admin on 2018/9/5.
 */
class BookListAdapter(data: List<PdfBook>, var downloaded: List<String>, val type: Int) : BaseQuickAdapter<PdfBook, BaseViewHolder>(R.layout.item_data_source_pdf_list, data) {
    override fun convert(helper: BaseViewHolder, item: PdfBook) {
        val name = helper.getView<TextView>(R.id.pdfName)
        val tagTv = helper.getView<TextView>(R.id.docTag)
        val timeTv = helper.getView<TextView>(R.id.docTime)
        val download = helper.getView<ImageView>(R.id.download)
        helper.addOnClickListener(R.id.download)
        name.text = item.pdf_name
        timeTv.text = item.date
        downloaded.forEach {
            if (it == item.pdf_name || item.pdf_path.isEmpty()) {
                download.visibility = View.INVISIBLE
            }
        }
        when (type) {
            DocumentType.EQUITY -> {
                tagTv.text = "证监会"
            }
            DocumentType.INTELLIGENT -> {
                tagTv.text = "拆借合同"
            }
            DocumentType.INDUSTRY_REPORTS -> {
                tagTv.text = item.cat
            }
        }
    }
}