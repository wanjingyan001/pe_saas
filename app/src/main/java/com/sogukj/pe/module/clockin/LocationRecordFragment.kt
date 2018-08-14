package com.sogukj.pe.module.clockin

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView

import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.LocationRecordBean
import kotlinx.android.synthetic.main.fragment_location_record.*
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.ctx
import android.graphics.Color
import android.net.Uri
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.text.SpannableString
import com.bumptech.glide.Glide
import com.sogukj.pe.baselibrary.Extended.isNullOrEmpty
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.widgets.MyListView
import com.sogukj.pe.module.clockin.adapter.LocationAdapter
import com.sogukj.pe.service.ApproveService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class LocationRecordFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_location_record

    lateinit var adapter: RecyclerAdapter<LocationRecordBean>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        kotlin.run {
            //列表和adapter的初始化
            adapter = RecyclerAdapter(ctx, { _adapter, parent, type ->
                val convertView = _adapter.getView(R.layout.item_location_record, parent)
                object : RecyclerHolder<LocationRecordBean>(convertView) {
                    val tvTime = convertView.find<TextView>(R.id.time)
                    val mListView = convertView.find<MyListView>(R.id.list)
                    override fun setData(view: View, data: LocationRecordBean, position: Int) {

                        tvTime.text = data.date + "    " + data.week

                        var adapter = LocationAdapter(ctx, data.child)
                        mListView.adapter = adapter
                    }
                }
            })
            recycler_view.layoutManager = LinearLayoutManager(ctx)
            recycler_view.adapter = adapter

            refresh.setOnRefreshListener {
                doRequest()
                refresh.finishRefresh(1000)
            }
            refresh.setOnLoadMoreListener {
                doRequest()
                refresh.finishLoadMore(1000)
            }
        }

        isViewCreated = true

        Glide.with(ctx)
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
        iv_loading?.visibility = View.VISIBLE

        doRequest()
    }

    var isViewCreated = false

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && isViewCreated) {
            doRequest()
        }
    }

    fun doRequest() {
        SoguApi.getService(baseActivity!!.application, ApproveService::class.java)
                .outCardList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        adapter.dataList.clear()
                        payload?.payload?.apply {
                            adapter.dataList.addAll(this)
                        }
                        adapter.notifyDataSetChanged()
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                    iv_loading?.visibility = View.GONE
                }, { e ->
                    iv_empty.visibility = if (adapter.dataList.isEmpty()) View.VISIBLE else View.GONE
                    adapter.notifyDataSetChanged()
                    iv_loading?.visibility = View.GONE
                    Trace.e(e)
                })
    }
}
