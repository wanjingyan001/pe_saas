package com.sogukj.pe.service

import com.sogukj.pe.bean.*
import com.sogukj.pe.database.FuncReqBean
import com.sogukj.pe.database.HomeFunctionReq
import com.sogukj.pe.database.MainFunIcon
import io.reactivex.Observable
import retrofit2.http.*

/**
 * Created by admin on 2018/5/22.
 */
interface OtherService {
    //    @Headers(value = "Domain-Name: homeFunction")
    @FormUrlEncoded
    @POST("/api/Approve/getFundOrProject")
    fun listSelector(@FieldMap map: HashMap<String, Any>): Observable<Payload<List<CustomSealBean.ValueBean>>>

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
    fun qrNotify(@Url url: String, @Field("status") status: Int): Observable<Payload<Any>>

    @Headers(value = "Domain-Name: QRCode")
    @FormUrlEncoded
    @POST
    fun qrNotify_saas(@Url url: String, @Field("status") status: Int, @Field("phone") phone: String): Observable<Payload<Any>>

    //版本更新
    @POST("/api/Index/version")
    fun getVersion(): Observable<Payload<VersionBean>>

    /**
     * app首页按钮系列
     */
//    @Headers(value = "Domain-Name: homeFunction")
    @POST("/api/Index/homeButton")
    fun homeModuleButton(@Body req: HomeFunctionReq): Observable<Payload<List<MainFunIcon>>>


    @POST("/api/Message/sysMessageIndex")
    fun sysMsgIndex(): Observable<Payload<MessageIndexBean>>

    @FormUrlEncoded
    @POST("/api/Message/sysMessageInfo")
    fun sysMessageInfo(@Field("news_id") news_id: Int): Observable<Payload<GongGaoBean>>

    @POST("/api/Message/getNewPop")
    fun getNewPop(): Observable<Payload<MessageBean>>

    @FormUrlEncoded
    @POST("/api/Message/deleteSysNews")
    fun deleteSysNews(@Field("news_id") news_id: Int): Observable<Payload<Any>>

    //可空（1=>待审批，2=>已审批）
    @FormUrlEncoded
    @POST("/api/Message/sysMessageList")
    fun sysMsgList(@Field("page") page: Int? = 1,
                   @Field("pageSize") pageSize: Int? = 20): Observable<Payload<ArrayList<MessageBean>>>


    /**
     * 获取付费套餐
     */
    @POST("/api/Pay/payBillCombo")
    fun getPayType(): Observable<Payload<List<PackageBean>>>

    /**
     * 获取支付的订单信息
     */
    @POST("/api/Saas/Alipay")
    fun getPayInfo(@Field("key") key: String,
                   @Field("sid") sid: Int): Observable<Payload<String>>
}