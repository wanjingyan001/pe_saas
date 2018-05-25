package com.sogukj.pe.bean

import java.io.Serializable

/**
 * 基金首页列表数据
 * Created by admin on 2017/11/22.
 */
class FundSmallBean : Serializable {
    companion object {
        val FundAsc: Int = 8//基金名升序
        val FundDesc: Int = 4//基金名降序
        val RegTimeAsc: Int = 2//成立时间升序
        val RegTimeDesc: Int = 1//成立时间降序
    }
    var id: Int = 0 //基金公司id
    var fundName: String = ""//基金公司名
    var simpleName: String = ""//基金公司名
    var regTime: String = "--/--/--"//成立时间

    var logo:String = ""
    var invest:String = ""//7000
    var total:String = ""//80000
}