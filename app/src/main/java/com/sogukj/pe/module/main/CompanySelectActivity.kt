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
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.CustomSealBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.peUtils.SupportEmptyView
import com.sogukj.pe.service.ApproveService
import com.sogukj.pe.service.NewService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_company_select2.*
import kotlinx.android.synthetic.main.toolbar_search.*
import org.jetbrains.anko.backgroundColor

@Route(path = ARouterPath.CompanySelectActivity)
class CompanySelectActivity : BaseRefreshActivity() {
    lateinit var adapter: RecyclerAdapter<ProjectBean>
    var offset = 0

    @JvmField
    @Autowired(name = "ext.route.path")
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
            object : RecyclerHolder<ProjectBean>(convertView) {
                val tv1 = convertView.findViewById<TextView>(R.id.tv1) as TextView
                override fun setData(view: View, data: ProjectBean, position: Int) {
                    var label = data.shortName
                    if (TextUtils.isEmpty(label))
                        label = data.name
                    tv1.text = "${position + 1}.$label"
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
            ARouter.getInstance().build(routerPath)
                    .withSerializable(Extras.DATA, adapter.dataList[p])
                    .navigation()
        }
        search_bar.onTextChange = { text ->
            offset = 0
            handler.removeCallbacks(searchTask)
            handler.postDelayed(searchTask, 100)
        }
        search_bar.onSearch = { text ->
            offset = 0
            doRequest()
        }
        handler.postDelayed({
            doRequest()
        }, 100)
    }


    val searchTask = Runnable {
        offset = 0
        doRequest()
    }

    fun doRequest() {

        SoguApi.getService(application, NewService::class.java)
                .listProject(offset = offset, uid = Store.store.getUser(this)!!.uid)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (offset == 0)
                            adapter.dataList.clear()
                        payload.payload?.apply {
                            adapter.dataList.addAll(this)
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    showCustomToast(R.drawable.icon_toast_common, "暂无可用数据")
                }, {
                    SupportEmptyView.checkEmpty(this, adapter)
                    isLoadMoreEnable = adapter.dataList.size % 20 == 0
                    adapter.notifyDataSetChanged()
                    if (offset == 0)
                        finishRefresh()
                    else
                        finishLoadMore()
                })

    }


    override fun doRefresh() {
        offset = 0
        doRequest()
    }

    override fun doLoadMore() {
        offset = adapter.dataList.size
        doRequest()
    }

    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = true
        config.autoLoadMoreEnable = true
        config.disableContentWhenRefresh = true
        return config
    }

}
