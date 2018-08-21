package com.sogukj.pe.presenter

import android.content.Context

/**
 * Created by CH-ZH on 2018/8/20.
 */
abstract class BasePresenter {
    var mContext : Context? = null

    constructor(mContext : Context){
        this.mContext = mContext
    }

    abstract fun doRequest()
}