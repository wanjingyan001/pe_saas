package com.sogukj.pe.bean

/**
 * Created by sogubaby on 2018/5/12.
 */
class DirBean {
    // dir_id	int	目录id
    // dirname	string	目录名称
    // amend	int	目录属性	1=该文件夹可删除可改名,0=不可删除不可改名
    var dir_id: Int? = null
    var dirname: String? = null
    var amend: Int? = null

    var click = false
}