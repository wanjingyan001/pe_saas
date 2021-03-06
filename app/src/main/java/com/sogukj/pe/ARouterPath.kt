package com.sogukj.pe

/**
 * Created by admin on 2018/6/29.
 */
object ARouterPath {
    //储备信息
    const val StoreProjectAddActivity: String = "/project/cbxx"
    // 投决方案(原InvestSuggestActivity)
    const val ManagerActivity: String = "/project/tjsj"
    //投后跟进
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
    //舆情(项目舆情:跳转舆情的关注)
    const val MainNewsActivity: String = "/news/main"
    //项目跟踪
    const val ProjectTraceActivity: String = "/main/projectTrace"
    //基金总台账
    const val FundAccountActivity: String = "/fund/jjztz"
    //基金文书
    const val BookListActivity: String = "/main/bookList/fund"
    //基金项目
    const val FundProjectActivity: String = "/fund/jjxm"
    //本周周报
    const val WeeklyActivity: String = "/weekly/main"
    //外出打卡
    const val LocationActivity: String = "/main/location"
    //关注项目
    const val ProjectFocusActivity: String = "/main/projectFocus"
    //待我审批( ApproveListActivity.start(this, 1))
    const val ApproveListActivity: String = "/main/myApprove"
    //融资舆情(投资事件)
    const val InvestmentActivity:String = "/main/Investment"
    //尽调助手
    const val LpAssistantActivity:String = "/lp/assistant"
    //智能文书
    const val DocumentsListActivity:String = "/source/intelligent"
    //征信
    const val CreditSelectActivity:String = "/credit/main"
    //云盘
    const val SecretCloudActivity:String = "/im/clouddish"
    //大票抬头
    const val ReceiptHeaderActivity:String = "/receipt/header"
}