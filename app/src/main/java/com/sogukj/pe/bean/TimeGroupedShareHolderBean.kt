package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/10.
 */
class TimeGroupedShareHolderBean : Serializable {
    var time: String? = null
    var tenTotal: String? = null//	Varchar	前十大股东累计持有
    var tenPercent: String? = null//	Varchar	累计占总股本比
    var holdingChange: String? = null//	Varchar	较上期变化
    var data: List<ShareHolderBean>? = null
    var isSelected = false
}