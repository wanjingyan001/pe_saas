package com.sogukj.pe.baselibrary.Extended

import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by admin on 2018/6/26.
 */
class ExtrasDelegate<T>(private val extraName: String, private val defaultValue: T){

    private var extra: T? = null

    operator fun getValue(thisRef: AppCompatActivity, property: KProperty<*>): T {
        extra = getExtra(extra, extraName, thisRef)
        return extra ?: defaultValue
    }

    operator fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        extra = getExtra(extra, extraName, thisRef)
        return extra ?: defaultValue
    }

    operator fun  setValue(thisRef: AppCompatActivity, property: KProperty<*>, value: T) {
        extra = value
    }

    operator fun  setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        extra = value
    }
}

fun <T> extraDelegate(extra: String, default: T) = ExtrasDelegate(extra, default)

fun extraDelegate(extra: String) = extraDelegate(extra, null)

@Suppress("UNCHECKED_CAST")
private fun <T> getExtra(oldExtra: T?, extraName: String, thisRef: AppCompatActivity): T? =
        oldExtra ?: thisRef.intent?.extras?.get(extraName) as T?

@Suppress("UNCHECKED_CAST")
private fun <T> getExtra(oldExtra: T?, extraName: String, thisRef: Fragment): T? =
        oldExtra ?: thisRef.arguments?.get(extraName) as T?
