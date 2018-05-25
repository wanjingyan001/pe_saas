package com.sogukj.pe.service

import com.sogukj.pe.baselibrary.SoguService
import com.sogukj.pe.bean.*
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Created by admin on 2018/5/22.
 */
interface ScoreService:SoguService {
    //进入考评系统判断角色
    @POST("/api/grade/getType")
    fun getType(): Observable<Payload<TypeBean>>

    //全员考评分数总览
    @POST("/api/grade/pointRank")
    fun pointRank(): Observable<Payload<ArrayList<ScoreBean>>>

    //(领导)年总考核中心   	1=>进入绩效考核列表页面，2=>进入岗位胜任力列表 3=>进入风控部填写页，4=>进入投资部填写页
    @FormUrlEncoded
    @POST("/api/grade/check")
    fun check(@Field("type") type: Int): Observable<Payload<GradeCheckBean>>

    //投资部填写页提交
    @POST("/api/grade/invest_add")
    fun invest_add(@Body map: HashMap<String, ArrayList<HashMap<String, String>>>): Observable<Payload<Any>>

    //关键绩效指标评价
    //user_id被打分人id
    @FormUrlEncoded
    @POST("/api/grade/perAppraisal")
    fun perAppraisal_TZ(@Field("user_id") user_id: Int, @Field("type") type: Int): Observable<Payload<ArrayList<InvestManageItem>>>

    //关键绩效指标评价----------风控部
    //user_id被打分人id
    @FormUrlEncoded
    @POST("/api/grade/perAppraisal")
    fun perAppraisal_FK(@Field("user_id") user_id: Int, @Field("type") type: Int): Observable<Payload<FKItem>>

    //关键绩效指标评价----------普通部门
    //user_id被打分人id
    @FormUrlEncoded
    @POST("/api/grade/perAppraisal")
    fun perAppraisal_NORMAL(@Field("user_id") user_id: Int, @Field("type") type: Int): Observable<Payload<NormalItemBean>>

    //提交关键绩效和岗位胜任力打分   type  1:提交关键绩效打分 2:提交岗位胜任力打分
    @POST("/api/grade/giveGrade")
    fun giveGrade(@Body map: HashMap<String, Any>): Observable<Payload<Any>>

    //风控部填写页提交
    @POST("/api/grade/risk_add")
    fun risk_add(@Body map: HashMap<String, ArrayList<HashMap<String, String>>>): Observable<Payload<Any>>

    //(领导)岗位胜任力互评详情页面|员工互评结果      可空 为空则是查看员工互评结果 传入user_id则是互评详情
    @FormUrlEncoded
    @POST("/api/grade/info")
    fun grade_info(@Field("user_id") user_id: Int? = null): Observable<Payload<ArrayList<EmployeeInteractBean>>>

    //(领导)关键绩效考核结果
    @POST("/api/grade/achievement")
    fun achievement(): Observable<Payload<ArrayList<EmployeeInteractBean.EmployeeItem>>>

    //岗位胜任力评价页面  普通员工
    @FormUrlEncoded
    @POST("/api/grade/each_comment")
    fun each_comment(@Field("user_id") user_id: Int): Observable<Payload<ArrayList<EmployeeInteractBean.EmployeeItem>>>

    //岗位胜任力评分|查看评分  可空（若传递则查看评分）
    @FormUrlEncoded
    @POST("/api/grade/showJobPage")
    fun showJobPage(@Field("user_id") user_id: Int? = null): Observable<Payload<JobPageBean>>

    //岗位胜任力评分|查看评分  可空（若传递则查看评分）  -1=>还未打完分 1=>尚未完成打分，2=>已完成
    @POST("/api/grade/showSumScore")
    fun showSumScore(): Observable<Payload<TotalScoreBean>>

    //填写页展示页
    @POST("/api/grade/showWrite")
    fun showWrite(): Observable<Payload<TemplateBean>>

    //填写页提交
    @POST("/api/grade/writeAdd")
    fun writeAdd(@Body map: HashMap<String, ArrayList<HashMap<String, String>>>): Observable<Payload<Any>>


    //关键绩效指标评价  ----非空（1=>绩效，3=>加减项）
    @FormUrlEncoded
    @POST("/api/grade/perAppraisal")
    fun perAppraisal(@Field("user_id") user_id: Int, @Field("type") type: Int): Observable<Payload<NormalItemBean>>

    //全员互评进度表
    @POST("/api/Grade/progress")
    fun GradeProgress(): Observable<Payload<ProgressBean>>
}