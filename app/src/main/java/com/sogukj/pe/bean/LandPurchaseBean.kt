package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/21.
 */
class LandPurchaseBean : Serializable {
    var elecSupervisorNo: String? = null//	Int	电子监管号
    var signedDate: String? = null//	Date	签订日期
    var adminRegion: String? = null//		Varchar	行政区
    var totalArea: Float? = null//	Float	供地总面积(公顷)
    var dealPrice: Double? = null//	Double	成交价款（万元）
    var assignee: String? = null//		Varchar	受让人
    var location: String? = null//		Varchar	宗地位置
    var consignee: String? = null//		Varchar	受托人
    var parentCompany: String? = null//		Varchar	上级公司
    var purpose: String? = null//		Varchar	土地用途
    var supplyWay: String? = null//		Varchar	供应方式
    var minVolume: Float? = null//	Float	最小容积率
    var maxVolume: Float? = null//Float	最大容积率
    var startTime: String? = null//		Date	约定动工日期
    var endTime: String? = null//		Date	约定竣工日期
}