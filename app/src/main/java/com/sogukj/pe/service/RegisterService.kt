package com.sogukj.pe.service

import com.sogukj.pe.bean.CompanyTeamInfo
import com.sogukj.pe.bean.JoinTeamResult
import com.sogukj.pe.bean.RegisterVerResult
import com.sogukj.pe.bean.TeamInfoSupplementReq
import io.reactivex.Observable
import io.reactivex.Observer
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Created by admin on 2018/6/29.
 */
interface RegisterService {

    /**
     * 提交手机号,发送验证码
     */
    @FormUrlEncoded
    @POST("/api/Index/send_saas_code")
    fun sendVerCode(@Field("phone") phone: String): Observable<Payload<Any>>

    /**
     * 验证短信验证码
     */
    @FormUrlEncoded
    @POST("/api/Index/verify_saas_code")
    fun verifyCode(@Field("phone") phone: String,
                   @Field("code") code: String): Observable<Payload<RegisterVerResult>>

    /**
     * 邀请码验证
     */
    @FormUrlEncoded
    @POST("/api/Index/get_check_code")
    fun inviteCode(@Field("phone") phone: String,
                   @Field("code") code: String): Observable<Payload<CompanyTeamInfo>>

    /**
     * 团队信息补充
     */
    @POST("/api/Index/get_info_sup")
    fun teamInfoSupplement(@Body req: TeamInfoSupplementReq): Observable<Payload<JoinTeamResult>>


    /**
     * 上传名片
     */
    @POST("/api/index/uploadCard")
    fun uploadCard(@Body reqBean: RequestBody): Observable<Payload<String>>


    /**
     * 获取邀请码接口
     */
    @FormUrlEncoded
    @POST("/api/index/get_code")
    fun getInviteCode(@Field("key") key: String): Observable<Payload<String>>


    /**
     * 通过手机号邀请
     *
     */
    @FormUrlEncoded
    @POST("/api/Index/send_add_code")
    fun inviteByPhone(@Field("phone") phone: String,
                      @Field("code") code: String,
                      @Field("name") name: String): Observable<Payload<Any>>
}

