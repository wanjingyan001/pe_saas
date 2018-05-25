package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/22.
 */
class BrandBean : Serializable {
    var regNo: Int? = null//	Int	注册号
    var appdate: String? = null//	Date	申请时间
    var intCls: String? = null//	Varchar	类别
    var status: String? = null//	Varchar	申请状态
    var tmPic: String? = null//Varchar	商标图片链接
    var tmName: String? = null//	Varchar	商标名称
}