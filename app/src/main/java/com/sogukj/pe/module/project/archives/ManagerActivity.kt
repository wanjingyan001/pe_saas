package com.sogukj.pe.module.project.archives

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.ManagerBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.service.ProjectService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_manager.*
@Route(path = ARouterPath.ManagerActivity)
class ManagerActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Activity?, project: ProjectBean, type: Int, title: String) {
            val intent = Intent(ctx, ManagerActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.TITLE, title)
            ctx?.startActivity(intent)
        }
    }

    var mType: Int? = null
    lateinit var bean: ProjectBean
    lateinit var mModuleAdapter: RecyclerAdapter<ManagerBean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager)
        setBack(true)
        title = intent.getStringExtra(Extras.TITLE)
        mType = intent.getIntExtra(Extras.TYPE, 0)
        bean = intent.getSerializableExtra(Extras.DATA) as ProjectBean

        kotlin.run {
            mModuleAdapter = RecyclerAdapter<ManagerBean>(this) { _adapter, parent, type ->
                val convertView = _adapter.getView(R.layout.item_manager_header, parent) as View
                object : RecyclerHolder<ManagerBean>(convertView) {
                    val tvTitle = convertView.findViewById<TextView>(R.id.title) as TextView
                    override fun setData(view: View, data: ManagerBean, position: Int) {
                        tvTitle.text = data.moduleName
                    }
                }
            }
            val layoutManager1 = LinearLayoutManager(this)
            layoutManager1.orientation = LinearLayoutManager.VERTICAL
            moduleList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            moduleList.layoutManager = layoutManager1
            moduleList.adapter = mModuleAdapter
            mModuleAdapter.onItemClick = { v, p ->
                ManagerDetailActivity.start(context, bean, mModuleAdapter.dataList.get(p).moduleId!!,
                        "${intent.getStringExtra(Extras.TITLE)}-${mModuleAdapter.dataList.get(p).moduleName}")
            }
        }

        doRequest()
    }

    private fun doRequest() {
        SoguApi.getService(application,ProjectService::class.java)
                .getInvestMan(mType!!)
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
