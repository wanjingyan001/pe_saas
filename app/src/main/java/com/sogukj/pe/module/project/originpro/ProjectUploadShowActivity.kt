package com.sogukj.pe.module.project.originpro

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.Level0Item
import com.sogukj.pe.bean.Level1Item
import com.sogukj.pe.bean.Level2Item
import com.sogukj.pe.bean.ProjectFundInfo
import com.sogukj.pe.module.project.originpro.adapter.ExpandableItemAdapter
import kotlinx.android.synthetic.main.activity_upload_show.*
import java.util.*

/**
 * Created by CH-ZH on 2018/9/20.
 */
class ProjectUploadShowActivity : ToolbarActivity(){
    private var list = ArrayList<MultiItemEntity>()
    private lateinit var adapter : ExpandableItemAdapter
    private lateinit var fundAdapter : RecyclerAdapter<ProjectFundInfo>
    private var fundInfos = ArrayList<ProjectFundInfo>()
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
        for (i in 0 .. 2){
            val info = ProjectFundInfo()
            info.name = "海通证券${i}"
            info.account = 10000f+i
            info.ratio = "20%"
            fundInfos.add(info)
        }
        rv_fund.layoutManager = LinearLayoutManager(this)
        fundAdapter.dataList.addAll(fundInfos)
        rv_fund.adapter = fundAdapter
    }

    private fun bindListener() {

        adapter.setOnItemChildClickListener { adapter, view, position ->
            adapter.remove(position)
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

    inner class ProjectFundHolder(itemView: View) : RecyclerHolder<ProjectFundInfo>(itemView) {
        val tv_invest = itemView.findViewById<TextView>(R.id.tv_invest)
        val tv_amount_name = itemView.findViewById<TextView>(R.id.tv_amount_name)
        val tv_stock_ratio = itemView.findViewById<TextView>(R.id.tv_stock_ratio)
        override fun setData(view: View, data: ProjectFundInfo, position: Int) {
             if (null == data) return
            tv_invest.text = data.name
            tv_amount_name.text = data.account.toString()
            tv_stock_ratio.text = data.ratio
        }

    }
}


