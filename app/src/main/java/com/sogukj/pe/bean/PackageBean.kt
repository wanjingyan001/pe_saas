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
                       val type: Int,//type=1代表项目套餐,type=2代表征信套餐
                       val tel: String,
                       val used: Int,
                       val max: Int,
                       val list: List<PackageChild>,
                       val discountInfo: List<Discount>? = null) : Parcelable

@SuppressLint("ParcelCreator")
@Parcelize
data class PackageChild(val id: Int? = null,
                        val name: String,
                        val quantity: Int,
                        val pricestr: String? = null,
                        val price: Int) : Parcelable

@SuppressLint("ParcelCreator")
@Parcelize
data class Discount(val period: Int, //年限
                    val discount: Int //折扣,10代表不打折,8代表打8折
) : Parcelable


data class ProductInfo(var discountPrice: Int,//折扣价
                       var OriginalPrice: Int, // 原价
                       var calenderPrice: Int
) {
    constructor() : this(0, 0, 0)
}