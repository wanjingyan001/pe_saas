package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2017/12/12.
 */
class BelongBean : Serializable {
    var lx: Detail? = null//立项
    var yt: Detail? = null//已投
    var cb: Detail? = null//储备
    var tc: Detail? = null//退出
    var gz: Detail? = null//关注
    var dy: Detail? = null//调研

    inner class Detail {
        var count: Int? = null//数量
        var red: Int? = null//红点 (0 不显示红点 , 大于0显示)
    }
}