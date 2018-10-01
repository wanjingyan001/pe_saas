package com.sogukj.pe.module.project.originpro.callback

import com.sogukj.pe.bean.ProjectApproveInfo

/**
 * Created by CH-ZH on 2018/9/27.
 */
interface ProjectApproveCallBack {
    fun setProApproveInfo(infos : List<ProjectApproveInfo>)
    fun createApproveSuccess()
    fun goneCommitLodding()
}