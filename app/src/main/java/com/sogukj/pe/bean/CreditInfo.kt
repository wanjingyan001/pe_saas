package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2017/11/29.
 */
class CreditInfo : Serializable {
    var click = 0//1=> 一键查询，2=>查询中，请稍后再看，3=>查询完成
    var item = ArrayList<Item>()


    class Item : Serializable {
        var id = 0//id
        var company_id = 0//公司id
        var name = ""//名字
        var position = ""//职位
        var phone: String? = null//手机号码
        var idCard: String? = null//身份证号码
        var type = 0//人物级别 1=>董监高  2=>股东
        var status = -1//查询状态 1=>正在查询  2=>查询完成，3=>查询失败：
        var error_info: String? = null//错误信息
        var sum: Int? = null//负面信息总数(第一次进入不返回此字段)
        //var piece: Piece? = null//负面信息分布(进入详情时带入（第一次进入不返回此字段）)
        var company: String? = null
        var reason: String? = null

        inner class Piece : Serializable {
            var ns = 0
            var nd = 0
            var nc = 0
            var nco = 0
            var cou: Cou? = null

            inner class Cou : Serializable {
                var cpws: Int? = null
                var zxgg: Int? = null
                var ktgg: Int? = null
                var fygg: Int? = null
            }
        }
    }
}