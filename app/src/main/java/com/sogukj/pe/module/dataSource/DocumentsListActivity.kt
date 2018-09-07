package com.sogukj.pe.module.dataSource

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.IntRange
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.View
import androidx.core.content.edit
import com.alibaba.android.arouter.facade.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.scwang.smartrefresh.layout.api.RefreshFooter
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.DownloadUtil
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.bean.PdfBook
import com.sogukj.pe.module.other.OnlinePreviewActivity
import com.sogukj.pe.service.DataSourceService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_prospectus_list.*
import org.jetbrains.anko.ctx
@Route(path = ARouterPath.DocumentsListActivity)
class DocumentsListActivity : BaseRefreshActivity() {
    companion object {
        fun start(context: Context, @DocumentType type: Int) {
            val intent = Intent(context, DocumentsListActivity::class.java)
            intent.putExtra(Extras.TYPE, type)
            context.startActivity(intent)
        }
    }

    private var type: Int = -1
    private var page = 1
    private lateinit var listAdapter: BookListAdapter
    private val documents = ArrayList<PdfBook>()
    private val downloaded = mutableSetOf<String>()//已下载文件的名称的缓存,用于处理下载按钮是否显示
    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prospectus_list)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        Utils.setWindowStatusBarColor(this, R.color.white)
        setBack(true)
        type = intent.getIntExtra(Extras.TYPE, -1)
        when (type) {
            DocumentType.INTELLIGENT -> {
                title = "智能文书"
                filterCondition.setVisible(true)
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }
            DocumentType.EQUITY -> {
                title = "招股书"
                filterCondition.setVisible(false)
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
            DocumentType.INDUSTRY_REPORTS -> {
                title = "热门行业研报"
                filterCondition.setVisible(false)
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
            else -> {
                throw Exception("类型错误")
            }
        }
        Gson().fromJson<Array<String>>(sp.getString(Extras.DOWNLOADED_PDF, ""), Array<String>::class.java)?.let {
            it.isNotEmpty().yes {
                downloaded.addAll(it)
            }
        }

        listAdapter = BookListAdapter(documents, downloaded.toList())
        listAdapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val book = documents[position]
            PdfPreviewActivity.start(this, book.pdf_path, book.pdf_name, downloaded.contains(book.pdf_name))
        }
        listAdapter.onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
            val book = documents[position]
            showProgress("正在下载")
            DownloadUtil.getInstance().download(book.pdf_path, externalCacheDir.toString(), book.pdf_name, object : DownloadUtil.OnDownloadListener {
                override fun onDownloadSuccess(path: String?) {
                    downloaded.add(book.pdf_name)
                    hideProgress()
                    listAdapter.downloaded = downloaded.toList()
                    listAdapter.notifyItemChanged(position)
                }

                override fun onDownloading(progress: Int) {
                    progressDialog?.apply {
                        this.progress = progress
                    }
                }

                override fun onDownloadFailed() {
                    hideProgress()
                }

            })
        }
        filterResultList.apply {
            layoutManager = LinearLayoutManager(ctx)
            addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
            adapter = listAdapter
        }
        getPdfList()
        searchLayout.clickWithTrigger {
            PdfSearchActivity.start(this, type)
        }
    }

    @SuppressLint("WrongConstant")
    private fun getPdfList(searchKey: String? = null) {
        SoguApi.getService(application, DataSourceService::class.java)
                .getSourceBookList(page = page, keywords = searchKey, type = type)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                if (page == 1) {
                                    documents.clear()
                                }
                                documents.addAll(it)
                                listAdapter.notifyDataSetChanged()
                                filterResultTv.text = Html.fromHtml(getString(R.string.tv_title_result_search2, (payload.total as Double).toInt()))
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }

    override fun doRefresh() {
        page = 1
        getPdfList()
    }

    override fun doLoadMore() {
        page += 1
        getPdfList()
    }

    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = true
        config.autoLoadMoreEnable = true
        config.scrollContentWhenLoaded = true
        config.disableContentWhenRefresh = true
        return config
    }

    override fun initRefreshFooter(): RefreshFooter? = null

    override fun onPause() {
        super.onPause()
        downloaded.isNotEmpty().yes {
            sp.edit { putString(Extras.DOWNLOADED_PDF, downloaded.jsonStr) }
        }
    }
}
