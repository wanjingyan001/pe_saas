package com.sogukj.pe.module.project

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.ProgressView
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.service.NewService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_dangerous.*
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.ctx

class DangerousActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, DangerousActivity::class.java)
            ctx?.startActivity(intent)
        }
    }

    lateinit var adapter: RecyclerAdapter<ProjectBean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dangerous)
        setBack(true)
        title = "项目危险级列表"

        adapter = RecyclerAdapter(context, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_dangerous, parent)
            object : RecyclerHolder<ProjectBean>(convertView) {
                val icon = convertView.find<ImageView>(R.id.company_icon)
                val projName = convertView.find<TextView>(R.id.name)
                val fzrName = convertView.find<TextView>(R.id.fuzeren)
                val tvSeq = convertView.find<TextView>(R.id.seq)
                override fun setData(view: View, data: ProjectBean, position: Int) {
                    if (data.logo.isNullOrEmpty()) {
                        var bitmap = BitmapFactory.decodeResource(resources, R.drawable.default_icon)
                        var draw = RoundedBitmapDrawableFactory.create(resources, bitmap)
                        draw.cornerRadius = Utils.dpToPx(context, 4).toFloat()
                        icon.setBackgroundDrawable(draw)
                    } else {
                        Glide.with(context).asBitmap().load(data.logo).into(object : SimpleTarget<Bitmap>() {
                            override fun onResourceReady(bitmap: Bitmap, glideAnimation: Transition<in Bitmap>?) {
                                var draw = RoundedBitmapDrawableFactory.create(resources, bitmap)
                                draw.cornerRadius = Utils.dpToPx(context, 4).toFloat()
                                icon.setBackgroundDrawable(draw)
                            }
                        })
                    }
                    projName.text = if (data.name.isNullOrEmpty()) data.shortName else data.name
                    fzrName.text = "负责人：${data.chairman}"
                    if (position == 0) {
                        tvSeq.text = ""
                        tvSeq.setBackgroundResource(R.drawable.danger1)
                    } else if (position == 1) {
                        tvSeq.text = ""
                        tvSeq.setBackgroundResource(R.drawable.danger2)
                    } else if (position == 2) {
                        tvSeq.text = ""
                        tvSeq.setBackgroundResource(R.drawable.danger3)
                    } else {
                        tvSeq.text = "${position + 1}"
                        tvSeq.setBackgroundColor(Color.WHITE)
                    }
                }
            }
        })
        adapter.onItemClick = { _, position ->
            ProjectActivity.start(context, adapter.dataList.get(position))
        }
        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_view.adapter = adapter

        refresh.setOnRefreshListener {
            doRequest()
            refresh.finishRefresh(1000)
        }

        doRequest()
    }

    fun doRequest() {
        SoguApi.getService(application, NewService::class.java)
                .negativeCompanyList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        adapter.dataList.clear()
                        payload?.payload?.apply {
                            adapter.dataList.addAll(this)
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    iv_empty.visibility = if (adapter.dataList.isEmpty()) View.VISIBLE else View.GONE
                }, {
                    iv_empty.visibility = if (adapter.dataList.isEmpty()) View.VISIBLE else View.GONE
                    adapter.notifyDataSetChanged()
                })
    }
}
