package com.sogukj.pe.bean

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by admin on 2018/9/12.
 */

@SuppressLint("ParcelCreator")
@Parcelize
data class NewCreditInfo(
        var avg_amt: String? = null,//月均消费能力
        var rcnt_income: String? = null,// 近期收入能力
        var long_income: String? = null,// 长期收入能力
        var income_chg: String? = null,// 近期收入波动预测
        var regincome_level: String? = null,// regincome_level
        var rcnt_econ: String? = null,// 近期收入稳定性等级
        var long_econ: String? = null,// 0.21
        var noregincome_lst_mons: String? = null,//稳定工作时长
        var life_cons: String? = null,//生活消费偏好
        var digi_cons: String? = null,//数码消费偏好
        var trav_cons: String? = null,//出行消费偏好
        var invest_cons: String? = null,//投资消费偏好
        var income_prov: String? = null,// 常工作省份
        var if_house: String? = null,//是否有房预测
        var business_type: String? = null,// 行业类别预测
        var if_car: String? = null,//是否有车预测
        var noincome_lst_mons: String? = null,//无业时长预测
        var avg_fre: String? = null//月均消费频率
) : Parcelable


data class PersonCreList(
        var name: String,// 测试
        var phone: String,// 18701780256
        var idCard: String,// 362302199306076512
        var add_time: String// 2018-09-12 11:14:55
)