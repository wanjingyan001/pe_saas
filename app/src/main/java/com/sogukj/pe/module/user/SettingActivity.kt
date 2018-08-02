package com.sogukj.pe.module.user

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.content.edit
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.auth.AuthService
import com.sogukj.pe.App
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.fromJson
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.ActivityHelper
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.bean.MechanismBasicInfo
import com.sogukj.pe.database.Injection
import com.sogukj.pe.module.register.PhoneInputActivity
import com.sogukj.pe.module.register.UploadBasicInfoActivity
import com.sogukj.pe.peUtils.Store
import kotlinx.android.synthetic.main.activity_setting.*
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity

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
                RetrofitUrlManager.getInstance().removeGlobalDomain()
                sp.edit { putString(Extras.HTTPURL, "") }
                App.INSTANCE.resetPush(false)
                IMLogout()
                Store.store.clearUser(this)
                ActivityHelper.exit(App.INSTANCE)
                startActivity<PhoneInputActivity>()
                doAsync {
                    Injection.provideFunctionSource(this@SettingActivity).delete()
                }
                finish()
            }
            dialog.show()
        }
        val user = Store.store.getUser(this)
        createDep.setVisible((user?.is_admin == 1) or (user?.is_admin == 2))
        createDep.clickWithTrigger {
            user?.let {
                val company = sp.getString(Extras.SAAS_BASIC_DATA, "")
                val detail = Gson().fromJson<MechanismBasicInfo?>(company)
                detail?.mechanism_name?.let {
                    startActivity<UploadBasicInfoActivity>(Extras.NAME to it,
                            Extras.CODE to user.phone,
                            Extras.FLAG to true)
                }
            }
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
