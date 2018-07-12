package com.sogukj.pe.module.project.businessDev

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
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
import com.sogukj.pe.bean.ProductBean
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
class ProductInfoActivity : BaseRefreshActivity() {
    lateinit var adapter: RecyclerAdapter<ProductBean>
    lateinit var project: ProjectBean
    val df = SimpleDateFormat("yyyy-MM-dd")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_common)

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setBack(true)
        title = "竟品信息"

        adapter = RecyclerAdapter<ProductBean>(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_project_product_info, parent)
            object : RecyclerHolder<ProductBean>(convertView) {

                val ivUser = convertView.findViewById<ImageView>(R.id.iv_user) as ImageView
                val tvProduct = convertView.findViewById<TextView>(R.id.tv_product) as TextView
                val tvRound = convertView.findViewById<TextView>(R.id.tv_round) as TextView
                val tvYewu = convertView.findViewById<TextView>(R.id.tv_yewu) as TextView
                val tvDate = convertView.findViewById<TextView>(R.id.tv_date) as TextView
                val tvCompany = convertView.findViewById<TextView>(R.id.tv_company) as TextView

                override fun setData(view: View, data: ProductBean, position: Int) {

                    tvProduct.text = data.jingpinProduct
                    tvYewu.text = "${getString(R.string.tv_proj_product_biz)}${data.yewu}"
                    tvDate.text = "${getString(R.string.tv_proj_product_create_date)}${data.date}"
                    tvCompany.text = Html.fromHtml(getString(R.string.tv_proj_product_company, data.companyName))
                    Glide.with(this@ProductInfoActivity)
                            .load(data.icon)
                            .apply(RequestOptions().error(defaultHeadImg()))
                            .into(ivUser)
                    tvRound.visibility = View.GONE
                    data.round?.trim()?.takeIf {
                        it.length == 0
                    }.apply {
                        tvRound.visibility = View.VISIBLE
                        tvRound.text = this
                    }
                }

            }
        })
        adapter.onItemClick = { v, p ->
        }
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
//        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = adapter

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
        SoguApi.getService(application, InfoService::class.java)
                .listProductInfo(project.company_id!!, page = page)
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
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, ProductInfoActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
