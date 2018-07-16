package com.sogukj.pe.interceptor

import android.content.Context
import android.os.Bundle
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.annotation.Interceptor
import com.alibaba.android.arouter.facade.callback.InterceptorCallback
import com.alibaba.android.arouter.facade.template.IInterceptor
import com.alibaba.android.arouter.launcher.ARouter
import com.google.gson.Gson
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.Extras
import com.sogukj.pe.baselibrary.Extended.jsonStr
import com.sogukj.pe.bean.CustomSealBean
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

/**
 * Created by admin on 2018/6/26.
 */
@Interceptor(priority = 5)
class ProjectInterceptor : IInterceptor, AnkoLogger {
    private lateinit var mContext: Context

    override fun process(postcard: Postcard, callback: InterceptorCallback) {
//        && postcard.tag == Extras.ROUTH_FLAG
        val bundle = postcard.extras
        if (postcard.path.contains("/main/bookList")) {
            val name = postcard.path.substring(postcard.path.lastIndexOf("/") + 1, postcard.path.length)
            ARouter.getInstance()
                    .build(ARouterPath.CompanySelectActivity)
                    .withString(Extras.ROUTE_PATH, postcard.path)
                    .withString(Extras.NAME,name)
                    .navigation()
            callback.onInterrupt(null)
        } else if ((postcard.path.startsWith("/project") || postcard.path.startsWith("/fund")) && bundle.getInt(Extras.FLAG) == Extras.ROUTH_FLAG) {
            ARouter.getInstance()
                    .build(ARouterPath.CompanySelectActivity)
                    .withString(Extras.ROUTE_PATH, postcard.path)
                    .navigation()
            callback.onInterrupt(null)
        } else {
            callback.onContinue(postcard)
        }
    }

    override fun init(context: Context) {
        mContext = context
    }
}