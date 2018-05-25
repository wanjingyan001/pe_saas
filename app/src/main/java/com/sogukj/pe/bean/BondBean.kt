package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/18.
 */
class BondBean : Serializable {
    var bondName: String? = null//	Varchar	债券名称
    var bondNum: String? = null//	Int	债券代码
    var publisherName: String? = null//	Varchar	发行人
    var bondType: String? = null//	Varchar	债券类型
    var publishTime: String? = null//	Date	债劵发行日
    var publishExpireTime: String? = null//	Date	债劵到期日
    var bondTradeTime: String? = null//	Date	上市交易日
    var bondStopTime: String? = null//	Date	债劵摘牌日
    var startCalInterestTime: String? = null//	Date	债券起息日
    var bondTimeLimit: String? = null//	Varchar	债劵期限
    var calInterestType: String? = null//	Varchar	计息方式
    var debtRating: String? = null//	Varchar	债项评级
    var creditRatingGov: String? = null//	Varchar	信用评级机构
    var faceValue: String? = null//	Varchar	面值
    var refInterestRate: String? = null//	Varchar	参考利率
    var faceInterestRate: String? = null//	Varchar	票面利率(%)
    var realIssuedQuantity: String? = null//	Varchar	实际发行量(亿)
    var planIssuedQuantity: String? = null//	Varchar	计划发行量(亿)
    var issuedPrice: String? = null//	Varchar	发行价格(元)
    var interestDiff: String? = null//	Varchar	利差(BP)
    var payInterestHZ: String? = null//	Varchar	付息频率
    var exeRightType: String? = null//	Varchar	行权类型
    var exeRightTime: String? = null//	Date	行权日期
    var flowRange: String? = null//	Varchar	流通范围
    var escrowAgent: String? = null//	Varchar	托管机构
}