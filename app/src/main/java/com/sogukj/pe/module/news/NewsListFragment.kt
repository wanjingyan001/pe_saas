package com.sogukj.pe.module.news

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
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
import com.sogukj.pe.baselibrary.utils.DateUtils
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.FlowLayout
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.peUtils.SupportEmptyView
import com.sogukj.pe.service.NewService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_list_news.*
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.textColor
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by qinfei on 17/7/18.
 */
class NewsListFragment : BaseRefreshFragment(), SupportEmptyView {
    override val containerViewId: Int
        get() = R.layout.fragment_list_news //To change initializer of created properties use File | Settings | File Templates.

    lateinit var adapter: RecyclerAdapter<NewsBean>
    var index: Int = 0
    var type: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        index = arguments!!.getInt(Extras.INDEX)
        type = when (index) {
            0 -> null
            1 -> 3
            2 -> 1
            else -> null
        }
        Store.store.getRead(baseActivity!!)
    }

    fun isRead(data: NewsBean)
            = (Store.store.readList.contains("${KEY_NEWS}${data.data_id}"))

    fun read(data: NewsBean) {
        var readList = HashSet<String>()
        readList.addAll(Store.store.readList)
        readList.add("${KEY_NEWS}${data.data_id}")
        Store.store.setRead(baseActivity!!, readList)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RecyclerAdapter<NewsBean>(baseActivity!!, { _adapter, parent, type ->
            NewsHolder(_adapter.getView(R.layout.item_main_news, parent))
        })
        adapter.onItemClick = { v, p ->
            val news = adapter.getItem(p)
            NewsDetailActivity.start(baseActivity, news)
            read(news)
        }
        val layoutManager = LinearLayoutManager(baseActivity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = adapter
        showLoadding()
        handler.postDelayed({
            doRequest()
        }, 100)
    }

    private fun showLoadding() {
        getNewsFragment().showLoadding()
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


    override fun onStart() {
        super.onStart()
        doRequest()
    }

    var page = 1
    fun doRequest() {
        SoguApi.getService(baseActivity!!.application, NewService::class.java)
                .listNews(page = page, type = type, fuzzyQuery = queryTxt)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (page == 1) {
                            if (null != payload.payload && payload.payload!!.size > 0){
                                goneEmpty()
                            }else{
                                showEmpty()
                            }
                            adapter.dataList.clear()
                        }
                        payload.payload?.apply {
                            adapter.dataList.addAll(this)
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    goneLoadding()
                }, { e ->
                    Trace.e(e)
                    goneLoadding()
                    SupportEmptyView.checkEmpty(this, adapter)
                    loadDataEnd()
                }, {
                    SupportEmptyView.checkEmpty(this, adapter)
                    isLoadMoreEnable = adapter.dataList.size % 20 == 0
                    adapter.notifyDataSetChanged()
                    loadDataEnd()
                })
    }

    private fun goneLoadding() {
        getNewsFragment().goneLoadding()
    }

    private fun getNewsFragment():MainNewsFragment{
        return parentFragment as MainNewsFragment
    }
    private fun showEmpty(){
        fl_empty.visibility = View.VISIBLE
    }

    private fun goneEmpty(){
        fl_empty.visibility = View.INVISIBLE
    }
    private fun loadDataEnd() {
        if (page == 1)
            finishRefresh()
        else
            finishLoadMore()
    }

    private var queryTxt = ""

    fun setTagList(tags: ArrayList<String>) {
        page = 1
        queryTxt = ""
        if (tags.size == 0) {
            queryTxt = ""
        } else {
            for (tag in tags) {
                queryTxt = "${queryTxt},${tag}"
            }
            queryTxt = queryTxt.substring(1)
        }
        doRequest()
    }

    val fmt = SimpleDateFormat("yyyy/MM/dd HH:mm")
    val KEY_NEWS = "news"

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
                try {
                    tv_date.text = DateUtils.getTimeFormatText(strTime)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            tv_from.text = data.source
            if (data.source.isNullOrEmpty()) {
                tv_from.visibility = View.GONE
            }
            data.setTags(baseActivity!!, tags)
            var isRead = isRead(data)
            if (isRead) {
                tv_summary.textColor = resources.getColor(R.color.text_2)
            } else {
                tv_summary.textColor = resources.getColor(R.color.text_1)
            }

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

        fun newInstance(idx: Int): NewsListFragment {
            val fragment = NewsListFragment()
            val intent = Bundle()
            intent.putInt(Extras.INDEX, idx)
            fragment.arguments = intent
            return fragment
        }
    }
}