package com.sogukj.pe.presenter

import com.netease.nimlib.sdk.search.model.MsgIndexRecord
import com.sogukj.pe.bean.DepartmentBean
import com.sogukj.pe.bean.PlListInfos

/**
 * Created by CH-ZH on 2018/8/20.
 */
interface ImSearchCallBack {
    fun clearData()
    fun setFullData(param: List<MsgIndexRecord>)
    fun setEmpty(isEmpty: Boolean)
    fun setContractData(param:List<DepartmentBean>)
    fun refreshPlList(infos: List<PlListInfos>)
    fun loadMoreData(moreData: List<PlListInfos>)
    fun finishLoadMore()
}