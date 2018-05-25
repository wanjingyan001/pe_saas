package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2017/11/23.
 */
class FundStructure : Serializable {
    var director: String? = null//董事
    var gd: ArrayList<String> ? = null//股东
    var supervisor: String? = null//监事
    var total: String ? = null//合计
    var bl: List<FundedRatio> ? = null//各股东出资比例


    inner class FundedRatio {
        var partnerName: String ? = null//股东名字
        var contribute: String ? = null//实缴出资（万元）
        var investRate: String ? = null//出资比例
    }
}