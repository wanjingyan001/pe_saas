package com.sogukj.pe.service

import com.sogukj.pe.bean.*
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.util.*

/**
 * Created by admin on 2018/5/23.
 */
interface FundService {
    /**
     * 获取所有基金公司列表
     */
    @FormUrlEncoded
    @POST("/api/Foundation/fundList")
    fun getAllFunds(@Field("offset") offset: Int = 0,
                    @Field("pageSize") pageSize: Int = 20,
                    @Field("sort") sort: Int,
                    @Field("fuzzyQuery") fuzzyQuery: String = "",
                    @Field("type") type: Int): Observable<Payload<List<FundSmallBean>>>

    /**
     * 获取指定基金的详情
     */
    @FormUrlEncoded
    @POST("/api/Foundation/fundInfo")
    fun getFundDetail(@Field("fund_id") fund_id: Int): Observable<Payload<FundDetail>>

    /**
     * 获取指定基金的架构
     */
    @FormUrlEncoded
    @POST("/api/Foundation/fundStructure")
    fun getFundStructure(@Field("fund_id") fund_id: Int): Observable<Payload<FundStructure>>

    /**
     * 获取指定基金的台账
     */
    @FormUrlEncoded
    @POST("/api/Foundation/fundLedger")
    fun getFundAccount(@Field("fund_id") fund_id: Int): Observable<Payload<FundAccount>>

    @POST("/api/Foundation/editFundInfo")
    fun editFundInfo(@Body map: HashMap<String, Any?>): Observable<Payload<Any>>

    @FormUrlEncoded
    @POST("/api/Fundexcel/getBaseDate")
    fun getBaseDate(@Field("flag") flag: Int): Observable<Payload<ArrayList<FundAssociationBean>>>

    @FormUrlEncoded
    @POST("/api/Fundexcel/getModuleDetail")
    fun getModuleDetail(@Field("projId") projId: Int,
                        @Field("baseDateId") baseDateId: Int,
                        @Field("moduleId") moduleId: Int): Observable<Payload<ArrayList<ManagerDetailBean>>>

    @POST("/api/Fundexcel/modifyModuleDetail")
    fun modifiModuleInfo(@Body map: HashMap<String, Any>): Observable<Payload<Any>>

    @POST("/api/Listinformation/delDir")
    fun delDir(@Body map: HashMap<String, Any>): Observable<Payload<Any>>

    @POST("/api/Listinformation/delFile")
    fun delFile(@Body map: HashMap<String, Any>): Observable<Payload<Any>>

    @POST("/api/Listinformation/editDirname")
    fun editDirname(@Body map: HashMap<String, Any>): Observable<Payload<Any>>

    @POST("/api/Listinformation/moveFile")
    fun moveFile(@Body map: HashMap<String, Any>): Observable<Payload<Any>>

    @POST("/api/Listinformation/uploadArchives")
    fun uploadArchives(@Body body: RequestBody): Observable<Payload<UploadFileBean>>

    @POST("/api/Listinformation/previewFile")
    fun previewFile(@Body map: HashMap<String, String>): Observable<Payload<String>>

    @FormUrlEncoded
    @POST("/api/Listinformation/mkProjOrFundDir")
    fun mkProjOrFundDir(@Field("type") type: Int,
                        @Field("dirname") dirname: String,
                        @Field("id") id: Int,
                        @Field("pid") pid: Int ?= null): Observable<Payload<String>>

    @FormUrlEncoded
    @POST("/api/Listinformation/getCompanyOnFund")
    fun getCompanyOnFund(@Field("fund_id") fund_id: Int): Observable<Payload<ArrayList<FundCompany>>>

    @POST("/api/Listinformation/fileList")
    fun fileList(@Body map: HashMap<String, Any?>): Observable<Payload<ArrayList<FileListBean>>>

    //项目文书|基金档案文件 目录
    //type	number	1	文件类型	非空（1=>项目，2=>基金）
    //fc_id	number	 	项目或者基金id	非空
    @FormUrlEncoded
    @POST("/api/Listinformation/showCatalog")
    fun showCatalog(@Field("type") type: Int = 1, @Field("fc_id") fc_id: Int): Observable<Payload<ArrayList<DirBean>>>

    @POST("/api/Listinformation/projectFilter")
    fun projectFilter(): Observable<Payload<Map<Int, String>>>
}