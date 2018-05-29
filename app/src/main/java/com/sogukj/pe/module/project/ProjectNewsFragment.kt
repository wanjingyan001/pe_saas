package com.sogukj.pe.module.project

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseRefreshFragment
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.FlowLayout
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.module.news.NewsDetailActivity
import com.sogukj.pe.module.news.NewsListFragment
import com.sogukj.pe.peUtils.SupportEmptyView
import com.sogukj.pe.service.NewService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_list_project_news.*
import org.jetbrains.anko.support.v4.ctx
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/7/18.
 */
class ProjectNewsFragment : BaseRefreshFragment(), SupportEmptyView {
    override val containerViewId: Int
        get() = R.layout.fragment_list_project_news //To change initializer of created properties use File | Settings | File Templates.

    lateinit var adapter: RecyclerAdapter<NewsBean>
    var type: Int = 1
    lateinit var project: ProjectBean
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = arguments!!.getSerializable(Extras.DATA) as ProjectBean
        type = arguments!!.getInt(Extras.TYPE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = RecyclerAdapter(baseActivity!!, { _adapter, parent, type ->
            NewsHolder(_adapter.getView(R.layout.item_main_news, parent))
        })
        adapter.onItemClick = { v, p ->
            val news = adapter.getItem(p)
            NewsDetailActivity.start(baseActivity, news)
        }
        val layoutManager = LinearLayoutManager(baseActivity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_view.addItemDecoration(DividerItemDecoration(baseActivity, DividerItemDecoration.VERTICAL))
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = adapter

//        val header = ProgressLayout(baseActivity)
//        header.setColorSchemeColors(ContextCompat.getColor(baseActivity!!, R.color.color_main))
//        refresh.setHeaderView(header)
//        val footer = BallPulseView(baseActivity)
//        footer.setAnimatingColor(ContextCompat.getColor(baseActivity!!, R.color.color_main))
//        refresh.setBottomView(footer)
//        refresh.setOverScrollRefreshShow(false)
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
            refresh.autoRefresh()
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
        config.autoLoadMoreEnable = true
        config.loadMoreEnable = true
        config.disableContentWhenRefresh = true
        return config
    }


    var page = 1
    fun doRequest() {
        SoguApi.getService(baseActivity!!.application, NewService::class.java)
                .listNews(pageSize = 20, page = page, type = type, company_id = project.company_id)
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

    val fmt = SimpleDateFormat("yyyy/MM/dd HH:mm")

    inner class NewsHolder(view: View)
        : RecyclerHolder<NewsBean>(view) {
        val tv_summary = convertView.findViewById<TextView>(R.id.tv_summary) as TextView
        val tv_time = convertView.findViewById<TextView>(R.id.tv_time) as TextView
        val tv_from = convertView.findViewById<TextView>(R.id.tv_from) as TextView
        val tags = convertView.findViewById<FlowLayout>(R.id.tags) as FlowLayout
        val tv_date = convertView.findViewById<TextView>(R.id.tv_date) as TextView
        val iv_icon = convertView.findViewById<ImageView>(R.id.imageIcon) as ImageView

        override fun setData(view: View, data: NewsBean, position: Int) {
            var label = data.title
            tv_summary.text = Html.fromHtml(label)
            val strTime = data.time
            tv_time.visibility = View.GONE
            if (!TextUtils.isEmpty(strTime)) {
                val strs = strTime!!.trim().split(" ")
                if (!TextUtils.isEmpty(strs.getOrNull(1))) {
                    tv_time.visibility = View.VISIBLE
                }
                tv_date.text = strs
                        .getOrNull(0)
                tv_time.text = strs
                        .getOrNull(1)
            }
            tv_from.text = data.source
            data.setTags(baseActivity!!, tags)

            if (data.url.isNullOrEmpty()) {
                var bitmap = BitmapFactory.decodeResource(resources, R.drawable.default_icon)
                var draw = RoundedBitmapDrawableFactory.create(resources, bitmap)
                draw.cornerRadius = Utils.dpToPx(context, 4).toFloat()
                iv_icon.setBackgroundDrawable(draw)
            } else {
                Glide.with(ctx).asBitmap().load(data.url).into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(bitmap: Bitmap, glideAnimation: Transition<in Bitmap>?) {
                        var draw = RoundedBitmapDrawableFactory.create(resources, bitmap)
                        draw.cornerRadius = Utils.dpToPx(context, 4).toFloat()
                        iv_icon.setBackgroundDrawable(draw)
                    }
                })
            }
        }

    }

    companion object {
        val TAG = NewsListFragment::class.java.simpleName

        fun newInstance(project: ProjectBean, type: Int = 1): ProjectNewsFragment {
            val fragment = ProjectNewsFragment()
            val intent = Bundle()
            intent.putSerializable(Extras.DATA, project)
            intent.putInt(Extras.TYPE, type)
            fragment.arguments = intent
            return fragment
        }
    }
}