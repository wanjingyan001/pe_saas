package com.sogukj.pe.module.register

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import androidx.core.content.edit
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
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.MechanismBasicInfo
import com.sogukj.pe.bean.MechanismInfo
import com.sogukj.pe.bean.RegisterVerResult
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.interf.ReviewStatus
import com.sogukj.pe.module.main.MainActivity
import com.sogukj.pe.module.register.presenter.LoginPresenter
import com.sogukj.pe.module.register.presenter.LoginView
import com.sogukj.pe.peUtils.Store
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.android.synthetic.main.activity_select_company2.*
import kotlinx.android.synthetic.main.item_company_select_list.view.*
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.ctx
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity

class SelectCompanyActivity : BaseActivity(), LoginView {
    private val companys: List<RegisterVerResult>? by extraDelegate(Extras.LIST, null)
    private lateinit var listAdapter: RecyclerAdapter<RegisterVerResult>
    private val loginPresenter: LoginPresenter by lazy { LoginPresenter(application) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_company2)
        Utils.setWindowStatusBarColor(this, R.color.white)
        loginPresenter.loginView = this
        listAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            val item = _adapter.getView(R.layout.item_company_select_list, parent)
            object : RecyclerHolder<RegisterVerResult>(item) {
                override fun setData(view: View, data: RegisterVerResult, position: Int) {
                    view.companyName.isSelected = listAdapter.selectedPosition == position
                    view.companyName.text = data.mechanism_name
                }
            }
        }
        listAdapter.onItemClick = { v, p ->
            listAdapter.selectedPosition = p
            val result = listAdapter.dataList[p]
            judgeLoginProcess(result)
        }
        companyList.apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = listAdapter
        }
        companys?.let{
            listAdapter.refreshData(it)
        }
        back.clickWithTrigger {
            finish()
        }
    }


    private fun judgeLoginProcess(result: RegisterVerResult) {
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
                    CrashReport.putUserData(this@SelectCompanyActivity, Extras.HTTPURL, newBaseUtl)
                    RetrofitUrlManager.getInstance().setGlobalDomain(newBaseUtl)
                }
            }
            if (it.is_finish == null) {
                startActivity<InfoSupplementActivity>(Extras.DATA to it.phone)
            } else {
                sp.edit { putString(Extras.CompanyKey, it.key) }
                sp.edit { putInt(Extras.SaasUserId, it.user_id!!) }
                CrashReport.putUserData(this@SelectCompanyActivity, Extras.SaasUserId, it.user_id.toString())
                CrashReport.putUserData(this@SelectCompanyActivity, Extras.CompanyKey, it.key)
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

    override fun getCodeStart() {
    }

    override fun getCodeSuccess(phone: String) {
    }

    override fun verificationCodeSuccess(result: RegisterVerResult) {
    }

    override fun verificationCompanyCodeSuccess(result: List<RegisterVerResult>) {
    }

    override fun verificationCodeFail() {
    }

    override fun getUserBean(user: UserBean) {
        AnkoLogger("SAAS用户").info { user.jsonStr }
        Store.store.setUser(this, user)
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

    override fun getCompanyInfoSuccess(info: MechanismBasicInfo) {
        sp.edit { putString(Extras.SAAS_BASIC_DATA, info.jsonStr) }
        sp.edit { putInt(Extras.main_flag, info.homeCardFlag ?: 1) }
        startActivity<MainActivity>()
        finish()
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
