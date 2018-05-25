package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/10/21.
 */
class CustomSealBean : Serializable {
    var name: String? = null
//        get() {
//            return if (field == "项目名称") {
//                field + "(选填)"
//            } else {
//                field
//            }
//        }
    var fields: String = ""
    var control: Int? = null
    var floor: Int? = null
    var value: String? = null
    var value_list: ArrayList<ValueBean>? = null
    var value_map: ValueBean? = null
    var is_must: Int = 0 //1必填
    var is_fresh: Int ?= null


    class ValueBean : Serializable {
        var id: Int? = null
        var name: String? = null
        var is_select: Int = 0
        var count: Int = 0
        var file_name: String? = null
        var url: String? = null
        var size: String? = null
        var type: Int? = null
        val hide: String = ""
        val pla: String = ""
        val value: Any? = null
    }
}