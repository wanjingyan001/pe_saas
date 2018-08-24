package com.sogukj.pe.bean

import com.chad.library.adapter.base.entity.SectionEntity
import com.sogukj.pe.database.MainFunIcon

/**
 * Created by admin on 2018/8/23.
 */
class ProjectModules : SectionEntity<ProjectDetailBean.DetailBean.DetailSmallBean> {
    var state: Int = 0

    constructor(detail: ProjectDetailBean.DetailBean.DetailSmallBean) : super(detail)

    constructor(isHeader: Boolean, header: String, state: Int) : super(isHeader, header) {
        this.state = state
    }
}