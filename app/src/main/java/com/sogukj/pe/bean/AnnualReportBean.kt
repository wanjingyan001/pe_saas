package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/14.
 */
class AnnualReportBean : Serializable {
    var reportYear: String? = null//Varchar	年报年度
    var regNumber: String? = null//Varchar	工商注册号
    var manageState: String? = null//Varchar	企业经营状态
    var employeeNum: String? = null//Varchar	从业人员
    var phoneNumber: String? = null//Varchar	联系电话
    var email: String? = null//Varchar	电子邮箱
    var postcode: String? = null//Varchar	邮政编码
    var postalAddress: String? = null//Varchar	企业通信地址
    var totalAssets: String? = null//Varchar	资产总额
    var totalEquity: String? = null//Varchar	所有者权益合计
    var totalSales: String? = null//Varchar	销售总额	营业总收入
    var totalProfit: String? = null//Varchar	利润总额
    var primeBusProfit: String? = null//Varchar	主营业务收入
    var totalTax: String? = null//Varchar	纳税总额
    var totalLiability: String? = null//	Varchar	负债总额
    var have_onlineStore: String? = null//Int	是否有网店或网站	返回有；无
    var have_boundInvest: String? = null//Int	是否有投资信息或者购买其他公司股权	返回有；无
    var equityChangeInfoList: List<EquityChangeBean>? = null//Text	股东股权变更信息	json数据(详情见下面)
    var outGuaranteeInfoList: List<GuaranteeInfoBean>? = null//Text	对外提供保证担保信息	json数据(详情见下面)
    var outboundInvestmentList: List<InvestmentBean>? = null//Text	对外投资信息	json数据(详情见下面)
    var shareholderList: List<ShareHolderBean>? = null//Text	股东及出资信息	json数据(详情见下面)
    var webInfoList: List<WebInfoBean>? = null//Text	网站或网店信息	json数据(详情见下面)
    var changeRecordList: List<ChangeRecordBean>? = null//Text	年报变更记录	json数据(详情见下面)
}