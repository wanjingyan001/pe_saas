package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/7/31.
 */
class DepartmentBean : Serializable {
    var de_name: String = ""
    var depart_id: Int? = null
    var data: ArrayList<UserBean>? = null

    fun clone(): DepartmentBean {
        var bean = DepartmentBean()
        bean.de_name = this.de_name
        bean.depart_id = this.depart_id
        bean.data = ArrayList<UserBean>(this.data)
        return bean
    }
}