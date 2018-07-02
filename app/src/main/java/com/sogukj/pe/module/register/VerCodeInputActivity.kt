package com.sogukj.pe.module.register

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.SingleEditLayout
import com.sogukj.pe.service.RegisterService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_register_vercode.*
import org.jetbrains.anko.startActivity

class VerCodeInputActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_vercode)
        Utils.setWindowStatusBarColor(this, R.color.white)
        verCodeLayout.setFinishListener(object : SingleEditLayout.InputFinish {
            override fun finish(isFinish: Boolean, verCode: String) {
                nextStep.isEnabled = isFinish
            }
        })
        val phone = intent.getStringExtra(Extras.DATA)
        nextStep.clickWithTrigger {
            verificationCode(phone, verCodeLayout.getCompleteInput())
        }
    }

    private fun verificationCode(phone: String, verCode: String) {
        SoguApi.getService(application, RegisterService::class.java).verifyCode(phone, verCode)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            startActivity<InvCodeInputActivity>(Extras.DATA to phone)
                        }else{
                            showTopSnackBar(payload.message!!)
                        }
                    }
                }
    }
}
