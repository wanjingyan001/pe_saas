package com.sogukj.pe.service

import com.sogukj.pe.Consts
import com.sogukj.pe.bean.*
import io.reactivex.Observable
import retrofit2.http.*

/**
 * Created by admin on 2018/5/23.
 */
interface NewService {

    companion object {
        const val APPKEY_NAME = "appkey"
        const val APPKEY_VALUE = "d5f17cafef0829b5"
    }

    @FormUrlEncoded
    @POST("/api/news/newsData")
    fun listNews(@Field("page") page: Int, @Field("pageSize") pageSize: Int = 20
                 , @Field("type") type: Int? = null
                 , @Field("company_id") company_id: Int? = null
                 , @Field("fuzzyQuery") fuzzyQuery: String? = null)
            : Observable<Payload<ArrayList<NewsBean>>>

    //    @Headers(value = "Domain-Name: homeFunction")
    @FormUrlEncoded
    @POST("/api/news/focusCompanyLists")
    fun listProject(@Field("offset") offset: Int
                    , @Field("pageSize") pageSize: Int = 20
                    , @Field("uid") uid: Int? = null
                    , @Field("type") type: Int? = null
                    , @Field("sort") sort: Int? = null
                    , @Field("fuzzyQuery") fuzzyQuery: String? = null
                    , @Field("id") id: Int? = null
                    , @Field("principal_id") principal_id: Int? = null
                    , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE): Observable<Payload<List<ProjectBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/news/newsInfo")
    fun newsInfo(@Field("table_id") table_id: Int
                 , @Field("data_id") data_id: Int
                 , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE): Observable<Payload<Map<String, Any?>>>


