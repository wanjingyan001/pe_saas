package com.sogukj.pe.module.project

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
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.QuitBean
import com.sogukj.pe.service.NewService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_project_tc_history.*

class ProjectTcHistoryActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Activity?, bean: ProjectBean) {
            val intent = Intent(ctx, ProjectTcHistoryActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            ctx?.startActivity(intent)
        }
    }

    lateinit var bean: ProjectBean
    lateinit var adapter: RecyclerAdapter<QuitBean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_tc_history)
        setBack(true)
        title = "历史退出记录"

        bean = intent.getSerializableExtra(Extras.DATA) as ProjectBean

        adapter = RecyclerAdapter<QuitBean>(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_quit, parent) as LinearLayout
            object : RecyclerHolder<QuitBean>(convertView) {
                val title = convertView.findViewById<TextView>(R.id.title) as TextView
                val name = convertView.findViewById<TextView>(R.id.name) as TextView
                val outTime = convertView.findViewById<TextView>(R.id.outTime) as TextView
                override fun setData(view: View, data: QuitBean, position: Int) {
                    title.text = data.title
                    name.text = "操作人：${data.name}"
                    data.outTime?.let {
//                        outTime.text = it.substring(0, it.length - 5)
                        outTime.text = it
                    }
                }
            }
        })
        adapter.onItemClick = { v, p ->
            var item = adapter.dataList.get(p)
            var innerbean = ProjectBean()
            innerbean.company_id = item.id
            innerbean.number = bean.number
            innerbean.name = bean.name
            ProjectTCActivity.start(context, true, innerbean)
        }
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        list.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 10)))
        list.layoutManager = layoutManager
        list.adapter = adapter

        load()
    }

    fun load() {
        SoguApi.getService(application,NewService::class.java)
                .quitHistory(bean.company_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        var data = payload.payload
                        data?.apply {
                            adapter.dataList.clear()
                            adapter.dataList.addAll(this)
                            adapter.notifyDataSetChanged()
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                }, {
                    if (adapter.dataList.size == 0) {
                        iv_empty.visibility = View.VISIBLE
                    }
                })
    }
}
