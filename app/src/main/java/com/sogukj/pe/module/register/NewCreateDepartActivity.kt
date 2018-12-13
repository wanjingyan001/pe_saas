package com.sogukj.pe.module.register

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.bean.Department
import com.sogukj.pe.service.RegisterService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_department_setting.*
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity

/**
 * Created by CH-ZH on 2018/12/11.
 * 新版創建部門
 */
class NewCreateDepartActivity : ToolbarActivity() {
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
        setContentView(R.layout.activity_new_department)
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
        model.loadOrganizationData(application).observe(this@NewCreateDepartActivity, Observer { data: List<Department>? ->
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