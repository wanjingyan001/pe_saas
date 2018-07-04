package com.sogukj.pe.bean

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

/**
 * Created by admin on 2018/6/29.
 */
data class TeamInfoSupplementReq(val position: String?,
                                 val name: String?,
                                 val scale: Int?,//1 ：少于10人    2 ：10～30人 3：30～50人  4：50～100人 5：100人以上。创建必传  加入可不传
                                 val mechanism_name: String?,//机构名称 创建传  加入可不传
                                 val type: Int,//1 ：加入.    2  : 创建  3 修改信息
                                 val phone: String,//	号码
                                 val key: String?,//关联企业和用户的key 创建不传     加入传
                                 val user_id: String?//修改信息传  加入和创建不传
)

data class JoinTeamResult(val user_id: Int,
                          val key: String) : Serializable

data class RegisterVerResult(val user_id: Int?,//用户ID
                             val status: Int?,//状态   0审核失败 进入审核失败页面  1审核中 进入审核中页面            2审核通过 进入审核通过页面
                             val phone: String,//号码
                             val reason: String?//失败原因
)

data class CompanyTeamInfo(val scale: Int,//规模 1 ：少于10人 2 ：10～30人 3：30～50人  4：50～100人 5：100人以上
                           val key: String,//关联企业和用户的key
                           val phone: String,//号码
                           val mechanism_name: String,//机构名称
                           val ip: String?,//IP
                           val port: String?,//端口
                           val domain_name: String? //域名
)

@SuppressLint("ParcelCreator")
@Parcelize
data class MechanismInfo(val mechanism_name: String?,//机构名称
                         val scale: Int?,//规模 1 ：少于10人    2 ：10～30人 3：30～50人  4：50～100人 5：100人以上
                         val business_card: String?,//名片URL
                         val name: String?,//姓名
                         val position: String?,//职位
                         val key: String//关联企业和用户的key
) : Parcelable

data class Department(var name: String? = null,
                      var childs: ArrayList<Department>? = null,
                      var level: Int)