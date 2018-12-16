package com.sogukj.pe.module.register

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.ChildDepartmentBean
import com.sogukj.pe.bean.Department
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.service.RegisterService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_department_setting2.*
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult

class DepartmentSettingActivity : ToolbarActivity() {
    override val menuId: Int
        get() = R.menu.menu_complete
    private val memberData = ArrayList<UserBean>()
    private val memberAdapter: MemberAdapter by lazy { MemberAdapter(ctx, memberData) }
    private val departmentBean: Department? by extraDelegate(Extras.DATA)
    private val model: OrganViewModel by lazy { ViewModelProviders.of(this).get(OrganViewModel::class.java) }
    private var isNotEmpty: Boolean = false
    private var principal: UserBean? = null
    private var childenDepartment = ArrayList<ChildDepartmentBean>()
    private lateinit var childAdapter : RecyclerAdapter<ChildDepartmentBean>
    private var isShowDel = false
    private lateinit var alreadySelected: MutableSet<ChildDepartmentBean>
    private var selectCount = 0
    private var isAllSelect = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_department_setting2)
        setBack(true)
        title = "部门设置"
        departmentIsEmpty()
        departmentBean?.let {
            departmentName.text = it.name
            title = it.name
        }
        alreadySelected = java.util.ArrayList<ChildDepartmentBean>().toMutableSet()
        memberAdapter.onItemClick = { v, p ->
            if (p == memberData.size) {
                if (isNotEmpty) {
                    startActivityForResult<MemberSelectActivity>(Extras.requestCode1, Extras.LIST to memberData)
                } else {
                    toInviteActivity()
                }
            }
        }
        getDepartmentSetting()
        memberList.apply {
            layoutManager = GridLayoutManager(ctx, 6)
            adapter = memberAdapter
        }
        selectPrincipal.clickWithTrigger {
            if (isNotEmpty) {
                startActivityForResult<MemberSelectActivity>(Extras.REQUESTCODE, Extras.LIST to arrayListOf(principal), Extras.FLAG to true)
            } else {
                toInviteActivity()
            }
        }
        addMember.clickWithTrigger {
            toInviteActivity()
        }

        for (i in 1 .. 15){
            var childDepartmentBean = ChildDepartmentBean()
            childDepartmentBean.name = "子部门" + i
            childDepartmentBean.isCanSelect = false
            childDepartmentBean.isSelected = false
            childenDepartment.add(childDepartmentBean)
        }
        childAdapter = RecyclerAdapter(this){_adapter, parent, _ ->
            ChildDepartmentHolder(_adapter.getView(R.layout.item_child_department,parent))
        }
        childAdapter.dataList = childenDepartment
        recycler_view.apply {
            layoutManager = LinearLayoutManager(this@DepartmentSettingActivity)
            addItemDecoration(DividerItemDecoration(this@DepartmentSettingActivity,DividerItemDecoration.VERTICAL))
            adapter = childAdapter
        }

        tv_del.clickWithTrigger {
            //显示删除
            isShowDel = !isShowDel
            isShowDel.yes {
                rl_bottom.setVisible(true)
                tv_del.text = "完成"
                showCanDelList()
            }.otherWise {
                rl_bottom.setVisible(false)
                tv_del.text = "删除"
                showNormalList()
            }

        }

        ll_status.clickWithTrigger {
            //全选，全不选
            isAllSelect = !isAllSelect
            val dataList = childAdapter.dataList
            isAllSelect.yes {
                tv_status.text = "全不选"
                iv_select.isSelected = true
                if (null != dataList && dataList.size > 0){
                    alreadySelected.clear()
                    dataList.forEach {
                        it.isCanSelect = true
                        it.isSelected = true
                        alreadySelected.add(it)
                    }
                    childAdapter.notifyDataSetChanged()
                    selectCount = dataList.size
                    tv_count.text = "已选择：${selectCount}个"
                    tv_delete.setBackgroundResource(if (selectCount > 0){R.drawable.bg_depart_delete}else{R.drawable.selector_sure_gray})
                }
            }.otherWise {
                tv_status.text = "全选"
                iv_select.isSelected = false
                if (null != dataList && dataList.size > 0){
                    alreadySelected.clear()
                    dataList.forEach {
                        it.isCanSelect = true
                        it.isSelected = false
                    }
                    childAdapter.notifyDataSetChanged()

                    selectCount = 0
                    tv_count.text = "已选择：${selectCount}个"
                    tv_delete.setBackgroundResource(R.drawable.selector_sure_gray)
                }
            }
        }

        tv_delete.clickWithTrigger {
            //批量删除
            if (selectCount > 0){
                showDeleteDepartDialog()
            }
        }

        addDepartment.clickWithTrigger {
            //添加顶级部门
            if (isShowDel){
                showTopSnackBar("编辑状态下不能添加")
                return@clickWithTrigger
            }
            if (et_name.textStr.isNotEmpty()) {
                addDepartment(et_name.textStr)
                et_name.setText("")
            } else {
                showTopSnackBar("请填写子部门名称")
            }
        }
    }

    /**
     * 添加部门
     */
    private fun addDepartment(name : String){
        val dataList = childAdapter.dataList
        val child = ChildDepartmentBean()
        child.name = name
        child.isCanSelect = false
        child.isSelected = false
        if (null != dataList){
            dataList.add(child)
            childAdapter.notifyDataSetChanged()
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

    private fun showNormalList() {
        val dataList = childAdapter.dataList
        if (null != dataList && dataList.size > 0){
            dataList.forEach {
                it.isCanSelect = false
                it.isSelected = false
            }
            childAdapter.notifyDataSetChanged()
        }
    }

    private fun showCanDelList() {
        val dataList = childAdapter.dataList
        if (null != dataList && dataList.size > 0){
            dataList.forEach {
                it.isCanSelect = true
                it.isSelected = false
            }
            childAdapter.notifyDataSetChanged()
        }
    }


    private fun departmentIsEmpty() = runBlocking {
        val key = sp.getString(Extras.CompanyKey, "")
        model.loadMemberList(application, key).observe(this@DepartmentSettingActivity, Observer {
            isNotEmpty = it?.isNotEmpty() ?: false
        })
    }


    private fun toInviteActivity() {
        val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS),
                    Extras.REQUESTCODE)
        } else {
            startActivity<InviteMainActivity>()
        }
    }

    private fun getDepartmentSetting() {
        departmentBean?.let {
            SoguApi.getService(application, RegisterService::class.java).setDepartment(it.depart_id)
                    .execute {
                        onNext { payload ->
                            if (payload.isOk) {
                                payload.payload?.let {
                                    it.depart_member?.let {
                                        memberCount.text = "${it.size}人"
                                        memberAdapter.members.addAll(it)
                                        memberAdapter.notifyDataSetChanged()
                                    }
                                    it.depart_head?.let {
                                        principal = it
                                        departmentPrincipal.text = it.name
                                    }
                                }
                            } else {
                                showTopSnackBar(payload.message)
                            }
                        }
                    }
        }
    }

    private fun setDepartmentSite() {
        departmentBean?.let {
            val memberUids = memberAdapter.members.map { it.user_id }.joinToString(",")
            SoguApi.getService(application, RegisterService::class.java).addDepartmentMember(it.depart_id, principal?.user_id, memberUids)
                    .execute {
                        onNext { payload ->
                            if (payload.isOk) {
                                finish()
                            } else {
                                showTopSnackBar(payload.message)
                            }
                        }
                        onError { e ->
                            Trace.e(e)
                            showTopSnackBar("请填写信息")
                        }
                    }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == Extras.REQUESTCODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startActivity<InviteMainActivity>()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.complete -> {
                setDepartmentSite()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && resultCode == Extras.RESULTCODE) {
            val list = data.getSerializableExtra(Extras.DATA) as ArrayList<UserBean>
            when (requestCode) {
                Extras.requestCode1 -> {
                    memberAdapter.members.clear()
                    memberAdapter.members.addAll(list)
                    memberAdapter.notifyDataSetChanged()
                    memberCount.text = "${memberAdapter.members.size}人"
                }
                Extras.REQUESTCODE -> {
                    if (list.isNotEmpty()){
                        principal = list[0]
                        departmentPrincipal.text = principal?.name
                    }else{
                        principal = null
                        departmentPrincipal.text = ""
                    }
                }
            }
        }
    }

    inner class ChildDepartmentHolder(itemView: View) : RecyclerHolder<ChildDepartmentBean>(itemView) {
        val departmentName = itemView.find<TextView>(R.id.departmentName)
        val iv_select = itemView.find<ImageView>(R.id.iv_select)
        val ll_content = itemView.find<LinearLayout>(R.id.ll_content)
        val tv_watch = itemView.find<TextView>(R.id.tv_watch)
        override fun setData(view: View, data: ChildDepartmentBean, position: Int) {
            if (null == data) return
            departmentName.text = data.name
            iv_select.isSelected = data.isSelected
            if (data.isCanSelect){
                iv_select.setVisible(true)
                ll_content.clickWithTrigger {
                    //
                    if (alreadySelected.contains(data)){
                        alreadySelected.remove(data)
                        selectCount--
                        if (selectCount < 0){
                            selectCount = 0
                        }
                    }else{
                        alreadySelected.add(data)
                        selectCount++
                    }
                    data.isSelected = !data.isSelected
                    iv_select.isSelected = data.isSelected
                    tv_count.text = "已选择：${selectCount}个"
                    tv_delete.setBackgroundResource(if (selectCount > 0){R.drawable.bg_depart_delete}else{R.drawable.selector_sure_gray})
                }
                tv_watch.clickWithTrigger {
                    //查看
                    startActivity<DepartmentSettingActivity>(Extras.DATA to Department(1,data.name))
                }
            }else{
                iv_select.setVisible(false)
                itemView.clickWithTrigger {
                    //查看
                    startActivity<DepartmentSettingActivity>(Extras.DATA to Department(1,data.name))
                }
            }
        }

    }
}


