package com.sogukj.pe.module.project.originpro

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.Level0Item
import com.sogukj.pe.bean.Level1Item
import com.sogukj.pe.bean.Level2Item
import com.sogukj.pe.module.fileSelector.FileMainActivity
import com.sogukj.pe.module.project.originpro.adapter.ExpandableItemAdapter
import kotlinx.android.synthetic.main.activity_project_upload.*
import kotlinx.android.synthetic.main.add_fund_item.*
import kotlinx.android.synthetic.main.layout_link_fund.*
import org.jetbrains.anko.startActivity
import java.io.File
import java.util.*

/**
 * Created by CH-ZH on 2018/9/19.
 */
class ProjectUploadActivity : ToolbarActivity() {
    private var list = ArrayList<MultiItemEntity>()
    private lateinit var adapter : ExpandableItemAdapter
    val REQ_SELECT_FILE = 0x2018
    private var addPosition = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_upload)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        setBack(true)
        setTitle("信息填写")
    }

    private fun initData() {
        list = getLocalData()
        adapter = ExpandableItemAdapter(list,0)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
        adapter.expandAll()

    }

    private fun bindListener() {
        rl_invest_subject.setOnClickListener {
            //选择投资主体
        }

        tv_add_fund.setOnClickListener {
            //添加关联基金
            val convertView = View.inflate(this,R.layout.add_fund_item,null)
            val rl_invest_subject = convertView.findViewById<RelativeLayout>(R.id.rl_invest_subject)
            val tv_invest = convertView.findViewById<TextView>(R.id.tv_invest)
            val et_amount_name = convertView.findViewById<EditText>(R.id.et_amount_name)
            val et_stock_ratio = convertView.findViewById<EditText>(R.id.et_stock_ratio)

            rl_invest_subject.setOnClickListener {
                //选择投资主体
            }

            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
            params.topMargin = Utils.dip2px(this,15f)
            convertView.layoutParams = params

            ll_other_fund.addView(convertView)
        }

        adapter.setOnItemChildClickListener { adapter, view, position ->
            when(view.id){
                R.id.iv_delete -> {
                    //移除文件
                    adapter.remove(position)
                }
                R.id.tv_add_file -> {
                    //添加文件
                    addPosition = position
                    FileMainActivity.start(context, 1,requestCode = REQ_SELECT_FILE)
                }
            }

        }

        ll_create.setOnClickListener {
            startActivity<ProjectUploadShowActivity>()
        }
    }

    private fun getLocalData(): ArrayList<MultiItemEntity> {
        val res = ArrayList<MultiItemEntity>()
        for (i in 0 .. 3){
            val level0Item = Level0Item("行业及业务类${i}")
            for (j in 0 .. 2){
                val level1Item = Level1Item("产品及技术知识介绍${i}")
                level1Item.addSubItem(Level2Item("","",-1))
                level0Item.addSubItem(level1Item)
            }
            res.add(level0Item)
        }
        return res
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (null != data && requestCode == REQ_SELECT_FILE && resultCode == Activity.RESULT_OK){
            val paths = data?.getStringArrayListExtra(Extras.LIST)
            val filePath = paths[0]
            val file = File(filePath)
            if (null == file) return
            val level2Item = Level2Item()
            level2Item.name = file.name
            level2Item.file = file
            level2Item.type = 0
            if (null != adapter){
                adapter.addData(addPosition,level2Item)
            }
        }
    }
}