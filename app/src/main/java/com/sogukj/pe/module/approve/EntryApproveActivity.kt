package com.sogukj.pe.module.approve

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.isNullOrEmpty
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.bean.SpGroupBean
import com.sogukj.pe.bean.SpGroupItemBean
import com.sogukj.pe.service.ApproveService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_approve.*
import kotlinx.android.synthetic.main.layout_network_error.*
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/10/18.
 */
@Route(path = ARouterPath.EntryApproveActivity)
class EntryApproveActivity : ToolbarActivity(), View.OnClickListener {
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.resetRefresh -> doRequest()
        }
    }

    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val gson = Gson()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_approve)
        setBack(true)
        title = "审批"
        ll_custom.removeAllViews()
        doRequest()
        item_dwsp.setOnClickListener {
            ApproveListActivity.start(this, 1)
        }
        item_wysp.setOnClickListener {
            ApproveListActivity.start(this, 2)
        }
        item_wfqd.setOnClickListener {
            ApproveListActivity.start(this, 3)
        }
        item_cswd.setOnClickListener {
            ApproveListActivity.start(this, 4)
        }

        var sp = intent.getIntExtra(Extras.DATA, 0)
        if (sp == null || sp == 0) {
            point.visibility = View.INVISIBLE
        } else {
            point.visibility = View.VISIBLE
        }
    }

    private fun doRequest() {
        SoguApi.getService(application, ApproveService::class.java)
                .mainApprove(3)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        approveLayout.visibility = View.VISIBLE
                        initView(payload.payload)
                        hideEmptyView()
                    } else {
                        //showToast(payload.message)
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        approveLayout.visibility = View.GONE
                        showEmptyView()
                        resetRefresh.setOnClickListener(this)
                    }
                }, { e ->
                    Trace.e(e)
                    //showToast("暂无可用数据")
                    approveLayout.visibility = View.GONE
                    showEmptyView()
                    resetRefresh.setOnClickListener(this)
                    ToastError(e)
                })
    }

    fun initView(payload: List<SpGroupBean>?) {
        if (payload == null) return
        val inflater = LayoutInflater.from(this)
        payload.forEach { spGroupBean ->
            if(spGroupBean.item.isNullOrEmpty()){
                return@forEach
            }
            val groupView = inflater.inflate(R.layout.cs_group, null, false) as LinearLayout
            ll_custom.addView(groupView)
            groupView.removeAllViews()
//            val groupDivider = inflater.inflate(R.layout.cs_group_divider, null, false) as View
            val groupHeader = inflater.inflate(R.layout.cs_group_header, null, false) as LinearLayout
//            groupView.addView(groupDivider)
            groupView.addView(groupHeader)
            val tv_title = groupHeader.findViewById<TextView>(R.id.tv_title) as TextView
            tv_title.text = spGroupBean.title

            val items = spGroupBean.item
            if (null != items && items.isNotEmpty()) {
                val gridRow = inflater.inflate(R.layout.cs_grid_row3, null, false) as LinearLayout
                groupView.addView(gridRow)

                setGridItem(items.getOrNull(3), gridRow.getChildAt(3)!!, spGroupBean)
                setGridItem(items.getOrNull(2), gridRow.getChildAt(2)!!, spGroupBean)
                setGridItem(items.getOrNull(1), gridRow.getChildAt(1)!!, spGroupBean)
                setGridItem(items.getOrNull(0), gridRow.getChildAt(0)!!, spGroupBean)
            }

            if (null != items && items.size > 4) {
                val gridRow = inflater.inflate(R.layout.cs_grid_row3, null, false) as LinearLayout
                groupView.addView(gridRow)

                setGridItem(items.getOrNull(7), gridRow.getChildAt(3)!!, spGroupBean)
                setGridItem(items.getOrNull(6), gridRow.getChildAt(2)!!, spGroupBean)
                setGridItem(items.getOrNull(5), gridRow.getChildAt(1)!!, spGroupBean)
                setGridItem(items.getOrNull(4), gridRow.getChildAt(0)!!, spGroupBean)
            }
        }
    }

    fun setGridItem(itemBean: SpGroupItemBean?, itemView: View, spGroupBean: SpGroupBean) {
        if (itemBean == null) return
        itemBean.type = spGroupBean.type
        itemView.visibility = View.VISIBLE
        val iv_icon = itemView.findViewById<ImageView>(R.id.iv_icon) as ImageView
        val tv_label = itemView.findViewById<TextView>(R.id.tv_label) as TextView
        Glide.with(this)
                .load(itemBean.icon)
                .into(iv_icon)
        tv_label.text = itemBean.name
        itemView.tag = "${itemBean.id}"
        itemView.setOnClickListener {
            if (itemBean.id == null) return@setOnClickListener
            ApproveFillActivity.start(context, itemBean)
//            when (itemBean.type) {
//                1 -> LeaveBusinessActivity.start(context, false, itemBean.id!!, "")
//                2 -> BuildSealActivity.start(this, itemBean!!)
//                3 -> BuildSignActivity.start(this, itemBean!!)
//                else -> {
//                }
//            }
        }

    }

    companion object {
        fun start(ctx: Activity?, sp: Int?) {
            val intent = Intent(ctx, EntryApproveActivity::class.java).putExtra(Extras.DATA, sp)
            ctx?.startActivity(intent)
        }
    }
}
