package com.sogukj.pe.module.project

import android.app.Activity
import android.content.Intent
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
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.DateUtils
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.FlowLayout
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.module.news.NewsDetailActivity
import com.sogukj.pe.peUtils.SupportEmptyView
import com.sogukj.pe.service.NewService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_project_news.*
import kotlinx.android.synthetic.main.layout_loading.*
import org.jetbrains.anko.find

class ProjectNewsActivity : BaseRefreshActivity() {

    lateinit var adapter: RecyclerAdapter<NewsBean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_news)

        setBack(true)

        type = intent.getIntExtra(Extras.TYPE, 0)
        company_id = intent.getIntExtra(Extras.ID, 0)
        var title = intent.getStringExtra(Extras.TITLE)
        setTitle(title)


        adapter = RecyclerAdapter(context, { adapter, parent, type ->
            NewsHolder(adapter.getView(R.layout.item_main_news, parent))
        })

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_view.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = adapter

        adapter.onItemClick = { view, position ->
            val data = adapter.dataList[position]
            NewsDetailActivity.start(this, data)
        }

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

    var page = 1
    var type = 0
    var company_id = 0

    fun doRequest() {
        SoguApi.getService(application, NewService::class.java)
                .listNews(page = page, type = type, company_id = company_id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (page == 1)
                            adapter.dataList.clear()
                        payload.payload?.apply {
                            adapter.dataList.addAll(this)
                        }
                        adapter.notifyDataSetChanged()
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    iv_loading?.visibility = View.GONE
                }, { e ->
                    Trace.e(e)
                    iv_loading?.visibility = View.GONE
                    SupportEmptyView.checkEmpty(this, adapter)
                    finishLoad(page)
                }, {
                    SupportEmptyView.checkEmpty(this, adapter)
                    adapter.notifyDataSetChanged()
                    if (page == 1)
                        finishRefresh()
                    else
                        finishLoadMore()
                })
    }

    inner class NewsHolder(convertView: View) : RecyclerHolder<NewsBean>(convertView) {
        val tv_summary: TextView = convertView.find(R.id.tv_summary)
        val tv_time: TextView = convertView.find(R.id.tv_time)
        val tv_from: TextView = convertView.find(R.id.tv_from)
        val tags: FlowLayout = convertView.find(R.id.tags)
        val tv_date: TextView = convertView.find(R.id.tv_date)
        val icon: ImageView = convertView.find(R.id.imageIcon)
        override fun setData(view: View, data: NewsBean, position: Int) {
            var label = data.title
            tv_summary.text = Html.fromHtml(label)
            val strTime = data.time
            tv_time.visibility = View.GONE
            if (!TextUtils.isEmpty(strTime)) {
                try {
                    tv_date.text = DateUtils.getTimeFormatText(strTime)
                } catch (e: Exception) {
                    tv_date.text = DateUtils.getTimeFormatText("${strTime} 00:00")
                }
            }
            tv_from.text = data.source
            if (data.source.isNullOrEmpty()) {
                tv_from.visibility = View.GONE
            }
            tags.removeAllViews()
            data.tag?.split("#")
                    ?.forEach { str ->
                        if (!TextUtils.isEmpty(str)) {
                            val itemTag = View.inflate(this@ProjectNewsActivity, R.layout.item_tag_news, null)
                            val tvTag = itemTag.find<TextView>(R.id.tv_tag)
                            tvTag.text = str
                            tags.addView(itemTag)
                            data.setTags(this@ProjectNewsActivity, tags)
                        }
                    }

            if (data.url.isNullOrEmpty()) {
                var bitmap = BitmapFactory.decodeResource(resources, R.drawable.default_icon)
                var draw = RoundedBitmapDrawableFactory.create(resources, bitmap)
                draw.cornerRadius = Utils.dpToPx(context, 4).toFloat()
                icon.setBackgroundDrawable(draw)
            } else {
                Glide.with(context).asBitmap().load(data.url).into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(bitmap: Bitmap, glideAnimation: Transition<in Bitmap>?) {
                        var draw = RoundedBitmapDrawableFactory.create(resources, bitmap)
                        draw.cornerRadius = Utils.dpToPx(context, 4).toFloat()
                        icon.setBackgroundDrawable(draw)
                    }
                })
            }
        }

    }

    companion object {
        fun start(ctx: Activity, title: String, type: Int, id: Int) {
            var intent = Intent(ctx, ProjectNewsActivity::class.java)
            intent.putExtra(Extras.TITLE, title)
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.ID, id)
            ctx.startActivity(intent)
        }
    }
}
