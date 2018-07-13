package com.sogukj.pe.module.register

import android.os.Bundle
import androidx.core.content.edit
import com.alibaba.android.arouter.facade.annotation.Route
import com.amap.api.mapcore.util.it
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.jsonStr
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.SingleEditLayout
import com.sogukj.pe.bean.MechanismInfo
import com.sogukj.pe.interf.ReviewStatus
import com.sogukj.pe.module.main.LoginActivity
import com.sogukj.pe.module.main.MainActivity
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.RegisterService
import com.sogukj.service.SoguApi
import io.reactivex.internal.util.HalfSerializer.onNext
import kotlinx.android.synthetic.main.activity_register_vercode.*
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
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
                                    if (it.isNotEmpty()) {
                                        val newBaseUtl: String = if (!it.startsWith("http://")) {
                                            "http://$it"
                                        } else {
                                            it
                                        }
                                        sp.edit { putString(Extras.HTTPURL, newBaseUtl) }
                                        RetrofitUrlManager.getInstance().setGlobalDomain(newBaseUtl)
                                    }
                                }
                                if (it.is_finish == null) {
                                    startActivity<InvCodeInputActivity>(Extras.DATA to phone)
                                } else {
                                    sp.edit { putString(Extras.CompanyKey, it.key) }
                                    sp.edit { putInt(Extras.SaasUserId, it.user_id!!) }
                                    when (it.is_finish) {
                                        0 -> {
                                            if (it.mechanism_name.isNullOrEmpty()) {
                                                startActivity<InvCodeInputActivity>(Extras.DATA to phone)
                                            } else {
                                                if (it.business_card.isNullOrEmpty()) {
                                                    val isAdmin = it.is_admin != 1
                                                    val info = MechanismInfo(it.mechanism_name, it.scale, it.business_card, it.name, it.position, it.key)
                                                    startActivity<InfoSupplementActivity>(Extras.DATA to phone
                                                            , Extras.DATA2 to info
                                                            , Extras.FLAG to isAdmin
                                                            , Extras.ID to it.user_id.toString())
                                                } else {
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
                                            }
                                        }
                                        1 -> {
                                            login(phone, it.user_id!!)
                                        }
                                    }
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
        showProgress("正在获取数据...")
        SoguApi.getService(application, RegisterService::class.java).getUserBean(phone, userId)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            payload.payload?.let {
                                AnkoLogger("SAAS用户").info { it.jsonStr }
                                Store.store.setUser(this@VerCodeInputActivity, it)
                                startActivity<MainActivity>()
                            }
                        } else {
                            hideProgress()
                            showTopSnackBar(payload.message)
                        }
                    }
                    onError { e ->
                        hideProgress()
                        Trace.e(e)
                    }
                }
    }

    override fun onDestroy() {
        super.onDestroy()
        hideProgress()
    }
}
