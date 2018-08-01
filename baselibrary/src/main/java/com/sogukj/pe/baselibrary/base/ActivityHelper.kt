package com.sogukj.pe.baselibrary.base

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import anet.channel.util.Utils.context
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

    fun finishAllWithoutTop() {
        activities.forEachIndexed { index, _ ->
            if (index != activities.size - 1) {
                activities[index].finish()
            }
        }
    }
    //退出整个应用
    fun exit(context: Context) {
        for (activity in activities) {
            activity.finish()
        }
        activities.clear()
//        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        activityManager.killBackgroundProcesses(context.packageName)
//        System.exit(0)
    }

    fun getActivityList(): ArrayList<BaseActivity> {
        return activities
    }
}