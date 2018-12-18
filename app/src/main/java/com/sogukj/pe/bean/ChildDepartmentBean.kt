package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by CH-ZH on 2018/12/16.
 */
class ChildDepartmentBean : Serializable {
    var depart_id : Int ? = null
    var de_name = ""
    var isCanSelect = false
    var isSelected = false
}