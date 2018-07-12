package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/10/30.
 */
class MessageIndexBean : Serializable {
    var count: Int = 0// number 消息总数
    var title: String = "" //string 消息标题
    var time: String = ""//    date    时间


    var flag: Int = 0//1审批助手,2消息助手
}