package com.sogukj.service

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.auth.AuthService
import com.sogukj.pe.App
import com.sogukj.pe.Consts
import com.sogukj.pe.Extras
import com.sogukj.pe.baselibrary.base.ActivityHelper
import com.sogukj.pe.baselibrary.utils.EncryptionUtil
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.database.Injection
import com.sogukj.pe.module.register.LoginActivity
import com.sogukj.pe.peExtended.getEnvironment
import com.sogukj.pe.peExtended.getIntEnvironment
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.peUtils.ToastUtil
import com.sogukj.pe.service.DzhHttpUtils
import com.sogukj.pe.service.StaticHttpUtils
import com.sogukj.pe.service.socket.DzhInterceptor
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.startActivity
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by qinfei on 17/7/18.
 */
class SoguApi {
    private val context: Context
    private var retrofit: Retrofit
    private val environment = getIntEnvironment()
    private val dzhHttp: DzhHttpUtils
    private val staticHttp: StaticHttpUtils

    private constructor(context: Context) {
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
                .baseUrl(getHost())
//                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build()
        val newBaseUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(Extras.HTTPURL, "")
        if (newBaseUrl.isNotEmpty()) {
            RetrofitUrlManager.getInstance().setGlobalDomain(newBaseUrl)
        }

        val dzhRetrofit = Retrofit.Builder()
                .baseUrl(Consts.DZH_HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(getDzhClient())
                .build()

        dzhHttp = dzhRetrofit.create(DzhHttpUtils::class.java)

        val statusRetrifit = Retrofit.Builder()
                .baseUrl(getHost())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(getStatusClient())
                .build()
        staticHttp = statusRetrifit.create(StaticHttpUtils::class.java)
    }

    private fun getDzhClient(): OkHttpClient? {
        return OkHttpClient.Builder()
                .addInterceptor(DzhInterceptor.newInstance(context))
                .readTimeout(100, TimeUnit.SECONDS)
                .connectTimeout(100, TimeUnit.SECONDS)
                .build()
    }

    private fun getStatusClient(): OkHttpClient? {
        return OkHttpClient.Builder()
                .addInterceptor(initLogInterceptor())
                .addInterceptor(initInterceptor(context))
                .retryOnConnectionFailure(true)
                .readTimeout(100, TimeUnit.SECONDS)
                .connectTimeout(100, TimeUnit.SECONDS)
                .build()
    }

    private fun getHost(): String {
        var host = Consts.HTTP_HOST
        when (environment) {
            0 -> {
                //dev
                host = Consts.DEV_HTTP_HOST
            }
            1 -> {
                //online
                host = Consts.HTTP_HOST
            }
        }
        return host
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
        val timestamp = System.currentTimeMillis()
        val key = PreferenceManager.getDefaultSharedPreferences(context).getString(Extras.CompanyKey, "")
        val newBuilder = chain.request().newBuilder()
        user?.let {
            newBuilder.addHeader("uid", it.uid.toString())
            newBuilder.addHeader("uids", it.uids)
            it.table_token?.let {
                newBuilder.addHeader("table_token", it)
            }
        }
        if (getEnvironment() == "sr") {
            newBuilder.addHeader("key", "d5f17cafef0829b5")
        } else {
            key.isNotEmpty().takeIf {
                newBuilder.addHeader("key", key)
                return@takeIf true
            }
        }
        val request = newBuilder
                .addHeader("appkey", "d5f17cafef0829b5")
                .addHeader("device", Build.MODEL)
                .addHeader("version", Utils.getVersionName(context))
                .addHeader("client", "android")
                .addHeader("system", Build.VERSION.RELEASE)
                .addHeader("timestamp", timestamp.toString())
                .addHeader("sign", EncryptionUtil.getSign(EncryptionUtil.getSign(user?.let { it.app_token }
                        + Extras.SIGN_CODE) + timestamp))
                .build()
//        val response = chain.proceed(request)
//        response
        val headers = request.headers()
        headers.names().forEachIndexed { index, s ->
            Log.e("OkHttp", "name:$s===>value:${headers.get(s)}")
        }
        val response = chain.proceed(request)
        val mediaType = response.body()!!.contentType()
        val content = response.body()!!.string()
        try {
            val jsonObject = JSONObject(content)
            val msgNo = jsonObject.getString("msgNo")
            if (msgNo == "1001") {
                ToastUtil.showErrorToast("签名过期,请重新登录",context)
                //签名过期
                exitUser()
            }
        } catch (e: Exception) {
            Log.e("OkHttp", content)
            e.printStackTrace()
        }
        response.newBuilder().body(ResponseBody.create(mediaType, content)).build()
    }

    fun exitUser() {
        context.startActivity<LoginActivity>()
        Store.store.clearUser(context)
        RetrofitUrlManager.getInstance().removeGlobalDomain()
        Store.store.setRootUrl(context, "")
        App.INSTANCE.resetPush(false)
        IMLogout()
        ActivityHelper.exit(App.INSTANCE)
        doAsync {
            Injection.provideFunctionSource(context).delete()
        }

    }

    /**
     * 网易云信IM注销
     */
    private fun IMLogout() {
        val xmlDb = XmlDb.open(context)
        xmlDb.set(Extras.NIMACCOUNT, "")
        xmlDb.set(Extras.NIMTOKEN, "")
        NimUIKit.logout()
        NIMClient.getService(AuthService::class.java).logout()
    }

    companion object {
        private var TAG = SoguApi::class.java.simpleName

        @SuppressLint("StaticFieldLeak")
        private var sApi: SoguApi? = null

        @Synchronized
        private fun getApi(ctx: Context): SoguApi {
            if (null == sApi) sApi = SoguApi(ctx)
            return sApi!!
        }

        fun <T> getService(ctx: Context, service: Class<T>): T {
            return getApi(ctx).getService(service)
        }

        fun getDzhHttp(ctx: Application?): DzhHttpUtils {
            if (null == ctx) {
                throw NullPointerException("context can't be null")
            }
            return getApi(ctx).dzhHttp
        }

        fun getStaticHttp(ctx: Application?): StaticHttpUtils {
            if (null == ctx) {
                throw NullPointerException("context can't be null")
            }
            return getApi(ctx).staticHttp
        }
    }
}
