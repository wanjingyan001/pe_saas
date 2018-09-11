package com.sogukj.pe.module.dataSource.lawcase

import com.sogukj.pe.bean.LawSearchResultInfo

/**
 * Created by CH-ZH on 2018/9/10.
 */
interface LawSearchCallBack {
    fun refreshLawList(it: List<LawSearchResultInfo>?)
    fun loadMoreData(it: List<LawSearchResultInfo>?)
    fun dofinishRefresh()
    fun dofinishLoadMore()
    fun loadError()
}