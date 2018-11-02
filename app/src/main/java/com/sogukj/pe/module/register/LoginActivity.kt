package com.sogukj.pe.module.register

import android.os.Bundle
import android.os.Handler
import androidx.core.content.edit
import com.alibaba.android.arouter.facade.annotation.Route
import com.amap.api.mapcore.util.it
import com.google.gson.internal.LinkedTreeMap
import com.jakewharton.rxbinding2.widget.RxTextView
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.StatusCode
import com.netease.nimlib.sdk.auth.LoginInfo
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ActivityHelper
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.StatusBarUtil
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.bean.MechanismBasicInfo
import com.sogukj.pe.bean.MechanismInfo
import com.sogukj.pe.bean.RegisterVerResult
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.interf.ReviewStatus
import com.sogukj.pe.module.main.MainActivity
import com.sogukj.pe.module.register.presenter.LoginPresenter
import com.sogukj.pe.module.register.presenter.LoginView
import com.sogukj.pe.peUtils.LoginTimer
import com.sogukj.pe.peUtils.MobLogin
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.RegisterService
import com.sogukj.service.SoguApi
import com.tencent.bugly.crashreport.CrashReport
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_phone_input.*
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import org.jetbrains.anko.*
import java.util.*

/**
 * 注册--手机号输入界面
 */
@Route(path = "/register/main")
class LoginActivity : BaseActivity(), LoginView {
    private val loginPresenter: LoginPresenter by lazy { LoginPresenter(application) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_input)
        StatusBarUtil.setTranslucentForCoordinatorLayout(this, 0)
        StatusBarUtil.setLightMode(this)
        loginPresenter.loginView = this
        val inputList = ArrayList<Observable<CharSequence>>()
        inputList.add(RxTextView.textChanges(phoneEdt.getEditText()))
        inputList.add(RxTextView.textChanges(mVerCodeInput))
        Observable.combineLatest(inputList) { strArray ->
            delete.setVisible((strArray[1] as CharSequence).isNotEmpty())
            strArray.any { (it as CharSequence).isEmpty() || it.length < 6 }
        }.subscribe {
            it.no {
                loginPresenter.verificationCode(phoneEdt.getInput(), mVerCodeInput.textStr)
            }
        }

        mGetCode.clickWithTrigger {
            if (Utils.isMobileExact(phoneEdt.getInput())) {
                loginPresenter.sendPhoneInput(phoneEdt.getInput())
            } else {
                showErrorToast("手机号格式有误")
            }
        }
        QQLogin.clickWithTrigger {
            MobLogin.QQLogin(this) {
                sp.edit { putString(Extras.THIRDLOGIN, "qq_$it") }
                checkThirdBinding("qq", it)

            }
        }
        WeChatLogin.clickWithTrigger {
            MobLogin.WeChatLogin(this) {
                sp.edit { putString(Extras.THIRDLOGIN, "wechat_$it") }
                checkThirdBinding("wechat", it)
            }
        }
        phoneEdt.block = {
            mVerCodeInput.setText("")
        }
        delete.clickWithTrigger {
            mVerCodeInput.setText("")
        }

