package com.sogukj.pe.module.register

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.bean.Depart0Item
import com.sogukj.pe.bean.Depart1Item
import com.sogukj.pe.bean.Department
import com.sogukj.pe.bean.MineDepartmentBean
import com.sogukj.pe.service.RegisterService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_new_department.*
import kotlinx.android.synthetic.main.commom_blue_title.*
import kotlinx.android.synthetic.main.layout_department_header.*
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity
import java.util.*

/**
 * Created by CH-ZH on 2018/12/11.
 * 新版創建部門
 */
class NewCreateDepartActivity : ToolbarActivity() {
    private lateinit var mechanismName: String
    private lateinit var phone: String
    private  var departAdapter : DepartmentItemAdapter ? = null
    private var list = ArrayList<MultiItemEntity>()
    private val logoUrl: String by extraDelegate(Extras.DATA, "")
    private val flag: Boolean by extraDelegate(Extras.FLAG, false)
    private val fromRegister: Boolean by extraDelegate(Extras.FLAG2, true)
    private var isEdit = false
    private var rootPid = 0
    private var isCanDelete = false
    private var isDeleteChild = false
    private var parentPos = -1
    private var childPos = -1
    private var depart_id = -1
    private var childDepartCount = 0
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
        isCanDelete = false
    }

    private fun initData() {
        getDepartmentFormNet()
    }

    private fun getDepartmentFormNet() {
        SoguApi.getService(this,RegisterService::class.java)
                .getCompanyDepartment(1)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val data = payload.payload
                            if (null != data && data.size > 0){
                                list = getServiceData(data)
                                setAdaperForList()
                            }
                        }else{
                            showErrorToast(payload.message)
                        }
                    }
                    onError {
                        it.printStackTrace()
                        showErrorToast("获取数据失败")
                        list = getLocalData()
                        setAdaperForList()
                    }
                }
    }

    private fun getServiceData(data: List<MineDepartmentBean>): ArrayList<MultiItemEntity> {
        val res = ArrayList<MultiItemEntity>()
            data.forEach {
                if (null != it){
                    rootPid = it.pid!!
                    val departParent = Depart0Item(it.name,it.id!!,it.pid!!)
                    val children = it.children
                    if (null != children && children.size > 0){
                        children.forEach {
                            if (null != it){
                                val departChild = Depart1Item(it.name,it.id!!,it.pid!!)
                                departParent.addSubItem(departChild)
                            }
                        }
                    }
                    res.add(departParent)
                }
            }
        return res
    }

    private fun setAdaperForList(){
        departAdapter = DepartmentItemAdapter(list,this)
        departmentList.apply {
            layoutManager = LinearLayoutManager(this@NewCreateDepartActivity)
            adapter = departAdapter
        }
        departAdapter!!.expandAll()

        if (null == departAdapter!!.data || departAdapter!!.data.size == 0){
            tv_edit.visibility = View.GONE
        }
        setClickChildListener()
    }
    private fun getLocalData(): ArrayList<MultiItemEntity> {
        val res = ArrayList<MultiItemEntity>()
        val item0 = Depart0Item("高管",1,0)
        val item1 = Depart0Item("投资部",2,0)
        val item2 = Depart0Item("组织部",3,0)

        val item3 = Depart1Item("高管一部",4,1)
        val item4 = Depart1Item("高管二部",5,1)
        val item5 = Depart1Item("高管三部",6,1)
        val item6 = Depart1Item("投资一部",7,2)
        val item7 = Depart1Item("投资二部",8,2)
        val item8 = Depart1Item("组织一部",9,3)
        val item9 = Depart1Item("组织二部",10,3)
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
                tv_edit.text = "编辑"
                rl_bottom.visibility = View.GONE
                showNormalList()
            }.otherWise {
                tv_edit.text = "完成"
                //编辑
                rl_bottom.visibility = View.VISIBLE
                showEditList()
            }
            isEdit = !isEdit
        }
        addDepartment.clickWithTrigger {
            //添加顶级部门
            if (isEdit){
                showTopSnackBar("编辑状态下不能添加部门")
                return@clickWithTrigger
            }
            if (departmentName.textStr.isNotEmpty()) {
                addDepartment(departmentName.textStr)
                departmentName.setText("")
            } else {
                showTopSnackBar("请填写部门名称")
            }
        }

        tv_delete.clickWithTrigger {
            //删除
            if (isCanDelete ){
                showDeleteDepartDialog()
            }
        }
    }

    private fun setClickChildListener(){
        departAdapter!!.setOnItemChildClickListener { _adapter, view, position ->
            val data = _adapter.data
            val entity = data[position]
            when(view.id){
                R.id.ll_content -> {
                    val iv_select = view.find<ImageView>(R.id.iv_select)
                    if (entity is Depart0Item){
                        if (!entity.isCanSelect){
                            startActivity<DepartmentSettingActivity>(Extras.DATA to Department(1,entity.name))
                            return@setOnItemChildClickListener
                        }
                        entity.isSelected = !entity.isSelected
                        iv_select.isSelected = entity.isSelected
                        data.forEachIndexed { index, it ->
                            if (it is Depart0Item){
                                it.isCanSelect = true
                                if (index == position){
                                    it.isSelected = entity.isSelected
                                }else{
                                    it.isSelected = false
                                }
                            }
                            if (it is Depart1Item){
                                it.isCanSelect = true
                                it.isSelected = false
                            }
                        }
                        _adapter.notifyDataSetChanged()
                        isCanDelete = entity.isSelected
                        tv_delete.setBackgroundResource(if (entity.isSelected){R.drawable.bg_depart_delete}else{R.drawable.selector_sure_gray})
                        if (null != entity.subItems && entity.subItems.size > 0){
                            Log.e("TAG","  child size ==" + entity.subItems.size + "  position ==" + position)
                            childDepartCount = entity.subItems.size
                        }else{
                            Log.e("TAG","  parent position ==" + position)
                            childDepartCount = 0
                        }
                        isDeleteChild = false
                        parentPos = position

                        depart_id = entity.id!!
                    }

                    if (entity is Depart1Item){
                        if (!entity.isCanSelect){
                            startActivity<DepartmentSettingActivity>(Extras.DATA to Department(1,entity.name))
                            return@setOnItemChildClickListener
                        }
                        entity.isSelected = !entity.isSelected
                        iv_select.isSelected = entity.isSelected

                        data.forEachIndexed { index, it ->
                            if (it is Depart1Item){
                                it.isCanSelect = true
                                if (index == position){
                                    it.isSelected = entity.isSelected
                                }else{
                                    it.isSelected = false
                                }
                            }
                            if (it is Depart0Item){
                                it.isCanSelect = true
                                it.isSelected = false
                            }
                        }
                        _adapter.notifyDataSetChanged()
                        isCanDelete = entity.isSelected
                        tv_delete.setBackgroundResource(if (entity.isSelected){R.drawable.bg_depart_delete}else{R.drawable.selector_sure_gray})
                        Log.e("TAG","  child position ==" + position)
                        isDeleteChild = true
                        childPos = position

                        depart_id = entity.id!!
                    }

                }

                R.id.tv_watch -> {
                    if (entity is Depart0Item){
                        startActivity<DepartmentSettingActivity>(Extras.DATA to Department(1,entity.name))
                    }

                    if (entity is Depart1Item){
                        startActivity<DepartmentSettingActivity>(Extras.DATA to Department(1,entity.name))
                    }
                }
            }
        }
    }

    private fun showNormalList() {
        if (null == departAdapter) return
        val data = departAdapter!!.data
        if (null != data && data.size > 0){
            data.forEach {
                if (it is Depart0Item){
                    it.isCanSelect = false
                    it.isSelected = false
                }
                if (it is Depart1Item){
                    it.isCanSelect = false
                    it.isSelected = false
                }
            }
            departAdapter!!.notifyDataSetChanged()
        }
    }

    private fun showDeleteDepartDialog() {
        val dialog = MaterialDialog.Builder(context)
                .theme(Theme.DARK)
                .customView(R.layout.layout_delete_depart, false)
                .canceledOnTouchOutside(false)
                .build()
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        val iv_del = dialog.find<ImageView>(R.id.iv_del)
        val tv_cancel = dialog.find<TextView>(R.id.tv_cancel)
        val tv_comfirm = dialog.find<TextView>(R.id.tv_comfirm)

        iv_del.clickWithTrigger {
            if (dialog.isShowing){
                dialog.dismiss()
            }
        }

        tv_cancel.clickWithTrigger {
            if (dialog.isShowing){
                dialog.dismiss()
            }
        }

        tv_comfirm.clickWithTrigger {
            //确定删除
            deleteDepartmentInfo(dialog)
        }
    }

    private fun deleteDepartmentInfo(dialog: MaterialDialog) {
        SoguApi.getService(this,RegisterService::class.java)
                .deleteDepartmentInfo(depart_id)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            showSuccessToast("删除部门成功")
                            deleteDepartLocal()
                        }else{
                            showErrorToast(payload.message)
                        }
                        if (dialog.isShowing){
                            dialog.dismiss()
                        }
                    }
                    onError {
                        it.printStackTrace()
                        showErrorToast("删除部门失败")
                        deleteDepartLocal()
                        if (dialog.isShowing){
                            dialog.dismiss()
                        }
                    }
                }
    }

    private fun deleteDepartLocal() {
        if (isDeleteChild){
            //删除子部门
            if (childPos != -1){
                departAdapter!!.remove(childPos)
            }
        }else{
            //删除的一级部门
            if (childDepartCount > 0){
                //删除一级部门和当前部门的子部门
                for (i in 0 .. childDepartCount){
                    departAdapter!!.remove(parentPos)
                }
            }else{
                departAdapter!!.remove(parentPos)
            }
        }
    }

    private fun showEditList() {
        if (null == departAdapter) return
        val data = departAdapter!!.data
        if (null != data && data.size > 0){
            data.forEach {
                if (it is Depart0Item){
                    it.isCanSelect = true
                    it.isSelected = false
                }
                if (it is Depart1Item){
                    it.isCanSelect = true
                    it.isSelected = false
                }
            }
            departAdapter!!.notifyDataSetChanged()
        }
    }

    /**
     * 添加部门
     */
    private fun addDepartment(name : String){
        if (null == departAdapter) return
        val data = departAdapter!!.data
        SoguApi.getService(this,RegisterService::class.java)
                .createDepartmentInfo(name,rootPid)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            if (null != data && data.size > 0){
                                departAdapter!!.addData(data.size,Depart0Item(name,1,rootPid))
                            }else{
                                departAdapter!!.addData(0,Depart0Item(name,1,rootPid))
                            }
                        }else{
                          showErrorToast(payload.message)
                        }
                    }
                    onError {
                        it.printStackTrace()
                        showErrorToast("创建部门失败")
                        if (null != data && data.size > 0){
                            departAdapter!!.addData(data.size,Depart0Item(name,1,rootPid))
                        }else{
                            departAdapter!!.addData(0,Depart0Item(name,1,rootPid))
                        }
                    }
                }
    }

}