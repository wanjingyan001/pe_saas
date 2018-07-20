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
    const val ProjectBookActivity: String = "/main/bookList/project"
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
    //项目跟踪
    const val ProjectTraceActivity:String = "/main/projectTrace"
    //基金总台账
    const val FundAccountActivity:String = "/fund/jjztz"
    //基金文书
    const val BookListActivity:String = "/main/bookList/fund"
    //基金项目
    const val FundProjectActivity:String = "/fund/jjxm"

    const val WeeklyActivity:String = "/weekly/main"
}