package com.sogukj.pe.module.dataSource

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.core.content.edit
import com.amap.api.mapcore.util.it
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.gson.Gson
import com.scwang.smartrefresh.layout.footer.BallPulseFooter
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.DownloadUtil
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.bean.PdfBook
import com.sogukj.pe.module.other.OnlinePreviewActivity
import com.sogukj.pe.service.DataSourceService
import com.sogukj.service.SoguApi
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter
import kotlinx.android.synthetic.main.activity_pdf_search.*
import kotlinx.android.synthetic.main.search_header.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.dip
import org.jetbrains.anko.sdk25.coroutines.textChangedListener

class PdfSearchActivity : BaseActivity() {

    companion object {
        fun start(context: Context, @DocumentType type: Int, category: Int? = null) {
            val intent = Intent(context, PdfSearchActivity::class.java)
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.TYPE1, category)
            context.startActivity(intent)
        }
    }

    private val type: Int by extraDelegate(Extras.TYPE, -1)
    private val category: Int? by extraDelegate(Extras.TYPE1, null)
    private var page = 1
    private lateinit var listAdapter: BookListAdapter
    private val documents = ArrayList<PdfBook>()
    private val downloaded = mutableSetOf<String>()//已下载文件的名称的缓存,用于处理下载按钮是否显示

    private lateinit var historyAdapter: TagAdapter<String>
    private val historyList = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_search)
        Gson().fromJson<Array<String>>(sp.getString(Extras.DOWNLOADED_PDF, ""), Array<String>::class.java)?.let {
            it.isNotEmpty().yes {
                downloaded.addAll(it)
            }
        }
        listAdapter = BookListAdapter(documents, downloaded.toList(), type)
        listAdapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val book = documents[position]
            PdfPreviewActivity.start(this,book, downloaded.contains(book.pdf_name))
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
        searchResultList.apply {
            layoutManager = LinearLayoutManager(ctx)
            addItemDecoration(SpaceItemDecoration(dip(10)))
            adapter = listAdapter
        }
        initData()
        initListener()
    }

    private fun initData() {
        val historyStr = sp.getString(Extras.SOURCE_PDF_HISTORY + type, "")
        val localHistory = historyStr.split(",").filter { it.isNotEmpty() }.toMutableList()
        localHistory.isNotEmpty().yes {
            historyList.addAll(localHistory)
            ll_empty_his.setVisible(false)
        }.otherWise {
            ll_empty_his.setVisible(true)
        }
        historyAdapter = PdfHistoryAdapter()
        tfl.adapter = historyAdapter

        refresh.isEnableRefresh = true
        refresh.isEnableLoadMore = true
        refresh.isEnableAutoLoadMore = true
        refresh.setRefreshHeader(ClassicsHeader(this))
        val footer = BallPulseFooter(ctx)
        footer.setIndicatorColor(Color.parseColor("#7BB4FC"))
        footer.setAnimatingColor(Color.parseColor("#7BB4FC"))
        refresh.setRefreshFooter(footer)
        refresh.setOnRefreshListener {
            page = 1
            et_search.textStr.isNotEmpty().yes {
                getPdfList(et_search.textStr)
            }.otherWise {
                getPdfList()
            }
        }
        refresh.setOnLoadMoreListener {
            page += 1
            et_search.textStr.isNotEmpty().yes {
                getPdfList(et_search.textStr)
            }.otherWise {
                getPdfList()
            }
        }
    }

    private fun initListener() {
        tv_cancel.clickWithTrigger {
            finish()
        }
        iv_del.clickWithTrigger {
            et_search.setText("")
        }
        tv_his.clickWithTrigger {
            sp.edit { putString(Extras.INVEST_SEARCH_HISTORY, "") }
            historyList.clear()
            historyAdapter = PdfHistoryAdapter()
            tfl.adapter = historyAdapter
            ll_empty_his.setVisible(true)
        }
        tfl.setOnTagClickListener { view, position, parent ->
            historyList.toMutableList().let {
                it.isNotEmpty().yes {
                    val str = it[position]
                    et_search.setText(str)
                    et_search.setSelection(str.length)
                }
            }
            true
        }
        et_search.textChangedListener {
            onTextChanged { charSequence, start, before, count ->
                et_search.textStr.isNotEmpty().yes {
                    getPdfList(et_search.textStr)
                }
            }
            afterTextChanged {
                if (et_search.text.isNotEmpty()) {
                    iv_del.setVisible(true)
                } else {
                    iv_del.setVisible(false)
                    et_search.setHint(R.string.search)
                    refresh.setVisible(false)
                }
            }
        }
    }

    @SuppressLint("WrongConstant")
    private fun getPdfList(searchKey: String? = null) {
        SoguApi.getService(application, DataSourceService::class.java)
                .getSourceBookList(page = page, keywords = searchKey, type = type, category = category)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                if (page == 1) {
                                    documents.clear()
                                }
                                documents.addAll(it)
                                listAdapter.notifyDataSetChanged()
                                ifNotNull(searchKey, it, { str, list ->
                                    list.isNotEmpty().yes {
                                        historyList.add(str)
                                    }
                                })
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                    onComplete {
                        onComplete {
                            (page == 1).yes {
                                refresh.finishRefresh()
                            }.otherWise {
                                refresh.finishLoadMore()
                            }
                            documents.isNotEmpty().yes {
                                search_iv_empty.setVisible(false)
                                refresh.setVisible(true)
                                historyLayout.setVisible(false)
                            }.otherWise {
                                search_iv_empty.setVisible(true)
                                refresh.setVisible(false)
                                historyLayout.setVisible(true)
                            }
                        }
                    }
                }
    }

    override fun onPause() {
        super.onPause()
        downloaded.isNotEmpty().yes {
            sp.edit { putString(Extras.DOWNLOADED_PDF, downloaded.jsonStr) }
        }
    }

    override fun onStop() {
        super.onStop()
        historyList.isNotEmpty().yes {
            sp.edit { putString(Extras.SOURCE_PDF_HISTORY + type, historyList.joinToString(",")) }
        }
    }

    inner class PdfHistoryAdapter : TagAdapter<String>(historyList.toMutableList()) {
        override fun getView(parent: FlowLayout?, position: Int, t: String?): View {
            val itemView = View.inflate(this@PdfSearchActivity, R.layout.search_his_item, null)
            val history = itemView.findViewById<TextView>(R.id.tv_item)
            history.text = t
            return itemView
        }
    }
}
