package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by CH-ZH on 2018/10/22.
 */
class RechargeRecordBean : Serializable {
    var phone = ""
    var balance = ""
    var list : List<AccountList> ? = null

    class AccountList : Serializable{
        var fee = ""
        var add_time =""
        var source = 1 // 1 支付宝 2 微信
    }
}