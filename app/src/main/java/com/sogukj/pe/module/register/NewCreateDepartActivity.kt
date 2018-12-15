package com.sogukj.pe.module.register

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.bean.Depart0Item
import com.sogukj.pe.bean.Depart1Item
import kotlinx.android.synthetic.main.activity_new_department.*
import kotlinx.android.synthetic.main.commom_blue_title.*
import kotlinx.android.synthetic.main.layout_department_header.*
import java.util.*

/**
 * Created by CH-ZH on 2018/12/11.
 * 新版創建部門
 */
class NewCreateDepartActivity : ToolbarActivity() {
    private lateinit var mechanismName: String
    private lateinit var phone: String
    private lateinit var departAdapter : DepartmentItemAdapter
    private var list = ArrayList<MultiItemEntity>()
    private val logoUrl: String by extraDelegate(Extras.DATA, "")
    private val flag: Boolean by extraDelegate(Extras.FLAG, false)
    private val fromRegister: Boolean by extraDelegate(Extras.FLAG2, true)
    private var isEdit = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_department)
        setBack(true)
        title = "创建部门"
        mechanismName = intent.getStringExtra(Extras.NAME)
        phone = intent.getStringExtra(Extras.CODE)
        companyName.text = mechanismName
        Glide.with(this)
                .load(logoUrl)
                .apply(RequestOptions().placeholder(R.mipmap.ic_launcher_pe).error(R.mipmap.ic_launcher_pe))
                .into(companyLogo)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        tv_edit.visibility = View.VISIBLE
        rl_bottom.visibility = View.GONE
        isEdit = false
    }

    private fun initData() {
        list = getLocalData()
        departAdapter = DepartmentItemAdapter(list,this)
        departmentList.apply {
            layoutManager = LinearLayoutManager(this@NewCreateDepartActivity)
            adapter = departAdapter
        }
        departAdapter.expandAll()
    }

    private fun getLocalData(): ArrayList<MultiItemEntity> {
        val res = ArrayList<MultiItemEntity>()
        val item0 = Depart0Item("高管")
        val item1 = Depart0Item("投资部")
        val item2 = Depart0Item("组织部")

        val item3 = Depart1Item("高管一部")
        val item4 = Depart1Item("高管二部")
        val item5 = Depart1Item("高管三部")
        val item6 = Depart1Item("投资一部")
        val item7 = Depart1Item("投资二部")
        val item8 = Depart1Item("组织一部")
        val item9 = Depart1Item("组织二部")
        item0.addSubItem(item3)
        item0.addSubItem(item4)
        item0.addSubItem(item5)
        item1.addSubItem(item6)
        item1.addSubItem(item7)
        item2.addSubItem(item8)
        item2.addSubItem(item9)
        res.add(item0)
        res.add(item1)
        res.add(item2)
        return res
    }

    private fun bindListener() {
        tv_edit.clickWithTrigger {
            isEdit.yes {
                //完成
                finish()
            }.otherWise {
                tv_edit.text = "完成"
                isEdit = true
                //编辑
                rl_bottom.visibility = View.VISIBLE
            }
        }

        departAdapter.setOnItemChildClickListener { _adapter, view, position ->
            val data = _adapter.data
            val entity = data[position]
            when(view.id){

            }
        }

        addDepartment.clickWithTrigger {
            //添加顶级部门
            if (departmentName.textStr.isNotEmpty()) {
                addDepartment(departmentName.textStr)
                departmentName.setText("")
            } else {
                showTopSnackBar("请填写部门名称")
            }
        }
    }

    /**
     * 添加部门
     */
    private fun addDepartment(name : String){
        val data = departAdapter.data
        if (null != data && data.size > 0){

        }else{

        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}