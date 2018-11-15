package com.sogukj.pe.service

import com.google.gson.internal.LinkedHashTreeMap
import com.google.gson.internal.LinkedTreeMap
import com.sogukj.pe.bean.*
import com.sogukj.pe.module.dataSource.DocumentType
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by admin on 2018/9/5.
 */
interface DataSourceService {
    /**
     * 投资事件
     */
    @Headers(value = ["Domain-Name: DataSource"])
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
    @Headers(value = ["Domain-Name: DataSource"])
    @POST("/api/Datasource/invest_category")
    fun getInvestCategory(): Observable<Payload<List<InvestCategory>>>


    /**
     * 获取数据源各类型文书
     */
    @Headers(value = ["Domain-Name: DataSource"])
    @FormUrlEncoded
    @POST("/api/Datasource/industryReport")
    fun getSourceBookList(@Field("page") page: Int = 1,
                          @Field("pageSize") pageSize: Int = 20,
                          @Field("type") type: Int,
                          @Field("condition") condition: Int? = null,
                          @Field("keywords") keywords: String? = null): Observable<Payload<List<PdfBook>>>


    /**
     * 专利搜索列表
     */
    @Headers(value = ["Domain-Name: DataSource"])
    @FormUrlEncoded
    @POST("/api/Datasource/getSoopatcontent")
    fun getPatentList(@Field("page") page: Int = 1,
                      @Field("searchWords") searchWords: String? = null): Observable<Payload<List<PatentItem>>>

    /**
     * 专利详情
     */
    @Headers(value = ["Domain-Name: DataSource"])
    @FormUrlEncoded
    @POST("/api/Datasource/getSoopatInfo")
    fun getPatentDetail(@Field("link") link: String): Observable<Payload<PatentDetail>>

    /**
     * 热门行业研报
     */
    @Headers(value = ["Domain-Name: DataSource"])
    @POST("/api/Datasource/hotReport")
    fun getHotReport(): Observable<Payload<List<HotPostInfo>>>


    /**
     * 政策速递新闻banner
     */
    @Headers(value = ["Domain-Name: DataSource"])
    @POST("api/Datasource/policyNewsBanner")
    fun getPolicyExpressBanner(): Observable<Payload<List<PolicyBannerInfo.BannerInfo>>>

    /**
     * 政策速递新闻列表
     */
    @Headers(value = ["Domain-Name: DataSource"])
    @FormUrlEncoded
    @POST("api/Datasource/policyNews")
    fun getPolicyExpressList(@Field("page") page: Int = 1,
                             @Field("pageSize") pageSize: Int = 20,
                             @Field("keywords") keywords: String? = null,
                             @Field("type") type: Int? = null): Observable<Payload<List<PlListInfos>>>

    /**
     * 政策速递新闻详情
     */
    @Headers(value = ["Domain-Name: DataSource"])
    @FormUrlEncoded
    @POST("api/Datasource/newsInfo")
    fun getPolicyExpressDetail(@Field("news_id") news_id: Int): Observable<Payload<PlDetailInfo>>

    /**
     * 法律助手列表
     */
    @Headers(value = ["Domain-Name: DataSource"])
    @FormUrlEncoded
    @POST("api/Datasource/getbdflLists")
    fun getLawResultList(@Field("key_word") key_word: String,
                         @Field("page") page: Int = 0,
                         @Field("type") type: Int = 1): Observable<Payload<List<LawSearchResultInfo>>>

    /**
     * 法律助手详情
     */
    @Headers(value = ["Domain-Name: DataSource"])
    @FormUrlEncoded
    @POST("api/Datasource/getbdxwContent")
    fun getLawResultDetail(@Field("href") href: String): Observable<Payload<LawNewsDetailBean>>

    /**
     * 热门行业研报选择标签
     */
    @Headers(value = ["Domain-Name: DataSource"])
    @POST("api/Datasource/selectTag")
    fun getAllTag(): Observable<Payload<List<HotPostInfo>>>

    /**
     * 热门行业研报提交标签
     */
    @Headers(value = ["Domain-Name: DataSource"])
    @FormUrlEncoded
    @POST("api/Datasource/subTag")
    fun submitTags(@Field("tags") tags: String): Observable<Payload<Any>>

    /**
     * 热门行业研报—是否提交过
     */
    @Headers(value = ["Domain-Name: DataSource"])
    @POST("api/Datasource/isFirstCome")
    fun isFirstCome(): Observable<Payload<Any>>
}