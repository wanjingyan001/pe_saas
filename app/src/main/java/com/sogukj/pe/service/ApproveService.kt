package com.sogukj.pe.service

import com.sogukj.pe.bean.*
import com.sogukj.pe.module.approve.baseView.viewBean.*
import com.sogukj.pe.module.approve.baseView.viewBean.CityBean
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * Created by admin on 2018/5/23.
 */
interface ApproveService {
    companion object {
        const val APPKEY_NAME = "appkey"
        const val APPKEY_VALUE = "d5f17cafef0829b5"
    }

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
    fun listSelector(@FieldMap map: HashMap<String, Any>): Observable<Payload<List<CustomSealBean.ValueBean>>>

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

    @POST("/api/Approve/editApprove")
    fun editApprove(@Body body: RequestBody): Observable<Payload<Any>>

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
    fun showVacation(): Observable<Payload<ArrayList<VacationBean>>>

    @POST("api/Skip/myHolidays")
    fun getNewHoliday(): Observable<Payload<List<VacationBean>>>

    @FormUrlEncoded
    @POST("/api/Approve/historyCity")
    fun getHistoryCity(@Field("template_id") template_id: Int): Observable<Payload<ArrayList<CityArea.City>>>

    //计算出差天数|请假小时-----1=>出差天数，2=>请假时间
    @FormUrlEncoded
    @POST("/api/Approve/calcTotalTime")
    fun calcTotalTime(@Field("start_time") start_time: String,
                      @Field("end_time") end_time: String,
                      @Field("type") type: Int): Observable<Payload<String>>

    @FormUrlEncoded
    @POST("/api/Message/specApprove")
    fun specApprove(
            @Field("news_id") news_id: Int
            , @Field("add_time") add_time: Int? = null
            , @Field("flag") flag: Int
            , @Field("search") search: String? = null): Observable<Payload<List<ApprovalBean>>>

    @FormUrlEncoded
    @POST("/api/Approve/discuss")
    fun discuss(@Field("approval_id") approval_id: Int, @Field("message") message: String): Observable<Payload<Any>>

    // flag	int		标识	1外出打卡记录列表,2打卡显示页面,3添加打卡记录   废弃
    @FormUrlEncoded
    @POST("/api/Index/operateOutCard")
    fun operateOutCard(@Field("flag") flag: Int): Observable<Payload<ArrayList<LocationRecordBean>>>

    @POST("/api/Index/outCardList")
    fun outCardList(): Observable<Payload<ArrayList<LocationRecordBean>>>

    //外出打卡详情
    @FormUrlEncoded
    @POST("/api/Index/outCardInfo")
    fun outCardInfo(@Field("stamp") stamp: Int): Observable<Payload<ArrayList<LocationRecordBean.LocationCellBean>>>

    @POST("/api/Index/outCardApproveList")
    fun outCardApproveList(): Observable<Payload<ArrayList<LocationRecordBean.LocationCellBean>>>

    //    stamp	int		时间戳(年月日时分秒)	非空
//    place	str		地点	非空
//    longitude	str		经度	非空
//    latitude	str		纬度	非空
//    sid	int		关联的出差,请假或外出的审批id	可空
//    id	int		关联的出差,请假或外出的审批id	可空
    // approve_type int 审批类型
    @FormUrlEncoded
    @POST("/api/Index/outCardSubmit")
    fun outCardSubmit(@Field("stamp") stamp: Int,
                      @Field("place") place: String,
                      @Field("longitude") longitude: String,
                      @Field("latitude") latitude: String,
                      @Field("sid") sid: Int? = null,
                      @Field("id") id: Int? = null,
                      @Field("approve_type") approve_type : Int ? = null): Observable<Payload<Int>>


    /**
     * 审批分组布局界面
     */
    @POST("/api/Sptemplate/spWindow")
    fun approveGroup(): Observable<Payload<List<ApproveGroup>>>


