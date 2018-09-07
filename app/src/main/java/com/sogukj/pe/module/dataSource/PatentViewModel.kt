package com.sogukj.pe.module.dataSource

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.core.content.edit
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.baselibrary.Extended.jsonStr
import com.sogukj.pe.bean.PatentItem

/**
 * Created by admin on 2018/9/7.
 */
class PatentViewModel(context: Context) : ViewModel() {
    private val searchHistory = mutableSetOf<PatentItem>()
    private val sp: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(context) }


    fun getPatentHistory(): Set<PatentItem> {
        val localData = Gson().fromJson<Array<PatentItem>>(sp.getString(Extras.PATENT_HISTORY, ""), Array<PatentItem>::class.java)
        localData?.let {
            searchHistory.addAll(localData)
        }
        return searchHistory
    }

    fun saveLocalData() {
        sp.edit { putString(Extras.PATENT_HISTORY, searchHistory.jsonStr) }
    }
}