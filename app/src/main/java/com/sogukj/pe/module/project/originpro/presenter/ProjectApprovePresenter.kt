package com.sogukj.pe.module.project.originpro.presenter

import android.content.Context
import com.sogukj.pe.App
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.module.project.originpro.callback.ProjectApproveCallBack
import com.sogukj.pe.peUtils.ToastUtil
import com.sogukj.pe.presenter.BasePresenter
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi

/**
 * Created by CH-ZH on 2018/9/27.
 */
class ProjectApprovePresenter : BasePresenter {
    private var ctx : Context? =null
    private var callBack: ProjectApproveCallBack? = null
    constructor(context: Context) : super(context){
        this.ctx = context
    }

    constructor(context: Context, callBack: ProjectApproveCallBack):super(context){
        this.callBack = callBack
        this.ctx = context
    }
    override fun doRequest() {

    }

    open fun getProApproveInfo(company_id : Int,floor : Int){
        SoguApi.getService(App.INSTANCE,OtherService::class.java)
                .getProApproveInfo(company_id,floor)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val infos = payload.payload
                            if (null != infos && infos.size > 0){
                                val list = infos[0]
                                if (null != list && list.size > 0){
                                    if (null != callBack){
                                        callBack!!.setProApproveInfo(list)
                                    }
                                }
                            }
                        }else{
                            ToastUtil.showCustomToast(R.drawable.icon_toast_fail, payload.message, ctx!!)
                        }
                        if (null != callBack){
                            callBack!!.goneLoading()
                        }
                    }

                    onComplete {

                    }

                    onError {
                        it.printStackTrace()
                        ToastUtil.showCustomToast(R.drawable.icon_toast_fail, "获取数据失败", ctx!!)
                        if (null != callBack){
                            callBack!!.goneLoading()
                        }
                    }
                }
    }

    open fun createApprove(map: HashMap<String, Any>, type: Int){
        SoguApi.getService(App.INSTANCE,OtherService::class.java)
                .createProjectApprove(map)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            if (null != callBack){
                                callBack!!.createApproveSuccess()
                            }
                        }else{
                            ToastUtil.showCustomToast(R.drawable.icon_toast_fail, payload.message, ctx!!)
                        }
                        if (null != callBack){
                            callBack!!.goneCommitLodding()
                        }
                    }

                    onError {
                        it.printStackTrace()
                        if (type == 2){
                            ToastUtil.showCustomToast(R.drawable.icon_toast_fail, "提交失败", ctx!!)
                        }else {
                            ToastUtil.showCustomToast(R.drawable.icon_toast_fail, "保存失败", ctx!!)
                        }
                        if (null != callBack){
                            callBack!!.goneCommitLodding()
                        }
                    }
                }
    }
}