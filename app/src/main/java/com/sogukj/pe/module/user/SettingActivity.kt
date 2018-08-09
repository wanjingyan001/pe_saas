package com.sogukj.pe.module.user

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.edit
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.google.gson.Gson
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.auth.AuthService
import com.sogukj.pe.App
import com.sogukj.pe.Consts
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ActivityHelper
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.DownloadUtil
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.bean.MechanismBasicInfo
import com.sogukj.pe.database.Injection
import com.sogukj.pe.module.register.PhoneInputActivity
import com.sogukj.pe.module.register.UploadBasicInfoActivity
import com.sogukj.pe.peUtils.FileUtil
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.OtherService
import com.sogukj.pe.widgets.MyProgressBar
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_setting.*
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.startActivity
import java.io.File
import java.util.*

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
        checkUpdate.clickWithTrigger {
            checkUpdate()
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

    @SuppressLint("SetTextI18n")
    private fun checkUpdate() {
        val mDialog = MaterialDialog.Builder(this)
                .customView(R.layout.dialog_updated, false)
                .theme(Theme.LIGHT)
                .cancelable(false)
                .build()
        mDialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val icon = mDialog.find<ImageView>(R.id.updated_icon)
        val bar = mDialog.find<MyProgressBar>(R.id.update_progress)
        val title = mDialog.find<TextView>(R.id.update_title)
        val updateInfo = mDialog.find<TextView>(R.id.update_info)
        val scrollView = mDialog.find<ScrollView>(R.id.update_message_layout)
        val message = mDialog.find<TextView>(R.id.update_message)
        val update = mDialog.find<Button>(R.id.update)
        val prompt = mDialog.find<TextView>(R.id.update_prompt)
        title.text = "正在检测更新"
        updateInfo.visibility = View.GONE
        scrollView.visibility = View.GONE
        update.visibility = View.GONE
        prompt.visibility = View.VISIBLE
        if (isWifi()) {
            prompt.text = "当前处于Wi-Fi网络，请放心下载"
        } else {
            val hint = SpannableString("当前处于移动网络，下载将消耗流量")
            hint.setSpan(ForegroundColorSpan(Color.parseColor("#FF6174")), 9, hint.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            prompt.text = hint
        }
        RetrofitUrlManager.getInstance().putDomain("upgrade", Consts.HTTP_HOST)
        SoguApi.getService(application, OtherService::class.java).getVersion()
                .execute {
                    onNext { payload ->
                        payload.payload?.apply {
                            if (app_url.isNotEmpty()) {
                                when (force) {
                                    0 -> showSuccessToast("当前已是最新版本")
                                    else -> {
                                        mDialog.setCanceledOnTouchOutside(force == 1)
                                        updateInfo.visibility = View.VISIBLE
                                        update.visibility = View.VISIBLE
                                        title.text = "当前版本过低(" + Utils.getVersionName(context) + ")"
                                        updateInfo.text = "请立即升级到最新版($version)"
                                        scrollView.visibility = View.GONE
                                        message.text = info.toString()
                                        update.clickWithTrigger {
                                            icon.imageResource = R.drawable.update_icon1
                                            update.visibility = View.GONE
                                            title.text = "新版功能介绍"
                                            updateInfo.visibility = View.GONE
                                            scrollView.visibility = View.VISIBLE
                                            prompt.visibility = View.GONE
                                            bar.visibility = View.VISIBLE
//                                            val fileName = app_url.substring(app_url.lastIndexOf("/") + 1, app_url.indexOf("?"))
                                            val fileName = "Saas_${Utils.getVersionName(context)}_${Utils.getYMD(Date(System.currentTimeMillis()))}.apk"
                                            DownloadUtil.getInstance().download(app_url
                                                    , externalCacheDir.toString()
                                                    , fileName
                                                    , object : DownloadUtil.OnDownloadListener {
                                                override fun onDownloadSuccess(path: String?) {
                                                    bar.visibility = View.GONE
                                                    update.visibility = View.VISIBLE
                                                    prompt.visibility = View.VISIBLE
                                                    update.text = "立刻安装"
                                                    prompt.text = if (isWifi()) "已在Wi-Fi网络下完成下载" else "已在移动网络下完成下载"
                                                    update.clickWithTrigger {
                                                        mDialog.dismiss()
                                                        path?.let {
                                                            installAPK(it)
                                                        }
                                                    }
                                                }

                                                override fun onDownloading(progress: Int) {
                                                    bar.setProgress(progress)
                                                    mDialog.setCanceledOnTouchOutside(false)
                                                    mDialog.setCancelable(false)
                                                }

                                                override fun onDownloadFailed() {

                                                }

                                            })
                                        }
                                        mDialog.show()
                                    }
                                }
                            }
                        }
                    }
                }
    }

    private fun installAPK(path: String) {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.action = Intent.ACTION_VIEW
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            val uri = FileProvider.getUriForFile(context, FileUtil.getFileProvider(this), File(path))
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
        } else {
            intent.setDataAndType(Uri.fromFile(File(path)), "application/vnd.android.package-archive")
        }
        startActivity(intent)
    }
}
