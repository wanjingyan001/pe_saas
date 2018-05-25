package com.sogukj.pe.service

import com.sogukj.pe.baselibrary.SoguService
import com.sogukj.pe.bean.CreditInfo
import com.sogukj.pe.bean.SecondaryBean
import com.sogukj.pe.bean.SensitiveInfo
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Created by admin on 2018/5/23.
 */
interface CreditService:SoguService {
    /**
     * 征信开始界面接口
     */
    @FormUrlEncoded
    @POST("/api/Credit/showCreditInfo")
    fun showCreditInfo(@Field("company_id") company_id: Int): Observable<Payload<CreditInfo>>

    /**
     * 征信开始界面接口(改版)
     */
    @FormUrlEncoded
    @POST("/api/Credit/creditList")
    fun showCreditList(@Field("company_id") company_id: Int? = null,
                       @Field("offset") offset: Int = 0,
                       @Field("pageSize") pageSize: Int = 20,
                       @Field("fuzzyQuery") fuzzyQuery: String? = null): Observable<Payload<ArrayList<CreditInfo.Item>>>

    /**
     * 征信-敏感信息
     */
    @POST("/api/Credit/sensitiveData")
    fun sensitiveData(@Body map: HashMap<String, Any>): Observable<Payload<SensitiveInfo>>

    /**
     * 征信-敏感信息(修改)
     */
    @FormUrlEncoded
    @POST("/api/Credit/sensitiveInfo")
    fun sensitiveInfo(@Field("idCard") idCard: String): Observable<Payload<SensitiveInfo>>

    /**
     * 涉诉信息相关
     */
    @FormUrlEncoded
    @POST("/api/Credit/declarationList")
    fun declarationList(@Field("id") id: Int,
                        @Field("type") type: String): Observable<Payload<List<SecondaryBean>>>


    /**
     * 一键查询
     */
//    @FormUrlEncoded
    @POST("/api/Credit/surveyCredit")
    fun queryCreditInfo(@Body map: HashMap<String, Any>): Observable<Payload<CreditInfo.Item>>

    @FormUrlEncoded
    @POST("/api/Credit/deleteCredit")
    fun deleteCredit(@Field("id") id: Int): Observable<Payload<Any>>

}