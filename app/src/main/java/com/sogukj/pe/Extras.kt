package com.sogukj.pe

import com.sogukj.pe.peExtended.getIntEnvironment

/**
 * Created by qinfei on 17/3/28.
 */

object Extras {
    val FLAG = "ext.flag"
    val FLAG2 = "ext.flag2"
    val ID = "ext.id"
    val ID2 = "ext.id2"
    val TYPE = "ext.type"
    val TITLE = "ext.title"
    val NAME = "ext.name"
    val CODE = "ext.code"
    val INDEX = "ext.index"
    const val BEAN = "ext.bean"
    val STOCK_CODES = "ext.stockCodes"
    val DATA = "ext.data"
    val DATA2 = "ext.data2"
    val LIST = "ext.list"
    val LIST2 = "ext.list2"
    val MAP = "ext.map"
    val SEND_USERS = "ext.sendUsers"
    val COPY_FOR_USERS = "ext.copyForUsers"
    val REQUESTCODE = 1001
    val requestCode1 = 1000
    val RESULTCODE = 1002
    val RESULTCODE2 = 1003
    val RESULTCODE3 = 1004
    val REQ_EDIT = 0xe0
    val TIME1 = "ext.s_time"
    val TIME2 = "ext.e_time"

    val TYPE1 = "ext.type1"
    val TYPE2 = "ext.type2"

    val TYPE_EMPLOYEE = 3 //领导
    val TYPE_MANAGE = 4  // 员工

    val TYPE_INTERACT = 100
    val TYPE_LISTITEM = 101

    val RED = 70
    val BLACK = 71


    val RED_BLACK = 75
    val JIXIAO = 76

    val QUANXIAN = "ext.quanxian"
    val RULE = "ext.rule"
    val ROLE = "ext.role"
    val ADJUST = "ext.adjust"

    val TYPE_TIAOZHENG = 500
    val TYPE_JIXIAO = 501
    val NIMACCOUNT = "nim.account"//用于存储网易IM自动登录账户
    val NIMTOKEN = "nim.token"//用于存储网易IM自动登录token
    val CREATE_TEAM = "nim.create_team"
    val USER_REMARKS = "ext.Remarks"
    val CAN_REMOVE_MEMBER = "ext.Remove.Member"

    val DEFAULT = "ext.default"
    val REFRESH = "ext.refresh"
    val REFRESH1 = 9999

    val RESTART = "restart"

    val SCHEDULE_DRAFT = "ext.schedule.draft"//用于保存日程和任务的草稿


    val URL = "ext.url"
    val MODULE_ID = "ext.module_id"

    val CompanyKey = "companyKey"
    val CompanyName = "CompanyName"
    val UserName = "saas.userName"
    val SAAS_BASIC_DATA = "saas.basic.data"
    val SaasPhone = "saas.Phone"
    val SaasUserId = "saas.userId"
    val HTTPURL = "saas.httpUrl"
    const val ROUTE_PATH = "ext.route.path"//在拦截器中传递path
    val ROUTH_FLAG = Int.MAX_VALUE


    val STAGE = "ext.stage"
    val COMPANY_ID = "ext.company_id"
    val DIR_ID = "ext.dir_id"

    val isFirstEnter = "ext.first.enter"//第一次进入APP

    val main_flag = "main.flag"
    val CITY_JSON = "city_json"
    const val CONNECT = 100
    const val CONNECTED = 101
    const val MESSAGE = 102
    val DZH_TOKEN = "dzh_token"
    const val DZH_APP_ID = "dcdc435cc4aa11e587bf0242ac1101de"
    const val DZH_SECRET_KEY = "InsQbm2rXG5z"
    const val PAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsCwJulgR5oVnHMkf3YiY5b1e02BYrLGq+NYuKhtGtTMb7cRHdnw44iEw1SNmxVEgi1+FEI0bCnB1tYGqOdWL8k+RfIMonjs4Z4MYkb88ioa0GnyVfa7r3B2xcDEHWowrFX2Tk0hdHRXvTdoQ0PgKIfXB1o2bleWfbqLWATpOyJ/iDxDy0rA4ADpqa3BN+UD4D5S7bVIz8AVRCYZmCHVyW70JvLMfHwov86WGr5VRROgsEprWNKktYTQNlh4+lwsBRTQTgurla98sC4GiLETOTtvrss3OKjV1zAfKSWBByMjO14e/XFZ3ZRxO6aqHXpfhw2/30mJrtO3ap9sBTq/2fwIDAQAB"

    const val INVEST_SEARCH_HISTORY = "ext.invest_search_history"//投资事件查询历史记录
    const val SOURCE_PDF_HISTORY = "ext.source_pdf_history"//数据源PDF查询记录
    const val DOWNLOADED_PDF = "ext.downloaded_pdf"//已下载文件
    const val PATENT_HISTORY = "ext.patent_history"//专利查询的查看历史
    const val APPROVE_SEARCH_HISTORY = "ext.approve_search_history"//审批查询历史
    const val IS_FIRST_LAW = "is_first_law"
    const val IS_FIRST_PATENT = "is_first_patent"

    const val THIRDLOGIN = "ext.third_login_account"

    val TYPE_LEVEL_0 = 0
    val TYPE_LEVEL_1 = 1
    val TYPE_FILE = 2
    val FUND = "ext.fund"
    val PROJECT = "ext.project"
    val APPROVE_CONFIG = "approve_config"
    val SIGN_CODE = "pe2017Signkey"

    val DECLARE_URL = "https://sougu.pewinner.com/uploads/xieyi/%E5%85%8D%E8%B4%A3%E5%A3%B0%E6%98%8E.pdf"
    val SAFE_PRE_URL = "http://prehts.pewinner.com/uploads/xieyi/X-PE%E5%AE%89%E5%85%A8%E7%99%BD%E7%9A%AE%E4%B9%A6.pdf"
    val SAFE_ONLINE_URL = "https://sougu.pewinner.com/uploads/xieyi/X-PE%E5%AE%89%E5%85%A8%E7%99%BD%E7%9A%AE%E4%B9%A6.pdf"
    fun getSafeUrl():String{
        var safe_url = ""
        when(getIntEnvironment()){
            0 -> {
                //dev
                safe_url = SAFE_PRE_URL
            }
            1 -> {
                //online
                safe_url = SAFE_ONLINE_URL
            }
        }
        return safe_url
    }

    const val WEIXIN_APP_ID = "wxf4e25190d6389f53"
    const val WX_PAY_TYPE = "WX_PAY_TYPE"
    val SORT = "ext.sort"
    val DIR = "ext.dir"
}
