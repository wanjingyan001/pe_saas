package com.sogukj.pe.module.user

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.auth.AuthService
import com.sogukj.pe.App
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.module.main.LoginActivity
import com.sogukj.pe.peUtils.Store
import kotlinx.android.synthetic.main.activity_setting.*
import org.jetbrains.anko.find

class SettingActivity : BaseActivity() {

    companion object {
        fun start(ctx: Context?) {
            ctx?.startActivity(Intent(ctx, SettingActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar_title.text = "设置中心"
        toolbar_back.clickWithTrigger {
            onBackPressed()
        }

        feedBack.clickWithTrigger {
            FeedBackActivity.start(this)
        }
        loginOut.clickWithTrigger {
            val inflate = LayoutInflater.from(this).inflate(R.layout.layout_input_dialog1, null)
            val dialog = MaterialDialog.Builder(this)
                    .customView(inflate, false)
                    .cancelable(true)
                    .build()
            dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val veto = inflate.find<TextView>(R.id.veto_comment)
            val confirm = inflate.find<TextView>(R.id.confirm_comment)
            val title = inflate.find<TextView>(R.id.approval_comments_title)
            title.text = "确定要退出此帐号?"
            veto.text = "取消"
            confirm.text = "确定"
            veto.clickWithTrigger {
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }
            confirm.clickWithTrigger {
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
                App.INSTANCE.resetPush(false)
                IMLogout()
                Store.store.clearUser(this)
                LoginActivity.start(this)
                finish()
            }
            dialog.show()
        }

    }

    /**
     * 网易云信IM注销
     */
    private fun IMLogout() {
        val xmlDb = XmlDb.open(this)
        xmlDb.set(Extras.NIMACCOUNT, "")
        xmlDb.set(Extras.NIMTOKEN, "")
        NimUIKit.logout()
        NIMClient.getService(AuthService::class.java).logout()
    }
}
