package com.sogukj.pe.bean

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by admin on 2018/7/18.
 */

@SuppressLint("ParcelCreator")
@Parcelize
data class PackageBean(val mealName: String,
                       val tel: String,
                       val used: Int,
                       val max: Int,
                       val list: List<PackageChild>) : Parcelable

@SuppressLint("ParcelCreator")
@Parcelize
data class PackageChild(val id: Int,
                        val name: String,
                        val quantity: Int,
                        val pricestr: String,
                        val price:Int) : Parcelable