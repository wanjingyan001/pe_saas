package com.sogukj.pe.module.project.listingInfo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.service.InfoService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_company_info.*
import java.text.SimpleDateFormat


/**
 * Created by qinfei on 17/8/11.
 */
class CompanyInfoActivity : ToolbarActivity() {

    lateinit var project: ProjectBean
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val gson = Gson()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setContentView(R.layout.activity_company_info)
        setBack(true)
        title = "企业简介"

        SoguApi.getService(application,InfoService::class.java)
                .companyInfo(project.company_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        val data = payload.payload
                        data?.apply {
                            tv_name.text = companyName
                            tv_name_en.text = engName
                            tv_hangye.text = industry
                            tv_name_short.text = usedName
                            tv_bis.text = mainBusiness

                            tv_dongshi.text = chairman
                            tv_dongmi.text = secretaries
                            tv_faren.text = legal
                            tv_manager.text = generalManager
                            tv_zhuceziben.text = registeredCapital
                            tv_employees.text = employeesNum
                            tv_shareholder.text = controllingShareholder
                            tv_owner.text = actualController
                            tv_owner_final.text = finalController

                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })

    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, CompanyInfoActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
