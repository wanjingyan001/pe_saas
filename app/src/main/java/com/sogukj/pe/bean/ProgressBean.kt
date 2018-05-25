package com.sogukj.pe.bean

/**
 * Created by sogubaby on 2018/1/9.
 */
class ProgressBean {
    var finish: ArrayList<ProgressItem>? = null
    var unfinish: ArrayList<ProgressItem>? = null

    class ProgressItem {
        var name: String? = null//	姓名
        var wri: Int? = null// 个人是否输入填写项    0=>未完成，1=>已完成，2=>延时完成
        var gws: Int? = null//是否为别人打岗位分    同上
        var jxs: Int? = null//上级是否为我打绩效分    同上
        var les: Int? = null//上级是否为我打绩效分    -1无部门领导（因为自己就是部门领导），0=>未完成，1=>已完成，
    }
}