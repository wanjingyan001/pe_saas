package com.sogukj.pe.service

import com.sogukj.pe.Consts
import com.sogukj.pe.bean.*
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * Created by admin on 2018/5/24.
 */
interface ProjectService {
    companion object {
        const val APPKEY_NAME = "appkey"
        const val APPKEY_VALUE = "d5f17cafef0829b5"
    }

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Stockinfo/tenShareHolder")
    fun listTenShareHolders(
            @Field("company_id") company_id: Int
            , @Field("shareholder_type") shareholder_type: Int = 1
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<TimeGroupedShareHolderBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Stockinfo/tenShareHolder")
    fun gubenjiegou(
            @Field("company_id") company_id: Int
            , @Field("shareholder_type") shareholder_type: Int = 3
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<TimeGroupedCapitalStructureBean>>>

    //股权变动列表
    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/StockInfo/equityList")
    fun equityList(@Field("company_id") company_id: Int): Observable<Payload<ArrayList<EquityListBean>>>

    //股权信息
    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/StockInfo/equityInfo")
    fun equityInfo(@Field("hid") hid: Int): Observable<Payload<ArrayList<StructureBean>>>

    //财务列表
    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/StockInfo/financialList")
    fun financialList(@Field("company_id") company_id: Int): Observable<Payload<ArrayList<FinanceListBean>>>

    //财务信息详情
    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/StockInfo/financialInfo")
    fun financialInfo(@Field("fin_id") fin_id: Int): Observable<Payload<ArrayList<FinanceDetailBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Stockinfo/Copyreg")
    fun listCopyright(
            @Field("company_id") company_id: Int
            , @Field("type") type: Int = 1
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<CopyRightBean>>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Stockinfo/Companyintro")
    fun companyInfo2(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<String>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Stockinfo/InvestDistribute")
    fun listInvestDistribute(
            @Field("company_id") company_id: Int
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<Map<String, Any>>>


    // 记录列表
    @FormUrlEncoded
    @POST("/api/Archives/recodeInfo")
    fun recodeInfo(@Field("company_id") company_id: Int): Observable<Payload<RecordInfoBean>>

    //跟踪记录-新增记录
    @POST("/api/Archives/addRecord")
    fun addRecord(@Body map: HashMap<String, Any>): Observable<Payload<Object>>

    @FormUrlEncoded
    @POST("/api/Company/projIncomeOutcome")
    fun projIncomeOutcome(@Field("projId") projId: Int): Observable<Payload<JinChuKuan>>

    @POST("/api/Index/showStage")
    fun showStage(): Observable<Payload<ArrayList<StageBean>>>

    //1=>尽调数据,8=>投决数据,10=>投后管理,非空
    @FormUrlEncoded
    @POST("/api/Investman/getInvestMan")
    fun getInvestMan(@Field("type") type: Int): Observable<Payload<ArrayList<ManagerBean>>>

    @POST("/api/InvestMan/subInvestMan")
    fun subInvestMan(@Body map: HashMap<String, Any>): Observable<Payload<Any>>

    @FormUrlEncoded
    @POST("/api/Investman/getInvestManDetail")
    fun getInvestManDetail(@Field("projId") projId: Int, @Field("moduleId") moduleId: Int): Observable<Payload<ArrayList<ManagerDetailBean>>>

    @POST("/api/Investman/uploadFile")
    fun uploadFile(@Body body: RequestBody): Observable<Payload<ArrayList<String>>>

    @POST("/api/news/searchCompany")
    fun searchCompany(@Body map: HashMap<String, String>): Observable<Payload<ArrayList<CompanySelectBean>>>

    @POST("/api/Archives/recordsNum")
    fun recordsNum(): Observable<Payload<RecordsNumBean>>

    @FormUrlEncoded
    @POST("/api/Archives/projectRecords")
    fun projectRecords(@Field("type") type: Int): Observable<Payload<ArrayList<ProjectRecordBean>>>

    //股权结构
    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/StockInfo/equityRatio")
    fun equityRatio(@Field("company_id") company_id: Int): Observable<Payload<EquityStructureBean>>

    /**
     * 项目总览
     */
    @POST("api/Userfont/getAllProject")
    fun getAllProjectOverview(): Observable<Payload<List<Company>>>

    /**
     * 项目负责人下的项目数量
     */
    @FormUrlEncoded
    @POST("api/Userfont/getPrincipal")
    fun getPrincipal(@Field("more") more: Int = 0): Observable<Payload<List<UserProjectInfo>>>

    /**
     *  本周内的新增项目
     */
    @FormUrlEncoded
    @POST("api/Userfont/newCompanyAdd")
    fun newCompanyAdd(@Field("more") more: Int = 0): Observable<Payload<ProjectAdd>>

    /**
     * 本周内项目动向
     */
    @FormUrlEncoded
    @POST("api/Userfont/companyTrends")
    fun companyTrends(@Field("more") more: Int = 0): Observable<Payload<ProjectAdd>>

    /**
     * 本周舆情发生数排行
     */
    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("api/Userfont/companyNewsRank")
    fun companyNewsRank(@Field("more") more: Int = 0): Observable<Payload<List<Opinion>>>
}