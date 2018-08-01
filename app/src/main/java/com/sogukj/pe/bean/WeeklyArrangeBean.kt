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

    inner class Person : Serializable {
        var user_id: Int? = null
        var url: String? = null
        var name: String? = null
        var position: String? = null
    }
}

@SuppressLint("ParcelCreator")
@Parcelize
data class NewArrangeBean(val pid: Int,//具体某一天的id
                          val date: String,//日期
                          val weekday: String, //周几
                          val child: List<ChildBean> //存那天中的所有安排,如果那天一个安排都没有则child:[]
) : Parcelable


@SuppressLint("ParcelCreator")
@Parcelize
data class ChildBean(val id: Int = 0,
                     val reasons: String? = null,//事由
                     val place: String? = null,//地点
                     val attendee: List<UserBean>? = null,//出席人
                     val participant: List<UserBean>? = null//参加人
) : Parcelable