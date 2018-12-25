package com.sogukj.pe.module.creditCollection

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import com.sogukj.pe.bean.PersonCreList

/**
 * Created by admin on 2018/9/12.
 */
class CreditViewModel : ViewModel() {
    private lateinit var creditList: LiveData<List<PersonCreList>>

    fun getCreditList(offset : Int = 0): LiveData<List<PersonCreList>> {
        creditList = CreditRepository().getHundredCreList(offset)
        return creditList
    }
}