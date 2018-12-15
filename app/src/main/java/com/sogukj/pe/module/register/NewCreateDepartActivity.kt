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
import kotlinx.android.synthetic.main.activity_new_department.*
import kotlinx.android.synthetic.main.commom_blue_title.*
import kotlinx.android.synthetic.main.layout_department_header.*
import org.jetbrains.anko.find
import java.util.*

/**
 * Created by CH-ZH on 2018/12/11.
 * 新版創建部門
 */
class NewCreateDepartActivity : ToolbarActivity() {
    private lateinit var mechanismName: String
    private lateinit var phone: String
    private lateinit var departAdapter : DepartmentItemAdapter
    private lateinit var alreadySelected: MutableSet<MultiItemEntity>
    private var list = ArrayList<MultiItemEntity>()
    private val logoUrl: String by extraDelegate(Extras.DATA, "")
    private val flag: Boolean by extraDelegate(Extras.FLAG, false)
    private val fromRegister: Boolean by extraDelegate(Extras.FLAG2, true)
    private var isEdit = false
    private var totalAmount = 0
    private var isAllSelect = false
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
        alreadySelected = ArrayList<MultiItemEntity>().toMutableSet()
    }

    private fun initData() {
        totalAmount = 0
        list = getLocalData()
        departAdapter = DepartmentItemAdapter(list,this)
        departmentList.apply {
            layoutManager = LinearLayoutManager(this@NewCreateDepartActivity)
            adapter = departAdapter
        }
        departAdapter.expandAll()

        if (null == departAdapter.data || departAdapter.data.size == 0){
            tv_edit.visibility = View.GONE
        }
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
                tv_edit.text = "编辑"
                isEdit = false
                rl_bottom.visibility = View.GONE
                showNormalList()
            }.otherWise {
                tv_edit.text = "完成"
                isEdit = true
                //编辑
                rl_bottom.visibility = View.VISIBLE
                showEditList()
            }
        }

        departAdapter.setOnItemChildClickListener { _adapter, view, position ->
            val data = _adapter.data
            val entity = data[position]
            when(view.id){
                R.id.item_view -> {
                    val iv_select = view.find<ImageView>(R.id.iv_select)
                    if (entity is Depart0Item){
                        Log.e("TAG","Depart0Item isCanSelect ==" + entity.isCanSelect)
                        //一级部门
                        if (!entity.isCanSelect){
                            //查看
                            showSuccessToast("position ==" + position)
                            return@setOnItemChildClickListener
                        }
                        entity.isSelected = !entity.isSelected
                        iv_select.isSelected = entity.isSelected
                    }

                    if (entity is Depart1Item){
                        Log.e("TAG","Depart1Item isCanSelect ==" + entity.isCanSelect)
                        //二级部门
                        if (!entity.isCanSelect){
                            //查看
                            showSuccessToast("position ==" + position)
                            return@setOnItemChildClickListener
                        }
                        entity.isSelected = !entity.isSelected
                        iv_select.isSelected = entity.isSelected
                    }
                    if (alreadySelected.contains(entity)){
                        alreadySelected.remove(entity)
                        totalAmount--
                        if (totalAmount < 0){
                            totalAmount = 0
                        }
                    }else{
                        alreadySelected.add(entity as MultiItemEntity)
                        totalAmount++
                    }

                    tv_count.text = "已选择：${totalAmount}个"
                    tv_delete.setBackgroundResource(if (totalAmount > 0){R.drawable.bg_depart_delete}else{R.drawable.selector_sure_gray})
                }
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

        tv_delete.clickWithTrigger {
            //删除
            if (totalAmount > 0){
                showDeleteDepartDialog()
            }
        }
        iv_select.clickWithTrigger {
            selectAllOrNone()
        }

        tv_status.clickWithTrigger {
            selectAllOrNone()
        }
    }

    private fun selectAllOrNone() {
        isAllSelect = !isAllSelect
        iv_select.isSelected = isAllSelect
        val data = departAdapter.data
        isAllSelect.yes {
            tv_status.text = "全不选"
            if (null != data && data.size > 0){
                data.forEach {
                    alreadySelected.clear()
                    alreadySelected.add(it)
                    if (it is Depart0Item){
                        it.isCanSelect = true
                        it.isSelected = true
                    }
                    if (it is Depart1Item){
                        it.isCanSelect = true
                        it.isSelected = true
                    }
                }
            }
            totalAmount = departAdapter.data.size
        }.otherWise {
            tv_status.text = "全选"
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
                alreadySelected.clear()
            }
            totalAmount = 0
        }
        departAdapter.notifyDataSetChanged()
        tv_delete.setBackgroundResource(if (totalAmount > 0){R.drawable.bg_depart_delete}else{R.drawable.selector_sure_gray})
        tv_count.text = "已选择：${totalAmount}个"
    }

    private fun showNormalList() {
        val data = departAdapter.data
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
            departAdapter.notifyDataSetChanged()
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
            if (dialog.isShowing){
                dialog.dismiss()
            }
        }
    }

    private fun showEditList() {
        val data = departAdapter.data
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
            departAdapter.notifyDataSetChanged()
        }
    }

    /**
     * 添加部门
     */
    private fun addDepartment(name : String){
        val data = departAdapter.data
        if (null != data && data.size > 0){
            departAdapter.addData(data.size,Depart0Item(name))
        }else{
            departAdapter.addData(0,Depart0Item(name))
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}