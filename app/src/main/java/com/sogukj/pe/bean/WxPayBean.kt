package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by CH-ZH on 2018/11/21.
 */
class WxPayBean : Serializable {
    var prepayid = "" //预支付交易会话ID
    var appid = ""
    var partnerid = "" //商户号
    var noncestr = "" //随机字符串
    var timestamp = ""
    var sign = ""
}