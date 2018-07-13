package com.sogukj.pe.module.register

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.service.notification.StatusBarNotification
import android.support.constraint.ConstraintLayout
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.widget.PopupWindowCompat
import android.view.Gravity
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RemoteViews
import androidx.core.content.edit
import com.alibaba.android.arouter.facade.annotation.Route
import com.app.hubert.guide.NewbieGuide
import com.app.hubert.guide.model.GuidePage
import com.app.hubert.guide.model.HighLight
import com.jakewharton.rxbinding2.widget.RxTextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.base.ActivityHelper
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.service.Payload
import com.sogukj.pe.service.RegisterService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_phone_input.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity

/**
 * 注册--手机号输入界面
 */
@Route(path = "/register/main")
class PhoneInputActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_input)
        Utils.setWindowStatusBarColor(this, R.color.white)
        RxTextView.textChanges(phoneEdt.getEditText())
                .subscribe({ t ->
                    nextStep.isEnabled = !t.isNullOrEmpty()
                })
        nextStep.clickWithTrigger {
            if (Utils.isMobileExact(phoneEdt.getInput())) {
                sendPhoneInput(phoneEdt.getInput())
            } else {
                showTopSnackBar("手机号格式有误")
            }
        }
        ActivityHelper.finishAllWithoutTop()
    }


    private fun initGuideView() {
        NewbieGuide.with(this)
                .setLabel("Register")
                .addGuidePage(GuidePage.newInstance()
                        .addHighLight(nextStep, HighLight.Shape.ROUND_RECTANGLE)
                        .setLayoutRes(R.layout.layout_guide))
                .show()
    }

    private fun initNotice() {
        val topWindow = PopupWindow(this)
        val notice = layoutInflater.inflate(R.layout.layout_top_window, null)
        topWindow.contentView = notice
        topWindow.isFocusable = false
        topWindow.setBackgroundDrawable(ColorDrawable(0x00000000))
        topWindow.animationStyle = R.style.TopWindowAnimStyle
        topWindow.isOutsideTouchable = true
        topWindow.width = ViewGroup.LayoutParams.MATCH_PARENT
        topWindow.height = ViewGroup.LayoutParams.WRAP_CONTENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            topWindow.elevation = dip(8).toFloat()
        }
        val parent = find<ConstraintLayout>(R.id.parentLayout)
        topWindow.showAsDropDown(parent, 0, 0, Gravity.TOP)
    }


    private fun sendPhoneInput(phone: String) {
        SoguApi.getService(application, RegisterService::class.java).sendVerCode(phone)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            sp.edit { putString(Extras.SaasPhone,phone) }
                            showSuccessToast("验证码已经发送，请查收")
                            startActivity<VerCodeInputActivity>(Extras.DATA to phone)
                            finish()
                        }
                    }
                }
    }
}
