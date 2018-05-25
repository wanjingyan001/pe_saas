package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by sogubaby on 2017/11/29.
 */
class RecordInfoBean : Serializable {
    var info: Info? = null
    val list: Array<ListBean>? = null//

    class Info {
        var investCost: String? = null//投资成本
        var investDate: String? = null//投资日期
        var equityRatio: String? = null//股权比例
        var riskControls: String? = null//风控经理
        var invests: String? = null//投资经理
    }

    class ListBean : Serializable {
        var id: Long? = null//记录ID
        var start_time: Long? = null//开始时间
        var end_time: Long? = null//结束时间
        var visits: String? = null//拜访人员
        var des: String? = null//跟踪情况描述
        var important: Int? = null//是否重大事件	0=>否，1=>是
    }
}