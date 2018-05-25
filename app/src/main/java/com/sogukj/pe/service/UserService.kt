package com.sogukj.pe.service

import com.sogukj.pe.bean.*
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Created by admin on 2018/5/22.
 */
interface UserService{
    companion object {
        const val APPKEY_NAME = "appkey"
        const val APPKEY_VALUE = "d5f17cafef0829b5"
    }
    @FormUrlEncoded
    @POST("/api/index/send_code")
    fun sendVerifyCode(@Field("phone") phone: String
                       , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE): Observable<Payload<Any>>


    @FormUrlEncoded
    @POST("/api/index/verify_code")
    fun login(@Field("phone") phone: String
              , @Field("code") code: String
              , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE): Observable<Payload<UserBean>>

    @FormUrlEncoded
    @POST("/api/Message/recentContacts")
    fun recentContacts(@Field("from") from: String): Observable<Payload<ArrayList<UserBean>>>


    @FormUrlEncoded
    @POST("/api/UserFont/getDepartmentInfo")
    fun userDepart(@Field(APPKEY_NAME) appkey: String = APPKEY_VALUE): Observable<Payload<List<DepartmentBean>>>

    /**
     * 省市区选择
     */
    @POST("/api/Index/getCityArea")
    fun getCityArea(): Observable<Payload<ArrayList<CityArea>>>

    /**
     * 行业分类
     */
    @POST("/api/Index/industryCategory")
    fun industryCategory(): Observable<Payload<List<Industry>>>

    /**
     *获取个人简历所有信息
     */
    @FormUrlEncoded
    @POST("/api/Userfont/getPersonalResume")
    fun getPersonalResume(@Field("user_id") user_id: Int): Observable<Payload<Resume>>

    /**
     * 简历-添加教育经历
     */
    @POST("/api/Userfont/addExperience")
    fun addExperience(@Body reqBean: EducationReqBean): Observable<Payload<EducationBean>>

    /**
     * 简历-添加工作经历
     */
    @POST("/api/Userfont/addExperience")
    fun addWorkExperience(@Body reqBean: WorkReqBean): Observable<Payload<WorkEducationBean>>

    /**
     * 简历-修改个人简历基本信息
     */
    @POST("/api/Userfont/editResumeBaseInfo")
    fun editResumeBaseInfo(@Body ae: UserReq): Observable<Payload<Any>>


    /**
     * 简历-修改教育经历
     */
    @POST("/api/Userfont/editExperience")
    fun editExperience(@Body reqBean: EducationReqBean): Observable<Payload<EducationBean>>

    /**
     * 简历-修改工作经历
     */
    @POST("/api/Userfont/editExperience")
    fun editExperience(@Body reqBean: WorkReqBean): Observable<Payload<WorkEducationBean>>

    /**
     * 删除（教育|工作）经历
     */
    @FormUrlEncoded
    @POST("/api/UserFont/deleteExperience")
    fun deleteExperience(@Field("we_id") we_id: Int,//教育|工作ID（非空必传 )
                         @Field("type") type: Int //非空（1=>教育，2=>工作）
    ): Observable<Payload<Any>>

    /**
     * 意见反馈
     */
    @FormUrlEncoded
    @POST("/api/Userfont/addFeedback")
    fun addFeedback(@Field("suggestion") suggestion: String,
                    @Field("contacter") contacter: String? = null,
                    @Field("contactWay") contactWay: String): Observable<Payload<Any>>

    /**
     * 上传头像
     */
    @POST("/api/Userfont/uploadImage")
    fun uploadImg(@Body body: RequestBody): Observable<Payload<Any>>


    @FormUrlEncoded
    @POST("/api/Userfont/changeMyInfo")
    fun saveUser(@Field("uid") uid: Int
                 , @Field("name") name: String? = null
                 , @Field("depart_id") depart_id: Int? = null
                 , @Field("position") position: String? = null
                 , @Field("phone") phone: String? = null
                 , @Field("project") project: String? = null
                 , @Field("memo") memo: String? = null
                 , @Field("email") email: String? = null
                 , @Field("advice_token") advice_token: String? = null
                 , @Field("phone_type") phone_type: Int = 1
                 , @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE
    ): Observable<Payload<Any>>

    //进入考评系统判断角色
    @POST("/api/grade/getType")
    fun getType(): Observable<Payload<TypeBean>>

    /**
     * 获取个人项目归属信息
     */
    @FormUrlEncoded
    @POST("/api/UserFont/getBelongProject")
    fun getBelongProject(@Field("user_id") user_id: Int): Observable<Payload<BelongBean>>

    @FormUrlEncoded
    @POST("/api/userFont/getFrontUserInfo")
    fun userInfo(@Field("uid") uid: Int, @Field(APPKEY_NAME) appkey: String = APPKEY_VALUE): Observable<Payload<UserBean>>

    @POST("/api/UserFont/getDepartment")
    fun getDepartment(): Observable<Payload<ArrayList<ReceiveSpinnerBean>>>
}