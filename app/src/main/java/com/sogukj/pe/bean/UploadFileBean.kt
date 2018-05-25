package com.sogukj.pe.bean

/**
 * Created by sogubaby on 2018/5/12.
 */
class UploadFileBean {
    var doc_title: String? = null//	文件原始名称
    var url: String? = null//  文件url
    var submitter: String? = null//  提交人
    var company_id: Int? = null//   公司id
    var dir_id: Int? = null// 文件目录
    var fileClass: String? = null//  文件小类
    var add_time: String? = null// 上传时间
    var fileSize: String? = null//  文件大小
    var from: Int? = null//   文件上传来源    1=自动归档(审批附件上传),2=机构网页上传,3=机构APP上传
    var type: Int? = null//  1=>项目，2=>基金
    var mark_id: Int? = null//  文件格式    1=>文档类文件,2=>表格类文件,3=>演示类文件，4=>pdf类文件,5=>图片类文件，6=>其他
}