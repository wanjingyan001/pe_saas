package com.sogukj.pe.module.register

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.jsonStr
import com.sogukj.pe.bean.Department
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.service.RegisterService
import com.sogukj.service.SoguApi
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

/**
 * Created by admin on 2018/7/4.
 */
class OrganViewModel : ViewModel() {
    private val deps = MutableLiveData<List<Department>>()
    suspend fun loadOrganizationData(application: Application): LiveData<List<Department>> {
        if (deps.value == null) {
            launch(CommonPool) {
                SoguApi.getService(application, RegisterService::class.java)
                        .getDepartList()
                        .execute {
                            onNext { payload ->
                                if (payload.isOk) {
                                    deps.value = payload.payload
                                }
                            }
                        }
            }.join()
        }
        return deps
    }

    private val members = MutableLiveData<List<UserBean>>()
    suspend fun loadMemberList(application: Application, key: String): LiveData<List<UserBean>> {
        if (members.value == null) {
            launch(CommonPool) {
                SoguApi.getService(application, RegisterService::class.java)
                        .getMemberList(key)
                        .execute {
                            onNext { payload ->
                                AnkoLogger("WJY").info { payload.jsonStr }
                                if (payload.isOk) {
                                    payload.payload?.let {
                                        members.value = it.list
                                    }
                                }
                            }
                        }
            }.join()
        }
        return members
    }

}