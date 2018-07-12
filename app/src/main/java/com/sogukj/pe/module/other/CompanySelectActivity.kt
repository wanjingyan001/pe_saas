package com.sogukj.pe.module.other

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.CustomSealBean
import com.sogukj.pe.peUtils.SupportEmptyView
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_list_search.*
import kotlinx.android.synthetic.main.toolbar.*

class CompanySelectActivity : BaseRefreshActivity() {
    lateinit var adapter: RecyclerAdapter<CustomSealBean.ValueBean>
    var page = 1

    companion object {
        fun start(ctx: Activity?) {
            ctx?.startActivityForResult(Intent(ctx, CompanySelectActivity::class.java), Extras.REQUESTCODE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_company_select)
        title = "选择项目"
        setBack(true)

        adapter = RecyclerAdapter(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_main_project_search, parent)
            object : RecyclerHolder<CustomSealBean.ValueBean>(convertView) {
                val tv1 = convertView.findViewById<TextView>(R.id.tv1) as TextView

                override fun setData(view: View, data: CustomSealBean.ValueBean, position: Int) {
                    tv1.text = data.name
                }
            }
        })

        adapter.onItemClick = { v, p ->
            Utils.closeInput(this, v)
            val data = adapter.dataList[p]
            intent.putExtra(Extras.DATA, data)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = adapter

        toolbar_menu.setOnClickListener {
            search_bar.visibility = View.VISIBLE
        }
        search_bar.setCancel(true, {
            page = 1
            search_bar.visibility = View.GONE
            search_bar.search = ""
            doRequest()
        })
        search_bar.onTextChange = { text ->
            page = 1
            handler.removeCallbacks(searchTask)
            handler.postDelayed(searchTask, 100)
        }
        search_bar.onSearch = { text ->
            page = 1
            doRequest()
        }
        handler.postDelayed({
            doRequest()
        }, 100)
    }

    private val searchTask = Runnable {
        page = 1
        doRequest()
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
        config.loadMoreEnable = true
        config.autoLoadMoreEnable = true
        config.disableContentWhenRefresh = true
        return config
    }

    fun doRequest() {
        val text = search_bar.search
        val params = HashMap<String, Any>()
        params.put("type", 2)
        params.put("page", page)
        params.put("pageSize", 20)
        params.put("fuzzyQuery", text)
        SoguApi.getService(application, OtherService::class.java)
                .listSelector(params)
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
                    //showToast("暂无可用数据")
                    showCustomToast(R.drawable.icon_toast_common, "暂无可用数据")
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
}
