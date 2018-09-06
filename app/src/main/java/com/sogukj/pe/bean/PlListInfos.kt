package com.sogukj.pe.bean

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by CH-ZH on 2018/9/5.
 */

@SuppressLint("ParcelCreator")
@Parcelize
data class PlListInfos(val id: Int,
                       val title : String,
                       val time : String
) : Parcelable
