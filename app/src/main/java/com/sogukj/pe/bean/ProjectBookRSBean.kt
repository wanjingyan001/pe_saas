package com.sogukj.pe.bean

/**
 * Created by qinfei on 17/10/16.
 */
class ProjectBookRSBean {
    var list1: List<ProjectBookBean>? = null
    var list2: List<ProjectBookBean>? = null
    var list3: List<ProjectBookBean>? = null
}

class ProjectBookBean {
    var doc_title: String? = null//	Varchar	文书标题
    var add_time: String? = null//	Date	上传时间
    var submitter: String? = null//	Varchar	提交人
    var url: String? = null//	Varchar	文件地址
    var name: String? = null//	Varchar	分类名
}
//1	doc_title	Varchar	文书标题
//2	add_time	Date	上传时间
//3	submitter	Varchar	提交人
//4	url	Varchar	文件地址
//5	name	Varchar	分类名