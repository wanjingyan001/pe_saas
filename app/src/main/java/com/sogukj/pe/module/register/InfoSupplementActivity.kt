package com.sogukj.pe.module.register

import android.content.Intent
import android.os.Bundle
import androidx.core.content.edit
import com.jakewharton.rxbinding2.widget.RxTextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.StatusBarUtil
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.MechanismInfo
import com.sogukj.pe.bean.TeamInfoSupplementReq
import com.sogukj.pe.interf.ReviewStatus
import com.sogukj.pe.module.main.MainActivity
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.RegisterService
import com.sogukj.service.SoguApi
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_register_info_supplement.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivity
import kotlin.properties.Delegates

class InfoSupplementActivity : ToolbarActivity() {

    private lateinit var phone: String
    private var index: Int by Delegates.notNull()
    private var mechanismInfo: MechanismInfo? = null
    private var flag: Boolean = false
    private var user_id: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_info_supplement)
        StatusBarUtil.setTranslucentForCoordinatorLayout(this, 0)
        StatusBarUtil.setLightMode(this)
        toolbar?.setBackgroundColor(resources.getColor(R.color.transparent))
        setBack(true)
        phone = intent.getStringExtra(Extras.DATA)
        mechanismInfo = intent.getParcelableExtra(Extras.DATA2)
        flag = intent.getBooleanExtra(Extras.FLAG, false)
        user_id = intent.getStringExtra(Extras.ID)
        val inputList = ArrayList<Observable<CharSequence>>()
        inputList.add(RxTextView.textChanges(organNameEdt.getEditText()))
        inputList.add(RxTextView.textChanges(organScale))
        inputList.add(RxTextView.textChanges(mNameEdt.getEditText()))
        inputList.add(RxTextView.textChanges(mPositionEdt.getEditText()))
        Observable.combineLatest(inputList) { str ->
            (str[0] as CharSequence).isEmpty()
                    || (str[1] as CharSequence).isEmpty()
                    || (str[2] as CharSequence).isEmpty()
                    || (str[3] as CharSequence).isEmpty()
        }.subscribe { b ->
            infoNextStep.isEnabled = !b
        }
        organScale.clickWithTrigger {
            startActivityForResult(intentFor<ScaleSelectionActivity>(Extras.DATA to organScale.text.toString()), Extras.REQUESTCODE)
        }
        infoNextStep.clickWithTrigger {
            teamInfoSupplement()
        }
        invCode.clickWithTrigger {
            startActivity<InvCodeInputActivity>(Extras.DATA to phone)
        }
        initView()
    }

    private fun teamInfoSupplement() {
        if (index == 0) {
            showTopSnackBar("请选择规模")
            return
        }
        val type = if (user_id.isNullOrEmpty()) {
            if (mechanismInfo != null) {
                1 //加入
            } else {
                2//创建
            }
        } else {
            3//修改信息
        }
        val teamInfo = TeamInfoSupplementReq(mPositionEdt.getInput(), mNameEdt.getInput(), index, organNameEdt.getInput(), type, phone, mechanismInfo?.key, user_id)
        SoguApi.getService(application, RegisterService::class.java).teamInfoSupplement(teamInfo)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            payload.payload?.let {
                                user_id = it.user_id.toString()
                                sp.edit { putString(Extras.CompanyKey, it.key) }
                                sp.edit { putString(Extras.CompanyName, organNameEdt.getInput()) }
                                sp.edit { putString(Extras.UserName,mNameEdt.getInput()) }
                                when (type) {
                                    1 -> login(phone, user_id!!.toInt())
                                    2, 3 -> {
                                        if (!flag) {
                                            startActivity<TakeCardActivity>(Extras.DATA to it)
                                        } else {
                                            login(phone, user_id!!.toInt())
                                        }
                                    }
                                }
                            }
                        } else {
                            showTopSnackBar(payload.message)
                        }
                    }
                }
    }

    private fun initView() {
        mechanismInfo?.let {
            organNameEdt.setText(it.mechanism_name)
            index = it.scale ?: 0
            organScale.text = when (it.scale) {
                1 -> "少于10人"
                2 -> "10～30人"
                3 -> "30～50人"
                4 -> "50～100人"
                5 -> "100人以上"
                else -> ""
            }
            scaleHint.setVisible(it.scale != null)
            mNameEdt.setText(it.name)
            mPositionEdt.setText(it.position)
            if (flag) {
                organNameEdt.setEnable(false)
                organScale.isEnabled = false
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
                                Store.store.setUser(this@InfoSupplementActivity, it)
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && resultCode == Extras.RESULTCODE && data != null) {
            organScale.text = data.getStringExtra(Extras.DATA)
            index = data.getIntExtra(Extras.INDEX, 0)
            scaleHint.setVisible(true)
        }
    }
}
