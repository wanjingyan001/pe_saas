package com.sogukj.pe.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import com.chad.library.adapter.base.entity.SectionEntity
import java.util.*

/**
 * Created by admin on 2018/6/20.
 */
@Entity(tableName = "Function")
data class MainFunIcon(
        @ColumnInfo(name = "icon")
        val icon: Int,//图标
        @ColumnInfo(name = "name")
        val name: String,//名字
        @ColumnInfo(name = "address")
        val address: String,//地址(路由的前半段)
        @ColumnInfo(name = "port")
        val port: String,//路由端口(路由后半段)
        @ColumnInfo(name = "isCurrent")
        var isCurrent: Boolean,//是否是当前功能
        @ColumnInfo(name = "editable")
        var editable: Boolean,//该功能是否可编辑
        @PrimaryKey
        @ColumnInfo(name = "functionId")
        val id: Long = 0
)


