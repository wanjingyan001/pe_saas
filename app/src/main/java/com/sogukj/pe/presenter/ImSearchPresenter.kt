package com.sogukj.pe.presenter

import android.content.Context
import android.util.Log
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.lucene.LuceneService
import com.netease.nimlib.sdk.search.model.MsgIndexRecord
import com.sogukj.pe.App
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.peUtils.ToastUtil
import com.sogukj.pe.peUtils.ToastUtil.Companion.showCustomToast
import com.sogukj.pe.service.OtherService
import com.sogukj.pe.service.UserService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by CH-ZH on 2018/8/20.
 */
class ImSearchPresenter : BasePresenter {
    private var ctx : Context ? =null
    private var imSearchCallBack: ImSearchCallBack ? = null
    private var page = 1
    private var pageSize = 20
    constructor(context: Context) : super(context){
        this.ctx = context
    }

    constructor(context: Context, imSearchCallBack: ImSearchCallBack):super(context){
        this.imSearchCallBack = imSearchCallBack
        this.ctx = context
    }
    override fun doRequest() {

    }

    open fun doRequest(query : String){
        NIMClient.getService(LuceneService::class.java).searchAllSession(query, 20)
                .setCallback(object : RequestCallback<List<MsgIndexRecord>> {
                    override fun onSuccess(param: List<MsgIndexRecord>?) {
                        if (null != imSearchCallBack){
                            imSearchCallBack!!.clearData()
                        }
                        param?.let {
                            if (it.isEmpty()) {
                                if (null != imSearchCallBack){
                                    imSearchCallBack!!.setEmpty(true)
                                }
                            } else {
                                if (null != imSearchCallBack){
                                    imSearchCallBack!!.setFullData(it)
                                }
                                if (null != imSearchCallBack){
                                    imSearchCallBack!!.setEmpty(false)
                                }
                            }
                        }
                    }

                    override fun onException(exception: Throwable?) {

                    }

                    override fun onFailed(code: Int) {

                    }
                })
    }

    open fun getDepartData(){
        SoguApi.getService(App.INSTANCE, UserService::class.java)
                .userDepart()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        val datas = payload.payload
                        if (null != datas && datas.size > 0){
                            if (null != imSearchCallBack){
                                imSearchCallBack!!.setContractData(datas)
                            }
                        }else{
                            if (null != imSearchCallBack){
                                imSearchCallBack!!.setEmpty(true)
                            }
                        }
                    } else{
                        if (null != imSearchCallBack){
                            imSearchCallBack!!.setEmpty(true)
                        }
                        showCustomToast(R.drawable.icon_toast_fail, payload.message,ctx!!)
                    }
                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_fail, "数据获取失败",ctx!!)
                    if (null != imSearchCallBack){
                        imSearchCallBack!!.setEmpty(true)
                    }
                })
    }

    open fun getPlExpressResultData(isRefresh : Boolean,query:String){
        if (isRefresh){
            page = 1
        }else{
            page++
        }
        Log.e("TAG","page ==" + page)
        SoguApi.getService(App.INSTANCE, OtherService::class.java)
                .getPolicyExpressList(page, pageSize,query)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                if (isRefresh) {
                                    if (null != it && it.size > 0){
                                        if (null != imSearchCallBack) imSearchCallBack!!.refreshPlList(it)
                                    }else{
                                        if (null != imSearchCallBack){
                                            imSearchCallBack!!.setEmpty(true)
                                        }
                                    }
                                } else {
                                    if (null != imSearchCallBack){
                                        imSearchCallBack!!.loadMoreData(it)
                                    }
                                }
                            }

                        }.otherWise {
                            ToastUtil.showCustomToast(R.drawable.icon_toast_fail, payload.message, ctx!!)
                            if (null != imSearchCallBack){
                                imSearchCallBack!!.setEmpty(true)
                            }
                        }
                    }
                    onComplete {
                        (isRefresh).yes {

                        }.otherWise {
                            if (null != imSearchCallBack){
                                imSearchCallBack!!.finishLoadMore()
                            }
                        }
                    }

                    onError {
                        if (null != imSearchCallBack){
                            imSearchCallBack!!.setEmpty(true)
                        }
                        ToastUtil.showCustomToast(R.drawable.icon_toast_fail, "获取数据失败", ctx!!)
                    }
                }
    }

}