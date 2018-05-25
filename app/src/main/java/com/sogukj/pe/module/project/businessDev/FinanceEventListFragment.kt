package com.sogukj.pe.module.project.businessDev

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.InvestEventBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.module.news.NewsListFragment
import com.sogukj.pe.peExtended.defaultHeadImg
import com.sogukj.pe.peUtils.SupportEmptyView
import com.sogukj.pe.service.InfoService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_list_news.*

/**
 * Created by qinfei on 17/7/18.
 */
class FinanceEventListFragment : BaseFragment(), SupportEmptyView {
    override val containerViewId: Int
        get() = R.layout.fragment_list_news //To change initializer of created properties use File | Settings | File Templates.

    lateinit var adapter: RecyclerAdapter<InvestEventBean>
    lateinit var project: ProjectBean
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = arguments!!.getSerializable(Extras.DATA) as ProjectBean
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = RecyclerAdapter(baseActivity!!, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_project_finance_event, parent)
            object : RecyclerHolder<InvestEventBean>(convertView) {

                val ivUser = convertView.findViewById<ImageView>(R.id.iv_user) as ImageView
                val tvProduct = convertView.findViewById<TextView>(R.id.tv_product) as TextView
                val tvRound = convertView.findViewById<TextView>(R.id.tv_round) as TextView
                val tvFinance = convertView.findViewById<TextView>(R.id.tv_finance) as TextView
                val tvLunci = convertView.findViewById<TextView>(R.id.tv_lunci) as TextView
                val tvCompany = convertView.findViewById<TextView>(R.id.tv_company) as TextView

                override fun setData(view: View, data: InvestEventBean, position: Int) {

                    tvProduct.text = data.product
                    tvFinance.text = "${data.tzdate}${data.money}"
                    tvLunci.text = "${getString(R.string.tv_proj_finance_lunci)}${data.lunci}"
                    tvCompany.text = Html.fromHtml(getString(R.string.tv_proj_product_company, data.organization_name))
                    Glide.with(baseActivity!!)
                            .load(data.iconOssPath)
                            .apply(RequestOptions().error(defaultHeadImg()))
                            .into(ivUser)
                }

            }
        })
        adapter.onItemClick = { v, p ->
        }
        val layoutManager = LinearLayoutManager(baseActivity!!)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
//        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = adapter

        val header = ProgressLayout(baseActivity!!)
        header.setColorSchemeColors(ContextCompat.getColor(baseActivity!!, R.color.color_main))
        refresh.setHeaderView(header)
        val footer = BallPulseView(baseActivity!!)
        footer.setAnimatingColor(ContextCompat.getColor(baseActivity!!, R.color.color_main))
        refresh.setBottomView(footer)
        refresh.setOverScrollRefreshShow(false)
        refresh.setEnableLoadmore(true)
        refresh.setOnRefreshListener(object : RefreshListenerAdapter() {
            override fun onRefresh(refreshLayout: TwinklingRefreshLayout?) {
                page = 1
                doRequest()
            }

            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
                ++page
                doRequest()
            }

        })
        refresh.setAutoLoadMore(true)
        handler.postDelayed({
            doRequest()
        }, 100)
    }

    var page = 1
    fun doRequest() {
        SoguApi.getService(baseActivity!!.application,InfoService::class.java)
                .listInvestEvent(project.company_id!!, page = page)
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
                    SupportEmptyView.checkEmpty(this,adapter)
                    refresh?.setEnableLoadmore(adapter.dataList.size % 20 == 0)
                    adapter.notifyDataSetChanged()
                    if (page == 1)
                        refresh?.finishRefreshing()
                    else
                        refresh?.finishLoadmore()
                })
    }

    companion object {
        val TAG = NewsListFragment::class.java.simpleName

        fun newInstance(project: ProjectBean): FinanceEventListFragment {
            val fragment = FinanceEventListFragment()
            val intent = Bundle()
            intent.putSerializable(Extras.DATA, project)
            fragment.arguments = intent
            return fragment
        }
    }
}