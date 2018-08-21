package com.sogukj.pe.presenter

import android.content.Context
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.lucene.LuceneService
import com.netease.nimlib.sdk.search.model.MsgIndexRecord
import com.sogukj.pe.App
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.peUtils.ToastUtil.Companion.showCustomToast
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
                        if (param!!.isEmpty()) {
                            if (null != imSearchCallBack){
                                imSearchCallBack!!.setEmpty(true)
                            }
                        } else {
                            if (null != imSearchCallBack){
                                imSearchCallBack!!.setFullData(param!!)
                            }
                            if (null != imSearchCallBack){
                                imSearchCallBack!!.setEmpty(false)
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
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message,ctx!!)
                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_fail, "公司组织架构人员获取失败",ctx!!)
                })
    }
}