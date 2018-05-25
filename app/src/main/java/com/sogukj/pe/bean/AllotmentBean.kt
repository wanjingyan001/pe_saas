package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/10.
 */
class AllotmentBean : Serializable {
    var issueDate: String? = null//	Varchar	配股上市日
    var name: String? = null//		Varchar	配股简称
    var progress: String? = null//		Varchar	方案进度
    var year: String? = null//		Varchar	配股年份
    var issueCode: String? = null//		Varchar	配股代码
    var pubDate: String? = null//		Varchar	证监会核准公告日
    var price: String? = null//		Varchar	每股配股价格
    var sDate: String? = null//		Varchar	缴款起止日
    var announceDate: String? = null//		Date	发审委公告日
    var actualRaise: String? = null//		Varchar	实际募集资金净额
    var proportion: String? = null//		Varchar	预案配股比例
    var exDate: String? = null//		Varchar	除权日
    var saDate: String? = null//		Date	股东大会公告日
    var raiseCeiling: String? = null//		Varchar	预案募资金额上限
    var registerDate: String? = null//	Varchar	股权登记日
    var dDate: String? = null//		Date	董事会公告日
}