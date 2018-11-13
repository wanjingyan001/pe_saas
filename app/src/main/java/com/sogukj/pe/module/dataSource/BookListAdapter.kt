package com.sogukj.pe.module.dataSource

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.bean.PdfBook

/**
 * Created by admin on 2018/9/5.
 */
class BookListAdapter(data: List<PdfBook>, var downloaded: List<String>, val type: Int) : BaseQuickAdapter<PdfBook, BaseViewHolder>(R.layout.item_data_source_pdf_list, data) {
    var callBack : ClickPayCallBack ? = null
    override fun convert(helper: BaseViewHolder, item: PdfBook) {
        val name = helper.getView<TextView>(R.id.pdfName)
        val tagTv = helper.getView<TextView>(R.id.docTag)
        val timeTv = helper.getView<TextView>(R.id.docTime)
        val download = helper.getView<ImageView>(R.id.download)
        val tv_price = helper.getView<TextView>(R.id.tv_price)
        val tv_pay = helper.getView<TextView>(R.id.tv_pay)
        val tv_pay_already = helper.getView<TextView>(R.id.tv_pay_already)
        val docTime_normal = helper.getView<TextView>(R.id.docTime_normal)
        helper.addOnClickListener(R.id.download)
        name.text = item.pdf_name
        timeTv.text = item.date
        docTime_normal.text = item.date
        if (item.price.isNullOrEmpty()){
            tv_price.text= "￥0.99"
        }else{
            tv_price.text= "￥${item.price}"
        }
        if (item.status == 1){
            //已购买
            tv_pay_already.setVisible(true)
            tv_price.setVisible(false)
            tv_pay.setVisible(false)
        }else{
            tv_pay.setVisible(true)
            tv_price.setVisible(true)
            tv_pay_already.visibility = View.INVISIBLE
            tv_pay.clickWithTrigger {
                //智能文书购买
                if (null != callBack){
                    var price = "0.99"
                    if (!item.price.isNullOrEmpty()){
                        price = item.price!!
                    }
                    callBack!!.clickPay(item.pdf_name,price,1,item.id.toString(),item)
                }
            }
        }
        downloaded.forEach {
            if (it == item.pdf_name || item.pdf_path.isEmpty()) {
                download.visibility = View.INVISIBLE
            }
        }
        when (type) {
            DocumentType.EQUITY -> {
                //招股书
                tagTv.text = "证监会"
                tv_price.setVisible(false)
                tv_pay.setVisible(false)
                docTime_normal.setVisible(true)
                timeTv.setVisible(false)
            }
            DocumentType.INTELLIGENT -> {
                //智能文书
                tagTv.text = "拆借合同"
                docTime_normal.visibility = View.INVISIBLE
                timeTv.setVisible(true)
            }
            DocumentType.INDUSTRY_REPORTS -> {
                //行业研报
                tagTv.text = item.cat
                tv_price.setVisible(false)
                tv_pay.setVisible(false)
                docTime_normal.setVisible(true)
                timeTv.setVisible(false)
            }
        }
    }

    open fun setClickPayListener(callBack : ClickPayCallBack){
        this.callBack = callBack
    }

    interface ClickPayCallBack{
        fun clickPay(title:String,price:String,count:Int,id:String,book:PdfBook)
    }
}