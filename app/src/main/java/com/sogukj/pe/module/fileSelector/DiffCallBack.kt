package com.sogukj.pe.module.fileSelector

import android.support.v7.util.DiffUtil
import java.io.File

/**
 * Created by admin on 2018/4/26.
 */
class DiffCallBack constructor(val mOldDatas: List<File>, val mNewDatas: List<File>) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
            = mOldDatas[oldItemPosition].absolutePath == mNewDatas[newItemPosition].absolutePath

    override fun getOldListSize(): Int = mOldDatas.size

    override fun getNewListSize(): Int = mNewDatas.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
            = mOldDatas[oldItemPosition] == mNewDatas[newItemPosition]
}