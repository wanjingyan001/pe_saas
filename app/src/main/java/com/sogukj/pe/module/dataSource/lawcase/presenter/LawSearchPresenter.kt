package com.sogukj.pe.module.dataSource.lawcase.presenter

import android.content.Context
import android.util.Log
import com.sogukj.pe.App
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.module.dataSource.lawcase.LawSearchCallBack
import com.sogukj.pe.peUtils.ToastUtil
import com.sogukj.pe.presenter.BasePresenter
import com.sogukj.pe.service.DataSourceService
import com.sogukj.service.SoguApi

/**
 * Created by CH-ZH on 2018/9/10.
 */
class LawSearchPresenter : BasePresenter {
    private var ctx : Context? =null
    private var lawSearchCallBack: LawSearchCallBack? = null
    private var page = 0
    constructor(context: Context) : super(context){
        this.ctx = context
    }

    constructor(context: Context, lawSearchCallBack: LawSearchCallBack):super(context){
        this.lawSearchCallBack = lawSearchCallBack
        this.ctx = context
    }
    override fun doRequest() {

    }

    open fun doLawSearchRequest(key:String,type:Int,isRefresh:Boolean){
        Log.e("TAG","doLawSearchRequest -- searchKey ==" + key)
        if (isRefresh){
            page = 0
        }else{
            page++
        }
        SoguApi.getService(App.INSTANCE, DataSourceService::class.java)
                .getLawResultList(key,page,type)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                if (isRefresh) {
                                    if (null != lawSearchCallBack){
                                        lawSearchCallBack!!.refreshLawList(it, payload.total!!)
                                    }
                                } else {
                                    if (null != lawSearchCallBack){
                                        lawSearchCallBack!!.loadMoreData(it)
                                    }
                                }
                            }

                        }.otherWise {
                            if (isRefresh && null != lawSearchCallBack){
                                lawSearchCallBack!!.loadError()
                            }
                            ToastUtil.showCustomToast(R.drawable.icon_toast_fail, payload.message, ctx!!)
                        }
                    }
                    onComplete {
                        (isRefresh).yes {
                            if (null != lawSearchCallBack){
                                lawSearchCallBack!!.dofinishRefresh()
                            }

                        }.otherWise {
                            if (null != lawSearchCallBack){
                                lawSearchCallBack!!.dofinishLoadMore()
                            }
                        }
                    }

                    onError {
                        if (isRefresh && null != lawSearchCallBack){
                            lawSearchCallBack!!.loadError()
                        }
                        ToastUtil.showCustomToast(R.drawable.icon_toast_fail, "获取数据失败", ctx!!)
                    }
                }
    }
}