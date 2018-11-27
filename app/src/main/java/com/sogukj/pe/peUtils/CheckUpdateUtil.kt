package com.sogukj.pe.peUtils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.sogukj.pe.App
import com.sogukj.pe.Consts
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.isWifi
import com.sogukj.pe.baselibrary.utils.DownloadUtil
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.peExtended.RootPath
import com.sogukj.pe.peUtils.ToastUtil.Companion.showSuccessToast
import com.sogukj.pe.service.OtherService
import com.sogukj.pe.widgets.MyProgressBar
import com.sogukj.service.SoguApi
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import java.io.File
import java.util.*

/**
 * Created by CH-ZH on 2018/8/18.
 */
class CheckUpdateUtil {
    companion object {
        @SuppressLint("SetTextI18n")
         fun checkUpdate(context: Context) {
            val mDialog = MaterialDialog.Builder(context)
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
            if (context.isWifi()) {
                prompt.text = "当前处于Wi-Fi网络，请放心下载"
            } else {
                val hint = SpannableString("当前处于移动网络，下载将消耗流量")
                hint.setSpan(ForegroundColorSpan(Color.parseColor("#FF6174")), 9, hint.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                prompt.text = hint
            }
            RetrofitUrlManager.getInstance().putDomain("upgrade", RootPath)
            SoguApi.getService(App.INSTANCE, OtherService::class.java).getVersion()
                    .execute {
                        onNext { payload ->
                            payload.payload?.apply {
                                if (app_url.isNotEmpty()) {
                                    when (force) {
                                        0 -> showSuccessToast("当前已是最新版本",context)
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
                                                        , context.externalCacheDir.toString()
                                                        , fileName
                                                        , object : DownloadUtil.OnDownloadListener {
                                                    override fun onDownloadSuccess(path: String?) {
                                                        bar.visibility = View.GONE
                                                        update.visibility = View.VISIBLE
                                                        prompt.visibility = View.VISIBLE
                                                        update.text = "立刻安装"
                                                        prompt.text = if (context.isWifi()) "已在Wi-Fi网络下完成下载" else "已在移动网络下完成下载"
                                                        update.clickWithTrigger {
                                                            mDialog.dismiss()
                                                            path?.let {
                                                                installAPK(it,context)
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

        fun installAPK(path: String,context: Context) {
            val intent = Intent()
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.action = Intent.ACTION_VIEW
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                val uri = FileProvider.getUriForFile(context, FileUtil.getFileProvider(context), File(path))
                intent.setDataAndType(uri, "application/vnd.android.package-archive")
            } else {
                intent.setDataAndType(Uri.fromFile(File(path)), "application/vnd.android.package-archive")
            }
            context.startActivity(intent)
        }
    }
}