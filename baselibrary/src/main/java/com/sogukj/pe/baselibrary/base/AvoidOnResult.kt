package com.sogukj.pe.baselibrary.base

import android.content.Intent
import android.app.Activity
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.FragmentActivity
import io.reactivex.Observable
import org.jetbrains.anko.AnkoException
import java.io.Serializable


/**
 * Created by admin on 2018/9/26.
 */
class AvoidOnResult constructor(activity: FragmentActivity) {
    private val TAG = "AvoidOnResult"
    var mAvoidOnResultFragment: AvoidOnResultFragment

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
        return activity.fragmentManager.findFragmentByTag(TAG) as AvoidOnResultFragment?
    }


    inline fun <reified T : Activity> startForResult(requestCode: Int, vararg params: Pair<String, Any?>): Observable<ActivityResultInfo> {
        val intent = Intent(mAvoidOnResultFragment.activity, T::class.java)
        if (params.isNotEmpty()) fillIntentArguments(intent, params)
        return mAvoidOnResultFragment.startForResult(requestCode,intent)
    }


    fun fillIntentArguments(intent: Intent, params: Array<out Pair<String, Any?>>) {
        params.forEach {
            val value = it.second
            when (value) {
                null -> intent.putExtra(it.first, null as Serializable?)
                is Int -> intent.putExtra(it.first, value)
                is Long -> intent.putExtra(it.first, value)
                is CharSequence -> intent.putExtra(it.first, value)
                is String -> intent.putExtra(it.first, value)
                is Float -> intent.putExtra(it.first, value)
                is Double -> intent.putExtra(it.first, value)
                is Char -> intent.putExtra(it.first, value)
                is Short -> intent.putExtra(it.first, value)
                is Boolean -> intent.putExtra(it.first, value)
                is Serializable -> intent.putExtra(it.first, value)
                is Bundle -> intent.putExtra(it.first, value)
                is Parcelable -> intent.putExtra(it.first, value)
                is Array<*> -> when {
                    value.isArrayOf<CharSequence>() -> intent.putExtra(it.first, value)
                    value.isArrayOf<String>() -> intent.putExtra(it.first, value)
                    value.isArrayOf<Parcelable>() -> intent.putExtra(it.first, value)
                    else -> throw AnkoException("Intent extra ${it.first} has wrong type ${value.javaClass.name}")
                }
                is IntArray -> intent.putExtra(it.first, value)
                is LongArray -> intent.putExtra(it.first, value)
                is FloatArray -> intent.putExtra(it.first, value)
                is DoubleArray -> intent.putExtra(it.first, value)
                is CharArray -> intent.putExtra(it.first, value)
                is ShortArray -> intent.putExtra(it.first, value)
                is BooleanArray -> intent.putExtra(it.first, value)
                else -> throw AnkoException("Intent extra ${it.first} has wrong type ${value.javaClass.name}")
            }
            return@forEach
        }
    }
}