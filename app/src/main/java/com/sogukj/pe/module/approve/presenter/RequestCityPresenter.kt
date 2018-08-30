package com.sogukj.pe.module.approve.presenter

import android.content.Context
import com.sogukj.pe.App
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.module.approve.DstCityCallBack
import com.sogukj.pe.peUtils.ToastUtil.Companion.showCustomToast
import com.sogukj.pe.presenter.BasePresenter
import com.sogukj.pe.service.ApproveService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by CH-ZH on 2018/8/30.
 */
class RequestCityPresenter : BasePresenter {
    private var ctx : Context? =null
    private var dstCityCallBack: DstCityCallBack? = null
    constructor(context: Context) : super(context){
        this.ctx = context
    }

    constructor(context: Context, dstCityCallBack: DstCityCallBack):super(context){
        this.dstCityCallBack = dstCityCallBack
        this.ctx = context
    }
    override fun doRequest() {

    }

    open fun doRequest(id:Int) {
        SoguApi.getService(App.INSTANCE, ApproveService::class.java)
                .getHistoryCity(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        var list = payload.payload
                        if (null != dstCityCallBack){
                            dstCityCallBack!!.setHisCityData(list!!)
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message,ctx!!)
                    }
                }, { e ->
                    Trace.e(e)
                })
    }
}