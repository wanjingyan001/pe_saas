package com.sogukj.pe.module.register

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.edit
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.bean.Department
import com.sogukj.pe.module.main.MainActivity
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.RegisterService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_department_setting.*
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity

class CreateDepartmentActivity : ToolbarActivity() {
    override val menuId: Int
        get() = R.menu.menu_complete
    private lateinit var departmentAdapter: DepartmentAdapter
    private lateinit var mechanismName: String
    private lateinit var phone: String
    private val logoUrl: String by extraDelegate(Extras.DATA, "")
    private val departments = ArrayList<Department>()
    private val model: OrganViewModel by lazy { ViewModelProviders.of(this).get(OrganViewModel::class.java) }
    private val flag: Boolean by extraDelegate(Extras.FLAG, false)
    private val fromRegister: Boolean by extraDelegate(Extras.FLAG2, true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_department_setting)
        setBack(true)
        title = "创建部门"
        mechanismName = intent.getStringExtra(Extras.NAME)
        phone = intent.getStringExtra(Extras.CODE)
        fromRegister.no {
            supportInvalidateOptionsMenu()
        }
        companyName.text = mechanismName
        Glide.with(this)
                .load(logoUrl)
                .apply(RequestOptions().placeholder(R.mipmap.ic_launcher_pe).error(R.mipmap.ic_launcher_pe))
                .into(companyLogo)
        departmentAdapter = DepartmentAdapter(departments)
        departmentAdapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            startActivity<DepartmentSettingActivity>(Extras.DATA to departmentAdapter.data[position])
        }
        departmentAdapter.onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.deleteDepartment -> {
                    deleteDepartment(position)
                }
            }
        }
        departmentAdapter.addHeaderView(initHeader())
        departmentList.apply {
            adapter = departmentAdapter
            layoutManager = LinearLayoutManager(ctx)
        }
        getTissueData()
    }

    private fun initHeader(): View {
        val header = layoutInflater.inflate(R.layout.layout_add_department_header, null)
        val nameEdt = header.find<EditText>(R.id.departmentName)
        val add = header.find<ImageView>(R.id.addDepartment)
        add.clickWithTrigger {
            if (nameEdt.textStr.isNotEmpty()) {
                addDepartment(nameEdt.textStr)
                nameEdt.setText("")
            } else {
                showTopSnackBar("请填写部门名称")
            }
        }
        return header
    }


    private fun getTissueData() = runBlocking {
        model.loadOrganizationData(application).observe(this@CreateDepartmentActivity, Observer { data: List<Department>? ->
            data?.let {
                departments.addAll(it)
                departmentAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun addDepartment(name: String) {
        SoguApi.getService(application, RegisterService::class.java).addDepartment(name)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            payload.payload?.let {
                                departmentAdapter.addData(it)
                            }
                        } else {
                            showTopSnackBar(payload.message)
                        }
                    }
                }
    }

    private fun deleteDepartment(position: Int) {
        val id = departmentAdapter.data[position].depart_id
        SoguApi.getService(application, RegisterService::class.java).deleteDepartment(id)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            departmentAdapter.remove(position)
                        } else {
                            showTopSnackBar(payload.message)
                        }
                    }
                }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        fromRegister.yes {
            menuInflater.inflate(R.menu.menu_complete, menu)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.complete -> {
                login(phone)
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun login(phone: String) {
        showProgress("正在获取数据...")
        var source: String? = null
        var unique: String? = null
        sp.getString(Extras.THIRDLOGIN, "").apply {
            isNotEmpty().yes {
                source = split("_")[0]
                unique = split("_")[1]
            }
        }
        SoguApi.getService(application, RegisterService::class.java).getUserBean(phone, sp.getInt(Extras.SaasUserId, 0), source, unique)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            payload.payload?.let {
                                if (flag) {
                                    finish()
                                } else {
                                    Store.store.setUser(this@CreateDepartmentActivity, it)
                                    getCompanyInfo()
                                }
                            }
                        } else {
                            hideProgress()
                            showTopSnackBar(payload.message)
                        }
                    }
                    onError { e ->
                        hideProgress()
                        Trace.e(e)
                    }
                }
    }


    private fun getCompanyInfo() {
        val key = sp.getString(Extras.CompanyKey, "")
        if (key.isNotEmpty()) {
            SoguApi.getService(application, RegisterService::class.java)
                    .getBasicInfo(key)
                    .execute {
                        onNext { payload ->
                            if (payload.isOk) {
                                payload.payload?.let {
                                    sp.edit { putString(Extras.SAAS_BASIC_DATA, it.jsonStr) }
                                    sp.edit { putInt(Extras.main_flag, it.homeCardFlag ?: 1) }
                                    startActivity<MainActivity>()
                                }
                            } else {
                                showErrorToast(payload.message)
                            }
                        }
                    }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hideProgress()
    }

    inner class DepartmentAdapter(data: List<Department>) : BaseQuickAdapter<Department, BaseViewHolder>(R.layout.item_create_department, data) {
        override fun convert(helper: BaseViewHolder, item: Department) {
            helper.setText(R.id.departmentName, item.name)
            helper.addOnClickListener(R.id.deleteDepartment)
        }
    }
}
