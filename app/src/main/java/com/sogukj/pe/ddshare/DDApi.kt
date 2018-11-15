/**
 * Copyright (C), 2018-2018, 搜股科技有限公司
 * FileName: DDApi
 * Author: admin
 * Date: 2018/11/15 下午4:20
 * Description: 钉钉请求类
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
package com.sogukj.pe.ddshare

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * @ClassName: DDApi
 * @Description: 钉钉请求类
 * @Author: admin
 * @Date: 2018/11/15 下午4:20
 */
class DDApi {
    companion object {
        private val retrofit by lazy {
            val client = OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .retryOnConnectionFailure(false)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .build()
            val retrofit = Retrofit.Builder()
                    .baseUrl("https://oapi.dingtalk.com/")
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
            retrofit
        }

        fun getService(): DDService {
            return retrofit.create(DDService::class.java)
        }
    }
}