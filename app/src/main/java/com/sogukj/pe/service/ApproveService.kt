package com.sogukj.pe.service

import com.sogukj.pe.baselibrary.SoguService
import com.sogukj.pe.bean.*
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Created by admin on 2018/5/23.
 */
interface ApproveService:SoguService {
    @POST("/api/Approve/uploadApprove")
    fun uploadApprove(@Body body: RequestBody): Observable<Payload<CustomSealBean.ValueBean>>

    //type	number	1	文件类型	非空（1=>项目文书，2=>基金类型）
    @FormUrlEncoded
    @POST("/api/Listinformation/projectSelect")
    fun projectBookSearch(
            @Field("company_id") company_id: Int
            , @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field("type") type: Int
            , @Field("fileClass") fileClass: String? = null
            , @Field("fuzzyQuery") fuzzyQuery: String? = null
            , @Field("status") status: Int? = null): Observable<Payload<List<ProjectBookBean>>>

    @FormUrlEncoded
    @POST("/api/Index/apply")
    fun mainApprove(@Field("pid") pid: Int = 3): Observable<Payload<List<SpGroupBean>>>

    @FormUrlEncoded
    @POST("/api/Approve/componentInfo")
    fun approveInfo(@Field("template_id") template_id: Int? = null
                    , @Field("sid") sid: Int? = null): Observable<Payload<List<CustomSealBean>>>

    @FormUrlEncoded
    @POST("/api/Approve/leaveInfo")
    fun leaveInfo(@Field("template_id") template_id: Int? = null,
                  @Field("project_id") project_id: Int? = null,
                  @Field("fund_id") fund_id: Int? = null
                  , @Field("sid") sid: Int? = null): Observable<Payload<LeaveBean>>

    @FormUrlEncoded
    @POST("/api/Approve/approveInfo")
    fun approver(@Field("template_id") template_id: Int? = null
                 , @Field("sid") sid: Int? = null
                 , @Field("type") type: Int? = null
                 , @Field("fund_id") fund_id: Int? = null): Observable<Payload<List<ApproverBean>>>

    //获取用户上次审批单id
    @FormUrlEncoded
    @POST("/api/Approve/getLastApprove")
    fun getLastApprove(@Field("template_id") template_id: Int): Observable<Payload<LastApproveBean>>

    @FormUrlEncoded
    @POST("/api/Approve/getFundOrProject")
    fun listSelector(
            @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field("type") type: Int
            , @Field("fuzzyQuery") fuzzyQuery: String? = null): Observable<Payload<List<CustomSealBean.ValueBean>>>

    @POST("/api/Approve/submitApprove")
    fun submitApprove(@Body body: RequestBody): Observable<Payload<Any>>

    @POST("/api/Approve/updateApprove")
    fun updateApprove(@Body body: RequestBody): Observable<Payload<Int>>

    @POST("/api/Approve/saveDraft")
    fun saveDraft(@Body map: HashMap<String, Any>): Observable<Payload<Any>>

    @FormUrlEncoded
    @POST("/api/Approve/waitingMeApproval")
    fun listApproval(
            @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field("status") status: Int? = null
            , @Field("fuzzyQuery") fuzzyQuery: String? = null
            , @Field("type") type: Int? = null
            , @Field("template_id") template_id: String? = null
            , @Field("filter") filter: String? = null
            , @Field("sort") sort: Int? = null
            , @Field("project_id") project_id: Int? = null): Observable<Payload<List<ApprovalBean>>>

    @FormUrlEncoded
    @POST("/api/Approve/projectApprovalHistory")
    fun projectApprovalHistory(
            @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field("project_id") project_id: Int): Observable<Payload<List<ApprovalBean>>>


    @POST("/api/Approve/approveFilter")
    fun approveFilter(): Observable<Payload<ApproveFilterBean>>

