package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/7.
 */
class StockBean : Serializable {
    var stockcode: Int? = null// 1,
    var stockname: String? = null// "平安银行",
    var stockType: Int? = null// 1,
    var timeshow: String = ""//"2017-08-02 15:50:04",
    var fvaluep: String? = null//"6.321",
    var tvalue: String? = null//"0.84",
    var flowvalue: String? = null//"1548.00亿",
    var tvaluep: String? = null//"0.84",
    var topenprice: String? = null//"9.25",
    var tamount: String? = null//"4969.32万",
    var trange: String? = null//"0.77%",
    var thighprice: String? = null//"9.18",
    var tamounttotal: String? = null//"4.54亿",
    var tchange: String? = null//"0.29%",
    var tlowprice: String? = null//"9.11",
    var pprice: String? = null//"9.12",
    var tmaxprice: String? = null//"10.03",
    var tminprice: String? = null//"8.21",
    var hexm_curPrice: String? = null//"9.15",
    var hexm_float_price: String? = null//"0.03",
    var hexm_float_rate: String? = null//"0.03%"
}