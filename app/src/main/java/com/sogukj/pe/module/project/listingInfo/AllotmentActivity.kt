package com.sogukj.pe.module.project.listingInfo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.bean.AllotmentBean
import com.sogukj.pe.bean.ProjectBean
import kotlinx.android.synthetic.main.activity_project_allotment.*
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/7/18.
 */
class AllotmentActivity : ToolbarActivity() {

    lateinit var project: ProjectBean
    var allotment: AllotmentBean? = null
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val gson = Gson()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        allotment = intent.getSerializableExtra(AllotmentBean::class.java.simpleName) as AllotmentBean?
        setContentView(R.layout.activity_project_allotment)
        setBack(true)
        title = "配股详情"

        allotment?.apply {
            tv_issueDate.text = issueDate
            tv_name.text = name
            tv_progress.text = progress
            tv_year.text = year
            tv_issueCode.text = issueCode
            tv_pubDate.text = pubDate
            tv_price.text = price
            tv_sDate.text = sDate
            tv_announceDate.text = announceDate
            tv_actualRaise.text = actualRaise
            tv_proportion.text = proportion
            tv_exDate.text = exDate
            tv_saDate.text = saDate
            tv_raiseCeiling.text = raiseCeiling
            tv_registerDate.text = registerDate
            tv_dDate.text = dDate
        }

    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean, data: AllotmentBean) {
            val intent = Intent(ctx, AllotmentActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(AllotmentBean::class.java.simpleName, data)
            ctx?.startActivity(intent)
        }
    }
}
