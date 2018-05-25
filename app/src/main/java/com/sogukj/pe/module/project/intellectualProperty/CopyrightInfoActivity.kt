package com.sogukj.pe.module.project.intellectualProperty

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.TableLayout
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.bean.CopyRightBean
import com.sogukj.pe.bean.ProjectBean
import kotlinx.android.synthetic.main.activity_copyright_1.*
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/8/11.
 */
class CopyrightInfoActivity : ToolbarActivity() {
    lateinit var project: ProjectBean
    lateinit var data: CopyRightBean
    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    fun addRow(table: TableLayout, key: String, value: String? = "") {
        val convertView = View.inflate(this, R.layout.item_value_pairs, null)
        val tvLabel = convertView.findViewById<TextView>(R.id.tv_label) as TextView
        val tvValue = convertView.findViewById<TextView>(R.id.tv_value) as TextView
        tvLabel.text = "${key}:"
        tvValue.text = value
        table.addView(convertView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        data = intent.getSerializableExtra(CopyRightBean::class.java.simpleName) as CopyRightBean
        val type = intent.getIntExtra(Extras.TYPE, 1)
        setContentView(R.layout.activity_copyright_1)
        setBack(true)

//        if (type == 1) {
//            setTitle("软著权详情")
//            data.apply {
//                tv_title.text = fullname
//                addRow(tab_values, "简称", simplename)
//                addRow(tab_values, "登记号", regnum)
//                addRow(tab_values, "分类号", catnum)
//                addRow(tab_values, "版本号", version)
//                addRow(tab_values, "著作权人", authorNationality)
//                addRow(tab_values, "首次发表日期", publishtime)
//                addRow(tab_values, "登记日期", regtime)
//            }
//        } else {
//            setTitle("著作权详情")
//            data.apply {
//                tv_title.text = simplename
//                val buff = StringBuffer()
//                addRow(tab_values, "类别", category)
//                addRow(tab_values, "著作权人", authorNationality)
//                addRow(tab_values, "登记号", regnum)
//                addRow(tab_values, "完成日期", finishTime)
//                addRow(tab_values, "首次发表日期", publishtime)
//                addRow(tab_values, "登记日期", regtime)
//            }
//        }
        if (type == 1) {
            title = "软著权详情"
            data.apply {
                tv_title.text = fullname
                val buff = StringBuffer()
                appendLine(buff, "简称", simplename)
                appendLine(buff, "登记号", regnum)
                appendLine(buff, "分类号", catnum)
                buff.append("\n")
                appendLine(buff, "版本号", version)
                buff.append("\n")
                appendLine(buff, "著作权人", authorNationality)
                buff.append("\n")
                appendLine(buff, "首次发表日期", publishtime)
                appendLine(buff, "登记日期", regtime)
                tv_content.text = Html.fromHtml(buff.toString())
            }
        } else {
            title = "著作权详情"
            data.apply {
                tv_title.text = simplename
                val buff = StringBuffer()
                appendLine(buff, "类别", category)
                appendLine(buff, "著作权人", authorNationality)
                buff.append("\n")
                appendLine(buff, "登记号", regnum)
                appendLine(buff, "完成日期", finishTime)
                appendLine(buff, "首次发表日期", publishtime)
                appendLine(buff, "登记日期", regtime)
                buff.append("\n")
                tv_content.text = Html.fromHtml(buff.toString())
            }
        }
    }

    fun appendLine(buff: StringBuffer, k: String, v: String) {
        buff.append("$k: <font color='#666666'>$v</font><br/>")
    }


    companion object {
        fun start(ctx: Activity?, project: ProjectBean, data: CopyRightBean, type: Int = 1) {
            val intent = Intent(ctx, CopyrightInfoActivity::class.java)
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(CopyRightBean::class.java.simpleName, data)
            ctx?.startActivity(intent)
        }
    }
}