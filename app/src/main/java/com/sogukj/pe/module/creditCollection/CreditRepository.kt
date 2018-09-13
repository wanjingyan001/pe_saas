package com.sogukj.pe.module.creditCollection

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.sogukj.pe.App
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.bean.PersonCreList
import com.sogukj.pe.service.CreditService
import com.sogukj.service.SoguApi

/**
 * Created by admin on 2018/9/12.
 */
class CreditRepository {

    public fun getHundredCreList(offset:Int) :LiveData<List<PersonCreList>>{
        val list = MutableLiveData<List<PersonCreList>>()
        SoguApi.getService(App.INSTANCE, CreditService::class.java)
                .getPersonalCreList(offset)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                list.value = it
                            }
                        }
                    }
                }
        return list
    }
}