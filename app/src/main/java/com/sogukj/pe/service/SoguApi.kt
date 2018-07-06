package com.sogukj.service

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import com.sogukj.pe.Consts
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.peExtended.getEnvironment
import com.sogukj.pe.peUtils.Store
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by qinfei on 17/7/18.
 */
class SoguApi {
    private val context: Application
    private var retrofit: Retrofit

    private constructor(context: Application) {
        this.context = context
//        val client = OkHttpClient.Builder()
        val client = RetrofitUrlManager.getInstance().with(OkHttpClient.Builder())
                .addInterceptor(initLogInterceptor())
                .addInterceptor(initInterceptor(context))
                .retryOnConnectionFailure(false)
                .readTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .build()

        //test
        val user = Store.store.getUser(context)
        var url = ""
        if (getEnvironment() == "ht") {
            url = if (user == null) {
                "http://hts.pewinner.com"
            } else {
                if (user.phone == "15800421946") {
                    "http://prehts.pewinner.com"
                } else {
                    "http://hts.pewinner.com"
                }
            }
        } else if (getEnvironment() == "pe") {
            //url = "http://dev.ht.stockalert.cn"
            //url = "http://pre.pe.stockalert.cn"
            url = "http://prehts.pewinner.com"
        }

        retrofit = Retrofit.Builder()
                .baseUrl(Consts.HTTP_HOST)
//                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build()
    }

    private fun <T> getService(service: Class<T>): T {
        return retrofit.create(service)
    }

    /**
     * 日志拦截器
     */
    private fun initLogInterceptor(): Interceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }

    /**
     * 其他统一拦截器
     */
    private fun initInterceptor(context: Context) = Interceptor { chain ->
        val user = Store.store.getUser(context)
        val newBuilder = chain.request().newBuilder()
        user?.let {
            newBuilder.addHeader("uid", it.uid.toString())
        }
        val request = newBuilder
                .addHeader("appkey", "d5f17cafef0829b5")
                .addHeader("device", Build.MODEL)
                .addHeader("version", Utils.getVersionName(context))
                .addHeader("client", "android")
                .addHeader("system", Build.VERSION.RELEASE)
                .build()
        chain.proceed(request)
    }


    companion object {
        private var TAG = SoguApi::class.java.simpleName

        @SuppressLint("StaticFieldLeak")
        private var sApi: SoguApi? = null

        @Synchronized
       private fun getApi(ctx: Application): SoguApi {
            if (null == sApi) sApi = SoguApi(ctx)
            return sApi!!
        }

        fun <T> getService(ctx: Application, service: Class<T>): T {
            return getApi(ctx).getService(service)
        }
    }
}
