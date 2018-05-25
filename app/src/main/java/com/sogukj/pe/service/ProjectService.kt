package com.sogukj.pe.service

import com.sogukj.pe.baselibrary.SoguService
import com.sogukj.pe.baselibrary.SoguService.Companion.APPKEY_NAME
import com.sogukj.pe.baselibrary.SoguService.Companion.APPKEY_VALUE
import com.sogukj.pe.bean.*
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Created by admin on 2018/5/24.
 */
interface ProjectService:SoguService {
    @FormUrlEncoded
    @POST("/api/Stockinfo/tenShareHolder")
    fun listTenShareHolders(
            @Field("company_id") company_id: Int
            , @Field("shareholder_type") shareholder_type: Int = 1
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<TimeGroupedShareHolderBean>>>

    @FormUrlEncoded
    @POST("/api/Stockinfo/tenShareHolder")
    fun gubenjiegou(
            @Field("company_id") company_id: Int
            , @Field("shareholder_type") shareholder_type: Int = 3
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<TimeGroupedCapitalStructureBean>>>

    //股权变动列表
    @FormUrlEncoded
    @POST("/api/StockInfo/equityList")
    fun equityList(@Field("company_id") company_id: Int): Observable<Payload<ArrayList<EquityListBean>>>

    //股权信息
    @FormUrlEncoded
    @POST("/api/StockInfo/equityInfo")
    fun equityInfo(@Field("hid") hid: Int): Observable<Payload<ArrayList<StructureBean>>>

    //财务列表
    @FormUrlEncoded
    @POST("/api/StockInfo/financialList")
    fun financialList(@Field("company_id") company_id: Int): Observable<Payload<ArrayList<FinanceListBean>>>

    //财务信息详情
    @FormUrlEncoded
    @POST("/api/StockInfo/financialInfo")
    fun financialInfo(@Field("fin_id") fin_id: Int): Observable<Payload<ArrayList<FinanceDetailBean>>>


    @FormUrlEncoded
    @POST("/api/Stockinfo/Copyreg")
    fun listCopyright(
            @Field("company_id") company_id: Int
            , @Field("type") type: Int = 1
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<List<CopyRightBean>>>

    @FormUrlEncoded
    @POST("/api/Stockinfo/Companyintro")
    fun companyInfo2(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<String>>

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

}