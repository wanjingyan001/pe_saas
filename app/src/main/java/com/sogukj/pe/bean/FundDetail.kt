package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2017/11/23.
 */
class FundDetail : Serializable {
    var id: Int = 0
    var simpleName: String? = null//基金公司名(简称)
    var regTime: String? = null//成立时间
    var contributeSize: String? = null//认缴规模（万元）
    var actualSize: String? = null//实缴规模（万元
    var duration: String? = null//存续期限
    var partners: String? = null//合伙人人数
    var mode: String? = null//GP模式
    var commission: String? = null//投委会通过方
    var manageFees: String? = null//管理费
    var carry: String? = null//carry分成
    var administrator: String? = null//管理人
    var list: Collection<UserBean>? = null

//    class NameList {
//        var name: String = ""
//        var url: String = ""
//        var user_id:Int? = null
//    }
}