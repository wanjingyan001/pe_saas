package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/10.
 */
class TimeGroupedCapitalStructureBean : Serializable {
    var time: String? = null
    var data: List<CapitalStructureBean>? = null
}