package com.sogukj.pe

import com.sogukj.pe.module.project.StoreProjectAddActivity
import com.sogukj.pe.module.project.archives.InvestSuggestActivity

/**
 * Created by admin on 2018/6/29.
 */
object ARouterPath {
    //储备信息
    const val StoreProjectAddActivity: String = "/project/cbxx"
    //   投决数据
    const val InvestSuggestActivity: String = "/project/tjsj"
    //投后管理
    const val ManageDataActivity: String = "/project/thgl"
    //项目文书
    const val ProjectBookActivity: String = "/project/xmws"
    //跟踪记录
    const val RecordTraceActivity: String = "/project/gzjl"
    //尽调数据
    const val SurveyDataActivity: String = "/project/jdsj"
    //日历,安排
    const val CalendarMainActivity: String = "/calendar/main"
    //公司选择
    const val CompanySelectActivity: String = "/main/companySelect"
    //审批
    const val EntryApproveActivity: String = "/approve/main"
    //首页功能编辑
    const val MainEditActivity: String = "/main/edit"
    //情报
    const val MainNewsActivity: String = "/news/main"
}