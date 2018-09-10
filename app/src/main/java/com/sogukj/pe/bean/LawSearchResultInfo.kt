package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by CH-ZH on 2018/9/10.
 */
class LawSearchResultInfo : Serializable {
    var href : String = ""
    var title : String = ""
    var fwzh : String = ""  //发文字号
    var sxx : String = ""   //时效性
    var fbrq : String = ""  //发布日期
    var ssrq : String = ""  //实施日期
}