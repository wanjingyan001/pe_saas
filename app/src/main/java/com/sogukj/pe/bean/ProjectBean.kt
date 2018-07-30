package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/7/21.
 */
class ProjectBean : Serializable {
    var name: String? = null//	Varchar	公司名称
    var shortName: String? = null;
    var state: String? = null//	Datetime	现状	A轮  B轮  类似这些（type=2时取此数据）
    var type: Int? = null//（4是储备，1是立项，3是关注，5是退出，6是调研）
    var update_time: String? = null//	Varchar	最近更新时间	（type=2时取此数据）
    var next_time: String? = null//	Varchar	退出时间	（type=5时取此数据）
    var add_time: String? = null//	Varchar	录入时间	（type=1时取此数据）
    var status: Int = 1//	Int	状态（默认1）	0禁用 1准备中  2已完成（type=1时取此数据）
    var company_id: Int? = null
    var is_focus: Int = 0//	Int	是否关注	is_focus=1表示关注is_focus=0表示未关注
    var chargeName: String? = null
    var charge: Int? = null
    var legalPersonName: String? = null//	varchar		法人	可空
    var regLocation: String? = null//	varchar		注册地址	可空
    var creditCode: String? = null//	varchar		统一社会信用代码	可空
    var info: String? = null//	text		其他信息	可空
    var is_volatility = 0

    var is_ability: Int? = null//有无能力 1 有能力 2 无能力
    var is_business: Int? = null//有无商业价值  1有价值  2无价值
    var red :Int? = null//大于0显示红点

    var logo: String? = null
    var chairman: String? = null
    var track_time: String? = null//	track_time	string	跟踪时间
    var number:String? = null
    var quit: Int = 1 //0---在投，1---部分退出

    var sortLetters:String = ""//显示数据拼音的首字母
}