    @FormUrlEncoded
    @POST("/api/Approve/approveShow")
    fun showApprove(@Field("approval_id") approval_id: Int, @Field("type") type: Int? = null, @Field("classify") classify: Int? = null
                    , @Field("is_mine") is_mine: Int)
            : Observable<Payload<ApproveViewBean>>

    @FormUrlEncoded
    @POST("/api/Approve/signShow")
    fun showApproveSign(@Field("approval_id") approval_id: Int, @Field("type") type: String? = null): Observable<Payload<ApproveViewBean>>


    @FormUrlEncoded
    @POST("/api/Approve/approveResult")
    fun examineApprove(@Field("approval_id") approval_id: Int
                       , @Field("type") type: Int? = null
                       , @Field("content") content: String): Observable<Payload<Any>>

    @FormUrlEncoded
    @POST("/api/Approve/leaveResult")
    fun examineLeaveApprove(@Field("approval_id") approval_id: Int
                            , @Field("type") type: Int? = null
                            , @Field("content") content: String): Observable<Payload<Any>>

    @FormUrlEncoded
    @POST("/api/Approve/applyUrgent")
    fun approveUrgent(@Field("approval_id") approval_id: Int): Observable<Payload<Any>>

    //撤销
    @FormUrlEncoded
    @POST("/api/Approve/cancelApprove")
    fun cancelApprove(@Field("approval_id") approval_id: Int): Observable<Payload<Any>>

    @FormUrlEncoded
    @POST("/api/Approve/finishApprove")
    fun finishApprove(@Field("approval_id") approval_id: Int): Observable<Payload<Any>>

    @POST("/api/Approve/signResult")
    fun approveSign(@Body body: RequestBody): Observable<Payload<Any>>

    @FormUrlEncoded
    @POST("/api/Approve/Comment")
    fun submitComment(@Field("hid") hid: Int
                      , @Field("comment_id") comment_id: Int = 0
                      , @Field("content") content: String): Observable<Payload<Any>>

    @FormUrlEncoded
    @POST("/api/Approve/updateApprove")
    fun resubApprove(@Field("approval_id") approval_id: Int): Observable<Payload<Any>>

    @FormUrlEncoded
    @POST("/api/Approve/derivePdf")
    fun exportPdf(@Field("approval_id") approval_id: Int): Observable<Payload<ApprovalForm>>

    //type  1=>出差，2=>请假
    @FormUrlEncoded
    @POST("/api/Approve/showLeaveTravel")
    fun showLeaveTravel(@Field("user_id") user_id: Int? = null,
                        @Field("type") type: Int,
                        @Field("page") page: Int? = 1,
                        @Field("pageSize") pageSize: Int? = 20): Observable<Payload<ArrayList<LeaveRecordBean>>>

    //修改出差请假提交
    @POST("/api/Approve/editLeave")
    fun editLeave(@Body body: RequestBody): Observable<Payload<Any>>

    //撤销出差请假
    @FormUrlEncoded
    @POST("/api/Approve/cancelLeave")
    fun cancalLeave(@Field("approval_id") approval_id: Int, @Field("content") content: String): Observable<Payload<Any>>

    //抄送我的
    @FormUrlEncoded
    @POST("/api/Approve/showCopy")
    fun showCopy(
            @Field("page") page: Int = 1
            , @Field("pageSize") pageSize: Int = 20
            , @Field("template_id") template_id: String? = null): Observable<Payload<List<ApprovalBean>>>



    @POST("/api/Approve/showVacation")
    fun showVacation():Observable<Payload<ArrayList<VacationBean>>>

    @FormUrlEncoded
    @POST("/api/Approve/historyCity")
    fun getHistoryCity(@Field("template_id") template_id: Int): Observable<Payload<ArrayList<CityArea.City>>>

    //计算出差天数|请假小时-----1=>出差天数，2=>请假时间
    @FormUrlEncoded
    @POST("/api/Approve/calcTotalTime")
    fun calcTotalTime(@Field("start_time") start_time: String,
                      @Field("end_time") end_time: String,
                      @Field("type") type: Int): Observable<Payload<String>>
}