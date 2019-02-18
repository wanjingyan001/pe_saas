package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by CH-ZH on 2018/10/17.
 */
class MineReceiptBean : Serializable {
    var fee = ""  //单价
    var is_invoice = 0 //1:已开 0:未开
    var type = "" //标题
    var order_no = "" //订单号
    var pay_time = ""
    var count  = 0
    var isSelect = false
    var pay_source = "" //支付方式
    var invoice_type = 1  // 1：账号付费 2：智能文书付费 3：征信付费 4:舆情监控 5：纸质发票运费
    var price = ""//总价
}