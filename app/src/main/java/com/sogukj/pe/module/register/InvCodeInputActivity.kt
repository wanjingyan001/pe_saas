package com.sogukj.pe.module.register

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.SingleEditLayout
import com.sogukj.pe.bean.MechanismInfo
import com.sogukj.pe.service.RegisterService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_register_invcode.*
import kotlinx.android.synthetic.main.white_toolbar.*
import org.jetbrains.anko.startActivity

class InvCodeInputActivity : ToolbarActivity(), SingleEditLayout.InputFinish {
    private lateinit var phone: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_invcode)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        title = ""
        setBack(true)
        phone = intent.getStringExtra(Extras.DATA)
        initJumpLink()
        invCodeLayout.setFinishListener(this)
        toolbar_back.clickWithTrigger { finish() }
        joinNow.clickWithTrigger {
            verificationInviteCode()
        }
    }

    private fun initJumpLink() {
        val link = resources.getString(R.string.jump_link)
        val spa = SpannableString(link)
        spa.setSpan(ClickSpann(this), 5, link.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        toCreateTeam.text = spa
        toCreateTeam.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun finish(isFinish: Boolean, verCode: String) {
        joinNow.isEnabled = isFinish
    }

    private fun verificationInviteCode() {
        SoguApi.getService(application, RegisterService::class.java).inviteCode(phone, invCodeLayout.getCompleteInput())
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            payload.payload?.let {
                                val info = MechanismInfo(it.mechanism_name, it.scale, null, null, null, it.key)
                                startActivity<InfoSupplementActivity>(Extras.DATA to phone
                                        , Extras.DATA2 to info
                                        , Extras.FLAG to true)
                            }
                        } else {
                            showTopSnackBar(payload.message)
                        }
                    }
                }
    }

    inner class ClickSpann(val context: Context) : ClickableSpan() {
        override fun onClick(widget: View?) {
            startActivity<InfoSupplementActivity>(Extras.DATA to phone)
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = context.resources.getColor(R.color.main_blue_color)
            ds.isUnderlineText = false
        }
    }

}
