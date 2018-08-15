package com.sogukj.pe.module.clockin

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.utils.DateUtils
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.widgets.DotView
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.LocationRecordBean
import com.sogukj.pe.module.approve.LeaveBusinessApproveActivity
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.ApproveService
import com.sogukj.pe.widgets.MyMapView
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_location_clock.*
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.ctx
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class LocationClockFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_location_clock

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var map = MyMapView(ctx)
        waichudaka.setOnClickListener {
            SoguApi.getService(baseActivity!!.application, ApproveService::class.java)
                    .outCardApproveList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        var list = ArrayList<LocationRecordBean.LocationCellBean>()
                        payload?.payload?.forEach {
                            list.add(it)
                        }
                        map.show(savedInstanceState, list, object : MyMapView.onFinishListener {
                            override fun onFinish() {
                                doRequest()
                            }
                        })
                    }, { e ->
                        showCustomToast(R.drawable.icon_toast_fail, "网络请求出错，无法定位打卡")
                        Trace.e(e)
                    })
        }

        kotlin.run {
            adapter = RecyclerAdapter(ctx, { _adapter, parent, type ->
                val convertView = _adapter.getView(R.layout.item_locate_clock, parent)
                object : RecyclerHolder<LocationRecordBean.LocationCellBean>(convertView) {
                    val tvClockTime = convertView.find<TextView>(R.id.clockTime)
                    val tvLocate = convertView.find<TextView>(R.id.locate)
                    val tvRelate = convertView.find<TextView>(R.id.relate)
                    val dotView = convertView.find<DotView>(R.id.dotView)
                    override fun setData(view: View, data: LocationRecordBean.LocationCellBean, position: Int) {
                        tvClockTime.text = "打卡时间  ${data.time}"
                        tvLocate.text = data.place
                        if (data.sid == null) {
                            tvRelate.visibility = View.GONE
                        } else {
                            tvRelate.visibility = View.VISIBLE
                            tvRelate.text = "关联审批：${data.add_time!!.split(" ")[0]}  ${data.title}"
                            tvRelate.setOnClickListener {
                                try {
                                    LeaveBusinessApproveActivity.start(activity, data.sid!!, data.stype!!)
                                } catch (e: Exception) {
                                }
                            }
                        }
                        dotView.importantColor = Color.parseColor("#ffd8d8d8")
                        dotView.setImportant(true)
                        if (position == 0) {
                            dotView.setUp(false)
                            dotView.setLow(true)
                        } else if (position == adapter.dataList.size - 1) {
                            dotView.setUp(true)
                            dotView.setLow(false)
                        } else {
                            dotView.setUp(true)
                            dotView.setLow(true)
                        }
                        if (adapter.dataList.size == 1) {
                            dotView.setUp(false)
                            dotView.setLow(false)
                        }
                    }
                }
            })
//            adapter.onItemClick = { _, position ->
//                ProjectActivity.start(context, adapter.dataList.get(position))
//            }
            recycler_view.layoutManager = LinearLayoutManager(context)
            //recycler_view.addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
            recycler_view.adapter = adapter

            refresh.setOnRefreshListener {
                doRequest()
                refresh.finishRefresh(1000)
            }
        }

        loadHeader()

        isViewCreate = true

        Glide.with(ctx)
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
        iv_loading?.visibility = View.VISIBLE

        doRequest()
    }

    lateinit var adapter: RecyclerAdapter<LocationRecordBean.LocationCellBean>

    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == 0x001) {
                synchronized(this) {
                    var str = format.format(Date())
                    instantTime.text = str.split(" ")[1]
                    sendMessageDelayed(obtainMessage(0x001), 1000)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeMessages(0x001)
    }

    val format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())

    fun doRequest() {
        mHandler.removeMessages(0x001)
        var str = format.format(Date())
        instantTime.text = str.split(" ")[1]
        mHandler.sendMessageDelayed(mHandler.obtainMessage(0x001), 0)

        iv_loading?.visibility = View.VISIBLE
        iv_empty.visibility = View.GONE
        var stamp = DateUtils.getTimestamp(str, "yyyy/MM/dd HH:mm:ss").toInt()
        SoguApi.getService(baseActivity!!.application, ApproveService::class.java)
                .outCardInfo(stamp)
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
                    iv_empty.visibility = if (adapter.dataList.size == 0) View.VISIBLE else View.GONE
                    iv_loading?.visibility = View.GONE
                }, { e ->
                    iv_empty.visibility = if (adapter.dataList.size == 0) View.VISIBLE else View.GONE
                    iv_loading?.visibility = View.GONE
                    Trace.e(e)
                })
    }

    fun loadHeader() {
        val user = Store.store.getUser(baseActivity!!)
        if (user?.url.isNullOrEmpty()) {
            val ch = user?.name?.first()
            userHeadImg.setChar(ch)
        } else {
            Glide.with(ctx)
                    .load(MyGlideUrl(user?.url))
                    .listener(object : RequestListener<Drawable> {
                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            userHeadImg.setImageDrawable(resource)
                            return true
                        }

                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            val ch = user?.name?.first()
                            userHeadImg.setChar(ch)
                            return true
                        }
                    })
                    .into(userHeadImg)
        }
        userName.text = user?.name
        userPosition.text = "职位：${user?.position}"
    }

    var isViewCreate = false

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && isViewCreate) {
            doRequest()
        }
    }
}
