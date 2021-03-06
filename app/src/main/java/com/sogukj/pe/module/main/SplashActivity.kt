package com.sogukj.pe.module.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.core.content.edit
import com.sogukj.pe.Consts
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.jsonStr
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.StatusBarUtil
import com.sogukj.pe.module.register.LoginActivity
import com.sogukj.pe.peExtended.RootPath
import com.sogukj.pe.peExtended.getEnvironment
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.FileFindService
import com.sogukj.pe.service.RegisterService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_splash.*
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import me.leolin.shortcutbadger.ShortcutBadger
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startService

/**
 * Created by qinfei on 17/8/11.
 */
class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        StatusBarUtil.setTransparent(this)
        ShortcutBadger.removeCount(this)
        when (getEnvironment()) {
            "zgh" -> {
                splash_bg.imageResource = R.mipmap.img_logo_splash_zh
            }
            else -> {
                splash_bg.imageResource = R.mipmap.img_logo_splash
                val params = splash_bg.layoutParams as FrameLayout.LayoutParams
                params.width = FrameLayout.LayoutParams.MATCH_PARENT
                params.height = FrameLayout.LayoutParams.MATCH_PARENT
                splash_bg.layoutParams = params
                params.setMargins(0, 0, 0, 0)
            }
        }

        //todo WX之后要移除
        RetrofitUrlManager.getInstance().putDomain(Consts.WX, RootPath)

        RetrofitUrlManager.getInstance().putDomain(Consts.CREDIT_COLLECTION, RootPath)

        RetrofitUrlManager.getInstance().putDomain(Consts.DEV, Consts.NEW_DEV)
        saveDzhToken()
        getCompanyInfo()

        startService<FileFindService>()
    }

    override fun onResume() {
        super.onResume()

        handler.postDelayed({
            //            || (NIMClient.getStatus().shouldReLogin() && NimUIKit.getAccount().isNullOrEmpty())
            if (!Store.store.checkLogin(this)) {
                if (sp.getBoolean(Extras.isFirstEnter, true)) {
                    startActivity<GuideActivity>()
                } else {
                    startActivity<LoginActivity>()
                }
//                LoginActivity.start(this)
                finish()
            } else {
                val url = sp.getString(Extras.HTTPURL, "")
                if (url.isNotEmpty()) {
                    RetrofitUrlManager.getInstance().setGlobalDomain(url)
                }
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }, 500)
    }

    private fun saveDzhToken() {
        SoguApi.getDzhHttp(application).getDzhToken(Extras.DZH_APP_ID, Extras.DZH_SECRET_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ t ->
                    if (null != t && null != t.token) {
                        Log.e("TAG", "token ==" + t.token)
                        Store.store.setDzhToken(this, t.token)
                    }
                }, {
                    it.printStackTrace()
                })
    }

    private fun getCompanyInfo() {
        val key = sp.getString(Extras.CompanyKey, "")
        if (key.isNotEmpty()) {
            SoguApi.getService(application, RegisterService::class.java)
                    .getBasicInfo(key)
                    .execute {
                        onNext { payload ->
                            if (payload.isOk) {
                                payload.payload?.let {
                                    sp.edit { putString(Extras.SAAS_BASIC_DATA, it.jsonStr) }
                                    sp.edit { putInt(Extras.main_flag, it.homeCardFlag ?: 1) }
                                }
                            }
                        }
                    }
        }
    }
}
