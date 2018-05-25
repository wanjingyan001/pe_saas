package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by sogubaby on 2018/4/20.
 */
class EquityListBean : Serializable {
    var hid: Int? = null//记录id
    var title: String? = null//标题
    var time: String? = null//添加|修改时间
    var is_edit: Int? = null//添加|修改    0=>添加，1=>修改
    var lunci: String? = null//轮次
}