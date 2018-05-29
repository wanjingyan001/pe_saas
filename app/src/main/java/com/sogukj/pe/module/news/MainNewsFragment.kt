package com.sogukj.pe.module.news

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v4.view.ViewPager
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.text.InputType
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseRefreshFragment
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.ArrayPagerAdapter
import com.sogukj.pe.baselibrary.widgets.FlowLayout
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.NewsBean
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.NewService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_main_news.*
import kotlinx.android.synthetic.main.search_view.*
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.textColor
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by qinfei on 17/7/18.
 */
class MainNewsFragment : BaseRefreshFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_main_news //To change initializer of created properties use File | Settings | File Templates.

    val fragments = arrayOf(
            NewsListFragment.newInstance(0),
            NewsListFragment.newInstance(1)
            //NewsListFragment.newInstance(2)
    )
    lateinit var adapter: RecyclerAdapter<NewsBean>
    lateinit var hisAdapter: RecyclerAdapter<String>
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //toolbar_back.visibility = View.GONE
        back.setOnClickListener {
            baseActivity?.finish()
        }
        Utils.setUpIndicatorWidth(tabs, 70, 70, context)
        adapter = RecyclerAdapter<NewsBean>(baseActivity!!, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_main_news, parent)
            object : RecyclerHolder<NewsBean>(convertView) {
                val tv_summary = convertView.findViewById<TextView>(R.id.tv_summary) as TextView
                val tv_time = convertView.findViewById<TextView>(R.id.tv_time) as TextView
                val tv_from = convertView.findViewById<TextView>(R.id.tv_from) as TextView
                val tags = convertView.findViewById<FlowLayout>(R.id.tags) as FlowLayout
                val tv_date = convertView.findViewById<TextView>(R.id.tv_date) as TextView
                val iv_icon = convertView.findViewById<ImageView>(R.id.imageIcon) as ImageView

                override fun setData(view: View, data: NewsBean, position: Int) {
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
                    var label = data.title
                    if (!TextUtils.isEmpty(label) && !TextUtils.isEmpty(key)) {
                        label = label!!.replaceFirst(key, "<font color='#ff3300'>${key}</font>")
                    }
                    tv_summary.text = Html.fromHtml(label)
                    val strTime = data.time
                    tv_time.visibility = View.GONE
                    if (!TextUtils.isEmpty(strTime)) {
                        val strs = strTime!!.trim().split(" ")
                        if (!TextUtils.isEmpty(strs.getOrNull(1))) {
                            tv_time.visibility = View.VISIBLE
                        }
//                        tv_date.text = strs
//                                .getOrNull(0)
                        try {
                            tv_date.text = Utils.formatDate2(strTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        tv_time.text = strs
                                .getOrNull(1)
                    }
                    tv_from.text = data.source

                    data.setTags(baseActivity!!, tags)
                }
            }
        })
        hisAdapter = RecyclerAdapter<String>(baseActivity!!, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_project_search_item, parent)
            object : RecyclerHolder<String>(convertView) {
                val tv1 = convertView.findViewById<TextView>(R.id.tv1) as TextView
                val delete = convertView.findViewById<ImageView>(R.id.delete) as ImageView
                override fun setData(view: View, data: String, position: Int) {
                    tv1.text = data
                    delete.setOnClickListener {
                        hisAdapter.dataList.removeAt(position)
                        hisAdapter.notifyDataSetChanged()
                        Store.store.newsSearchRemover(baseActivity!!, position)
                    }
                }
            }
        })
        run {
            val layoutManager = LinearLayoutManager(baseActivity)
            recycler_his.addItemDecoration(DividerItemDecoration(baseActivity, DividerItemDecoration.VERTICAL))
            recycler_his.layoutManager = layoutManager
            recycler_his.adapter = hisAdapter
            hisAdapter.onItemClick = { v, p ->
                val data = hisAdapter.dataList.get(p)
                search_view.search = (data)
                doSearch(data)

            }
        }
        run {
            val layoutManager = LinearLayoutManager(baseActivity)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            recycler_result.addItemDecoration(DividerItemDecoration(baseActivity, DividerItemDecoration.VERTICAL))
            recycler_result.layoutManager = layoutManager
            recycler_result.adapter = adapter

            adapter.onItemClick = { v, p ->
                val news = adapter.getItem(p)
                NewsDetailActivity.start(baseActivity, news)
//                read(news)
            }
        }
        tv_result_title.text = Html.fromHtml(getString(R.string.tv_title_result_news, 0))
        search_view.tv_cancel.visibility = View.VISIBLE
        search_view.tv_cancel.setOnClickListener {
            this.key = ""
            search_view.search = ""
            ll_search.visibility = View.GONE

            hisAdapter.dataList.clear()
            hisAdapter.dataList.addAll(Store.store.newsSearch(baseActivity!!))
            hisAdapter.notifyDataSetChanged()
            ll_history.visibility = View.VISIBLE
        }
        iv_clear.setOnClickListener {
            MaterialDialog.Builder(ctx)
                    .theme(Theme.LIGHT)
                    .title("提示")
                    .content("确认全部删除?")
                    .positiveText("确认")
                    .negativeText("取消")
                    .onPositive { dialog, which ->
                        Store.store.newsSearchClear(baseActivity!!)
                        hisAdapter.dataList.clear()
                        hisAdapter.dataList.addAll(Store.store.newsSearch(baseActivity!!))
                        hisAdapter.notifyDataSetChanged()
                        last_search_layout.visibility = View.GONE
                    }
                    .show()
        }
        search_view.onSearch = { text ->
            if (null != text && !TextUtils.isEmpty(text))
                doSearch(text)
        }
        iv_search.setOnClickListener {
            ll_search.visibility = View.VISIBLE
            et_search.postDelayed({
                et_search.inputType = InputType.TYPE_CLASS_TEXT
                et_search.isFocusable = true
                et_search.isFocusableInTouchMode = true
                et_search.requestFocus()
                Utils.toggleSoftInput(baseActivity, et_search)
            }, 100)
            if (Store.store.newsSearch(baseActivity!!).isEmpty()) {
                last_search_layout.visibility = View.GONE
            } else {
                last_search_layout.visibility = View.VISIBLE
            }
        }
        var adapter = ArrayPagerAdapter(childFragmentManager, fragments)
        view_pager.adapter = adapter
        tabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                view_pager?.currentItem = tab.position
                resetGrid()
            }

        })
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                tabs?.getTabAt(position)?.select()
            }

        })
        val search = Store.store.newsSearch(baseActivity!!)
        hisAdapter.dataList.clear()
        hisAdapter.dataList.addAll(search)
        hisAdapter.notifyDataSetChanged()
        ll_history.visibility = View.VISIBLE
        iv_filter.setOnClickListener {
            if (fl_filter.visibility == View.GONE) {
                view_pager.visibility = View.GONE
                fl_filter.visibility = View.VISIBLE
            } else {
                view_pager.visibility = View.VISIBLE
                fl_filter.visibility = View.GONE
            }
        }

        btn_reset.setOnClickListener {
            resetGrid()
        }

        btn_ok.setOnClickListener {
            fragments[view_pager.currentItem].setTagList(getTagList())
            view_pager.visibility = View.VISIBLE
            fl_filter.visibility = View.GONE
        }

        loadTags()
    }


    override fun doRefresh() {
        page = 1
        handler.post(searchTask)
    }

    override fun doLoadMore() {
        ++page
        handler.post(searchTask)
    }

    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = true
        config.autoLoadMoreEnable = true
        config.disableContentWhenRefresh = true
        return config
    }

    fun loadTags() {
        SoguApi.getService(baseActivity!!.application, NewService::class.java)
                .getHotTag()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        var tags = payload.payload!!
                        for (i in 0 until grid.childCount) {
                            var child = grid.getChildAt(i) as TextView
                            try {
                                var tag = tags.get(i)
                                child.text = tag
                                child.setOnClickListener {
                                    if (child.tag.equals("F")) {
                                        child.textColor = Color.parseColor("#1787fb")
                                        child.setBackgroundResource(R.drawable.tg_bg_t)
                                        child.tag = "T"
                                    } else {
                                        child.textColor = Color.parseColor("#808080")
                                        child.setBackgroundResource(R.drawable.tag_bg_f)
                                        child.tag = "F"
                                    }
                                }
                            } catch (e: IndexOutOfBoundsException) {
                                child.visibility = View.GONE
                                child.text = ""
                            }
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_fail, "请求失败")
                })
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {   // 不在最前端显示 相当于调用了onPause();

        } else {  // 在最前端显示 相当于调用了onResume();
            resetGrid()
            ll_search.visibility = View.GONE
        }
    }

    fun resetGrid() {
        fragments[view_pager.currentItem].setTagList(ArrayList<String>())
        view_pager.visibility = View.VISIBLE
        fl_filter.visibility = View.GONE
        for (i in 0 until grid.childCount) {
            var child = grid.getChildAt(i) as TextView
            if (child.text.isEmpty() && child.visibility == View.GONE) {
                continue
            }
            child.textColor = Color.parseColor("#808080")
            child.setBackgroundResource(R.drawable.tag_bg_f)
            child.tag = "F"
        }
    }

    fun getTagList(): ArrayList<String> {
        //获取gridlayout中的tag
        val tagList = ArrayList<String>()
        (0 until grid.childCount)
                .map { grid.getChildAt(it) as TextView }
                .filter { !(it.text.isEmpty() && it.visibility == View.GONE) && it.tag == "T" }
                .mapTo(tagList) { it.text.toString() }
        return tagList
    }

    val searchTask = Runnable {
        doSearch(search_view.search)
    }
    var type = 1
    var key = ""
    var page = 1
    fun doSearch(text: String) {
        search_view.et_search.setSelection(text.length)
        this.key = text
        if (TextUtils.isEmpty(key)) return
        var type = when (tabs.selectedTabPosition) {
            0 -> null
            1 -> 3
            2 -> 1
            else -> null
        }
        val tmplist = LinkedList<String>()
        tmplist.add(text)
        Store.store.newsSearch(baseActivity!!, tmplist)
        SoguApi.getService(baseActivity!!.application, NewService::class.java)
                .listNews(page = page, pageSize = 20, type = type, fuzzyQuery = text)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (page == 1)
                            adapter.dataList.clear()
                        payload.payload?.apply {
                            adapter.dataList.addAll(this)
                        }
                        payload?.apply {
                            tv_result_title.text = Html.fromHtml(getString(R.string.tv_title_result_news, adapter.dataList.size))
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                }, {
                    ll_history.visibility = View.GONE
                    adapter.notifyDataSetChanged()
                    if (page == 1)
                        finishRefresh()
                    else
                        finishLoadMore()

                    hisAdapter.dataList.clear()
                    hisAdapter.dataList.addAll(Store.store.newsSearch(baseActivity!!))
                    hisAdapter.notifyDataSetChanged()
                })
    }

    val fmt = SimpleDateFormat("yyyy/MM/dd HH:mm")
    val KEY_NEWS = "news"

    companion object {
        val TAG = MainNewsFragment::class.java.simpleName

        fun newInstance(): MainNewsFragment {
            val fragment = MainNewsFragment()
            val intent = Bundle()
            fragment.arguments = intent
            return fragment
        }
    }


}