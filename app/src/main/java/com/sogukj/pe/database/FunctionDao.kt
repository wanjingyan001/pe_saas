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

    @Query("SELECT * FROM Function WHERE isCurrent = :status AND (functionId != 4294967295 OR isAdmin == 1) ORDER BY seq")
    fun getCurrentFunctions(status: Boolean):List<MainFunIcon>

    @Query("SELECT * FROM Function WHERE module = :mid AND (name != :name OR isAdmin == 1)")
    fun getModuleFunction(mid: Int, name: String = "调整"): Flowable<List<MainFunIcon>>

    @Query("SELECT * FROM Function WHERE module = :mid AND (name != :name OR isAdmin == 1)")
    fun getModuleData(mid: Int, name: String = "调整"): LiveData<List<MainFunIcon>>

    @Query("SELECT * FROM Function")
    fun getAllFunctions(): List<MainFunIcon>

    @Query("SELECT * FROM Function WHERE functionId = 4294967295 OR isAdmin == 1")
    fun findEditBtn():MainFunIcon

    @Query("DELETE FROM Function WHERE 1=1")
    fun delete()

    @Delete
    fun delete(bean: MainFunIcon)

    @Update
    fun updateFunction(function: MainFunIcon)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveFunction(function: MainFunIcon)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFunction(function: MainFunIcon)
}