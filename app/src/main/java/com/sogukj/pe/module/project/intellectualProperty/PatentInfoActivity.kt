package com.sogukj.pe.module.project.intellectualProperty

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Html
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.bean.PatentBean
import com.sogukj.pe.bean.ProjectBean
import kotlinx.android.synthetic.main.activity_patent_info.*
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/8/11.
 */
class PatentInfoActivity : ToolbarActivity() {
    lateinit var project: ProjectBean
    lateinit var data: PatentBean
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        data = intent.getSerializableExtra(PatentBean::class.java.simpleName) as PatentBean
        setContentView(R.layout.activity_patent_info)
        setBack(true)
        title = "专利信息详情"
        data.apply {
            tv_title.text = patentName
            val buff = StringBuffer()
            appendLine(buff, "申请公布号", applicationPublishNum)
            appendLine(buff, "申请日期", applicationTime)
            appendLine(buff, "申请公布日", applicationPublishTime)
//            appendLine(buff, "申请号/专利号", patentNum)
            buff.append("\n")
            appendLine(buff, "发明人", inventor)
            buff.append("\n")
            appendLine(buff, "申请人", applicantName)
            buff.append("\n")
            appendLine(buff, "代理机构", agency)
            buff.append("\n")
//            appendLine(buff, "地址", address)
//            buff.append("\n")
            appendLine(buff, "代理人", agent)
            buff.append("\n")
            appendLine(buff, "摘要", abstracts)
            tv_content.text = buff.toString()
        }
    }

    fun appendLine(buff: StringBuffer, key: String, value: String? = "") {
        val hval = Html.fromHtml("<span style='color:#666;'>${value}</span>")
        val hkey = Html.fromHtml("<span style='color:#000;'>${key}</span>")
        buff.append("$hkey:  $hval\n")
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean, data: PatentBean) {
            val intent = Intent(ctx, PatentInfoActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(PatentBean::class.java.simpleName, data)
            ctx?.startActivity(intent)
        }
    }
}