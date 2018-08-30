package com.sogukj.pe.service

import com.sogukj.pe.bean.DzhToken
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by CH-ZH on 2018/8/30.
 */
interface DzhHttpUtils {
    @GET("token/access")
    fun getDzhToken(@Query("appid") appid: String, @Query("secret_key") secret_key: String): Observable<DzhToken>
}