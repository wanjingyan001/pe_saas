package com.sogukj.pe.bean

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

/**
 * Created by admin on 2018/3/16.
 */
class WeeklyArrangeBean : Serializable {
    var id: Int? = null
    var reasons: String? = null//事由
    var place: String? = null//地点
    var attendee: List<Person>? = null//出席人
    var participant: List<Person>? = null//参加人
    var date: String? = null//日期
    var weekday: String? = null//周几


}

data class Person(var uid: Int,
                  var url: String? = null,
                  var name: String,
                  var position: String,
                  val depart_name: String? = null) : Serializable


data class ArrangeReqBean(val flag: Int,//1=列表,2添加或修改
                          val offset: Int? = null,//偏移量:-1上周,0本周,1下周 flag=1时非空
                          val data: List<NewArrangeBean>? = null//待提交的数据  flag=2时非空
)

data class NewArrangeBean(val pid: Int,//具体某一天的id
                          val date: String,//日期
                          val weekday: String, //周几
                          val child: ArrayList<ChildBean> //存那天中的所有安排
) : Serializable


data class ChildBean(val id: Int = 0,
                     var reasons: String? = null,//事由
                     var place: String? = null,//地点
                     var create_id: Int,//发表该条安排的用户的id,如果为0则说明发表该条安排的用户未知
                     var lv: Int, //发表该条安排的用户的等级,0普通用户,1管理员,2超级管理员
                     var attendee: List<Person>? = null,//出席人
                     var participant: List<Person>? = null//参加人
) : Serializable