    /**
     * 显示审批
     */
    @FormUrlEncoded
    @POST("api/Sptemplate/showTemplate")
    fun showTemplate(@Field("tid") tid: Int, //模板id
                     @Field("aid") aid: Int? = null//修改的审批id
    ): Observable<Payload<List<ControlBean>>>


    /**
     * 获取审批人/抄送人
     */
    @FormUrlEncoded
    @POST("api/Sptemplate/spInfo")
    fun getApprovers(@Field("tid") tid: Int,
                     @Field("aid") aid: Int? = null,
                     @Field("project_id") project_id: String? = null,
                     @Field("fund_id") fund_id: String? = null): Observable<Payload<Approvers>>


    /**
     * 计算时长
     */
    @FormUrlEncoded
    @POST("api/Skip/calcTotalTime")
    fun countDuration(@Field("start_time") start_time: String,//开始时间
                      @Field("end_time") end_time: String,//结束时间
                      @Field("scal_unit") scal_unit: String//计算方式 year=>年 month=>月 day=> 天 hour=>小时 min=>分钟 sec=>秒 work=>按工作时长计算 非空
    ): Observable<Payload<String>>


    /**
     * 上传文件
     */
    @POST("api/Skip/uploadFile")
    fun uploadFiles(@Body body: RequestBody): Observable<Payload<AttachmentBean>>


    /**
     * 请假类型
     */
    @POST("api/Skip/holidaysList")
    fun holidaysList(): Observable<Payload<List<ApproveValueBean>>>

    /**
     * 新审批的选择列表
     */
    @FormUrlEncoded
    @POST
    fun selectionList(@Url url: String, @Field("fund_id") fund_id: String? = null): Observable<Payload<List<ApproveValueBean>>>


    /**
     * 获取省市区数据
     */
    @POST("/api/Skip/cityArea")
    fun selectionCity(): Observable<Payload<List<CityBean>>>


    /**
     * 人员列表
     */
    @POST("/api/Skip/userList")
    fun approvalUsers(): Observable<Payload<List<ApprovalUser>>>

    /**
     * 关联审批单
     */
    @POST("/api/Skip/relateApprove")
    fun docAssociate(): Observable<Payload<List<Document>>>


    /**
     * 提交审批
     * (参数全部使用json传递)
     */
    @FormUrlEncoded
    @POST("/api/Sptemplate/subApp")
    fun submitNewApprove(@Field("tid") tid: Int,//模板id
                         @Field("data") data: String,//模板json
                         @Field("sp") sp: String,//审批人
                         @Field("cs") cs: String? = null,//抄送人
                         @Field("jr") jr: String? = null//经办人
    ): Observable<Payload<Any>>


    /**
     * 修改审批
     */
    @FormUrlEncoded
    @POST("/api/Sptemplate/spEdit")
    fun modifyApprove(@Field("aid") aid: Int,//审批id
                      @Field("data") data: String,//模板json
                      @Field("sp") sp: String,//审批人
                      @Field("cs") cs: String? = null,//抄送人
                      @Field("jr") jr: String? = null//经办人
    ): Observable<Payload<Any>>


    /**
     * 审批列表
     */
    @FormUrlEncoded
//    @POST("/api/Sptemplate/waitDoneApp")
    @POST("/api/Sptemplate/approveList")
    fun getApproveList(@Field("kind") kind: Int = 4,//1=>待我审批 2=>我已审批 3=>我发起的审批 4=>抄送我的
                       @Field("page") page: Int = 1,//页数
                       @Field("pageSize") pageSize: Int = 20,//每页数量
                       @Field("query") query: String? = null,//搜索标题或编号
                       @Field("type") type: String? = null,//审批分组类型,多个逗号隔开，全部请传null
                       @Field("tid") tid: String? = null//审批模板,多个逗号隔开，全部请传null
    ): Observable<Payload<ApproveList>>


