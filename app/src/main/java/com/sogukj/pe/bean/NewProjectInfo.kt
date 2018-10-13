package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by CH-ZH on 2018/9/27.
 */
class NewProjectInfo : Serializable {
    var duty : Duty ?= null //项目经理
    var lead : Lead ? = null //项目负责人
    var relate : List<RelateInfo> ? = null
    var operate : Int ? = null //1=>可操作，0=>不能操作
    var info : String ? = null //业务简介
    var floor : Int ? = null  //当前所在阶段floor
    var name = ""
    var shortName = ""
    var type = ""
    var creditCode = ""
    class Duty : Serializable{
        var principal = -1
        var name  = ""
    }

    class Lead : Serializable{
        var leader = -1
        var name : String = ""
    }

    class RelateInfo : Serializable{
        /**
         * "id": 43,
        "name": "何1",
        "phone": 13243172177,
        "email": "13243172177@qq.com",
        "idCard": "",  //身份证id
        "position": "董事长",
        "position_id": 45,
        "sum": 0,  // 0=>无风险 ，>0 =>有风险
        "status":"-1":  int // -1=>未查询 0=>信息不完整，1=>查询中，2=>查询完成，3=>查询失败
        "update_time":"2018-08-10 14:45:13"
        "credit_id": null //int 征信记录id
         */
        var id : Int ? = null
        var name : String ? = null
        var phone : String ? = null
        var email : String ? = null
        var idCard : String ? = null
        var position : String ? = null //职位
        var position_id : Int  = 0
        var sum : Int = 0 // 0=>无风险 ，>0 =>有风险
        var status : Int = -1 //-1=>未查询 0=>信息不完整，1=>查询中，2=>查询完成，3=>查询失败
        var credit_id : Int ?= null // 征信记录id
    }
}