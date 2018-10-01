package com.sogukj.pe.module.project.originpro.callback

import com.sogukj.pe.bean.NewProjectInfo

/**
 * Created by CH-ZH on 2018/9/27.
 */
interface NewOriginProCallBack {
    fun setProjectOriginData(data: NewProjectInfo)
    fun setCreateOriginSuccess()
}