package com.sogukj.pe.module.creditCollection

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.CreditInfo
import com.sogukj.pe.bean.SensitiveInfo
import com.sogukj.pe.service.CreditService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_sensitive_info.*
import kotlinx.android.synthetic.main.toolbar.*
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.textColor

class SensitiveInfoActivity : BaseActivity(), View.OnClickListener {
    lateinit var info: SensitiveInfo
    lateinit var data: CreditInfo.Item

    companion object {
        val TAG = SensitiveInfoActivity::class.java.simpleName
        val COURTNOTICE = 1//法庭公告
        val COURTROOMT = 2//开庭公告
        val REFEREEDOCUMENTS = 3//裁判文书
        val EXECUTIVEBULLETIN = 4//执行公告
        val LOSSCREDIT = 5//法院失信名单
        val CASEDETEIL = 6//案件详情
        fun start(ctx: Context?, data: CreditInfo.Item) {
            val intent = Intent(ctx, SensitiveInfoActivity::class.java)
            intent.putExtra(Extras.DATA, data)
            ctx?.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensitive_info)
//        Utils.setWindowStatusBarColor(this, R.color.white)
        data = intent.getSerializableExtra(Extras.DATA) as CreditInfo.Item
        toolbar_menu.visibility = View.VISIBLE
        toolbar_menu.setOnClickListener(this)
        toolbar_menu.setImageResource(R.drawable.refresh)
        toolbar_title.text = "征信"
        name.text = data.name
        post.text = when (data.type) {
            1 -> "董监高"
            2 -> "股东"
            else -> ""
        }
        if (data.type == 0) {
            post.visibility = View.INVISIBLE
        }
        toolbar_back.visibility = View.VISIBLE
        toolbar_back.setOnClickListener(this)

        if (data.sum == 0) {
            status.text = "正常"
            dangerImage.imageResource = R.drawable.ic_sensitive_green
        } else {
            status.text = "风险人物"
            dangerImage.imageResource = R.drawable.ic_sensitive_red
            infoNumber.text = "${data.sum}条"
        }

        doRequest()
    }


