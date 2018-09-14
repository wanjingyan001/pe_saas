package com.sogukj.pe.module.lpassistant

import com.sogukj.pe.bean.PlListInfos
import com.sogukj.pe.bean.PolicyBannerInfo

/**
 * Created by CH-ZH on 2018/9/5.
 */
interface PolicyExpressCallBack {
    fun setBannerInfo(bannerInfo: PolicyBannerInfo)
    fun setBannerError()
    fun refreshPlList(infos : List<PlListInfos>)
    fun loadMoreData(infos : List<PlListInfos>)
    fun dofinishRefresh()
    fun dofinishLoadMore()
    fun doError()
}