package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/8.
 */
class IssueRelatedBean : Serializable {
    var issueDate: String? = null//	Date	成立日期
    var listingDate: String? = null//	Date	上市日期
    var expectedToRaise: String? = null//	Varchar	预计募资
    var actualRaised: String? = null//	Varchar	实际募资
    var issueNumber: String? = null//	Varchar	发行数量
    var issuePrice: String? = null//	Varchar	发行价格
    var ipoRatio: String? = null//	Varchar	发行市盈率
    var rate: String? = null//	Varchar	发行中签率
    var openingPrice: String? = null//	Varchar	首日开盘价
    var mainUnderwriter: String? = null//	Varchar	主承销商
    var listingSponsor: String? = null//	Varchar	上市保荐人
    var history: String? = null//	longtext	历史沿革
}