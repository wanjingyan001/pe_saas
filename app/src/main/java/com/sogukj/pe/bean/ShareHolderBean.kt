package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/10.
 */
class ShareHolderBean : Serializable {
    var name: String? = null//	Date	公司名
    var holdingNum: String? = null//	Varchar	持股数量
    var proportion: String? = null//	Varchar	占股本比例（%）
    var shareType: String? = null//	Varchar	股票类型
    var tenTotal: String? = null//	Varchar	前十大股东累计持有
    var tenPercent: String? = null//	Varchar	累计占总股本比
    var holdingChange: String? = null//	Varchar	较上期变化
    var time: String? = null//	date	时间点

    var logo: String? = null//	Varchar	logo头像
    //  var name	Varchar	人或公司名
    var amount: String? = null//	Varchar	认缴出资额
    //  var time	Date	认缴时间
    var percent: String? = null//	Varchar	持股比例

    var investorName: String? = null//	股东名称
    var subscribeAmount: String? = null//	认缴出资额
    var subscribeTime: String? = null//	认缴出资时间
    var subscribeType: String? = null//	认缴出资方式
    var paidAmount: String? = null//	实缴出资额
    var paidTime: String? = null//	实缴出资时间
    var paidType: String? = null//	实缴出资方式
}