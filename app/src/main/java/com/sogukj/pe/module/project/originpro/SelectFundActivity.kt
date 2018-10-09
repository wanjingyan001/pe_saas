package com.sogukj.pe.module.project.originpro

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.sogukj.pe.App
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.InvestFundBean
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_select_fund.*

/**
 * Created by CH-ZH on 2018/10/9.
 */
class SelectFundActivity : ToolbarActivity() {
    lateinit var fundAdapter: RecyclerAdapter<InvestFundBean>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_fund)
        initView()
    }

    private fun initView() {
        setBack(true)
        setTitle("选择基金")
        fundAdapter = RecyclerAdapter<InvestFundBean>(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.fund_item, parent)
            val tv_name = convertView.findViewById<TextView>(R.id.tv_name)
            object : RecyclerHolder<InvestFundBean>(convertView) {
                override fun setData(view: View, data: InvestFundBean, position: Int) {
                    if (null == data) return
                    tv_name.text = data.name
                }
            }
        })
        rv_fund.layoutManager = LinearLayoutManager(this)
        rv_fund.adapter = fundAdapter

        getFundData()
        fundAdapter.onItemClick = {view,position ->
            val dataList = fundAdapter.dataList
            if (null != dataList && dataList.size > 0){
                if (null != dataList[position]){
                    doFinish(dataList[position])
                }
            }
        }
    }

    private fun getFundData() {
        SoguApi.getService(App.INSTANCE,OtherService::class.java)
                .getLinkFundList()
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val list = payload.payload
                            if (null != list && list.size > 0){
                                fundAdapter.dataList.clear()
                                fundAdapter.dataList.addAll(list)
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

    fun doFinish(bean: InvestFundBean) {
        val intent = Intent()
        intent.putExtra(Extras.DATA, bean)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    companion object {
        fun start(ctx: Activity?,code: Int) {
            val intent = Intent(ctx, SelectFundActivity::class.java)
            ctx?.startActivityForResult(intent, code)
        }
    }
}