    /**
     * 显示审批
     */
    @FormUrlEncoded
    @POST("/api/Sptemplate/showApp")
    fun getApproveDetail(@Field("aid") aid: Int,//审批id
                         @Field("is_mine") is_mine: Int? = null,//1=>是发起人，0=>审批人，主要是区分自审自批的情况
                         @Field("type") type: Int? = null//1=>只打印同意的数据，0=>全部打印，审批记录是否显示全部
    ): Observable<Payload<ApproveDetail>>


    /**
     * 提交评论
     */
    @FormUrlEncoded
    @POST("/api/Sptemplate/comment")
    fun submitComment(@Field("aid") aid: Int,//审批id
                      @Field("content") content: String//评论内容
    ): Observable<Payload<Any>>

    /**
     * 回复
     */
    @FormUrlEncoded
    @POST("/api/Sptemplate/reply")
    fun reply(@Field("hid") hid: Int,//审批流程id
              @Field("comment_id") comment_id: Int,//评论id
              @Field("content") content: String? = null//评论
    ): Observable<Payload<Any>>

    /**
     * 审批
     */
    @POST("/api/Sptemplate/spResult")
    fun doApprove(@Body body: RequestBody): Observable<Payload<Any>>

    /**
     * 撤销审批
     */
    @FormUrlEncoded
    @POST("/api/Sptemplate/spCancel")
    fun approveCancel(@Field("aid") aid: Int,//审批id
                      @Field("content") content: String? = null//撤销理由
    ): Observable<Payload<Any>>


    /**
     * 保存草稿
     */
    @FormUrlEncoded
    @POST("/api/Sptemplate/saveDraft")
    fun saveApproveDraft(@Field("tid") tid: Int,//审批id
                         @Field("data") data: String? = null,
                         @Field("save") save: Int//1保存 0不保存
    ): Observable<Payload<Any>>

    /**
     * 申请加急
     */
    @FormUrlEncoded
    @POST("/api/Sptemplate/spUrgent")
    fun expedited(@Field("aid") aid: Int): Observable<Payload<Any>>


    /**
     * 完成用印|签字完成
     */
    @FormUrlEncoded
    @POST("/api/Sptemplate/spOver")
    fun approveOver(@Field("aid") aid: Int): Observable<Payload<Any>>

    /**
     *  待我审批|我已审批|我发起的审批|抄送我的 的数量
     */
    @POST("/api/Sptemplate/waitDoneAppNum")
    fun newApproveListNum(): Observable<Payload<NewApproveNum>>


    /**
     *   一键复制（第一次请求）获取最近一次的审批的Id
     */
    @FormUrlEncoded
    @POST("/api/Sptemplate/getLastApprove")
    fun getLastApproveID(@Field("tid") tid: Int): Observable<Payload<LastApproveBean>>

    /**
     * 一键复制（第二次请求）获取详情
     */
    @FormUrlEncoded
    @POST("/api/Sptemplate/getPrevInfo")
    fun getLastApproveDetail(@Field("sid") sid: Int): Observable<Payload<List<ControlBean>>>


    /**
     * 导出用印单
     */
    @FormUrlEncoded
    @POST("/api/Sptemplate/deriveWps")
    fun deriveWps(@Field("aid") aid: Int): Observable<Payload<ApprovalForm>>

    /**
     * 新老审批整合———我的请假/出差/外出记录
     */
    @FormUrlEncoded
    @POST("/api/Sptemplate/showLeaveRecode")
    fun showApproveRecode(@Field("kind") kind: Int = 3,
                          @Field("page") page: Int = 1,
                          @Field("pageSize") pageSize: Int = 20,
                          @Field("query") query: String? = null,
                          @Field("type") type: String = "1",
                          @Field("tid") tid: String = "11"// 10 出差 11请假 14 外出
    ): Observable<Payload<ApproveRecordList>>
}