package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/10/21.
 */
class SpGroupBean {
    var title: String? = null
    val type: Int? = null
    var item: List<SpGroupItemBean>? = null
}

class SpGroupItemBean : Serializable {
    var id: Int? = null
    var type: Int? = null
    var name: String? = null
    var icon: String? = null
}