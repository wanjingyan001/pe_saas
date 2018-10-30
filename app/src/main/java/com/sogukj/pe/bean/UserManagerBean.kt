package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by CH-ZH on 2018/10/30.
 */
class UserManagerBean : Serializable{
    var img = ""
    var name = ""
    var phone = ""
    var isSelect = false
    val message : UserMessageBean ? = null

    class UserMessageBean : Serializable{
        val list : List<ManagerInfo> ? = null
        val price = "99"
        val max_yuqing_count = "10" //舆情额度
        val max_credit_count = "50" //征信额度
    }

    class ManagerInfo : Serializable{
        var url = ""
        var name = ""
        var phone = ""
        var user_id = ""
        var isSelect = false
    }
}