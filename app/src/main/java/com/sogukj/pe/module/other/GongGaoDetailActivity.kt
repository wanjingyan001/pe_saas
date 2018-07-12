package com.sogukj.pe.module.other

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil.setContentView
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import anet.channel.util.Utils.context
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.bean.GongGaoBean
import com.sogukj.pe.bean.MessageBean
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_gong_gao_detail.*
import kotlinx.android.synthetic.main.toolbar.*

class GongGaoDetailActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Activity?, bean: MessageBean) {
            val intent = Intent(ctx, GongGaoDetailActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            ctx?.startActivity(intent)
        }
    }

    lateinit var bean: MessageBean
    lateinit var adapter: RecyclerAdapter<GongGaoBean.GGFileBean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gong_gao_detail)
        title = "内部公告"
        setBack(true)
        toolbar_menu.setImageResource(R.drawable.delete_all)
        var distance = Utils.dpToPx(context, 15)
        toolbar_menu.setPadding(distance, distance, 0, distance)
        toolbar_menu.visibility = View.VISIBLE
        toolbar_menu.setOnClickListener {
            SoguApi.getService(application,OtherService::class.java)
                    .deleteSysNews(bean.news_id!!)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            showCustomToast(R.drawable.icon_toast_success, "删除成功")
                            finish()
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
                        showCustomToast(R.drawable.icon_toast_fail, "删除失败")
                    })
        }
        bean = intent.getSerializableExtra(Extras.DATA) as MessageBean

        kotlin.run {
            adapter = RecyclerAdapter(context, { _adapter, parent, type ->
                val convertView = _adapter.getView(R.layout.layout_gg, parent)
                object : RecyclerHolder<GongGaoBean.GGFileBean>(convertView) {
                    val tvTitle = convertView.findViewById<TextView>(R.id.name) as TextView
                    override fun setData(view: View, data: GongGaoBean.GGFileBean, position: Int) {
                        tvTitle.text = data.filename
                    }
                }
            })
            adapter.onItemClick = { v, p ->
                var data = adapter.dataList.get(p)
                if (!TextUtils.isEmpty(data.url)) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(data.url)
                    startActivity(intent)
                }
            }
            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            dataList.layoutManager = layoutManager
            dataList.adapter = adapter
            dataList.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 10)))
        }

        SoguApi.getService(application,OtherService::class.java)
                .sysMessageInfo(bean.news_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload?.payload?.apply {
                            tvTitle.text = this.title
                            tvTime.text = this.time
                            //tvContent.text = this.contents
                            webview.loadDataWithBaseURL(null, this.contents, "text/html", "utf-8", null)
                            if (this.adjunct == null || this.adjunct!!.size == 0) {
                                attach.visibility = View.GONE
                            } else {
                                attach.visibility = View.VISIBLE
                                adapter.dataList.clear()
                                adjunct?.apply {
                                    adapter.dataList.addAll(this)
                                }
                                adapter.notifyDataSetChanged()
                            }
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        scroll_plain.visibility = View.GONE
                        iv_empty.visibility = View.VISIBLE
                    }
                }, { e ->
                    Trace.e(e)
                    scroll_plain.visibility = View.GONE
                    iv_empty.visibility = View.VISIBLE
                })
    }
}
