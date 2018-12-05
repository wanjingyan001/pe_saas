package com.sogukj.pe.module.project.businessDev

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.BrandBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.peExtended.defaultHeadImg
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
class BrandListActivity : BaseRefreshActivity(), SupportEmptyView {
   lateinit var adapter: RecyclerAdapter<BrandBean>
    lateinit var project: ProjectBean
    val df = SimpleDateFormat("yyyy-MM-dd")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_common)

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setBack(true)
        title = "商标信息"

        adapter = RecyclerAdapter(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_project_brand, parent)
            object : RecyclerHolder<BrandBean>(convertView) {

                val ivLogo = convertView.findViewById<ImageView>(R.id.iv_logo) as ImageView
                val tvTmName = convertView.findViewById<TextView>(R.id.tv_tmName) as TextView
                val tvRegNo = convertView.findViewById<TextView>(R.id.tv_regNo) as TextView
                val tvAppdate = convertView.findViewById<TextView>(R.id.tv_appdate) as TextView
                val tvStatus = convertView.findViewById<TextView>(R.id.tv_status) as TextView
                val tvIntCls = convertView.findViewById<TextView>(R.id.tv_intCls) as TextView
                override fun setData(view: View, data: BrandBean, position: Int) {
                    tvTmName.text = "${data.tmName}"
                    tvRegNo.text = "注册号:${data.regNo}"
                    tvAppdate.text = "申请时间:${data.appdate}"
                    tvStatus.text = "申请状态:${data.status}"
                    tvIntCls.text = "类别:${data.intCls}"
                    Glide.with(this@BrandListActivity)
                            .load(data.tmPic)
                            .apply(RequestOptions().error(defaultHeadImg()))
                            .into(ivLogo)
                }

            }
        })
        adapter.onItemClick = { v, p ->
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
//        refresh.setEnableLoadmore(true)
//        refresh.setOnRefreshListener(object : RefreshListenerAdapter() {
//            override fun onRefresh(refreshLayout: TwinklingRefreshLayout?) {
//                page = 1
//                doRequest()
//            }
//
//            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
//                ++page
//                doRequest()
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
        config.loadMoreEnable = true
        config.autoLoadMoreEnable = true
        config.disableContentWhenRefresh = true
        return config
    }



    var page = 1
    fun doRequest() {
        SoguApi.getService(application,InfoService::class.java)
                .listBrand(project.company_id!!, page = page)
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
                    SupportEmptyView.checkEmpty(this,adapter)
                    isLoadMoreEnable  = adapter.dataList.size % 20 == 0
                    adapter.notifyDataSetChanged()
                    if (page == 1)
                       finishRefresh()
                    else
                       finishLoadMore()
                })
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, BrandListActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
