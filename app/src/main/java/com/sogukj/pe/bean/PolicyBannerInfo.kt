package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by CH-ZH on 2018/9/5.
 */
class PolicyBannerInfo : Serializable {
    var data : List<BannerInfo> ? = null

    class BannerInfo : Serializable{
        var id: Int = -1
        var title : String = ""
        var img : String ? = null
    }
}