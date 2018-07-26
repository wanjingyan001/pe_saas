package com.sogukj.pe.database

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import io.reactivex.Flowable

/**
 * Created by admin on 2018/6/22.
 */
@Dao
interface FunctionDao {

    @Query("SELECT * FROM Function WHERE isCurrent = :status ORDER BY seq")
    fun getSelectFunctions(status: Boolean): LiveData<List<MainFunIcon>>

    @Query("SELECT * FROM Function WHERE module = :mid AND name != :name")
    fun getModuleFunction(mid: Int, name: String = "调整"): Flowable<List<MainFunIcon>>

    @Query("SELECT * FROM Function WHERE module = :mid AND name != :name")
    fun getModuleData(mid: Int, name: String = "调整"): LiveData<List<MainFunIcon>>

    @Query("SELECT * FROM Function")
    fun getAllFunctions(): LiveData<List<MainFunIcon>>

    @Query("DELETE FROM Function WHERE 1=1")
    fun delete()

    @Update
    fun updateFunction(function: MainFunIcon)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveFunction(function: MainFunIcon)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFunction(function: MainFunIcon)
}