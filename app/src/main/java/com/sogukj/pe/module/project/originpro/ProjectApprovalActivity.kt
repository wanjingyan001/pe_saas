package com.sogukj.pe.module.project.originpro

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.ProjectFileInfo
import com.sogukj.pe.module.fileSelector.FileMainActivity
import kotlinx.android.synthetic.main.activity_project_approval.*
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity
import java.io.File
import java.util.*

/**
 * Created by CH-ZH on 2018/9/18.
 * 项目立项填写
 */
class ProjectApprovalActivity : ToolbarActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_approval)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        setBack(true)
        setTitle(R.string.project_info)
        addEditView()
        addEditView()
        addEditView()
    }

    private fun addEditView() {
        val convertView = View.inflate(this,R.layout.layout_pro_edit,null)
        val rl_project_time = convertView.findViewById<RelativeLayout>(R.id.rl_project_time)
        val tv_time = convertView.findViewById<TextView>(R.id.tv_time)
        val et_property_name = convertView.findViewById<EditText>(R.id.et_property_name)
        val et_income_name = convertView.findViewById<EditText>(R.id.et_income_name)
        val et_profit_name = convertView.findViewById<EditText>(R.id.et_profit_name)

        rl_project_time.setOnClickListener {
            val selectedDate = Calendar.getInstance()//系统当前时间
            val startDate = Calendar.getInstance()
            startDate.set(1949, 10, 1)
            val endDate = Calendar.getInstance()
            endDate.set(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH) + 1, selectedDate.get(Calendar.DAY_OF_MONTH))
            val timePicker = TimePickerBuilder(this, { date, view ->
                tv_time.text = Utils.getTime(date)
            })
                    //年月日时分秒 的显示与否，不设置则默认全部显示
                    .setType(booleanArrayOf(true, true, false, false, false, false))
                    .setDividerColor(Color.DKGRAY)
                    .setContentTextSize(21)
                    .setDate(selectedDate)
                    .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
                    .setRangDate(startDate, endDate)
                    .setDecorView(window.decorView.find(android.R.id.content))
                    .build()
            timePicker.show()
        }

        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
        params.topMargin = Utils.dip2px(this,20f)
        convertView.layoutParams = params

        ll_pro_info.addView(convertView)
    }

    private fun initData() {

    }

    private fun bindListener() {
        ll_create.setOnClickListener {
            //创建
            startActivity<ProjectApprovalShowActivity>()
        }
        //添加文件
        add_file_view.addFileListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                FileMainActivity.start(context, requestCode = REQ_SELECT_FILE)
            }
        })
    }

    companion object {
        val REQ_SELECT_FILE = 0x1001
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && requestCode == REQ_SELECT_FILE && resultCode == Activity.RESULT_OK) {
            val paths = data?.getStringArrayListExtra(Extras.LIST)
            paths?.forEach {
                val info = ProjectFileInfo()
                val file = File(it)
                info.file = file
                info.name = file.name
                add_file_view.addFileData(info)
            }
        }
    }
}