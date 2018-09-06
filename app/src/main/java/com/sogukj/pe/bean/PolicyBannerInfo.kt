package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by CH-ZH on 2018/9/5.
 */
class PolicyBannerInfo : Serializable {
    var data : ArrayList<BannerInfo> ? = null

    public class BannerInfo : Serializable{
        var image:String = ""
        var title : String = ""
    }
}