package com.sogukj.pe.service.socket

import android.content.Context
import com.sogukj.pe.peUtils.Store
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created by CH-ZH on 2018/8/30.
 */
class DzhInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain?): Response {
        var newRequest = chain!!.request()
        if (null != Store.store.getDzhToken(ctx as Context) && !("".equals(Store.store.getDzhToken(ctx as Context)))){
            newRequest = chain.request().newBuilder().addHeader("REQ-TOKEN", Store.store.getDzhToken(ctx as Context)).build()
        }else{
            newRequest = chain.request().newBuilder().build()
        }

        return chain.proceed(newRequest)
    }

    companion object {
       private var ctx : Context? = null
        fun newInstance(ctx : Context):DzhInterceptor{
            val instance = DzhInterceptor()
            this.ctx = ctx
            return instance
        }
    }
}