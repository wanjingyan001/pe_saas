package com.sogukj.pe.module.dataSource

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.content.edit
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.scwang.smartrefresh.layout.footer.BallPulseFooter
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.PatentItem
import com.sogukj.pe.service.DataSourceService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_patent_list.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.sdk25.coroutines.textChangedListener

class PatentListActivity : BaseActivity() {
    private val data = mutableListOf<PatentItem>()
    private lateinit var listAdapter: PatentAdapter
    private var page = 1
    private var search by extraDelegate(Extras.CODE, "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patent_list)
        initView()
        showLoadding()
        getPatentList(search)
    }

    private fun initView() {
        refresh.apply {
            isEnableRefresh = true
            isEnableLoadMore = true
            isEnableAutoLoadMore = true
            setRefreshHeader(ClassicsHeader(ctx))
            val footer = BallPulseFooter(ctx)
            footer.setIndicatorColor(Color.parseColor("#7BB4FC"))
            footer.setAnimatingColor(Color.parseColor("#7BB4FC"))
            setRefreshFooter(footer)
            setOnRefreshListener {
                page = 1
                getPatentList(searchEdt.textStr)
            }
            setOnLoadMoreListener {
                page += 1
                getPatentList(searchEdt.textStr)
            }
        }
        listAdapter = PatentAdapter(data)
        listAdapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            PatentDetailActivity.start(this, data[position])
        }
        patentList.apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = listAdapter
        }
        searchEdt.textChangedListener {
            afterTextChanged {
                clear.setVisible(searchEdt.textStr.isNotEmpty())
            }
        }
        searchEdt.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchEdt.postDelayed({
                    searchEdt.textStr.isNotEmpty().yes {
                        Utils.closeInput(this, searchEdt)
                        clear.setVisible(true)
                        page = 1
                        showLoadding()
                        getPatentList(searchEdt.textStr)
                    }.otherWise {
                        clear.setVisible(false)
                    }
                }, 300)
                true
            }
            false
        }
        searchEdt.setText(search)
        searchEdt.setSelection(search.length)
        clear.clickWithTrigger {
            searchEdt.setText("")
        }
        back.clickWithTrigger {
            finish()
        }

        Glide.with(ctx)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif")).into(iv_loading)
    }

    fun showLoadding() {
        view_recover.visibility = View.VISIBLE
    }

    fun goneLoadding() {
        view_recover.visibility = View.INVISIBLE
    }

    private fun getPatentList(searchKey: String? = null) {
        SoguApi.getService(application, DataSourceService::class.java)
                .getPatentList(page, searchKey)
                .execute {
                    onSubscribe {
                        sp.getBoolean(Extras.IS_FIRST_PATENT, true).yes {
                            sp.edit { putBoolean(Extras.IS_FIRST_PATENT, false) }
                        }
                    }
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                listAdapter.searchKey = searchKey
                                if (page == 1) {
                                    data.clear()
                                }
                                data.addAll(it)
                                listAdapter.notifyDataSetChanged()
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                    onComplete {
                        goneLoadding()
                        searchEdt.clearFocus()
                        if (page == 1) {
                            refresh.finishRefresh()
                        } else {
                            refresh.finishLoadMore(0)
                        }
                        data.isNotEmpty().yes {
                            patentList.setVisible(true)
                            emptyImg.setVisible(false)
                        }.otherWise {
                            patentList.setVisible(false)
                            emptyImg.setVisible(true)
                        }

                    }
                    onError {
                        goneLoadding()
                        if (page == 1) {
                            patentList.setVisible(false)
                            emptyImg.setVisible(true)
                            refresh.finishRefresh()
                        } else {
                            refresh.finishLoadMore(0)
                        }
                        Log.e("TAG", "专利列表 error ==" + it.message)
                        showErrorToast("数据获取失败")
                    }
                }
    }
}