    @FormUrlEncoded
    @POST("/api/news/applyNewProject")
    fun addProject(
            @Field("company_id") company_id: Int? = null
            , @Field("name") name: String
            , @Field("shortName") shortName: String
            , @Field("legalPersonName") legalPersonName: String? = null
            , @Field("regLocation") regLocation: String? = null
            , @Field("creditCode") creditCode: String? = null
            , @Field("info") info: String? = null
            , @Field("type") type: Int? = null
            , @Field("charge") charge: Int = 0
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<Any>>

    @FormUrlEncoded
    @POST("/api/news/showProject")
    fun showProject(
            @Field("company_id") company_id: Int): Observable<Payload<ProjectBean>>

    @FormUrlEncoded
    @POST("/api/news/ncFocus")
    fun mark(@Field("uid") uid: Int
             , @Field("company_id") company_id: Int
             , @Field("type") type: Int = 1
             , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<Any>>

    @FormUrlEncoded
    @POST("/api/news/companyData")
    fun projectPage(@Field("company_id") company_id: Int): Observable<Payload<ProjectDetailBean>>

    @FormUrlEncoded
    @POST("/api/news/deleteProject")
    fun delProject(
            @Field("company_id") company_id: Int
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<Any>>

    /*
 name	varchar		公司名称	必传参数（非空）
 uid	int		用户id	必传参数（非空）
 info	text		相关概念	可空
 estiblishTime	date		成立时间	可空(日期形式例如2017-01-01)
 enterpriseType	varchar		企业性质	可空
 regCapital	varchar		注册资金	可空
 mainBusiness	varchar		主营业务	可空
 ownershipRatio	varchar		股权比例	可空
 lastYearIncome	varchar		去年营收	可空
 lastYearProfit	varchar		去年利润	可空
 ThisYearIncome	varchar		今年营收	可空
 ThisYearProfit	varchar		今年利润	可空
 lunci	varchar		融资轮次	可空
 appraisement	varchar		投后估值	可空
 financeUse	varchar		融资用途	可空
 capitalPlan	varchar		资本规划	可空
  */
    @FormUrlEncoded
    @POST("/api/news/addStoreProject")
    fun addStoreProject(
            @Field("name") name: String
            , @Field("shortName") shortName: String
            , @Field("info") info: String? = null
            , @Field("estiblishTime") estiblishTime: String? = null//yyyy-MM-dd
            , @Field("enterpriseType") enterpriseType: String? = null
            , @Field("regCapital") regCapital: String? = null
            , @Field("mainBusiness") mainBusiness: String? = null
            , @Field("ownershipRatio") ownershipRatio: String? = null
            , @Field("lastYearIncome") lastYearIncome: String? = null
            , @Field("lastYearProfit") lastYearProfit: String? = null
            , @Field("thisYearIncome") thisYearIncome: String? = null
            , @Field("thisYearProfit") thisYearProfit: String? = null
            , @Field("lunci") lunci: String? = null
            , @Field("appraisement") appraisement: String? = null
            , @Field("financeUse") financeUse: String? = null
            , @Field("capitalPlan") capitalPlan: String? = null
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<Any>>

    @FormUrlEncoded
    @POST("/api/news/editStoreProject")
    fun editStoreProject(
            @Field("company_id") company_id: Int
            , @Field("name") name: String
            , @Field("shortName") shortName: String
            , @Field("info") info: String? = null
            , @Field("estiblishTime") estiblishTime: String? = null//yyyy-MM-dd
            , @Field("enterpriseType") enterpriseType: String? = null
            , @Field("regCapital") regCapital: String? = null
            , @Field("mainBusiness") mainBusiness: String? = null
            , @Field("ownershipRatio") ownershipRatio: String? = null
            , @Field("lastYearIncome") lastYearIncome: String? = null
            , @Field("lastYearProfit") lastYearProfit: String? = null
            , @Field("thisYearIncome") thisYearIncome: String? = null
            , @Field("thisYearProfit") thisYearProfit: String? = null
            , @Field("lunci") lunci: String? = null
            , @Field("appraisement") appraisement: String? = null
            , @Field("financeUse") financeUse: String? = null
            , @Field("capitalPlan") capitalPlan: String? = null
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<Any>>


    @FormUrlEncoded
    @POST("/api/news/getStoreProject")
    fun getStoreProject(@Field("company_id") company_id: Int): Observable<Payload<StoreProjectBean>>


//    @FormUrlEncoded
//    @POST("/api/news/setUpProject")
//    fun setUpProject(@Field("company_id") company_id: Int): Observable<Payload<Object>>

    // 项目改投（修改项目状态）
    // 非空（1=>调研转储备，2=>储备转立项，3=>立项转已投，4=>已投转退出）
    @FormUrlEncoded
    @POST("/api/news/changeStatus")
    fun changeStatus(@Field("company_id") company_id: Int, @Field("status") status: Int): Observable<Payload<Any>>

    @FormUrlEncoded
    @POST("/api/News/singleCom")
    fun singleCompany(@Field("cId") cId: Int): Observable<Payload<ProjectBean>>

    @POST("/api/News/hotTag")
    fun getHotTag(): Observable<Payload<ArrayList<String>>>

    @FormUrlEncoded
    @POST("/api/News/saveClick")
    fun saveClick(@Field("click_id") click_id: Int): Observable<Payload<Any>>

    // 项目退出输入
    @POST("/api/News/addQuit")
    fun addQuit(@Body map: HashMap<String, Any>): Observable<Payload<Any>>

    //历史退出记录
    @FormUrlEncoded
    @POST("/api/News/quitHistory")
    fun quitHistory(@Field("company_id") company_id: Int): Observable<Payload<ArrayList<QuitBean>>>

    //退出详情
    @FormUrlEncoded
    @POST("/api/News/quitInfo")
    fun quitInfo(@Field("id") id: Int): Observable<Payload<QuitInfoBean>>

    //加入项目群组,第一次调用是创建群组
    @FormUrlEncoded
    @POST("/api/News/createJoinGroup")
    fun createJoinGroup(@Field("accid") accid: String, @Field("company_id") company_id: String): Observable<Payload<Int>>

    @POST("/api/News/negativeCompanyList")
    fun negativeCompanyList(): Observable<Payload<ArrayList<ProjectBean>>>
}