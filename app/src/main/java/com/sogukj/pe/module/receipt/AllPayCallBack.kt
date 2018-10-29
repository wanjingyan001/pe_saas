package com.sogukj.pe.module.receipt

import android.widget.ImageView
import android.widget.TextView

/**
 * Created by CH-ZH on 2018/10/24.
 */
interface AllPayCallBack {
    fun pay(order_type: Int, count: Int, pay_type: Int, fee: String,
            tv_per_balance: TextView, iv_pre_select: ImageView,
            tv_bus_balance: TextView, iv_bus_select: ImageView)

    fun payForOther(id:String,order_type: Int, count: Int, pay_type: Int, fee: String,
                    tv_per_balance: TextView, iv_pre_select: ImageView,
                    tv_bus_balance: TextView, iv_bus_select: ImageView)
}