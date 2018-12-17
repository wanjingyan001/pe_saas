package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by CH-ZH on 2018/12/17.
 */
class MineDepartmentBean : Serializable {
    var id : Int ? = null //部门的id
    var name  = "" //部门的名称
    var depart_head = ""
    var pid : Int ? = null //上级部门id
    var children : List<MineDepartmentBean> ? = null //当前部门下的子部门
}