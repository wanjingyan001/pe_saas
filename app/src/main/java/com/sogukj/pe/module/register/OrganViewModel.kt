package com.sogukj.pe.module.register

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.bean.DepartmentBean
import com.sogukj.pe.service.UserService
import com.sogukj.service.SoguApi
import io.reactivex.internal.util.HalfSerializer.onNext
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch

/**
 * Created by admin on 2018/7/4.
 */
class OrganViewModel: ViewModel() {
    private val deps = MutableLiveData<List<DepartmentBean>>()
    suspend fun loadOrganizationData(application:Application):LiveData<List<DepartmentBean>>{
        if (deps.value == null){
            launch(CommonPool){
                SoguApi.getService(application, UserService::class.java)
                        .userDepart()
                        .execute {
                            onNext { payload->
                                if (payload.isOk) {
                                    deps.value = payload.payload
                                }
                            }
                        }
            }.join()
        }
        return deps
    }
}