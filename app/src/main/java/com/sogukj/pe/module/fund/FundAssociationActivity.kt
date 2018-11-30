package com.sogukj.pe.module.fund

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.FundAssociationBean
import com.sogukj.pe.bean.PdfBook
import com.sogukj.pe.module.dataSource.PdfPreviewActivity
import com.sogukj.pe.service.FundService
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_manager.*
import org.jetbrains.anko.find

class FundAssociationActivity : ToolbarActivity() {

    companion object {
        // id=基准日期id,当flag=1的时候不需要
        // title   当flag=1的时候不需要
        fun start(ctx: Activity?, flag: Int, id: Int, fund_Id: Int, title: String? = null) {
            val intent = Intent(ctx, FundAssociationActivity::class.java)
            intent.putExtra(Extras.FLAG, flag)
            intent.putExtra(Extras.ID, id)
            intent.putExtra(Extras.DATA2, fund_Id)
            intent.putExtra(Extras.TITLE, title)
            ctx?.startActivity(intent)
        }
    }

    var flag = 0  // 1获取基准日期,2获取模块信息
    var id = 0
    var fund_id = 0
    lateinit var mModuleAdapter: RecyclerAdapter<FundAssociationBean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager)
        setBack(true)
        flag = intent.getIntExtra(Extras.FLAG, 0)
        fund_id = intent.getIntExtra(Extras.DATA2, 0)
        if (flag == 1) {
            title = "基金运行情况"
            ll_export.setVisible(true)
        } else if (flag == 2) {
            title = intent.getStringExtra(Extras.TITLE)
            id = intent.getIntExtra(Extras.ID, 0)
            ll_export.setVisible(false)
        } else {
            title = ""
            ll_export.setVisible(false)
        }

        kotlin.run {
            mModuleAdapter = RecyclerAdapter<FundAssociationBean>(this, { _adapter, parent, type ->
                val convertView = _adapter.getView(R.layout.item_manager_header, parent)
                object : RecyclerHolder<FundAssociationBean>(convertView) {
                    val tvTitle = convertView.find<TextView>(R.id.title)
                    override fun setData(view: View, data: FundAssociationBean, position: Int) {
                        tvTitle.text = data.zhName
                    }
                }
            })
            val layoutManager1 = LinearLayoutManager(this)
            layoutManager1.orientation = LinearLayoutManager.VERTICAL
            moduleList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            moduleList.layoutManager = layoutManager1
            moduleList.adapter = mModuleAdapter
            mModuleAdapter.onItemClick = { v, p ->
                if (flag == 1) {
                    FundAssociationActivity.start(context, 2, mModuleAdapter.dataList.get(p).id!!, fund_id, mModuleAdapter.dataList.get(p).zhName)
                } else if (flag == 2) {
                    FundAssociationDetailActivity.start(context, fund_id, id, mModuleAdapter.dataList.get(p).id!!, mModuleAdapter.dataList.get(p).zhName!!)
                }
            }
        }

        doRequest()

        ll_export.clickWithTrigger {
            //导出报告
            SoguApi.getService(application,OtherService::class.java)
                    .getInvestorPost(fund_id)
                    .execute {
                        onNext { payload ->
                            if (payload.isOk){
                                val json = payload.payload
                                if (null != json){
                                    val path = json.get("preview_file_path").asString
                                    PdfPreviewActivity.invoke(this@FundAssociationActivity,
                                                PdfBook(0,"投资人报告",path,"","","","",1),false)
                                }else{
                                    showErrorToast("该基金的投资人报告不存在")
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
    }

    private fun doRequest() {
        SoguApi.getService(application, FundService::class.java)
                .getBaseDate(flag)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        mModuleAdapter.dataList.clear()
                        payload?.payload?.forEach {
                            mModuleAdapter.dataList.add(it)
                        }
                        mModuleAdapter.notifyDataSetChanged()
                        if (mModuleAdapter.dataList.size == 0) {
                            moduleList.visibility = View.GONE
                            iv_empty.visibility = View.VISIBLE
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        if (mModuleAdapter.dataList.size == 0) {
                            moduleList.visibility = View.GONE
                            iv_empty.visibility = View.VISIBLE
                        }
                    }
                }, { e ->
                    Trace.e(e)
                    if (mModuleAdapter.dataList.size == 0) {
                        moduleList.visibility = View.GONE
                        iv_empty.visibility = View.VISIBLE
                    }
                })
    }
}
