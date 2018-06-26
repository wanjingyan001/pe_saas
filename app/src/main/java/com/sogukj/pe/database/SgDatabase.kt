package com.sogukj.pe.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.amap.api.mapcore.util.it

/**
 * Created by admin on 2018/6/22.
 */
@Database(entities = arrayOf(MainFunIcon::class), version = 1, exportSchema = false)
abstract class SgDatabase : RoomDatabase() {
    abstract fun functionDao(): FunctionDao

    companion object {
        @Volatile private var INSTANCE: SgDatabase? = null

        fun getInstance(context: Context): SgDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
        }

        private fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext,
                        SgDatabase::class.java, "SGFunction.db")
                        .build()
    }
}