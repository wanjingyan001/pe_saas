package com.sogukj.pe.service

import com.sogukj.pe.bean.*
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Created by admin on 2018/9/5.
 */
interface DataSourceService {
    /**
     * 投资事件
     */
    @FormUrlEncoded
    @POST("/api/Datasource/investList")
    fun getInvestList(@Field("industry_id") industryId: Int? = null,
                      @Field("year") year: Int? = null,
                      @Field("search") search: String? = null,
                      @Field("page") page: Int = 1,
                      @Field("pageSize") pageSize: Int = 20): Observable<Payload<List<InvestmentEvent>>>

    /**
     * 获取投资分类(投资事件的筛选条件1)
     */
    @POST("/api/Datasource/invest_category")
    fun getInvestCategory(): Observable<Payload<List<InvestCategory>>>


    /**
     * 获取数据源各类型文书
     */
    @FormUrlEncoded
    @POST("/api/Datasource/zgsList")
    fun getSourceBookList(@Field("page") page: Int = 1,
                          @Field("pageSize") pageSize: Int = 20,
                          @Field("keywords") keywords: String? = null): Observable<Payload<List<PdfBook>>>


    /**
     * 专利搜索列表
     */
    @FormUrlEncoded
    @POST("/api/Datasource/getSoopatcontent")
    fun getPatentList(@Field("SearchWord") SearchWord: String? = null): Observable<Payload<List<PatentItem>>>

    /**
     * 专利详情
     */
    @FormUrlEncoded
    @POST("api/Datasource/getSoopatInfo")
    fun getPatentDetail(@Field("url")url:String):Observable<Payload<PatentDetail>>
}