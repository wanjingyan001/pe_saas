package com.sogukj.pe.module.calendar


import android.animation.ObjectAnimator
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.amap.api.mapcore.util.it
import com.bumptech.glide.Glide
import com.scwang.smartrefresh.layout.api.RefreshFooter
import com.scwang.smartrefresh.layout.api.RefreshHeader
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.BaseRefreshFragment
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.ArrangeReqBean
import com.sogukj.pe.bean.ChildBean
import com.sogukj.pe.bean.NewArrangeBean
import com.sogukj.pe.service.CalendarService
import com.sogukj.pe.widgets.ArrangeFooterView
import com.sogukj.pe.widgets.ArrangeHeaderView
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_arrange_list.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.sp


/**
 * A simple [Fragment] subclass.
 * Use the [ArrangeListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ArrangeListFragment : BaseRefreshFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_arrange_list
    private lateinit var arrangeAdapter: ArrangeAdapter
    var offset: Int = 0
    lateinit var inflate: View
    var isRefresh = false
    var isLoadMore = false
    var isNextWeekly = false
    var isLastWeekly = false
    private var isUpwards = true


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arrangeAdapter = ArrangeAdapter()
        recycler_view.layoutManager = LinearLayoutManager(ctx)
        recycler_view.adapter = arrangeAdapter
        backImg.setOnClickListener {
            offset = 0
            isNextWeekly = false
            isLastWeekly = false

            doRequest()
        }
    }


    override fun doRefresh() {
        if (!isRefresh) {
            offset += 1
            isRefresh = true
            if (offset > 0) {
                isNextWeekly = true
                isLastWeekly = false
            }
            doRequest()
        }
    }

    override fun doLoadMore() {
        if (!isLoadMore) {
            offset -= 1
            isLoadMore = true
            if (offset < 0) {
                isNextWeekly = false
                isLastWeekly = true
            }
            doRequest()
        }
    }

    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = true
        config.autoLoadMoreEnable = false
        config.disableContentWhenLoading = true
        config.disableContentWhenRefresh = true
        config.footerTranslationContent = false
        config.footerFollowWhenLoadFinished = true
        return config
    }

    override fun initRefreshHeader(): RefreshHeader? {
        return ArrangeHeaderView(ctx)
    }

    override fun initRefreshFooter(): RefreshFooter? {
        return ArrangeFooterView(ctx)
    }


    fun getWeeklyData(): ArrayList<NewArrangeBean> {
        val list = arrangeAdapter.dataList
        val newList = ArrayList<NewArrangeBean>()
        list.forEach {
            if (it is NewArrangeBean)
                newList.add(it)
        }
        return newList
    }


    override fun onResume() {
        super.onResume()
        doRequest()
    }

    private fun doRequest() {
        Glide.with(this)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
        iv_loading?.visibility = View.VISIBLE

        SoguApi.getService(baseActivity!!.application, CalendarService::class.java)
                .getWeeklyWorkList(ArrangeReqBean(flag = 1, offset = offset))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        arrangeAdapter.dataList.clear()
                        arrangeAdapter.dataList.add("")
                        payload.payload?.let {
                            arrangeAdapter.dataList.addAll(it)
                        }
                        arrangeAdapter.notifyDataSetChanged()
                    }
                }, { e ->
                    iv_loading.setVisible(false)
                    Trace.e(e)
                }, {
                    iv_loading.setVisible(false)
                    if (isRefresh) {
                        refresh.finishRefresh()
                        isRefresh = false
                    }
                    if (isLoadMore) {
                        refresh.finishLoadMore()
                        isLoadMore = false
                        recycler_view.run {
                            smoothScrollToPosition(0)
                            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                                override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                                    super.onScrolled(recyclerView, dx, dy)
                                    scrollBy(0, -Utils.dpToPx(ctx, 50))
                                    handler.postDelayed({ removeOnScrollListener(this) }, 1000)
                                }
                            })
                        }
                    }
//                    recycler_view.adapter = arrangeAdapter

                    if (isNextWeekly) {
                        backImg.visibility = View.VISIBLE
                        if (isUpwards) {
                            val animator = ObjectAnimator.ofFloat(backImg, "rotation", 0f, 180f)
                            animator.duration = 500
                            animator.start()
                            isUpwards = false
                        }
                    }
                    if (isLastWeekly) {
                        backImg.visibility = View.VISIBLE
                        if (!isUpwards) {
                            val animator = ObjectAnimator.ofFloat(backImg, "rotation", 180f, 0f)
                            animator.duration = 500
                            animator.start()
                            isUpwards = true
                        }
                    }

                })
    }


    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ArrangeListFragment.
         */
        fun newInstance(): ArrangeListFragment {
            return ArrangeListFragment()
        }
    }

    inner class ArrangeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var dataList = mutableListOf<Any>()
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is HeadHolder) {
                holder.setData(holder.itemView)
            } else if (holder is ArrangeHolder) {
                val bean = dataList[position] as NewArrangeBean
                holder.setData(holder.itemView, bean, position)
            }
        }

        override fun getItemCount() = dataList.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                0 -> HeadHolder(LayoutInflater.from(context).inflate(R.layout.layout_arrange_weekly_header, parent, false))
                else -> ArrangeHolder(LayoutInflater.from(context).inflate(R.layout.item_arrange_weekly, parent, false))
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == 0) {
                0
            } else
                1
        }

        inner class ArrangeHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val weekly = itemView.find<TextView>(R.id.weekly)
            val dayOfMonth = itemView.find<TextView>(R.id.dayOfMonth)
            val contentLayout = itemView.find<RecyclerView>(R.id.contentLayout)
            val emptyLayout = itemView.find<LinearLayout>(R.id.empty_layout)
            val dayOfWeeklyLayout = itemView.find<LinearLayout>(R.id.day_of_weekly_layout)
            fun setData(view: View, arrangeBean: NewArrangeBean, position: Int) {
                weekly.text = arrangeBean.weekday
                dayOfMonth.text = arrangeBean.date.substring(5, arrangeBean.date.length)
                if (arrangeBean.child.size == 1 && arrangeBean.child[0].id == 0) {
                    emptyLayout.visibility = View.VISIBLE
                    contentLayout.visibility = View.GONE
                    view.clickWithTrigger {
                        ArrangeEditActivity.start(baseActivity!!, arrayListOf(arrangeBean),offset.toString(),0)
                    }
                } else {
                    emptyLayout.visibility = View.GONE
                    contentLayout.visibility = View.VISIBLE
                    val mAdapter = RecyclerAdapter<ChildBean>(ctx) { _adapter, parent, _ ->
                        val itemView = _adapter.getView(R.layout.item_weekly_arrange_child, parent)
                        object : RecyclerHolder<ChildBean>(itemView) {
                            val content = itemView.find<TextView>(R.id.arrangement_content)
                            val attendTv = itemView.find<TextView>(R.id.attend)
                            val participateTv = itemView.find<TextView>(R.id.participate)
                            val address = itemView.find<TextView>(R.id.arrange_address)
                            val addressIcon = itemView.find<ImageView>(R.id.address_icon)
                            override fun setData(view: View, data: ChildBean, position: Int) {
                                if (data.reasons.isNullOrEmpty()){
                                    content.hint = "暂无事由信息"
                                    content.textSize = 10f
                                }else{
                                    content.text = data.reasons
                                    content.textSize = 14f
                                }

                                data.attendee?.let {
                                    if (it.isNotEmpty()) {
                                        val builder = StringBuilder()
                                        val list = if (it.size >= 3) {
                                            it.subList(0, 2)
                                        } else {
                                            it
                                        }
                                        list.forEach { person ->
                                            builder.append(person.name)
                                            builder.append(",")
                                        }
                                        val attend = builder.toString()
                                        var substring = attend.substring(0, attend.length - 1)
                                        if (it.size > 2) {
                                            substring += "..."
                                        }
                                        attendTv.text = "出席:$substring"
                                    } else {
                                        attendTv.text = "暂无出席人员"
                                    }
                                }
                                data.participant?.let {
                                    if (it.isNotEmpty()) {
                                        val builder = StringBuilder()
                                        val list = if (it.size >= 3) {
                                            it.subList(0, 2)
                                        } else {
                                            it
                                        }
                                        list.forEach { person ->
                                            builder.append(person.name)
                                            builder.append(",")
                                        }
                                        val attend = builder.toString()
                                        var substring = attend.substring(0, attend.length - 1)
                                        if (it.size > 2) {
                                            substring += "..."
                                        }
                                        participateTv.text = "参加:$substring"
                                    } else {
                                        participateTv.text = "暂无参加人员"
                                    }
                                }
                                if (data.place.isNullOrEmpty()) {
                                    addressIcon.isEnabled = false
                                    address.hint = "暂无地址信息"
                                } else {
                                    addressIcon.isEnabled = true
                                    address.text = data.place
                                }
                                view.setOnClickListener {
                                    ArrangeDetailActivity.start(baseActivity!!, arrangeBean, position)
                                }
                            }
                        }
                    }
                    mAdapter.dataList.addAll(arrangeBean.child)
                    contentLayout.apply {
                        layoutManager = LinearLayoutManager(ctx)
                        addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
                        adapter = mAdapter
                    }
                }
            }
        }

        inner class HeadHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val weeklyTv = itemView.find<TextView>(R.id.weeklyTv)
            fun setData(itemView: View) {
                when (offset) {
                    -1 -> {
                        itemView.backgroundResource = R.drawable.bg_last_week
                        weeklyTv.text = "上周"
                    }
                    0 -> {
                        itemView.backgroundResource = R.drawable.bg_this_week
                        weeklyTv.text = "本周"
                        backImg.visibility = View.GONE
                    }
                    1 -> {
                        itemView.backgroundResource = R.drawable.bg_next_week
                        weeklyTv.text = "下周"
                    }
                    else -> {
                        itemView.background = resources.getDrawable(R.color.white)
                        val bean = arrangeAdapter.dataList[1] as NewArrangeBean
                        val firstTime = bean.date
                        val bean1 = arrangeAdapter.dataList[7] as NewArrangeBean
                        val lastTime = bean1.date
                        weeklyTv.text = "${firstTime?.substring(5, firstTime.length)}~${lastTime?.substring(5, lastTime.length)}"
                        itemView.backgroundColor = Color.parseColor("#f7f9fc")
                    }
                }
            }
        }
    }
}
