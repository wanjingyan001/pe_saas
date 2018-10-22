package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by CH-ZH on 2018/10/17.
 */
class MineReceiptBean : Serializable {
    var fee = 0f  //支付金额
    var is_invoice = 0 //1:已开 0:未开
    var type = "" //标题
    var order_no = "" //订单号
    var pay_time = ""
    var count  = 0
}