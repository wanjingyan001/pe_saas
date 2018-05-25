package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by sogubaby on 2017/12/21.
 */
class EmployeeInteractBean : Serializable {
    var title: String? = null
    var pev_grade: String? = null
    var data: ArrayList<EmployeeItem>? = null

    class EmployeeItem : Serializable {
        var user_id: Int? = null
        var name: String? = null
        var grade_case: String? = null
        var sort: Int? = null
        var url: String? = null
        var department: String? = null
    }
}