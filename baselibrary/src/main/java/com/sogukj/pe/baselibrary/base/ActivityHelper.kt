package com.sogukj.pe.baselibrary.base

import android.annotation.SuppressLint
import java.util.ArrayList

/**
 * Created by admin on 2018/5/22.
 */
object ActivityHelper {
    val activities = ArrayList<BaseActivity>()
    @SuppressLint("StaticFieldLeak")
    private var curActivity: BaseActivity? = null

    val rootActivity: BaseActivity
        get() = activities[0]

    val count: Int
        get() = activities.size

    fun add(activity: BaseActivity) {
        activities.add(activity)
        curActivity = activity
    }

    fun remove(activity: BaseActivity) {
        activities.remove(activity)
    }

    //退出整个应用
    fun exit() {
        for (activity in activities) {
            activity.finish()
        }
    }

    fun getActivityList(): ArrayList<BaseActivity> {
        return activities
    }
}