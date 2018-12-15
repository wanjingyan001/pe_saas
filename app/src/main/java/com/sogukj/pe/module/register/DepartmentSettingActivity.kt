package com.sogukj.pe.module.register

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.extraDelegate
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
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
    private var childenDepartment = ArrayList<String>()
    private lateinit var childAdapter : RecyclerAdapter<String>
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
            childenDepartment.add("子部门" + i)
        }
        childAdapter = RecyclerAdapter(this){_adapter, parent, _ ->
            ChildDepartmentHolder(_adapter.getView(R.layout.item_child_department,parent))
        }
        recycler_view.apply {
            layoutManager = LinearLayoutManager(this@DepartmentSettingActivity)
            addItemDecoration(DividerItemDecoration(this@DepartmentSettingActivity,DividerItemDecoration.VERTICAL))
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

    class ChildDepartmentHolder(itemView: View) : RecyclerHolder<String>(itemView) {
        val departmentName = itemView.find<TextView>(R.id.departmentName)
        val iv_select = itemView.find<ImageView>(R.id.iv_select)
        val view_gaps = itemView.find<View>(R.id.view_gaps)
        override fun setData(view: View, data: String, position: Int) {

        }

    }
}


