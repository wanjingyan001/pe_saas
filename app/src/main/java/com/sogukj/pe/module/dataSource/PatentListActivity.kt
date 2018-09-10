package com.sogukj.pe.module.dataSource

import android.databinding.DataBindingUtil.setContentView
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
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
        getPatentList(search)
    }

    private fun initView() {
        refresh.apply {
            isEnableRefresh = true
            isEnableLoadMore = true
            isEnableAutoLoadMore = true
            setRefreshHeader(ClassicsHeader(ctx))
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
        searchEdt.setText(search)
        searchEdt.setSelection(search.length)
        searchEdt.textChangedListener {
            afterTextChanged {
                searchEdt.postDelayed({
                    searchEdt.textStr.isNotEmpty().yes {
                        clear.setVisible(true)
                        page = 1
                        getPatentList(searchEdt.textStr)
                    }.otherWise {
                        clear.setVisible(false)
                    }
                }, 300)
            }
        }
        clear.clickWithTrigger {
            searchEdt.setText("")
        }
        back.clickWithTrigger {
            finish()
        }
    }


    private fun getPatentList(searchKey: String? = null) {
        SoguApi.getService(application, DataSourceService::class.java)
                .getPatentList(page, searchKey)
                .execute {
                    onSubscribe {
                        Utils.closeInput(ctx, searchEdt)
                        showProgress("正在请求数据")
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
                        hideProgress()
                        if (page == 1) {
                            refresh.finishRefresh()
                        } else {
                            refresh.finishLoadMore()
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
                        hideProgress()
                        patentList.setVisible(false)
                        emptyImg.setVisible(true)
                    }
                }
    }
}