    fun doRequest() {
        SoguApi.getService(application,CreditService::class.java)
                .sensitiveInfo(data.idCard!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            info = this
                            this.crime?.let {
                                setDangerousStatus(it)
//                                var number = 0
//                                try {
//                                    number = it.num!!.toInt()
//                                } catch (e: Exception) {
//                                    number = 0
//                                }
//                                if (number == 0) {
//                                    status.text = "正常"
//                                    dangerImage.imageResource = R.drawable.ic_sensitive_green
//                                } else {
//                                    status.text = "风险人物"
//                                    dangerImage.imageResource = R.drawable.ic_sensitive_red
//                                    infoNumber.text = "${it.num}条"
//                                }
                            }
                            this.court?.let {
                                var flag = it.num != null && it.num != 0
                                if (flag) {
                                    recordNumber.text = "${it.num}条记录"
                                    recordNumber.textColor = resources.getColor(R.color.fund_deep_red)
                                } else {
                                    recordNumber.text = "无"
                                    recordNumber.textColor = resources.getColor(R.color.text_1)
                                }
                                recordNumber.setCompoundDrawables(null, null, null, null)
                                recordNumber.setOnClickListener(null)
                            }
                            this.court?.item?.let {
                                setInfo(courtNoticeCount, it.fygg?.num != null && it.fygg?.num != 0, "${it.fygg?.num}条记录")
                                setInfo(courtroomtNoticeCount, it.ktgg?.num != null && it.ktgg?.num != 0, "${it.ktgg?.num}条记录")
                                setInfo(refereeDocumentsCount, it.cpws?.num != null && it.cpws?.num != 0, "${it.cpws?.num}条记录")
                                setInfo(executiveBulletinCount, it.zxgg?.num != null && it.zxgg?.num != 0, "${it.zxgg?.num}条记录")
                            }
                            this.dishonest?.let { setLostList(it) }
                            this.bankRisk?.let { setBankRisk(it) }
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    showCustomToast(R.drawable.icon_toast_fail, "数据加载出错")
                    Trace.e(e)
                })

    }

    /**
     * 涉诉记录
     */
    private fun setInfo(tv: TextView, flag: Boolean, info: String = "无") {
        if (flag) {
            tv.text = info
            tv.textColor = resources.getColor(R.color.fund_deep_red)
            val drawable = resources.getDrawable(R.drawable.ic_right)
            drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
            tv.setCompoundDrawables(null, null, drawable, null)
            tv.compoundDrawablePadding = Utils.dpToPx(this, 10)
            tv.setOnClickListener(this)
        } else {
            tv.text = "无"
            tv.textColor = resources.getColor(R.color.text_1)
            tv.setCompoundDrawables(null, null, null, null)
            tv.setOnClickListener(null)
        }
    }

    /**
     * 银行风险用户
     */
    private fun setBankRisk(bankRisk: SensitiveInfo.BankRisk) {
        lowRisk.isEnabled = bankRisk.bank_bad == 1
        mediumRisk.isEnabled = bankRisk.bank_overdue == 1
        highRisk.isEnabled = bankRisk.bank_lost == 1
        DangerousCredit.isEnabled = bankRisk.bank_fraud == 1
        refuse.isEnabled = bankRisk.bank_refuse == 1
    }


    /**
     * 法院失信名单
     */
    private fun setLostList(dishonest: SensitiveInfo.Dishonest) {
        if (dishonest.num == null || dishonest.num == 0) {
            recordNumber2.text = "无"
            recordNumber2.textColor = Color.parseColor("#282828")
            recordNumber2.setOnClickListener(null)
            recordNumber2.setCompoundDrawables(null, null, null, null)
            return
        }
        recordNumber2.text = "${dishonest.num}条记录"
        if (dishonest.item.size != 0) {
            filingDate.text = dishonest.item[0].time
            caseNumberTv.text = dishonest.item[0].casenum
            executionCourtTv.text = dishonest.item[0].court
            fulfillStateTv.text = dishonest.item[0].performance
            executionBasisTv.text = dishonest.item[0].base
        }
        recordNumber2.setOnClickListener(this@SensitiveInfoActivity)
    }

    /**
     * 危险身份
     */
    private fun setDangerousStatus(crime: SensitiveInfo.Crime) {
        if (crime.checkCode.isNullOrEmpty() || crime.checkCode == "0") {
            identity_status.text = "正常"
            identity_status.background = resources.getDrawable(R.drawable.bg_shareholder_green)
            identity_status.textColor = Color.parseColor("#50D59D")
            return
        }
        var tagList = ArrayList<String>()
        try {
            tagList = crime.checkCode!!.split(",") as ArrayList<String>
        } catch (e: Exception) {
            return
        }
        tagList.forEach {
            when (it) {
                "1" -> {
                    identity_status.text = "危险"
                    isSuspects.text = "是"
                    isSuspects.textColor = resources.getColor(R.color.fund_deep_red)
                }
                "2" -> {
                    identity_status.text = "危险"
                    isFormerStaff.text = "是"
                    isFormerStaff.textColor = resources.getColor(R.color.fund_deep_red)
                }
                "3" -> {
                    identity_status.text = "危险"
                    isDrugRelated.text = "是"
                    isDrugRelated.textColor = resources.getColor(R.color.fund_deep_red)
                }
                "4" -> {
                    identity_status.text = "危险"
                    isDrugAddicts.text = "是"
                    isDrugAddicts.textColor = resources.getColor(R.color.fund_deep_red)
                }
            }
        }
        caseNumber2.text = crime.caseSource
        timeTv.text = crime.caseTime
        if(timeTv.text != "无"){
            timeTv.textColor = resources.getColor(R.color.fund_deep_red)
        }
        if(caseNumber2.text != "无"){
            caseNumber2.textColor = resources.getColor(R.color.fund_deep_red)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.toolbar_back -> finish()
            R.id.toolbar_menu -> {
                showCustomToast(R.drawable.icon_toast_common, "重新加载数据中")
                doRequest()
            }
            R.id.courtNoticeCount -> SecondaryActivity.start(this, COURTNOTICE, info, data.id)
            R.id.courtroomtNoticeCount -> SecondaryActivity.start(this, COURTROOMT, info, data.id)
            R.id.refereeDocumentsCount -> SecondaryActivity.start(this, REFEREEDOCUMENTS, info, data.id)
            R.id.executiveBulletinCount -> SecondaryActivity.start(this, EXECUTIVEBULLETIN, info, data.id)
            R.id.recordNumber2 -> SecondaryActivity.start(this, LOSSCREDIT, info, data.id)
        //R.id.toDetail -> SecondaryActivity.start(this, CASEDETEIL, info, data.id)
        }
    }

}
