package com.sogukj.pe.baselibrary.base

import android.content.Intent
import android.app.Activity
import android.support.v4.app.FragmentActivity
import io.reactivex.Observable


/**
 * Created by admin on 2018/9/26.
 */
class AvoidOnResult constructor(activity: FragmentActivity) {
    private val TAG = "AvoidOnResult"
    private var mAvoidOnResultFragment: AvoidOnResultFragment

    init {
        mAvoidOnResultFragment = getAvoidOnResultFragment(activity)
    }

    private fun getAvoidOnResultFragment(activity: FragmentActivity): AvoidOnResultFragment {
        var avoidOnResultFragment: AvoidOnResultFragment? = findAvoidOnResultFragment(activity)
        if (avoidOnResultFragment == null) {
            avoidOnResultFragment = AvoidOnResultFragment()
            val fragmentManager = activity.supportFragmentManager
            fragmentManager
                    .beginTransaction()
                    .add(avoidOnResultFragment, TAG)
                    .commitAllowingStateLoss()
            fragmentManager.executePendingTransactions()
        }
        return avoidOnResultFragment
    }

    private fun findAvoidOnResultFragment(activity: Activity): AvoidOnResultFragment? {
        return activity.fragmentManager.findFragmentByTag(TAG) as AvoidOnResultFragment
    }

    private fun startForResult(intent: Intent): Observable<ActivityResultInfo> {
        return mAvoidOnResultFragment.startForResult(intent)
    }

    fun startForResult(clazz: Class<*>): Observable<ActivityResultInfo> {
        val intent = Intent(mAvoidOnResultFragment.activity, clazz)
        return startForResult(intent)
    }
}