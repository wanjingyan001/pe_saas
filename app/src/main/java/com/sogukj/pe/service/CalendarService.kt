package com.sogukj.pe.service

import com.sogukj.pe.bean.*
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Created by admin on 2018/5/23.
 */
interface CalendarService {

    /**
     * 获取日程/团队日程
     * stat:1=>日程，2=>团队日程,3=>项目事项
     * time: 2017-10-10形式
     * filter:stat=2时，多个uid以逗号隔开,例如’1，2‘
     */
    @FormUrlEncoded
    @POST("/api/Calendar/showSchedule")
    fun showSchedule(@Field("page") page: Int = 1,
                     @Field("pageSize") pageSize: Int = 20,
                     @Field("stat") stat: Int,
                     @Field("time") time: String,
                     @Field("filter") filter: String? = null): Observable<Payload<List<ScheduleBean>>>

    /**
     * 获取项目事项
     */
    @FormUrlEncoded
    @POST("/api/Calendar/showSchedule")
    fun ShowMatterSchedule(@Field("page") page: Int = 1,
                           @Field("pageSize") pageSize: Int = 20,
                           @Field("stat") stat: Int = 3,
                           @Field("time") time: String,
                           @Field("filter") filter: String? = null,
                           @Field("company_id") company_id: String? = null): Observable<Payload<List<ProjectMattersBean>>>

    /**
     * 获取任务列表
     */
    @FormUrlEncoded
    @POST("/api/Calendar/showTask")
    fun showTask(@Field("page") page: Int = 1,
                 @Field("pageSize") pageSize: Int = 20,
                 @Field("range") range: String,//时间区间 可空（’w'=>一周内，'m'=>一月内，’y‘=>一年内）
                 @Field("is_finish") is_finish: Int//是否完成  （1=>完成，0=>未完成）全部请传 2
    ): Observable<Payload<List<TaskItemBean>>>


    /**
     * 项目关键节点|项目代办|项目完成
     *
     */
    @FormUrlEncoded
    @POST("/api/Calendar/projectMatter")
    fun projectMatter(@Field("company_id") company_id: Int,
                      @Field("project_type") project_type: Int = 1//1=>项目关键节点,2=>项目完成，3=>项目代办
    ): Observable<Payload<List<KeyNode>>>


    /**
     * 项目代办|项目完成
     */
    @FormUrlEncoded
    @POST("/api/Calendar/projectMatter")
    fun projectMatter2(@Field("company_id") company_id: Int,
                       @Field("project_type") project_type: Int? = null//1=>项目关键节点,2=>项目完成，3=>项目代办
    ): Observable<Payload<List<MatterDetails>>>

    /**
     * 任务详情
     */
    @FormUrlEncoded
    @POST("/api/Calendar/showTaskInfo")
    fun showTaskDetail(@Field("data_id") data_id: Int): Observable<Payload<TaskDetailBean>>

    /**
     * 日程详情
     */
    @FormUrlEncoded
    @POST("/api/Calendar/showTaskInfo")
    fun showScheduleDetail(@Field("data_id") data_id: Int): Observable<Payload<ScheduleDetailsBean>>

    /**
     * 添加评论
     */
    @FormUrlEncoded
    @POST("/api/Calendar/addComment")
    fun addComment(@Field("data_id") data_id: Int,
                   @Field("content") content: String): Observable<Payload<TaskDetailBean.Record>>


    /**
     * 获取要修改的日程/任务数据
     */
    @FormUrlEncoded
    @POST("/api/Calendar/showEditTask")
    fun showEditTask(@Field("data_id") data_id: Int): Observable<Payload<ModifiedTaskBean>>

    /**
     * 提交修改
     */
    @POST("/api/Calendar/aeCalendarInfo")
    fun aeCalendarInfo(@Body reqBean: TaskModifyBean): Observable<Payload<Any>>

    @FormUrlEncoded
    @POST("/api/Calendar/deleteTask")
    fun deleteTask(@Field("data_id") data_id: Int): Observable<Payload<Any>>

    /**
     * 重大事件
     */
    @FormUrlEncoded
    @POST("api/Calendar/showGreatPoint")
    fun showGreatPoint(@Field("timer") timer: String): Observable<Payload<List<String>>>

    /**
     * 完成任务
     */
    @FormUrlEncoded
    @POST("/api/Calendar/finishTask")
    fun finishTask(@Field("rid") rid: Int): Observable<Payload<Int>>

    //每周工作安排列表
    @FormUrlEncoded
    @POST("/api/Weekly/getWeeklyWorkList")
    fun getWeeklyWorkList(@Field("offset") offset: Int): Observable<Payload<List<WeeklyArrangeBean>>>

    /**
     * 新每周工作安排
     */
    @POST("/api/Weekly/newWeeklyWork")
    fun getWeeklyWorkList(@Body body:ArrangeReqBean): Observable<Payload<List<NewArrangeBean>>>

    //提交|修改 每周工作安排
    @POST("/api/Weekly/submitWeeklyWork")
    fun submitWeeklyWork(@Body body: WeeklyReqBean): Observable<Payload<Any>>
}