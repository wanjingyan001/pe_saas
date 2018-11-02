package com.sogukj.pe.module.dataSource

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.setDrawable
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
        helper.addOnClickListener(R.id.download)
        name.text = item.pdf_name
        timeTv.text = item.date
        if (item.price.isNullOrEmpty()){
            tv_price.text= "￥0.99"
        }else{
            tv_price.text= "￥${item.price}"
        }
        if (item.status == 1){
            //已购买
            tv_pay.setDrawable(tv_pay,0,mContext!!.getDrawable(R.mipmap.ic_haven_pay))
            tv_pay.text = "已购买"
        }else{
            tv_pay.setDrawable(tv_pay,-1,mContext!!.getDrawable(R.mipmap.ic_haven_pay))
            tv_pay.text = "立即购买"
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
                tagTv.text = "证监会"
                tv_price.setVisible(false)
                tv_pay.setVisible(false)
            }
            DocumentType.INTELLIGENT -> {
                tagTv.text = "拆借合同"
                tv_price.setVisible(true)
                tv_pay.setVisible(true)
            }
            DocumentType.INDUSTRY_REPORTS -> {
                tagTv.text = item.cat
                tv_price.setVisible(false)
                tv_pay.setVisible(false)
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