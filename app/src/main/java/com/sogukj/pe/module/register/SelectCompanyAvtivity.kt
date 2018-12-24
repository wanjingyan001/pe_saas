package com.sogukj.pe.module.register

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.SelectCompanyBean
import com.sogukj.pe.module.main.MainActivity
import kotlinx.android.synthetic.main.activity_select_company.*
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity

/**
 * Created by CH-ZH on 2018/12/19.
 * 选择登录公司
 */
class SelectCompanyAvtivity : ToolbarActivity() {
    private lateinit var companyAdapter : RecyclerAdapter<SelectCompanyBean>
    private var data  = ArrayList<SelectCompanyBean>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_company)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        setBack(true)
        setTitle("")
    }

    private fun initData() {
        companyAdapter = RecyclerAdapter(this){_adapter,parent,_ ->
             SelectCompanyHolder(_adapter.getView(R.layout.item_select_company,parent))
        }
        for ( i in 0 .. 3){
            val bean = SelectCompanyBean()
            bean.name = "海通创新资本管理有限公司" + i
            data.add(bean)
        }
        companyAdapter.dataList = data
        recycler_view.apply {
            layoutManager = LinearLayoutManager(this@SelectCompanyAvtivity)
            adapter = companyAdapter
        }
    }

    private fun bindListener() {

    }

    inner class SelectCompanyHolder(itemView: View) : RecyclerHolder<SelectCompanyBean>(itemView) {
        val tv_name = itemView.find<TextView>(R.id.tv_name)
        override fun setData(view: View, data: SelectCompanyBean, position: Int) {
            if (null == data)return
            tv_name.text = if (null != data.name){data.name}else{""}
            tv_name.isSelected = data.isSelected

            tv_name.clickWithTrigger {
                val dataList = companyAdapter.dataList
                dataList.forEachIndexed { index, selectCompanyBean ->
                    if (index == position){
                        selectCompanyBean.isSelected = !data.isSelected
                    }else{
                        selectCompanyBean.isSelected = false
                    }
                }
                companyAdapter.notifyDataSetChanged()
                this@SelectCompanyAvtivity.startActivity<MainActivity>()
            }
        }

    }
}


