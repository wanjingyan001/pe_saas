package com.sogukj.pe.service

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
interface PartyBuildService {

    //.栏目列表
    @POST("/api/Partybuild/categoryList")
    fun categoryList(): Observable<Payload<List<PartyTabBean>>>

    //栏目下的文章和文件列表
    @FormUrlEncoded
    @POST("/api/Partybuild/categoryInfo")
    fun categoryInfo(@Field("id") id: Int): Observable<Payload<PartyItemBean>>

    //上传栏目的文件
    @POST("/api/Partybuild/uploadCategoryFile")
    fun uploadPartyFile(@Body body: RequestBody): Observable<Payload<Any>>

    //文章详情
    @FormUrlEncoded
    @POST("/api/Partybuild/articleInfo")
    fun articleInfo(@Field("id") id: Int): Observable<Payload<ArticleDetail>>

    //栏目下的文章列表(查看更多)
    @FormUrlEncoded
    @POST("/api/Partybuild/articleList")
    fun articleList(@Field("id") id: Int,
                    @Field("page") page: Int,
                    @Field("pageSize") pageSize: Int? = 20): Observable<Payload<List<ArticleBean>>>

    //栏目下的文件列表(查看更多)
    @FormUrlEncoded
    @POST("api/Partybuild/categoryFileList")
    fun categoryFileList(@Field("id") id: Int,
                         @Field("page") page: Int,
                         @Field("pageSize") pageSize: Int? = 20): Observable<Payload<List<FileBean>>>

}