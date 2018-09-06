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
    private var bannerInfos  = ArrayList<PolicyBannerInfo.BannerInfo>()
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
        doBannerRequest()
        doListInfoRequest(false)
    }

    open fun doBannerRequest(){
        val bannerInfo1 = PolicyBannerInfo.BannerInfo()
        val bannerInfo = PolicyBannerInfo()
        bannerInfo1.image = "http://guwan-sg.oss-cn-hangzhou.aliyuncs.com/homePageIcon/BannerImage/kuaizhangxingqiu.png"
        bannerInfo1.title = "快涨星球介绍"
        val bannerInfo2 = PolicyBannerInfo.BannerInfo()
        bannerInfo2.image = "http://guwan-sg.oss-cn-hangzhou.aliyuncs.com/homePageIcon/BannerImage/HD8888Banner.png"
        bannerInfo2.title = "中国证券监督管理委员会行政许可实施程序规实施程序规实施程序规"
        val bannerInfo3 = PolicyBannerInfo.BannerInfo()
        bannerInfo3.image = "http://guwan-sg.oss-cn-hangzhou.aliyuncs.com/homePageIcon/BannerImage/SortAxg.png"
        bannerInfo3.title = "关于修改<中国证券监督管理委员会上市公司并购重组审核委员会工作规程>的决定》"
        bannerInfos.clear()
        bannerInfos.add(bannerInfo1)
        bannerInfos.add(bannerInfo2)
        bannerInfos.add(bannerInfo3)

        bannerInfo.data = bannerInfos

        if (null != callBack){
            callBack!!.setBannerInfo(bannerInfo)
        }
    }

    open fun doListInfoRequest(isRefresh : Boolean){
        if (isRefresh){
            page = 1
        }else{
            page++
        }
        Log.e("TAG","page ==" + page)
        SoguApi.getService(App.INSTANCE, OtherService::class.java)
                .getPolicyExpressList(page, pageSize)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                if (isRefresh) {

                                } else {

                                }
                            }

                        }.otherWise {
                            ToastUtil.showCustomToast(R.drawable.icon_toast_fail, payload.message, ctx!!)
                        }
                    }
                    onComplete {
                        (isRefresh).yes {
//                            finishRefresh()
                        }.otherWise {
//                            finishLoadMore()
                        }
                    }

                    onError {
                        ToastUtil.showCustomToast(R.drawable.icon_toast_fail, "获取数据失败", ctx!!)
                    }
                }
    }


}