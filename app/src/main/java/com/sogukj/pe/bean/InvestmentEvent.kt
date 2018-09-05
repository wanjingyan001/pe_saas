package com.sogukj.pe.bean

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by admin on 2018/9/4.
 */
@SuppressLint("ParcelCreator")
@Parcelize
data class InvestmentEvent(val id: Int,
                           val invested_sname: String,// 受资方名称
                           val invest_time: String,// 时间
                           val industry_name: String,// 所属行业
                           val money: String,// 投资金额
                           val view: String,//获取详情所需参数
                           val investor: List<String> // 投资方
) : Parcelable


data class InvestCategory(val id: Int,// 分类 id
                          val category_name: String,// 分类名
                          val child: List<CategoryChild1>? = null)

data class CategoryChild1(val id:Int,// 次级分类 id
                          val category_name:String,// 次级分类名
                          val pid:Int,
                          val child: List<CategoryChild2>? = null)

data class CategoryChild2(val id:Int,// 三级分类 id
                          val category_name:String,// 三级分类名
                          val pid:Int)