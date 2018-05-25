package com.sogukj.pe.module.project.businessBg

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.service.InfoService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_biz_info.*
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/8/11.
 */
class BizInfoActivity : ToolbarActivity() {
    lateinit var project: ProjectBean
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setContentView(R.layout.activity_biz_info)
        setBack(true)
        title = "工商信息"
        SoguApi.getService(application,InfoService::class.java)
                .bizinfo(company_id = project.company_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        val data = payload.payload
                        data?.apply {
                            tv_legalPersonName.text = legalPersonName
                            tv_estiblishTime.text = estiblishTime
                            tv_regCapital.text = regCapital
                            tv_regNumber.text = regNumber
                            tv_orgNumber.text = orgNumber
                            tv_creditCode.text = creditCode
                            tv_idNumber.text = idNumber
                            tv_regStatus.text = regStatus
                            tv_companyOrgType.text = companyOrgType
                            tv_industry.text = industry
                            tv_toTime.text = toTime
                            tv_regLocation.text = regLocation
                            tv_approvedTime.text = approvedTime
                            tv_regInstitute.text = regInstitute
                            tv_businessScope.text=businessScope
                            copySupport(tv_legalPersonName, legalPersonName)
                            copySupport(tv_estiblishTime, estiblishTime)
                            copySupport(tv_regCapital, regCapital)
                            copySupport(tv_regNumber, regNumber)
                            copySupport(tv_orgNumber, orgNumber)
                            copySupport(tv_idNumber, idNumber)
                            copySupport(tv_creditCode, creditCode)
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                })
    }

    fun copySupport(v: TextView, text: String?) {
        v.setTextColor(ContextCompat.getColor(this,R.color.colorBlue))
        v.setOnLongClickListener {
            Utils.copy(this@BizInfoActivity, "" + text)
            showCustomToast(R.drawable.icon_toast_common, "已复制")
            true
        }
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, BizInfoActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}