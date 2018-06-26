package com.sogukj.pe.database

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

/**
 * Created by admin on 2018/6/22.
 */
class ViewModelFactory(private val dataSource: FunctionDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FunctionViewModel::class.java)) {
            return FunctionViewModel(dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}