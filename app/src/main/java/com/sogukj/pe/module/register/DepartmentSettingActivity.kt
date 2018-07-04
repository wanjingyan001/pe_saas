package com.sogukj.pe.module.register

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.GridLayoutManager
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.bean.Department
import com.sogukj.pe.bean.DepartmentBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.module.main.ContactsActivity
import com.sogukj.pe.service.UserService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_department_setting2.*
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.ctx
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity

class DepartmentSettingActivity : ToolbarActivity() {
    override val menuId: Int
        get() = R.menu.menu_complete
    private val memberData = ArrayList<UserBean>()
    private val memberAdapter: MemberAdapter by lazy { MemberAdapter(ctx, memberData) }
    private val departmentBean: DepartmentBean? by extraDelegate(Extras.DATA)
    private val model: OrganViewModel by lazy { ViewModelProviders.of(this).get(OrganViewModel::class.java) }
    private var isNotEmpty: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_department_setting2)
        setBack(true)
        title = "部门设置"
        departmentIsEmpty()
        departmentBean?.let {
            departmentName.text = it.de_name
            if (!it.data.isNullOrEmpty()){
                memberAdapter.members.addAll(it.data!!)
                memberCount.text = "${it.data!!.size}人"
            }
        }
        memberAdapter.onItemClick = { v, p ->
            if (p == memberData.size) {
                if (isNotEmpty){
                    startActivity<MemberSelectActivity>(Extras.LIST to memberData)
                }else{
                    toInviteActivity()
                }
            }
        }
        memberList.apply {
            layoutManager = GridLayoutManager(ctx, 6)
            adapter = memberAdapter
        }
        selectPrincipal.clickWithTrigger {
            if (isNotEmpty){
                startActivity<MemberSelectActivity>(Extras.LIST to memberData, Extras.FLAG to true)
            }else{
                toInviteActivity()
            }
        }
        addMember.clickWithTrigger {
            if (isNotEmpty){
                startActivity<MemberSelectActivity>(Extras.LIST to memberData)
            }else{
                toInviteActivity()
            }
        }
    }


    private fun departmentIsEmpty() = runBlocking {
       model.loadOrganizationData(application).observe(this@DepartmentSettingActivity, Observer {
           isNotEmpty = it?.isNotEmpty() ?: false
       })
    }


    private fun toInviteActivity(){
        val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS),
                    Extras.REQUESTCODE)
        } else {
            startActivity<InviteMainActivity>()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == Extras.REQUESTCODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startActivity<InviteMainActivity>()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
