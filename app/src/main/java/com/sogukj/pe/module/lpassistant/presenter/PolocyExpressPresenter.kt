package com.sogukj.pe.module.lpassistant.presenter

import android.content.Context
import android.util.Log
import com.sogukj.pe.App
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.bean.PolicyBannerInfo
import com.sogukj.pe.module.lpassistant.PolicyExpressCallBack
import com.sogukj.pe.peUtils.ToastUtil
import com.sogukj.pe.presenter.BasePresenter
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi

/**
 * Created by CH-ZH on 2018/9/5.
 */
class PolocyExpressPresenter : BasePresenter {
    private var ctx : Context? =null
    private var callBack: PolicyExpressCallBack? = null
    private var page = 1
    private var pageSize = 20
    constructor(context: Context) : super(context){
        this.ctx = context
    }

    constructor(context: Context, callBack: PolicyExpressCallBack):super(context){
        this.callBack = callBack
        this.ctx = context
    }

    override fun doRequest() {

    }

    open fun doRefresh(type:Int?){
        doBannerRequest()
        doListInfoRequest(false,null,type)
    }

    open fun doBannerRequest(){
        val bannerInfo = PolicyBannerInfo()
        SoguApi.getService(App.INSTANCE,OtherService::class.java)
                .getPolicyExpressBanner()
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                bannerInfo.data = it
                                if (null != callBack){
                                    callBack!!.setBannerInfo(bannerInfo)
                                }
                            }
                        }.otherWise {
                            ToastUtil.showCustomToast(R.drawable.icon_toast_fail, payload.message, ctx!!)
                            if (null != callBack){
                                callBack!!.setBannerError()
                            }
                        }
                    }
                    onComplete {

                    }

                    onError {
                        it.printStackTrace()
                        if (null != callBack){
                            callBack!!.setBannerError()
                        }
                    }
                }
    }

    open fun doListInfoRequest(isRefresh: Boolean, keywords: String?, type: Int?){
        if (isRefresh){
            page = 1
        }else{
            page++
        }
        Log.e("TAG","page ==" + page)
        SoguApi.getService(App.INSTANCE, OtherService::class.java)
                .getPolicyExpressList(page, pageSize,keywords,type)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                if (isRefresh) {
                                    if (null != callBack){
                                         callBack!!.refreshPlList(it)
                                    }
                                } else {
                                    if (null != callBack){
                                        callBack!!.loadMoreData(it)
                                    }
                                }
                            }

                        }.otherWise {
                            ToastUtil.showCustomToast(R.drawable.icon_toast_fail, payload.message, ctx!!)
                        }
                    }
                    onComplete {
                        (isRefresh).yes {
                            if (null != callBack){
                                callBack!!.dofinishRefresh()
                            }

                        }.otherWise {
                            if (null != callBack){
                                callBack!!.dofinishLoadMore()
                            }
                        }
                    }

                    onError {
                        ToastUtil.showCustomToast(R.drawable.icon_toast_fail, "获取数据失败", ctx!!)
                    }
                }
    }


}