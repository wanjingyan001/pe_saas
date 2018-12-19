package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by CH-ZH on 2018/12/18.
 */
class DepartmentInfo : Serializable {
    var id : Int ? = null
    var name = ""
    var depart_head : Int? = null //部门负责人的id
    var pid : Int ? = null
    var depart_head_name = ""//部门负责人的名称
    var p_depart_name = "" //上级部门的名称
    var children : List<ChildDepartmentBean> ? = null

    var user_info : List<UserBean> ? = null

}