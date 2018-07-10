package com.sogukj.pe.database

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import io.reactivex.Flowable

/**
 * Created by admin on 2018/6/22.
 */
@Dao
interface FunctionDao {

    @Query("SELECT * FROM Function WHERE isCurrent = :status")
    fun getSelectFunctions(status:Boolean):LiveData<List<MainFunIcon>>

    @Query("SELECT * FROM Function WHERE module = :mid")
    fun getModuleFunction(mid:Int):Flowable<List<MainFunIcon>>

    @Query("SELECT * FROM Function WHERE module = :mid")
    fun getModuleData(mid:Int):LiveData<List<MainFunIcon>>

    @Query("SELECT * FROM Function")
    fun getAllFunctions():LiveData<List<MainFunIcon>>

    @Update
    fun updateFunction(function: MainFunIcon)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveFunction(function: MainFunIcon)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFunction(function: MainFunIcon)
}