package com.sogukj.pe.baselibrary.utils

import android.support.v7.util.DiffUtil

/**
 * Created by admin on 2018/5/25.
 */
class DiffCallBack<out T> constructor(private val oldData: List<T>, private val newData: List<T>) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldData[oldItemPosition]!!.hashCode() == newData[newItemPosition]!!.hashCode()
    }

    override fun getOldListSize(): Int {
        return oldData.size
    }

    override fun getNewListSize(): Int {
        return newData.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldData[oldItemPosition] == newData[newItemPosition]
    }
}