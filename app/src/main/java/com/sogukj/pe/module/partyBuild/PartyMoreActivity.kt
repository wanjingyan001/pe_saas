package com.sogukj.pe.module.partyBuild

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.IntRange
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.ArticleBean
import com.sogukj.pe.bean.FileBean
import com.sogukj.pe.module.partyBuild.PartyMainActivity.Companion.ARTICLE
import com.sogukj.pe.module.partyBuild.PartyMainActivity.Companion.FILE
import com.sogukj.pe.service.PartyBuildService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_party_more.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import kotlin.properties.Delegates

class PartyMoreActivity : BaseActivity() {
    var type: Int by Delegates.notNull()
    var id: Int by Delegates.notNull()
    var page = 1
    lateinit var adapter: RecyclerAdapter<Any>

    companion object {

        fun start(context: Context, tid: Int, @IntRange(from = 1, to = 2) type: Int, title: String) {
            val intent = Intent(context, PartyMoreActivity::class.java)
            intent.putExtra(Extras.ID, tid)
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.NAME, title)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_party_more)
        Utils.setWindowStatusBarColor(this, R.color.party_toolbar_red)
        type = intent.getIntExtra(Extras.TYPE, 1)
        id = intent.getIntExtra(Extras.ID, 0)
        val title = intent.getStringExtra(Extras.NAME)
        when (type) {
            ARTICLE -> {
                toolbar_title.text = "文章列表"
                articleList(id)
            }
            FILE -> {
                toolbar_title.text = "文件列表"
                categoryFileList(id)
            }
        }
        adapter = RecyclerAdapter(this, { _adapter, parent, _ ->
            val convertView = _adapter.getView(R.layout.item_party_list, parent)
            object : RecyclerHolder<Any>(convertView) {
                val contentTv = convertView.find<TextView>(R.id.contentTv)
                val timeTv = convertView.find<TextView>(R.id.timeTv)
                override fun setData(view: View, data: Any, position: Int) {
                    when (type) {
                        ARTICLE -> {
                            data as ArticleBean
                            contentTv.text = data.title
                            timeTv.text = data.time
                            view.setOnClickListener {
                                PartyDetailActivity.start(ctx, data.id!!, title)
                            }
                        }
                        else -> {
                            data as FileBean
                            contentTv.text = data.file_name
                            timeTv.text = data.time
                            view.setOnClickListener {
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data = Uri.parse(data.url)
                                context.startActivity(intent)
                            }
                        }
                    }

                }
            }
        })
        moreList.layoutManager = LinearLayoutManager(this)
        moreList.adapter = adapter

        val header = ProgressLayout(context)
        header.setColorSchemeColors(ContextCompat.getColor(this, R.color.color_main))
        refresh.setHeaderView(header)
        val footer = BallPulseView(context)
        footer.setAnimatingColor(ContextCompat.getColor(this, R.color.color_main))
        refresh.setBottomView(footer)
        refresh.setOverScrollRefreshShow(false)
        refresh.setEnableLoadmore(true)
        refresh.setOnRefreshListener(object : RefreshListenerAdapter() {
            override fun onRefresh(refreshLayout: TwinklingRefreshLayout?) {
                page = 1
                doRequest()
            }

            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
                page += 1
                doRequest()
            }

        })
        back.setOnClickListener {
            finish()
        }

    }

    fun doRequest() {
        when (type) {
            ARTICLE -> articleList(id)
            FILE -> categoryFileList(id)
        }
    }

    private fun articleList(id: Int) {
        SoguApi.getService(application,PartyBuildService::class.java)
                .articleList(id, page)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (page == 1) {
                            adapter.dataList.clear()
                        }
                        payload.payload?.let {
                            adapter.dataList.addAll(it)
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e -> Trace.e(e) }, {
                    if (page == 1) {
                        refresh?.finishRefreshing()
                    } else {
                        refresh?.finishLoadmore()
                    }
                    adapter.notifyDataSetChanged()
                })
    }


    private fun categoryFileList(id: Int) {
        SoguApi.getService(application,PartyBuildService::class.java)
                .categoryFileList(id, page)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (page == 1) {
                            adapter.dataList.clear()
                        }
                        payload.payload?.let {
                            adapter.dataList.addAll(it)
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e -> Trace.e(e) }, {
                    if (page == 1) {
                        refresh?.finishRefreshing()
                    } else {
                        refresh?.finishLoadmore()
                    }
                    adapter.notifyDataSetChanged()
                })
    }
}
