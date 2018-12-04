package com.sogukj.pe.service

import com.sogukj.pe.Consts
import com.sogukj.pe.bean.*
import io.reactivex.Observable
import retrofit2.http.*

/**
 * Created by admin on 2018/5/23.
 */
interface CreditService {
    /**
     * 征信开始界面接口
     */
    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Credit/showCreditInfo")
    fun showCreditInfo(@Field("company_id") company_id: Int): Observable<Payload<CreditInfo>>

    /**
     * 征信开始界面接口(改版)
     */
    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Credit/creditList")
    fun showCreditList(@Field("company_id") company_id: Int? = null,
                       @Field("offset") offset: Int = 0,
                       @Field("pageSize") pageSize: Int = 20,
                       @Field("fuzzyQuery") fuzzyQuery: String? = null): Observable<Payload<ArrayList<CreditInfo.Item>>>

    /**
     * 征信-敏感信息
     */
    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @POST("/api/Credit/sensitiveData")
    fun sensitiveData(@Body map: HashMap<String, Any>): Observable<Payload<SensitiveInfo>>

    /**
     * 征信-敏感信息(修改)
     */
    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Credit/sensitiveInfo")
    fun sensitiveInfo(@Field("idCard") idCard: String): Observable<Payload<SensitiveInfo>>

    /**
     * 涉诉信息相关
     */
    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Credit/declarationList")
    fun declarationList(@Field("id") id: Int,
                        @Field("type") type: String): Observable<Payload<List<SecondaryBean>>>


    /**
     * 一键查询
     */
//    @FormUrlEncoded
    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @POST("/api/Credit/surveyCredit")
    fun queryCreditInfo(@Body map: HashMap<String, Any>): Observable<Payload<CreditInfo.Item>>

    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Credit/deleteCredit")
    fun deleteCredit(@Field("id") id: Int): Observable<Payload<Any>>


    /**
     * 百融征信
     */
    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Credit/queryPersonalCre")
    fun HundredCredit(@Field("name") name: String,
                      @Field("idCard") idCard: String,
                      @Field("phone") phone: String): Observable<Payload<NewCreditInfo>>


    /**
     * 百融征信查询记录
     */
    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Credit/PersonalCreList")
    fun getPersonalCreList(@Field("offset") offset: Int = 0,
                           @Field("pageSize") pageSize: Int = 20,
                           @Field("fuzzyQuery") fuzzyQuery: String? = null): Observable<Payload<List<PersonCreList>>>


    /**
     * 个人资质-详情
     */
    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Credit/PersonalCreInfo")
    fun hundredCreditDetail(@Field("pid") pid: Int): Observable<Payload<NewCreditInfo>>

    /**
     *征信 个人资质-删除
     */
    @Headers(value = ["Domain-Name: ${Consts.CREDIT_COLLECTION}"])
    @FormUrlEncoded
    @POST("/api/Credit/PersonalCreDel")
    fun deleteHundredCredit(@Field("pid") pid: Int):Observable<Payload<Any>>
}