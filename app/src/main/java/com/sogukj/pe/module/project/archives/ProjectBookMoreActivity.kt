package com.sogukj.pe.module.project.archives

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.scwang.smartrefresh.layout.api.RefreshFooter
import com.scwang.smartrefresh.layout.api.RefreshHeader
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.DateUtils
import com.sogukj.pe.baselibrary.utils.DownloadUtil
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.FileListBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.peUtils.OpenFileUtil
import com.sogukj.pe.peUtils.SupportEmptyView
import com.sogukj.pe.service.InfoService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_list_common.*
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/8/11.
 */
class ProjectBookMoreActivity : BaseRefreshActivity() {
    lateinit var adapter: RecyclerAdapter<FileListBean>
    lateinit var project: ProjectBean
    var type = 1
    val df = SimpleDateFormat("yyyy-MM-dd")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = intent.getIntExtra(Extras.TYPE, 1)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setContentView(R.layout.activity_list_common)
        title = "项目文书"
        setBack(true)
        iv_filter.visibility = View.INVISIBLE
        adapter = RecyclerAdapter<FileListBean>(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_project_book, parent)
            object : RecyclerHolder<FileListBean>(convertView) {
                val tvSummary = convertView.findViewById<TextView>(R.id.tv_summary) as TextView
                val tvDate = convertView.findViewById<TextView>(R.id.tv_date) as TextView
                val tvTime = convertView.findViewById<TextView>(R.id.tv_time) as TextView
                val tvType = convertView.findViewById<TextView>(R.id.tv_type) as TextView
                val tvName = convertView.findViewById<TextView>(R.id.tv_name) as TextView
                override fun setData(view: View, data: FileListBean, position: Int) {
                    tvSummary.text = data.doc_title
                    val strTime = DateUtils.timedate(data.add_time)
                    tvTime.visibility = View.GONE
                    if (!TextUtils.isEmpty(strTime)) {
                        val strs = strTime!!.trim().split(" ")
                        if (!TextUtils.isEmpty(strs.getOrNull(1))) {
                            tvTime.visibility = View.VISIBLE
                        }
                        tvDate.text = strs.getOrNull(0)
                        tvTime.text = strs.getOrNull(1)
                    }
                    tvType.text = data.name
                    if (data.name.isNullOrEmpty()) {
                        tvType.visibility = View.INVISIBLE
                    }
                    tvName.text = data.submitter
                }

            }
        })
        adapter.onItemClick = { v, p ->
            val data = adapter.getItem(p)
            //download(data.url!!, data.doc_title!!)
            if (!TextUtils.isEmpty(data.url)) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(data.url)
                startActivity(intent)
            }
        }
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = adapter

//        val header = ProgressLayout(this)
//        header.setColorSchemeColors(ContextCompat.getColor(this, R.color.color_main))
//        refresh.setHeaderView(header)
//        val footer = BallPulseView(this)
//        footer.setAnimatingColor(ContextCompat.getColor(this, R.color.color_main))
//        refresh.setBottomView(footer)
//        refresh.setOverScrollRefreshShow(false)
//        refresh.setEnableLoadmore(false)
//        refresh.setOnRefreshListener(object : RefreshListenerAdapter() {
//            override fun onRefresh(refreshLayout: TwinklingRefreshLayout?) {
//
//            }
//
//            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
//
//            }
//
//        })
//        refresh.setAutoLoadMore(true)
        handler.postDelayed({
            doRequest()
        }, 100)
    }

    override fun doRefresh() {
        page = 1
        doRequest()
    }

    override fun doLoadMore() {
        ++page
        doRequest()
    }

    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = false
        config.autoLoadMoreEnable = true
        config.disableContentWhenRefresh = true
        return config
    }

    override fun initRefreshHeader(): RefreshHeader?  = ClassicsHeader(this)

    override fun initRefreshFooter(): RefreshFooter?  = null

    fun download(url: String, fileName: String) {
        showCustomToast(R.drawable.icon_toast_common, "开始下载")
        DownloadUtil.getInstance().download(url, externalCacheDir.toString(), fileName, object : DownloadUtil.OnDownloadListener {
            override fun onDownloadSuccess(path: String?) {
                var intent = OpenFileUtil.openFile(context, path)
                if (intent == null) {
                    showCustomToast(R.drawable.icon_toast_fail, "文件类型不合格")
                } else {
                    startActivity(intent)
                }
            }

            override fun onDownloading(progress: Int) {
            }

            override fun onDownloadFailed() {
                showCustomToast(R.drawable.icon_toast_fail, "下载失败")
            }
        })
    }

    var page = 1
    fun doRequest() {
        SoguApi.getService(application,InfoService::class.java)
                .fileList(project.company_id!!, 1, dir_id = type, page = page)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (page == 1)
                            adapter.dataList.clear()
                        payload.payload?.apply {
                            adapter.dataList.addAll(this)
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_common, "暂无可用数据")
                    finishLoad(page)
                }, {
                    SupportEmptyView.checkEmpty(this, adapter)
                    isLoadMoreEnable = adapter.dataList.size % 20 == 0
                    adapter.notifyDataSetChanged()
                    if (page == 1)
                       finishRefresh()
                    else
                      finishLoadMore()
                })
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean, type: Int) {
            val intent = Intent(ctx, ProjectBookMoreActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(Extras.TYPE, type)
            ctx?.startActivity(intent)
        }
    }
}