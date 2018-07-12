package com.sogukj.pe.database

import android.app.Application
import android.arch.lifecycle.*
import android.util.Log
import com.amap.api.mapcore.util.it
import com.google.gson.Gson
import com.sogukj.pe.R
import com.sogukj.pe.R.id.main
import com.sogukj.pe.baselibrary.Extended.arrayFromJson
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.fromJson
import com.sogukj.pe.baselibrary.Extended.jsonStr
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.util.HalfSerializer.onNext
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.info

/**
 * Created by admin on 2018/6/21.
 */
class FunctionViewModel(private val funDao: FunctionDao) : ViewModel() {

    fun getMainModules(): LiveData<List<MainFunIcon>> {
        return funDao.getSelectFunctions(true)
    }


    fun getModuleFunctions(module: Int): Flowable<List<MainFunIcon>> {
        return funDao.getModuleFunction(module)
    }

    fun updateFunction(function: MainFunIcon) {
        doAsync {
            funDao.updateFunction(function)
        }
    }

    fun getAllData(module: Int): LiveData<List<MainFunIcon>> {
        return funDao.getModuleData(module)
    }


    fun generateData(application: Application) {
//        RetrofitUrlManager.getInstance().putDomain("homeFunction", "http://prehts.pewinner.com")
        SoguApi.getService(application, OtherService::class.java).homeModuleButton(HomeFunctionReq(1))
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            payload.payload?.let {
                                AnkoLogger("WJY").info { it.jsonStr }
                                doAsync {
                                    it.forEach {
                                        funDao.saveFunction(it)
                                    }
                                }
                            }
                        }
                    }
                }
    }
}