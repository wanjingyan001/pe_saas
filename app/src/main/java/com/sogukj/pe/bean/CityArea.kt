package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by admin on 2017/12/2.
 */
class CityArea : Serializable {
    var sortLetters:String = ""//显示数据拼音的首字母
    var id: Int? = null//id
    var name: String? = null//省
    var pid: Int? = null//父id
    var seclected = false
    var city: ArrayList<City>? = null//市相关信息

    class City : Serializable  {
        var sortLetters:String = ""//显示数据拼音的首字母
        var id: Int? = null//id
        var name: String? = null//省
        var pid: Int? = null//父id
        var seclected = false
    }
}