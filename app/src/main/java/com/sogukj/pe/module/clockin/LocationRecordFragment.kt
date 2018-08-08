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

                    val rlDutyOn = convertView.find<RelativeLayout>(R.id.dutyOn)
                    val tvDutyOnTime = convertView.find<TextView>(R.id.dutyOnTime)
                    val tvDutyOnLocate = convertView.find<TextView>(R.id.dutyOnLocate)

                    val rlDutyOff = convertView.find<RelativeLayout>(R.id.dutyOff)
                    val tvDutyOffTime = convertView.find<TextView>(R.id.dutyOffTime)
                    val tvDutyOffLocate = convertView.find<TextView>(R.id.dutyOffLocate)

                    override fun setData(view: View, data: LocationRecordBean, position: Int) {

                        tvTime.text = data.date + "    " + data.week

                        if (data.child.isNullOrEmpty()) {
                            rlDutyOn.visibility = View.GONE
                            rlDutyOff.visibility = View.GONE
                        } else if (data.child!!.size == 1) {

                            rlDutyOn.visibility = View.VISIBLE
                            rlDutyOff.visibility = View.GONE

                            var cell1 = data.child!!.get(0)
                            val spannableString1 = SpannableString(cell1.time!!.substring(0, 5))
                            spannableString1.setSpan(ForegroundColorSpan(Color.parseColor("#282828")), 0, spannableString1.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                            val spannableString2 = SpannableString("定位打卡")
                            spannableString2.setSpan(ForegroundColorSpan(Color.parseColor("#808080")), 0, spannableString2.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                            tvDutyOnTime.text = spannableString1.toString() + "    " + spannableString2.toString()
                            var drawable = ContextCompat.getDrawable(ctx, R.drawable.location_clock_neg)
                            drawable!!.setBounds(0, 0, Utils.dpToPx(ctx, 10), Utils.dpToPx(ctx, 10))
                            tvDutyOnLocate.setCompoundDrawables(drawable, null, null, null)
                            tvDutyOnLocate.text = cell1.place

                        } else if (data.child!!.size >= 2) {

                            rlDutyOn.visibility = View.VISIBLE
                            rlDutyOff.visibility = View.VISIBLE

                            var cell1 = data.child!!.get(0)
                            val spannableString1 = SpannableString(cell1.time!!.substring(0, 5))
                            spannableString1.setSpan(ForegroundColorSpan(Color.parseColor("#282828")), 0, spannableString1.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                            val spannableString2 = SpannableString("定位打卡")
                            spannableString2.setSpan(ForegroundColorSpan(Color.parseColor("#808080")), 0, spannableString2.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                            tvDutyOnTime.text = spannableString1.toString() + "    " + spannableString2.toString()
                            var drawable = ContextCompat.getDrawable(ctx, R.drawable.location_clock_neg)
                            drawable!!.setBounds(0, 0, Utils.dpToPx(ctx, 10), Utils.dpToPx(ctx, 10))
                            tvDutyOnLocate.setCompoundDrawables(drawable, null, null, null)
                            tvDutyOnLocate.text = cell1.place

                            var cell2 = data.child!!.get(1)
                            val spannableString3 = SpannableString(cell2.time!!.substring(0, 5))
                            spannableString3.setSpan(ForegroundColorSpan(Color.parseColor("#282828")), 0, spannableString3.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                            tvDutyOffTime.text = spannableString3.toString() + "    " + spannableString2.toString()
                            tvDutyOffLocate.setCompoundDrawables(drawable, null, null, null)
                            tvDutyOffLocate.text = cell2.place

                        }
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
                .operateOutCard(1)
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
                    Trace.e(e)
                })
    }
}
