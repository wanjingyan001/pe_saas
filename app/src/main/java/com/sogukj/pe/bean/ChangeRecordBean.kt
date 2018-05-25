package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/14.
 */
class ChangeRecordBean : Serializable {
    var changeItem: String? = null // 修改事项
    var contentBefore: String? = null //    修改前
    var contentAfter: String? = null //    修改后
    var changeTime: String? = null //    修改时间
}