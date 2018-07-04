package com.sogukj.pe.module.register

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.jsonStr
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.bean.DepartmentBean
import kotlinx.android.synthetic.main.activity_department_setting.*
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.*

class CreateDepartmentActivity : ToolbarActivity() {
    override val menuId: Int
        get() = R.menu.menu_complete
    private lateinit var departmentAdapter: DepartmentAdapter
    private val departments = ArrayList<DepartmentBean>()
    private val model: OrganViewModel by lazy { ViewModelProviders.of(this).get(OrganViewModel::class.java) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_department_setting)
        setBack(true)
        title = "创建部门"
        departmentAdapter = DepartmentAdapter(departments)
        departmentAdapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            startActivity<DepartmentSettingActivity>(Extras.DATA to departments[position])
        }
        departmentAdapter.onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener{adapter, view, position ->
            when(view.id){
                R.id.deleteDepartment ->{
//                    departments.removeAt(position+1)
                    departmentAdapter.notifyItemRemoved(position+1)
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
            if (nameEdt.textStr.isNotEmpty()){
                val bean  = DepartmentBean()
                bean.de_name = nameEdt.textStr
                departmentAdapter.addData(bean)
            }else{
                showTopSnackBar("请填写部门名称")
            }
        }
        return header
    }



    private fun getTissueData() = runBlocking {
        model.loadOrganizationData(application).observe(this@CreateDepartmentActivity, Observer { data: List<DepartmentBean>? ->
            data?.let {
                departments.addAll(it)
                departmentAdapter.notifyDataSetChanged()
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.complete -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }


    inner class DepartmentAdapter(data: List<DepartmentBean>) : BaseQuickAdapter<DepartmentBean, BaseViewHolder>(R.layout.item_create_department, data) {
        override fun convert(helper: BaseViewHolder, item: DepartmentBean) {
            helper.setText(R.id.departmentName, item.de_name)
            helper.addOnClickListener(R.id.deleteDepartment)
        }
    }
}
