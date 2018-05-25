package com.sogukj.pe.bean

/**
 * Created by sogubaby on 2018/5/12.
 */
class FileListBean {
    var doc_title: String? = null//	文书标题
    var add_time: String? = null//上传时间
    var submitter: String? = null//提交人
    var url: String? = null// 文件地址
    var from: Int? = null// 文件上传来源    1=自动归档(审批附件上传),2=机构网页上传,3=机构APP上传
    var fileSize: String? = null// 文件大小
    var mark_id: Int? = null// 文件格式 1=>文档类文件, 2=>表格类文件, 3=>演示类文件，4=>pdf类文件, 5=>图片类文件，6=>其他
    var name: String? = null//  文件所属类型名
    var descrip: String? = null// 文件格式描述
}