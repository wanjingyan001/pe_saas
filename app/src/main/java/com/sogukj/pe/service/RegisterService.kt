package com.sogukj.pe.service

import com.sogukj.pe.bean.*
import io.reactivex.Observable
import io.reactivex.Observer
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * Created by admin on 2018/6/29.
 */
interface RegisterService {

    /**
     * 提交手机号,发送验证码
     */
    @Headers(value = "Domain-Name: Register")
    @FormUrlEncoded
    @POST("/api/Saas/send_saas_code")
    fun sendVerCode(@Field("phone") phone: String): Observable<Payload<Any>>

    /**
     * 验证短信验证码
     */
    @Headers(value = "Domain-Name: Register")
    @FormUrlEncoded
    @POST("/api/Saas/verify_saas_code")
    fun verifyCode(@Field("phone") phone: String,
                   @Field("code") code: String): Observable<Payload<RegisterVerResult>>

    /**
     * 邀请码验证
     */
    @Headers(value = "Domain-Name: Register")
    @FormUrlEncoded
    @POST("/api/Saas/get_check_code")
    fun inviteCode(@Field("phone") phone: String,
                   @Field("code") code: String): Observable<Payload<CompanyTeamInfo>>

    /**
     * 团队信息补充
     */
    @Headers(value = "Domain-Name: Register")
    @POST("/api/Saas/get_info_sup")
    fun teamInfoSupplement(@Body req: TeamInfoSupplementReq): Observable<Payload<JoinTeamResult>>


    /**
     * 上传名片
     */
    @Headers(value = "Domain-Name: Register")
    @POST("/api/Saas/uploadCard")
    fun uploadCard(@Body reqBean: RequestBody): Observable<Payload<String>>


    /**
     * 获取邀请码接口
     */
    @FormUrlEncoded
    @POST("/api/Saas/get_code")
    fun getInviteCode(@Field("key") key: String): Observable<Payload<InviteCode>>


    /**
     * 通过手机号邀请
     *
     */
    @Headers(value = "Domain-Name: Register")
    @FormUrlEncoded
    @POST("/api/Saas/send_add_code")
    fun inviteByPhone(@Field("phone") phone: String,
                      @Field("code") code: String,
                      @Field("name") name: String): Observable<Payload<Any>>

    /**
     * 审核失败后 机构信息查询
     */
    @Headers(value = "Domain-Name: Register")
    @FormUrlEncoded
    @POST("/api/Saas/get_mechanism_info")
    fun getMechanismInfo(@Field("user_id") user_id: Int): Observable<Payload<MechanismInfo>>

    /**
     * 基础资料上传
     */
    @FormUrlEncoded
    @POST("/api/Saas/basic_data_upload")
    fun uploadBasicInfo(@Field("key") key: String,
                        @Field("email") email: String? = null,
                        @Field("address") address: String? = null,
                        @Field("website") website: String? = null,
                        @Field("telephone") telephone: String? = null): Observable<Payload<Any>>

    /**
     * 上传logo
     */
    @POST("/api/Saas/addlogo")
    fun uploadLogo(@Body reqBean: RequestBody): Observable<Payload<String>>

    /**
     * 部门列表
     */
    @POST("/api/Saas/depart_list")
    fun getDepartList(): Observable<Payload<List<Department>>>

    /**
     * 添加部门
     */
    @FormUrlEncoded
    @POST("/api/Saas/addDepart")
    fun addDepartment(@Field("name") name: String): Observable<Payload<Department>>

    /**
     * 部门删除
     */
    @FormUrlEncoded
    @POST("/api/Saas/depart_del")
    fun deleteDepartment(@Field("depart_id") depart_id: Int): Observable<Payload<Any>>

    /**
     * 部门设置页面
     */
    @FormUrlEncoded
    @POST("/api/Saas/depart_set_list")
    fun setDepartment(@Field("depart_id") depart_id: Int): Observable<Payload<DepartmentSite>>


    /**
     * 人员列表
     */
    @FormUrlEncoded
    @POST("/api/Saas/user_list")
    fun getMemberList(@Field("key") key: String): Observable<Payload<MemberList>>

    /**
     * 部门设置---添加人员
     */
    @FormUrlEncoded
    @POST("/api/Saas/depart_set")
    fun addDepartmentMember(@Field("depart_id") depart_id: Int,
                            @Field("depart_head") depart_head: Int? = null,
                            @Field("depart_member") depart_member: String? = null): Observable<Payload<Any>>

    /**
     * 获取完整用户对象
     */
    @FormUrlEncoded
    @POST("/api/Saas/get_menu")
    fun getUserBean(@Field("phone") phone: String,
                    @Field("user_id") user_id: Int,
                    @Field("source") source: String? = null,
                    @Field("unique") unique: String? = null): Observable<Payload<UserBean>>

    /**
     * 获取基础资料信息
     */
    @FormUrlEncoded
    @POST("/api/Saas/get_basic_data")
    fun getBasicInfo(@Field("key") key: String): Observable<Payload<MechanismBasicInfo>>

    /**
     * 登录后告知服务端,服务端会推送一条IM消息,用来使消息列表不为空
     */
    @POST("api/Saas/pushMsg")
    fun NoticeService(): Observable<Payload<Any>>


    /**
     * 判断是否绑定过第三方登录
     */
    @Headers(value = "Domain-Name: WX")
    @FormUrlEncoded
    @POST("api/Saas/threePartyLogin")
    fun checkThirdBinding(@Field("source") source: String,
                          @Field("unique") unique: String): Observable<Payload<CheckThird>>
}