        val extra = intent.getSerializableExtra(Extras.FLAG) as? StatusCode
        extra?.let {
            when (extra) {
                StatusCode.KICKOUT, StatusCode.KICK_BY_OTHER_CLIENT -> showCustomToast(R.drawable.icon_toast_common, "您的帐号已在其他设备登陆，您已被迫下线")
                StatusCode.FORBIDDEN -> showCustomToast(R.drawable.icon_toast_common, "您的帐号已被禁止登录,请联系管理员")
            }
        }
        ActivityHelper.finishAllWithoutTop()
    }


    /**
     * 网易云信IM登录
     */
    private fun IMLogin(account: String, token: String) {
        val loginInfo = LoginInfo(account, token)
        NimUIKit.login(loginInfo, object : RequestCallback<LoginInfo> {
            override fun onSuccess(p0: LoginInfo?) {
                AnkoLogger("WJY").info { "登录成功:${p0?.account}===>${p0?.token}" }
                val xmlDb = XmlDb.open(ctx)
                xmlDb.set(Extras.NIMACCOUNT, account)
                xmlDb.set(Extras.NIMTOKEN, token)
//                NoticeService()
            }

            override fun onFailed(p0: Int) {
                if (p0 == 302 || p0 == 404) {
//                    showCustomToast(R.drawable.icon_toast_fail, "帐号或密码错误")
                } else {
//                    showCustomToast(R.drawable.icon_toast_fail, "登录失败")
                }
            }

            override fun onException(p0: Throwable?) {
//                showCustomToast(R.drawable.icon_toast_common, "无效输入")
            }
        })
    }

    private fun checkThirdBinding(source: String, unique: String) {
        SoguApi.getService(application, RegisterService::class.java)
                .checkThirdBinding(source, unique)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                when (it.band) {
                                    0 -> {
                                        startActivity<PhoneBindingActivity>(Extras.ID to unique)
                                    }
                                    1 -> {
                                        it.user_info?.let { user ->
                                            Store.store.setUser(this@LoginActivity, user)
                                            ifNotNull(user.accid, user.token, { accid, token ->
                                                IMLogin(accid, token)
                                            })
                                            sp.getString(Extras.CompanyKey, "").let {
                                                it.isNotEmpty().yes {
                                                    loginPresenter.getCompanyInfo(it)
                                                }
                                            }
                                        }
                                    }
                                    else -> {
                                        showErrorToast("数据错误")
                                    }
                                }
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }

    private fun NoticeService() {
        SoguApi.getService(application, RegisterService::class.java)
                .NoticeService()
                .execute { }
    }

    override fun getCodeStart() {
        Timer().scheduleAtFixedRate(LoginTimer(60, Handler(), mGetCode), 0, 1000)
    }

    override fun getCodeSuccess(phone: String) {
        sp.edit { putString(Extras.SaasPhone, phone) }
        showSuccessToast("验证码已经发送，请查收")
        mVerCodeInput.isFocusable = true//设置输入框可聚集
        mVerCodeInput.isFocusableInTouchMode = true//设置触摸聚焦
        mVerCodeInput.requestFocus()//请求焦点
        mVerCodeInput.findFocus()//获取焦点
    }

    override fun verificationCodeSuccess(result: RegisterVerResult) {
        result.let {
            it.domain_name?.let {
                if (it.isNotEmpty()) {
                    val newBaseUtl: String = if (!it.startsWith("http://")) {
                        if (it.startsWith("https://")) {
                            it
                        } else {
                            "http://$it"
                        }
                    } else {
                        it
                    }
                    sp.edit { putString(Extras.HTTPURL, newBaseUtl) }
                    CrashReport.putUserData(this@LoginActivity, Extras.HTTPURL, newBaseUtl)
                    RetrofitUrlManager.getInstance().setGlobalDomain(newBaseUtl)
                }
            }
            if (it.is_finish == null) {
                startActivity<InfoSupplementActivity>(Extras.DATA to it.phone)
            } else {
                sp.edit { putString(Extras.CompanyKey, it.key) }
                sp.edit { putInt(Extras.SaasUserId, it.user_id!!) }
                CrashReport.putUserData(this@LoginActivity, Extras.SaasUserId, it.user_id.toString())
                CrashReport.putUserData(this@LoginActivity, Extras.CompanyKey, it.key)
                when (it.is_finish) {
                    0 -> {
                        if (it.mechanism_name.isNullOrEmpty()) {
                            startActivity<InfoSupplementActivity>(Extras.DATA to it.phone)
                        } else {
                            if (it.business_card.isNullOrEmpty()) {
                                val isAdmin = it.is_admin != 2
                                val info = MechanismInfo(it.mechanism_name, it.scale, it.business_card, it.name, it.position, it.key)
                                startActivity<InfoSupplementActivity>(Extras.DATA to it.phone
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
                        showProgress("正在获取数据...")
                        loginPresenter.getUserBean(it.phone, it.user_id!!)
                    }
                }
            }

        }
    }

    override fun verificationCodeFail() {
        RetrofitUrlManager.getInstance().clearAllDomain()
        showErrorToast("验证码错误")
        mVerCodeInput.setText("")
    }

    override fun getUserBean(user: UserBean) {
        AnkoLogger("SAAS用户").info { user.jsonStr }
        Store.store.setUser(this@LoginActivity, user)
        ifNotNull(user.accid, user.token, { accid, token ->
            IMLogin(accid, token)
        })
        sp.getString(Extras.CompanyKey, "").let {
            it.isNotEmpty().yes {
                loginPresenter.getCompanyInfo(it)
            }
        }
    }

    override fun getCompanyInfoSuccess(info: MechanismBasicInfo) {
        sp.edit { putString(Extras.SAAS_BASIC_DATA, info.jsonStr) }
        sp.edit { putInt(Extras.main_flag, info.homeCardFlag ?: 1) }
        startActivity<MainActivity>()
    }

    override fun getInfoFinish() {
        hideProgress()
    }


    override fun onDestroy() {
        super.onDestroy()
        hideProgress()
        loginPresenter.loginView = null
    }
}
