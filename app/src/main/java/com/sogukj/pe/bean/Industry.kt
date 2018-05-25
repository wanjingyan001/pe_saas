package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2017/12/2.
 */
class Industry : Serializable {
    var id: Int? = null//id
    var name: String? = null//中文名称
    var pid: Int? = null//父id
    var seclected = false
    var children: ArrayList<Children>? = null

    inner class Children : Serializable  {
        var id: Int? = null//id
        var name: String? = null//中文名称
        var pid: Int? = null//父id
        var seclected = false
    }
}