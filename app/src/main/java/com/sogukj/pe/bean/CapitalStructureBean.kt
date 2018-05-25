package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by qinfei on 17/8/11.
 */
class CapitalStructureBean:Serializable {
  var time:String?=null//	Date	时间点
  var shareAll:String?=null//	Varchar	总股本(万股)
  var ashareAll:String?=null//	Varchar	A(H)总股本
  var noLimitShare:String?=null//	Varchar	流通A(H)股
  var limitShare:String?=null//	Varchar	限售A(H)股
  var changeReason:String?=null//	Varchar	变动原因
}