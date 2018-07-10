package com.sogukj.pe.module.register

import android.os.Bundle
import androidx.core.content.edit
import com.alibaba.android.arouter.facade.annotation.Route
import com.amap.api.mapcore.util.it
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.SingleEditLayout
import com.sogukj.pe.interf.ReviewStatus
import com.sogukj.pe.module.main.LoginActivity
import com.sogukj.pe.module.main.MainActivity
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.RegisterService
import com.sogukj.service.SoguApi
import io.reactivex.internal.util.HalfSerializer.onNext
import kotlinx.android.synthetic.main.activity_register_vercode.*
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
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
                            payload.payload?.let {
                                it.domain_name?.let {
                                    sp.edit { putString(Extras.HTTPURL, it) }
                                    RetrofitUrlManager.getInstance().setGlobalDomain(it)
                                }
                                if (it.status == null) {
                                    startActivity<InvCodeInputActivity>(Extras.DATA to phone)
                                    finish()
                                } else {
                                    sp.edit { putString(Extras.CompanyKey, it.key) }
                                    sp.edit { putInt(Extras.SaasUserId, it.user_id!!) }

                                    when (it.is_finish) {
                                        0 -> {
                                            val status = when (it.status) {
                                                0 -> ReviewStatus.FAILURE_REVIEW
                                                2 -> ReviewStatus.SUCCESSFUL_REVIEW
                                                else -> {
                                                    ReviewStatus.UNDER_REVIEW
                                                }
                                            }
                                            startActivity<ReviewActivity>(Extras.BEAN to it,
                                                    Extras.DATA to status,
                                                    Extras.DATA2 to it.user_id)
                                        }
                                        1 -> {
                                            login(phone, it.user_id!!)
                                        }
                                    }
//                                    finish()
                                }
                            }
                        } else {
                            payload.message?.contains("验证码错误").takeIf {
                                showTopSnackBar("验证码错误")
                                return@takeIf true
                            }
                        }
                    }
                }
    }

    private fun login(phone: String, userId: Int) {
        SoguApi.getService(application, RegisterService::class.java).getUserBean(phone, userId)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            payload.payload?.let {
                                Store.store.setUser(this@VerCodeInputActivity, it)
                                startActivity<MainActivity>()
                            }
                        }
                    }
                }
    }
}
