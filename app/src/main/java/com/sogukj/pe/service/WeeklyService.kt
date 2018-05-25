package com.sogukj.pe.service

import com.sogukj.pe.baselibrary.SoguService
import com.sogukj.pe.bean.WeeklySendBean
import com.sogukj.pe.bean.WeeklyThisBean
import com.sogukj.pe.bean.WeeklyWatchBean
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Created by admin on 2018/5/23.
 */
interface WeeklyService:SoguService {
    //周报-本周周报
    @FormUrlEncoded
    @POST("/api/Weekly/index")
    fun getWeekly(@Field("user_id") user_id: Int? = null,
                  @Field("issue") issue: Int? = null,
                  @Field("start_time") start_time: String? = null,
                  @Field("end_time") end_time: String? = null,
                  @Field("week_id") week_id: Int? = null): Observable<Payload<WeeklyThisBean>>

    // 补充工作日程 新增和编辑都是这个接口
    @FormUrlEncoded
    @POST("/api/Weekly/addReport")
    fun addEditReport(@Field("start_time") start_time: String,
                      @Field("end_time") end_time: String,
                      @Field("content") content: String,
                      @Field("week_id") week_id: Int? = null): Observable<Payload<Object>>

    //周报-发送周报
    @FormUrlEncoded
    @POST("/api/Weekly/sendReport")
    fun sendReport(@Field("week_id") week_id: Int? = null,
                   @Field("accept_uid") accept_uid: String,
                   @Field("watch_uid") watch_uid: String? = null): Observable<Payload<Object>>

    //周报-我发出的
    @FormUrlEncoded
    @POST("/api/Weekly/send")
    fun send(@Field("page") page: Int = 1,
             @Field("pageSize") pageSize: Int = 5,
             @Field("start_time") start_time: String,
             @Field("end_time") end_time: String): Observable<Payload<ArrayList<WeeklySendBean>>>

    //待我查看的周报
    @FormUrlEncoded
    @POST("/api/Weekly/receive")
    fun receive(@Field("is_read") is_read: Int? = null,
                @Field("de_id") de_id: Int? = null,
                @Field("start_time") start_time: String? = null,
                @Field("end_time") end_time: String? = null,
                @Field("page") page: Int = 1,
                @Field("pageSize") pageSize: Int = 5): Observable<Payload<ArrayList<WeeklyWatchBean>>>
}