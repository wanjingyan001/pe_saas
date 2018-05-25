package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2017/11/30.
 */
class SecondaryBean : Serializable {
    var title = ""//标题
    var sortTime = ""//审结时间(执行时间,开庭时间)
    var caseCause = ""//案由
    var judgeResult = ""//判决结果
    var proposer = ""//申请人
    var caseState = ""//案件状态
    var body = ""//内容概要(公告内容)
    var plaintiff = ""//原告
    var court = ""//法院名称
    var ggType = ""//公告类型
}