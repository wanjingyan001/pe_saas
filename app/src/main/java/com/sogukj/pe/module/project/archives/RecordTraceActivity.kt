package com.sogukj.pe.module.project.archives

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.gson.Gson
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.DateUtils
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.widgets.DotView
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.RecordInfoBean
import com.sogukj.pe.service.ProjectService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_record_trace.*
import org.jetbrains.anko.textColor
@Route(path = ARouterPath.RecordTraceActivity)
class RecordTraceActivity : ToolbarActivity() {

    lateinit var project: ProjectBean
    val gson = Gson()
    lateinit var adapter: RecyclerAdapter<RecordInfoBean.ListBean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_trace)

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setBack(true)
        title = "跟踪记录"
        company_name.text = project.name

//        project.company_id = 1

        adapter = RecyclerAdapter(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_recordtrace, parent) as LinearLayout
            object : RecyclerHolder<RecordInfoBean.ListBean>(convertView) {
                val dot = convertView.findViewById<DotView>(R.id.dot) as DotView
                val tvImportant = convertView.findViewById<TextView>(R.id.important) as TextView
                val tvEvent = convertView.findViewById<TextView>(R.id.event) as TextView
                val tvTime = convertView.findViewById<TextView>(R.id.time) as TextView
                override fun setData(view: View, data: RecordInfoBean.ListBean, position: Int) {
                    if (position == 0) {
                        dot.setUp(false)
                        dot.setLow(true)
                    } else if (position == adapter.dataList.size - 1) {
                        dot.setUp(true)
                        dot.setLow(false)
                    } else {
                        dot.setUp(true)
                        dot.setLow(true)
                    }
                    dot.setImportant(data.important != 0)

                    if (data.important == 0) {
                        tvImportant.visibility = View.GONE
                    } else {
                        tvImportant.visibility = View.VISIBLE
                    }
                    tvEvent.text = data.des
                    tvTime.text = DateUtils.timet("${data.start_time}")

                    if (data.id in isViewed) {
                        convertView.setBackgroundColor(Color.parseColor("#f9f9f9"))
                        tvEvent.textColor = Color.parseColor("#656565")
                    } else {
                        convertView.setBackgroundColor(Color.parseColor("#ffffff"))
                        tvEvent.textColor = Color.parseColor("#282828")
                    }
                }
            }
        })
        adapter.onItemClick = { v, p ->
            var item = adapter.dataList.get(p)
            isViewed.add(item.id ?: -1)
            RecordDetailActivity.startView(this@RecordTraceActivity, project, item)
        }
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        //list.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        list.layoutManager = layoutManager
        list.adapter = adapter

        project.company_id?.let {
            load(it)
        }
    }

    fun load(it: Int) {
        contentLayout.visibility = View.GONE
        SoguApi.getService(application,ProjectService::class.java)
                .recodeInfo(it)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        contentLayout.visibility = View.VISIBLE
                        var data = payload.payload
                        Log.d("WJY",Gson().toJson(payload.payload))
                        data?.apply {
                            tv_investCost.text = info?.investCost
                            tv_investDate.text = info?.investDate
                            tv_equityRatio.text = info?.equityRatio
                            tv_riskControls.text = info?.riskControls
                            tv_invests.text = info?.invests

                            if (list != null && list.size != 0) {
                                contentLayout.visibility = View.VISIBLE
                                iv_empty.visibility = View.GONE
                                adapter.dataList.clear()
                                adapter.dataList.addAll(list)
                                adapter.notifyDataSetChanged()
                            } else {
                                contentLayout.visibility = View.GONE
                                iv_empty.visibility = View.VISIBLE
                            }
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                })
    }

    override val menuId: Int
        get() = R.menu.menu_mark

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val flag = super.onCreateOptionsMenu(menu)
        val menuMark = menu.findItem(R.id.action_mark) as MenuItem
        menuMark.title = "添加"
        return flag
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_mark -> {
                RecordDetailActivity.startAdd(this@RecordTraceActivity, project)
            }
        }
        return false
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, RecordTraceActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x001 && resultCode == Activity.RESULT_OK) {
            project.company_id?.let {
                load(it)
            }
        } else if (requestCode == 0x002 && resultCode == Activity.RESULT_CANCELED) {
            adapter.notifyDataSetChanged()
        }
    }

    var isViewed = ArrayList<Long>()
}
