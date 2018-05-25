package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/23.
 */
class CopyRightBean : Serializable {
    var fullname: String = ""//	Varchar	全称
    var catnum: String = ""//	Varchar	分类号
    var regnum: String = ""//	Varchar	登记号
    var regtime: String = ""//	Date	登记日期
    var finishTime: String = ""//	Date	完成日期
    var publishtime: String = ""//	Date	首次发表日期
    var simplename: String = ""//	Varchar	简称
    var version: String = ""//	Varchar	版本号
    var authorNationality: String = ""//	Varchar	著作权人

    //   var simplename: String = ""//	Varchar	作品名称
    var category: String = ""//	Varchar	分类号
    //   var regnum	: String = ""//Varchar	登记号
//   var regtime: String = ""//	Date	登记日期
//   var finishTime: String = ""//	Date	完成日期
//   var publishtime: String = ""//	Date	首次发表日期
    var copy_type: String = ""//Varchar	著作权类别	0:软件著作权；1：作品著作权
//    var authorNationality: String = ""//	Varchar	作品著作权人
}