package com.sogukj.pe.database

import android.content.Context

/**
 * Created by admin on 2018/6/22.
 */
object Injection {
    fun provideFunctionSource(context: Context): FunctionDao {
        return SgDatabase.getInstance(context).functionDao()
    }

    fun provideViewModelFactory(context: Context): ViewModelFactory {
        return ViewModelFactory(provideFunctionSource(context))
    }
}