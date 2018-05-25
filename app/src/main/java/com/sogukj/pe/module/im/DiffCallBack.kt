package com.sogukj.pe.module.im

import android.support.v7.util.DiffUtil
import com.sogukj.pe.bean.UserBean

/**
 * Created by admin on 2018/5/2.
 */
class DiffCallBack constructor(val mOldDatas: List<UserBean>, val mNewDatas: List<UserBean>) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
            = mOldDatas[oldItemPosition].uid == mNewDatas[newItemPosition].uid

    override fun getOldListSize(): Int = mOldDatas.size

    override fun getNewListSize(): Int = mNewDatas.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
            = mOldDatas[oldItemPosition] == mNewDatas[newItemPosition]
}