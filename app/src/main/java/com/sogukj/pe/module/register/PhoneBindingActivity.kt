package com.sogukj.pe.module.register

import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import androidx.core.content.edit
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.google.gson.internal.LinkedTreeMap
import com.jakewharton.rxbinding2.widget.RxTextView
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.auth.LoginInfo
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.StatusBarUtil
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
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.RegisterService
import com.sogukj.service.SoguApi
import com.tencent.bugly.crashreport.CrashReport
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_phone_binding.*
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.ctx
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity
import java.util.*

class PhoneBindingActivity : BaseActivity(), LoginView {
    private val thirdId: String by ExtrasDelegate(Extras.ID, "")
    private val loginPresenter: LoginPresenter by lazy { LoginPresenter(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_binding)
        StatusBarUtil.setTranslucentForCoordinatorLayout(this, 0)
        StatusBarUtil.setLightMode(this)
        loginPresenter.loginView = this
        phoneEdt.block = {
            mVerCodeInput.setText("")
        }
        delete.clickWithTrigger {
            mVerCodeInput.setText("")
        }
        mGetCode.clickWithTrigger {
            if (Utils.isMobileExact(phoneEdt.getInput())) {
                hasBanded(phoneEdt.getInput())
            } else {
                showErrorToast("手机号格式有误")
            }
        }

        val inputList = ArrayList<Observable<CharSequence>>()
        inputList.add(RxTextView.textChanges(phoneEdt.getEditText()))
        inputList.add(RxTextView.textChanges(mVerCodeInput))
        Observable.combineLatest(inputList) { strArray ->
            delete.setVisible((strArray[1] as CharSequence).isNotEmpty())
            strArray.any { (it as CharSequence).isEmpty() || it.length < 6 }
        }.subscribe {
            it.no {
                loginPresenter.verificationCompanyCode(phoneEdt.getInput(), mVerCodeInput.textStr)
            }
        }
    }

    private fun hasBanded(phone: String) {
        val sp = PreferenceManager.getDefaultSharedPreferences(application)
        var source = ""
        sp.getString(Extras.THIRDLOGIN, "").apply {
            isNotEmpty().yes {
                source = split("_")[0]
            }
        }
        source.isNotEmpty().yes {
            SoguApi.getService(application, RegisterService::class.java)
                    .hasBanded(source, phone)
                    .execute {
                        onNext { payload ->
                            payload.isOk.yes {
                                payload.payload?.let {
                                    it as LinkedTreeMap<*, *>
                                    val hasBand = it["band"] as Number //1表示已绑定  0  表示未绑定
                                    if (hasBand.toInt() == 0) {
                                        loginPresenter.sendPhoneInput(phoneEdt.getInput())
                                    } else {
                                        MaterialDialog.Builder(this@PhoneBindingActivity)
                                                .theme(Theme.LIGHT)
                                                .content("该手机号已绑定过第三方账号,是否解绑并重新绑定")
                                                .positiveText("确定")
                                                .negativeText("取消")
                                                .onPositive { dialog, which ->
                                                    dialog.dismiss()
                                                    loginPresenter.sendPhoneInput(phoneEdt.getInput())
                                                }
                                                .onNegative { dialog, which ->
                                                    dialog.dismiss()
                                                    phoneEdt.getEditText().setText("")
                                                }.show()
                                    }
                                }
                            }.otherWise {
                                showErrorToast(payload.message)
                            }
                        }
                    }
        }
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

    override fun verificationCompanyCodeSuccess(result: List<RegisterVerResult>) {
        if(result.size == 1){
            verificationCodeSuccess(result[0])
        }else{
            startActivity<SelectCompanyActivity>(Extras.LIST to result)
        }
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
                    CrashReport.putUserData(this@PhoneBindingActivity, Extras.HTTPURL, newBaseUtl)
                    RetrofitUrlManager.getInstance().setGlobalDomain(newBaseUtl)
                }
            }
            if (it.is_finish == null) {
                startActivity<InfoSupplementActivity>(Extras.DATA to it.phone)
            } else {
                sp.edit { putString(Extras.CompanyKey, it.key) }
                sp.edit { putInt(Extras.SaasUserId, it.user_id!!) }
                CrashReport.putUserData(this@PhoneBindingActivity, Extras.SaasUserId, it.user_id.toString())
                CrashReport.putUserData(this@PhoneBindingActivity, Extras.CompanyKey, it.key)
                when (it.is_finish) {
                    0 -> {
                        if (it.mechanism_name.isNullOrEmpty()) {
                            startActivity<InfoSupplementActivity>(Extras.DATA to it.phone)
                        } else {
                            if (it.business_card.isNullOrEmpty()) {
                                val isAdmin = it.is_admin != 2
                                val info = MechanismInfo(it.mechanism_name, it.scale, it.business_card, it.name, it.position, it.key?:"")
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
        Store.store.setUser(this@PhoneBindingActivity, user)
        ifNotNull(user.accid, user.token) { accid, token ->
            IMLogin(accid, token)
        }
        sp.getString(Extras.CompanyKey, "").let {
            it.isNotEmpty().yes {
                loginPresenter.getCompanyInfo(it)
            }
        }
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

    override fun getCompanyInfoSuccess(info: MechanismBasicInfo) {
        sp.edit { putString(Extras.SAAS_BASIC_DATA, info.jsonStr) }
        sp.edit { putInt(Extras.main_flag, info.homeCardFlag ?: 1) }
        startActivity<MainActivity>()
//        startActivity<SelectCompanyAvtivity>()
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
