package com.sogukj.pe.service

import com.sogukj.pe.bean.*
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Created by admin on 2018/5/22.
 */
interface OtherService {
    @FormUrlEncoded
    @POST("/api/Approve/getFundOrProject")
    fun listSelector(
            @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field("type") type: Int
            , @Field("fuzzyQuery") fuzzyQuery: String? = null): Observable<Payload<List<CustomSealBean.ValueBean>>>

    @POST("/api/Message/getMessageIndex")
    fun msgIndex(): Observable<Payload<MessageIndexBean>>

    //可空（1=>待审批，2=>已审批）
    @FormUrlEncoded
    @POST("/api/Message/getMessageList")
    fun msgList(@Field("page") page: Int? = 1,
                @Field("pageSize") pageSize: Int? = 20,
                @Field("status") status: Int? = null,
                @Field("type") type: Int? = null,
                @Field("template_id") template_id: String? = null): Observable<Payload<ArrayList<MessageBean>>>

    //投资经理评价
    @FormUrlEncoded
    @POST("/api/Comment/assess")
    fun assess(@Field("company_id") company_id: Int,
               @Field("is_business") is_business: Int,
               @Field("is_ability") is_ability: Int): Observable<Payload<Any>>

    //首页小红点
    @POST("/api/Index/getNumber")
    fun getNumber(): Observable<Payload<NumberBean>>

    @FormUrlEncoded
    @POST
    fun qrNotify(@Url url:String, @Field("status") status: Int): Observable<Payload<Object>>

    //版本更新
    @POST("/api/Index/version")
    fun getVersion(): Observable<Payload<VersionBean>>
}