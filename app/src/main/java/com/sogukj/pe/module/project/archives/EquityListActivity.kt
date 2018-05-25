package com.sogukj.pe.module.project.archives

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.bean.EquityListBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.module.project.businessBg.EquityStructureActivity
import com.sogukj.pe.service.ProjectService
import com.sogukj.pe.widgets.CircleImageView
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_equity_list.*

class EquityListActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, EquityListActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }

    lateinit var adapter: RecyclerAdapter<EquityListBean>
    lateinit var project: ProjectBean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equity_list)
        setBack(true)
        title = "股权信息"

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        adapter = RecyclerAdapter(context, { _adapter, parent, t ->
            Holder(_adapter.getView(R.layout.equity_item, parent))
        })
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        list.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 10)))
        list.layoutManager = layoutManager
        list.adapter = adapter

        SoguApi.getService(application,ProjectService::class.java)
                .equityList(project.company_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.forEach {
                            adapter.dataList.add(it)
                        }
                        adapter.notifyDataSetChanged()
                        if (adapter.dataList.size == 0) {
                            list.visibility = View.GONE
                            iv_empty.visibility = View.VISIBLE
                        }
                    } else {
                        if (adapter.dataList.size == 0) {
                            list.visibility = View.GONE
                            iv_empty.visibility = View.VISIBLE
                        }
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    showCustomToast(R.drawable.icon_toast_fail, "查询失败")
                    if (adapter.dataList.size == 0) {
                        list.visibility = View.GONE
                        iv_empty.visibility = View.VISIBLE
                    }
                    Trace.e(e)
                })
    }

    inner class Holder(convertView: View) : RecyclerHolder<EquityListBean>(convertView) {

        val root: LinearLayout
        val tuxiang: CircleImageView
        val record_title: TextView
        val state_modify: TextView
        val state_create: TextView
        val record_time: TextView

        init {
            root = convertView.findViewById<LinearLayout>(R.id.root) as LinearLayout
            tuxiang = convertView.findViewById<CircleImageView>(R.id.tuxiang) as CircleImageView
            record_title = convertView.findViewById<TextView>(R.id.record_title) as TextView
            state_modify = convertView.findViewById<TextView>(R.id.state_modify) as TextView
            state_create = convertView.findViewById<TextView>(R.id.state_create) as TextView
            record_time = convertView.findViewById<TextView>(R.id.record_time) as TextView
        }

        override fun setData(view: View, data: EquityListBean, position: Int) {
            tuxiang.setImageResource(R.drawable.xx)
            record_title.text = data.title
            if (data.is_edit == 0) {
                state_modify.visibility = View.GONE
            } else if (data.is_edit == 1) {
                state_create.visibility = View.GONE
            }
            record_time.text = data.time
            root.setOnClickListener {
                EquityStructureActivity.start(context, data)
            }
        }
    }
}
