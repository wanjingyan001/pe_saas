package com.sogukj.pe.module.main

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nimlib.sdk.NIMClient
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.StatusBarUtil
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.module.register.PhoneInputActivity
import com.sogukj.pe.peExtended.getEnvironment
import com.sogukj.pe.peUtils.Store
import kotlinx.android.synthetic.main.activity_splash.*
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import me.leolin.shortcutbadger.ShortcutBadger
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.startActivity

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
            "civc" -> {
                splash_bg.imageResource = R.drawable.img_logo_splash_zd
            }
            "ht" -> {
                splash_bg.imageResource = R.drawable.img_logo_splash_ht
                val params = splash_bg.layoutParams as FrameLayout.LayoutParams
                params.setMargins(0, 0, 0, Utils.dpToPx(this, 40))
                splash_bg.layoutParams = params
            }
            "kk" -> {
                splash_bg.imageResource = R.drawable.img_logo_splash_kk
                val params = splash_bg.layoutParams as FrameLayout.LayoutParams
                params.setMargins(0, 0, 0, Utils.dpToPx(this, 40))
                splash_bg.layoutParams = params
            }
            "yge" -> {
                splash_bg.imageResource = R.drawable.img_logo_splash_yge
                val params = splash_bg.layoutParams as FrameLayout.LayoutParams
                params.setMargins(0, 0, 0, Utils.dpToPx(this, 40))
                splash_bg.layoutParams = params
            }
            "sr"->{
                splash_bg.imageResource = R.drawable.img_logo_splash_sr
                val params = splash_bg.layoutParams as FrameLayout.LayoutParams
                params.setMargins(0, 0, 0, Utils.dpToPx(this, 40))
                splash_bg.layoutParams = params
            }
            else -> {
                splash_bg.imageResource = R.drawable.img_logo_splash
            }
        }
    }

    override fun onResume() {
        super.onResume()

        handler.postDelayed({
//            || (NIMClient.getStatus().shouldReLogin() && NimUIKit.getAccount().isNullOrEmpty())
            if (!Store.store.checkLogin(this) ) {
                if (sp.getBoolean(Extras.isFirstEnter,true)) {
                    startActivity<GuideActivity>()
                }else{
                    startActivity<PhoneInputActivity>()
                }
//                LoginActivity.start(this)
                finish()
            } else {
                val url = sp.getString(Extras.HTTPURL, "")
                if (url.isNotEmpty()){
                    RetrofitUrlManager.getInstance().setGlobalDomain(url)
                }
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }, 500)
    }
}
