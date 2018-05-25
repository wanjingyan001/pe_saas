package com.sogukj.pe.bean

/**
 * Created by sogubaby on 2017/12/20.
 */
class TypeBean {
    var type: Int? = null// -1=>隐藏入口 0=>未开启  1=>进入评分中心，2=>进入填写页面
    var is_see: Int? = null// 领导是否可以查看	0=>不能查看，1=>可以
    var rule_url: String? = null
    var role: Int? = null  // 1=>领导班子 2=>部门负责人 3=>其他员工
    var is_adjust: Int? = null// 领导班子是否可以显示调整项	1显示 0不显示
    var time: Int? = null// 截止时间，秒
}