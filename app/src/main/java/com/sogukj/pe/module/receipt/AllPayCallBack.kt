package com.sogukj.pe.module.receipt

import android.app.Dialog
import android.widget.ImageView
import android.widget.TextView
import com.sogukj.pe.bean.PdfBook

/**
 * Created by CH-ZH on 2018/10/24.
 */
interface AllPayCallBack {
    fun pay(order_type: Int, count: Int, pay_type: Int, fee: String,
            tv_per_balance: TextView, iv_pre_select: ImageView,
            tv_bus_balance: TextView, iv_bus_select: ImageView,
            tv_per_title:TextView,tv_bus_title:TextView,dialog: Dialog)

    fun payForOther(id:String,order_type: Int, count: Int, pay_type: Int, fee: String,
                    tv_per_balance: TextView, iv_pre_select: ImageView,
                    tv_bus_balance: TextView, iv_bus_select: ImageView,
                    tv_per_title:TextView,tv_bus_title:TextView,dialog: Dialog,book: PdfBook)
}