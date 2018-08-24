package com.sogukj.pe.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import com.chad.library.adapter.base.entity.SectionEntity
import com.google.gson.annotations.JsonAdapter
import com.sogukj.pe.baselibrary.utils.BooleanTypeAdapter
import java.util.*

/**
 * Created by admin on 2018/6/20.
 */
@Entity(tableName = "Function")
data class MainFunIcon(
        @ColumnInfo(name = "icon")
        val icon: String,//图标
        @ColumnInfo(name = "name")
        val name: String,//名字
        @ColumnInfo(name = "address")
        val address: String,//地址(路由的前半段)
        @ColumnInfo(name = "port")
        val port: String,//路由端口(路由后半段)
        @JsonAdapter(value = BooleanTypeAdapter::class)
        @ColumnInfo(name = "isCurrent")
        var isCurrent: Boolean,//1=当前按钮(在首页显示的按钮),0=非当前按钮(未在首页显示的按钮)
        @JsonAdapter(value = BooleanTypeAdapter::class)
        @ColumnInfo(name = "editable")
        var editable: Boolean,//0=不可编辑(一定会显示在首页),1=可编辑
        @ColumnInfo(name = "seq")
        var seq: Long, //当前按钮的排序
        @ColumnInfo(name = "module")
        var mid: Int,//按钮所属的模块,1=默认模块,2=项目模块,3=基金模块
        @ColumnInfo(name = "floor")
        var floor: Long,//按钮在所属模块中的排序
        @PrimaryKey
        @ColumnInfo(name = "functionId")
        val id: Long = 0,
        @ColumnInfo(name = "isAdmin")
        var isAdmin: Int? = null//1是全部 0是其他
) {
    companion object {
        const val Default = 1
        const val Project = 2
        const val Fund = 3
    }
}


