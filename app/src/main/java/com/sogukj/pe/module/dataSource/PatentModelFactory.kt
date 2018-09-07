package com.sogukj.pe.module.dataSource

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context

/**
 * Created by admin on 2018/9/7.
 */
class PatentModelFactory(private val context: Context): ViewModelProvider.Factory  {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PatentViewModel::class.java)){
            return PatentViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}