package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/22.
 */
class AppBean : Serializable {
    var filterName: String = ""//	Varchar	产品简称
    var classes: String = ""//	Varchar	类型
    var brief: String = ""//Longtext	描述
    var icon: String = ""//Varchar	图标
}