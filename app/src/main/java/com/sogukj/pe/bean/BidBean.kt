package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/22.
 */
class BidBean : Serializable {
    var intro: String= ""//	Longtext	简述
    var title: String= ""//  Varchar    标题
    var updateTime: String= ""//   Date    更新日期
    var publishTime: String = ""//   Date    发布日期
    var purchaser: String = ""//   Varchar    采购人
    var proxy: String= ""//   Varchar    代理机构
    var link: String= ""//  Varchar    来源链接
    var abs: String= ""//  Text    摘要信息
    var content: String= ""//   Longtext    正文信息
}