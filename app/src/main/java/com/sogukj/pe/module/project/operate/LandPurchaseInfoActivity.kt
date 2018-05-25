package com.sogukj.pe.module.project.operate

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.bean.LandPurchaseBean
import com.sogukj.pe.bean.ProjectBean
import kotlinx.android.synthetic.main.activity_land_purchase_info.*
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/8/11.
 */
class LandPurchaseInfoActivity : ToolbarActivity() {
    lateinit var project: ProjectBean
    lateinit var data: LandPurchaseBean
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        data = intent.getSerializableExtra(LandPurchaseBean::class.java.simpleName) as LandPurchaseBean
        setContentView(R.layout.activity_land_purchase_info)
        setBack(true)
        title = "购地详情"
        data.apply {
            tv_elecSupervisorNo.text = elecSupervisorNo
            tv_signedDate.text = signedDate
            tv_adminRegion.text = adminRegion
            tv_totalArea.text = "$totalArea"
            tv_dealPrice.text = "$dealPrice"
            tv_assignee.text = assignee
            tv_location.text = location
            tv_consignee.text = consignee
            tv_parentCompany.text = parentCompany
            tv_purpose.text = purpose
            tv_supplyWay.text = supplyWay
            tv_minVolume.text = "$minVolume"
            tv_maxVolume.text = "$maxVolume"
            tv_startTime.text = "$startTime"
            tv_endTime.text = endTime
        }
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean, data: LandPurchaseBean) {
            val intent = Intent(ctx, LandPurchaseInfoActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(LandPurchaseBean::class.java.simpleName, data)
            ctx?.startActivity(intent)
        }
    }
}