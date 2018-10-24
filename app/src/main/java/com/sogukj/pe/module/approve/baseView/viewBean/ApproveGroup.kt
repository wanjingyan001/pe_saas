package com.sogukj.pe.module.approve.baseView.viewBean

import com.chad.library.adapter.base.entity.SectionEntity

/**
 * Created by admin on 2018/10/9.
 */

class AGroup : SectionEntity<Children>{
    constructor(item:Children) : super(item)
    constructor(isHeader: Boolean, header: String) : super(isHeader, header)
}

data class ApproveGroup(
		var type: Int,//   审批类型
		var title: String,// 出勤休假
		var children: List<Children>
)

data class Children(
		var id: Int,// 模板id
		var url: String,// 模板logo
		var name: String//  模板名
)