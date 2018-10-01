package com.sogukj.pe.module.project.originpro

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.sogukj.pe.App
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.*
import com.sogukj.pe.module.project.originpro.adapter.ExpandableItemAdapter
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_upload_show.*
import kotlinx.android.synthetic.main.commom_blue_title.*
import org.jetbrains.anko.startActivity
import java.util.*

/**
 * Created by CH-ZH on 2018/9/20.
 */
class ProjectUploadShowActivity : ToolbarActivity(){
    private var list = ArrayList<MultiItemEntity>()
    private lateinit var adapter : ExpandableItemAdapter
    private lateinit var fundAdapter : RecyclerAdapter<LinkFundBean>
    private var fundInfos = ArrayList<LinkFundBean>()
    private var project : ProjectBean ? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_show)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        setBack(true)
        setTitle("信息填写")
        tv_edit.visibility = View.VISIBLE
        tv_edit.text = "编辑"

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean?
    }

    private fun initData() {
        list = getLocalData()
        adapter = ExpandableItemAdapter(list,1)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
        adapter.expandAll()

        fundAdapter = RecyclerAdapter(this){adapter,parent,_ ->
            ProjectFundHolder(adapter.getView(R.layout.item_link_fund,parent))
        }

        rv_fund.layoutManager = LinearLayoutManager(this)
        rv_fund.adapter = fundAdapter

        getFundData()
    }

    private fun getFundData() {
      if (null == project) return
        SoguApi.getService(App.INSTANCE,OtherService::class.java)
                .getLinkFund(project!!.company_id!!)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val fundInfos = payload.payload
                            if (null != fundInfos && fundInfos.size > 0){
                                fundAdapter.dataList.clear()
                                fundAdapter.dataList.addAll(fundInfos)
                                fundAdapter.notifyDataSetChanged()
                            }
                        }else{
                            showErrorToast(payload.message)
                        }

                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("获取数据失败")
                    }
                }
    }

    private fun bindListener() {

        adapter.setOnItemChildClickListener { adapter, view, position ->
            adapter.remove(position)
        }

        tv_edit.setOnClickListener {
            //编辑
            startActivity<ProjectUploadActivity>()
        }
    }

    private fun getLocalData(): ArrayList<MultiItemEntity> {
        val res = ArrayList<MultiItemEntity>()
        for (i in 0 .. 3){
            val level0Item = Level0Item("行业及业务类${i}")
            for (j in 0 .. 2){
                val level1Item = Level1Item("产品及技术知识介绍${i}")
                if (i != 3){
                    level1Item.addSubItem(Level2Item("","",-1))
                    level0Item.addSubItem(level1Item)
                }
            }
            res.add(level0Item)
        }
        return res
    }

    inner class ProjectFundHolder(itemView: View) : RecyclerHolder<LinkFundBean>(itemView) {
        val tv_invest = itemView.findViewById<TextView>(R.id.tv_invest)
        val tv_amount_name = itemView.findViewById<TextView>(R.id.tv_amount_name)
        val tv_stock_ratio = itemView.findViewById<TextView>(R.id.tv_stock_ratio)
        override fun setData(view: View, data: LinkFundBean, position: Int) {
             if (null == data) return
            tv_invest.text = data.fundName
            tv_amount_name.text = data.had_invest
            tv_stock_ratio.text = data.proportion
        }

    }
}


