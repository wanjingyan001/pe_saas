package com.sogukj.pe.module.clockin

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.utils.DateUtils
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.bean.LocationRecordBean
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.ApproveService
import com.sogukj.pe.widgets.MyMapView
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_location_clock.*
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

        loadHeader()

        isViewCreate = true

        Glide.with(ctx)
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
        iv_loading?.visibility = View.VISIBLE

        doRequest()
    }

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
        today.text = str.split(" ")[0]
        instantTime.text = str.split(" ")[1]
        mHandler.sendMessageDelayed(mHandler.obtainMessage(0x001), 0)

        dutyOn.visibility = View.GONE
        dutyOff.visibility = View.GONE
        iv_loading?.visibility = View.VISIBLE
        iv_empty.visibility = View.GONE
        var stamp = DateUtils.getTimestamp(str, "yyyy/MM/dd HH:mm:ss").toInt()
        SoguApi.getService(baseActivity!!.application, ApproveService::class.java)
                .outCardInfo(stamp)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    var mSize: Int? = null
                    if (payload.isOk) {
                        mSize = payload?.payload?.size
                        if (mSize == 1) {
                            dutyOn.visibility = View.VISIBLE
                            dutyOff.visibility = View.GONE

                            var bean1 = payload!!.payload!!.get(0)
                            clockTime.text = "打卡时间  ${bean1.time!!.substring(0, 5)}"
                            locate.text = bean1.place
                            if (bean1.sid == null) {
                                relate.visibility = View.GONE
                            } else {
                                relate.visibility = View.VISIBLE
                                relate.text = "关联审批：${bean1.add_time}  ${bean1.title}"
                            }
                        } else if (mSize == 2) {
                            dutyOn.visibility = View.VISIBLE
                            dutyOff.visibility = View.VISIBLE

                            var bean1 = payload!!.payload!!.get(0)
                            clockTime.text = "打卡时间  ${bean1.time!!.substring(0, 5)}"
                            locate.text = bean1.place
                            if (bean1.sid == null) {
                                relate.visibility = View.GONE
                            } else {
                                relate.visibility = View.VISIBLE
                                relate.text = "关联审批：${bean1.add_time}  ${bean1.title}"
                            }

                            var bean2 = payload!!.payload!!.get(1)
                            clockOffTime.text = "打卡时间  ${bean2.time!!.substring(0, 5)}"
                            locate2.text = bean2.place
                            if (bean2.sid == null) {
                                relate2.visibility = View.GONE
                            } else {
                                relate2.visibility = View.VISIBLE
                                relate2.text = "关联审批：${bean2.add_time}  ${bean2.title}"
                            }

                            waichudaka.visibility = View.GONE
                        } else {
                            dutyOn.visibility = View.GONE
                            dutyOff.visibility = View.GONE
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                    iv_empty.visibility = if (mSize == 1 || mSize == 2) View.GONE else View.VISIBLE
                    iv_loading?.visibility = View.GONE
                }, { e ->
                    dutyOn.visibility = View.GONE
                    dutyOff.visibility = View.GONE
                    iv_empty.visibility = View.VISIBLE
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
