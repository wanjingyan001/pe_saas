package com.sogukj.pe.database

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.migration.Migration
import android.content.Context
import com.amap.api.mapcore.util.it

/**
 * Created by admin on 2018/6/22.
 */
@Database(entities = arrayOf(MainFunIcon::class), version = 2, exportSchema = false)
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
                        .addMigrations(MIGRATION_1_2)
                        .build()

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Function ADD COLUMN seq INTEGER," +
                        "module INTEGER," +
                        "floor INTEGER")
            }
        }
    }
}