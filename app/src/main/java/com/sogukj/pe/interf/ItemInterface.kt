package com.sogukj.pe.interf

import android.view.View
import com.ldf.calendar.model.CalendarDate
import com.sogukj.pe.bean.UserBean

/**
 * Created by admin on 2018/5/22.
 */
interface MonthSelectListener {
    fun onMonthSelect(date: CalendarDate)
}

interface ScheduleItemClickListener {
    fun onItemClick(view: View, position: Int)
    fun finishCheck(isChecked: Boolean, position: Int)
}

interface CommentListener {
    fun confirmListener(comment: String)
}

interface AddPersonListener {
    fun addPerson(tag: String)
    fun remove(tag: String, user: UserBean)
}