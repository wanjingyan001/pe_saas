package com.sogukj.pe.service

import com.sogukj.pe.bean.FundAccount
import com.sogukj.pe.bean.FundDetail
import com.sogukj.pe.bean.FundSmallBean
import com.sogukj.pe.bean.FundStructure
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

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
}