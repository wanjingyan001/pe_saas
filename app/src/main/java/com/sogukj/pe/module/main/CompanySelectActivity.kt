package com.sogukj.pe.module.main

import android.app.Activity
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.CustomSealBean
import com.sogukj.pe.bean.FundSmallBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.peUtils.SupportEmptyView
import com.sogukj.pe.service.ApproveService
import com.sogukj.pe.service.FundService
import com.sogukj.pe.service.NewService
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_company_select2.*
import kotlinx.android.synthetic.main.toolbar_search.*
import org.jetbrains.anko.backgroundColor

@Route(path = ARouterPath.CompanySelectActivity)
class CompanySelectActivity : BaseRefreshActivity() {
    lateinit var adapter: RecyclerAdapter<Any>
    var page = 1

    @JvmField
    @Autowired(name = Extras.ROUTE_PATH)
    var routerPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        ARouter.getInstance().inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_company_select2)
        setBack(true)
        toolbar_menu.visibility = View.INVISIBLE
        search_bar.setCancel(false, {})
        (search_bar.tv_cancel.parent as LinearLayout).backgroundColor = Color.parseColor("#ffffff")
        adapter = RecyclerAdapter(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_main_project_search, parent)
            object : RecyclerHolder<Any>(convertView) {
                val tv1 = convertView.findViewById<TextView>(R.id.tv1) as TextView
                override fun setData(view: View, data: Any, position: Int) {
                    if (data is CustomSealBean.ValueBean){
                        var label = data.name
                        if (TextUtils.isEmpty(label))
                            label = data.name
                        tv1.text = "${position + 1}.$label"
                    }else if (data is FundSmallBean){
                        tv1.text = data.fundName
                    }
                }
            }
        })
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = adapter
        toolbar_menu.setOnClickListener {
            search_bar.visibility = View.VISIBLE
        }
        adapter.onItemClick = { v, p ->
//            ARouter.getInstance().build(routerPath)
//                    .withSerializable(Extras.DATA, adapter.dataList[p])
//                    .navigation()
        }
        search_bar.onTextChange = { text ->
            page = 1
            handler.removeCallbacks(searchTask)
            handler.postDelayed(searchTask, 100)
        }
        search_bar.onSearch = { text ->
            page = 1
            getListData()
        }
        handler.postDelayed({
            getListData()
        }, 100)
    }


    val searchTask = Runnable {
        page = 1
        getListData()
    }

    fun doRequest() {
        SoguApi.getService(application, NewService::class.java)
                .listProject(offset = 0, uid = Store.store.getUser(this)!!.uid)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {

                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    showCustomToast(R.drawable.icon_toast_common, "暂无可用数据")
                })

    }

    private fun getListData() {
        if (routerPath.contains("project")) {
            title = "选择项目"
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
        } else if (routerPath.contains("fund")) {
            title = "选择基金"
            SoguApi.getService(application, FundService::class.java).getAllFunds(sort = FundSmallBean.FundDesc + FundSmallBean.RegTimeAsc, type = 0)
                    .execute {
                        onNext { payload ->
                            if (payload.isOk) {
                                if (page == 1)
                                    adapter.dataList.clear()
                                payload.payload?.apply {
                                    adapter.dataList.addAll(this)
                                }
                            }else{
                                showErrorToast("暂无可用数据")
                            }
                        }
                        onError { e ->
                            Trace.e(e)
                            //showToast("暂无可用数据")
                            showCustomToast(R.drawable.icon_toast_common, "暂无可用数据")
                        }
                        onComplete {
                            SupportEmptyView.checkEmpty(this@CompanySelectActivity, adapter)
                            isLoadMoreEnable = adapter.dataList.size % 20 == 0
                            adapter.notifyDataSetChanged()
                            if (page == 1)
                                finishRefresh()
                            else
                                finishLoadMore()
                        }
                    }
        }
    }


    override fun doRefresh() {
        page = 1
        getListData()
    }

    override fun doLoadMore() {
        page += 1
        getListData()
    }

    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = true
        config.autoLoadMoreEnable = true
        config.disableContentWhenRefresh = true
        return config
    }

}
