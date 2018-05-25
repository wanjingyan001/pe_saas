package com.sogukj.pe.module.project.operate

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.QualificationBean
import kotlinx.android.synthetic.main.activity_qualification.*
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/8/11.
 */
class QualificationActivity : ToolbarActivity() {
    lateinit var project: ProjectBean
    lateinit var data: QualificationBean
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        data = intent.getSerializableExtra(QualificationBean::class.java.simpleName) as QualificationBean
        setContentView(R.layout.activity_qualification)
        setBack(true)
        title = "证书详情"
        data.apply {
            tv_deviceName.text = deviceName
            tv_licenceNum.text = licenceNum
            tv_licenceType.text = licenceType
            tv_issueDate.text = issueDate
            tv_toDate.text = toDate
            tv_deviceType.text = deviceType
            tv_applyCompany.text = applyCompany
            tv_productCompany.text = productCompany
        }
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean, data: QualificationBean) {
            val intent = Intent(ctx, QualificationActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(QualificationBean::class.java.simpleName, data)
            ctx?.startActivity(intent)
        }
    }
}