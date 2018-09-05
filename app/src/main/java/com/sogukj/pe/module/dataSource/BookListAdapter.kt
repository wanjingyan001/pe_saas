package com.sogukj.pe.module.dataSource

import android.widget.ImageView
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.gson.Gson
import com.sogukj.pe.R
import com.sogukj.pe.bean.PdfBook
import java.nio.charset.Charset

/**
 * Created by admin on 2018/9/5.
 */
class BookListAdapter(data: List<PdfBook>) : BaseQuickAdapter<PdfBook, BaseViewHolder>(R.layout.item_data_source_pdf_list, data) {
    override fun convert(helper: BaseViewHolder, item: PdfBook) {
        val name = helper.getView<TextView>(R.id.pdfName)
        val tagTv = helper.getView<TextView>(R.id.docTag)
        val timeTv = helper.getView<TextView>(R.id.docTime)
        helper.addOnClickListener(R.id.download)
        name.text = item.name
        timeTv.text = item.time
    }
}