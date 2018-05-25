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
import kotlinx.android.synthetic.main.activity_project_faxin.*
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/8/11.
 */
class IssueRelatedActivity : ToolbarActivity() {

    lateinit var project: ProjectBean
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val gson = Gson()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setContentView(R.layout.activity_project_faxin)
        setBack(true)
        title = "发行相关"
        SoguApi.getService(application,InfoService::class.java)
                .issueInfo(project.company_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        val data = payload.payload
                        data?.apply {
                            tv_create_date.text = issueDate
                            tv_listed_date.text = listingDate
                            tv_founds_predict.text = expectedToRaise
                            tv_founds_real.text = actualRaised
                            tv_issue_count.text = issueNumber
                            tv_issue_price.text = issuePrice
                            tv_issue_pe.text = ipoRatio
                            tv_issue_win.text = rate
                            tv_open.text = openingPrice
                            tv_lead_underwriter.text = mainUnderwriter
                            tv_list_sponsor.text = listingSponsor
                            tv_his.text = history

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
            val intent = Intent(ctx, IssueRelatedActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
