package com.sogukj.pe.module.project.originpro.presenter

import android.content.Context
import com.sogukj.pe.App
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.module.project.originpro.callback.NewOriginProCallBack
import com.sogukj.pe.peUtils.ToastUtil
import com.sogukj.pe.presenter.BasePresenter
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi

/**
 * Created by CH-ZH on 2018/9/27.
 */
class NewOriginProPresenter : BasePresenter {
    private var ctx : Context? =null
    private var callBack: NewOriginProCallBack? = null
    constructor(context: Context) : super(context){
        this.ctx = context
    }

    constructor(context: Context, callBack: NewOriginProCallBack):super(context){
        this.callBack = callBack
        this.ctx = context
    }
    override fun doRequest() {

    }

    open fun getOriginProRequest(company_id : Int){
        SoguApi.getService(App.INSTANCE,OtherService::class.java)
                .getProBuildInfo(company_id)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val projectInfo = payload.payload
                            if (null != projectInfo){
                                if (null != callBack){
                                    callBack!!.setProjectOriginData(projectInfo)
                                }
                            }
                        }else{
                            ToastUtil.showCustomToast(R.drawable.icon_toast_fail, payload.message, ctx!!)
                        }
                        if (null != callBack){
                            callBack!!.goneLoadding()
                        }
                    }
                    onComplete {

                    }

                    onError {
                        it.printStackTrace()
                        ToastUtil.showCustomToast(R.drawable.icon_toast_fail, "获取数据失败", ctx!!)
                        if (null != callBack){
                            callBack!!.goneLoadding()
                        }
                    }
                }

    }

    open fun createProjectBuild(map : HashMap<String,Any>){
        SoguApi.getService(App.INSTANCE,OtherService::class.java)
                .createProjectBuild(map)
                .execute {
                    onNext {
                        payload ->
                        if (payload.isOk){
                            if (null != callBack){
                                callBack!!.setCreateOriginSuccess()
                            }
                        }else{
                            ToastUtil.showCustomToast(R.drawable.icon_toast_fail, payload.message, ctx!!)
                        }
                    }
                    onComplete {

                    }

                    onError {
                        it.printStackTrace()
                        ToastUtil.showCustomToast(R.drawable.icon_toast_fail, "创建项目失败", ctx!!)
                    }
                }
    }

}