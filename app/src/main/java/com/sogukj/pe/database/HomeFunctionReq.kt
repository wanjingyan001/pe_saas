package com.sogukj.pe.database

/**
 * Created by admin on 2018/7/9.
 */
data class HomeFunctionReq(val flag: Int,//
                           val data: List<FuncReqBean>? = null//当前按钮的排序
)

data class FuncReqBean(val id:Long,val seq:Int)