package com.sogukj.pe.module.register

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.GridLayoutManager
import android.view.MenuItem
import com.amap.api.mapcore.util.it
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.bean.Department
import com.sogukj.pe.bean.DepartmentBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.module.main.ContactsActivity
import com.sogukj.pe.service.RegisterService
import com.sogukj.pe.service.UserService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_department_setting2.*
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.*

class DepartmentSettingActivity : ToolbarActivity() {
    override val menuId: Int
        get() = R.menu.menu_complete
    private val memberData = ArrayList<UserBean>()
    private val memberAdapter: MemberAdapter by lazy { MemberAdapter(ctx, memberData) }
    private val departmentBean: Department? by extraDelegate(Extras.DATA)
    private val model: OrganViewModel by lazy { ViewModelProviders.of(this).get(OrganViewModel::class.java) }
    private var isNotEmpty: Boolean = false
    private var principal: UserBean? = null

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
//            if (isNotEmpty) {
//                startActivityForResult<MemberSelectActivity>(Extras.REQUESTCODE, Extras.LIST to arrayListOf(principal), Extras.FLAG to true)
//            } else {
//            }
            toInviteActivity()
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
                    principal = list[0]
                    departmentPrincipal.text = principal?.name
                }
            }
        }
    }


}
