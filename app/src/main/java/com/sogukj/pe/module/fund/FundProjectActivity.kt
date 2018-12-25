package com.sogukj.pe.module.fund

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.graphics.drawable.RoundedBitmapDrawable
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.bean.FundCompany
import com.sogukj.pe.bean.FundSmallBean
import com.sogukj.pe.service.FundService
import com.sogukj.pe.util.*
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_fund_project.*

@Route(path = ARouterPath.FundProjectActivity)
class FundProjectActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Context?, data: FundSmallBean) {
            val intent = Intent(ctx, FundProjectActivity::class.java)
            intent.putExtra(Extras.DATA, data)
            ctx?.startActivity(intent)
        }
    }

    lateinit var bean: FundSmallBean
    lateinit var mAdapter: RecyclerAdapter<FundCompany>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fund_project)
        setBack(true)
        title = "基金已投项目列表"
        bean = intent.getSerializableExtra(Extras.DATA) as FundSmallBean

        //文件列表
        kotlin.run {
            mAdapter = RecyclerAdapter(this) { _adapter, parent, type ->
                val convertView = _adapter.getView(R.layout.item_fundproject, parent)
                object : RecyclerHolder<FundCompany>(convertView) {
                    val icon = convertView.findViewById<ImageView>(R.id.icon) as ImageView
                    val status = convertView.findViewById<TextView>(R.id.status) as TextView
                    val title = convertView.findViewById<TextView>(R.id.title) as TextView
                    val time = convertView.findViewById<TextView>(R.id.timeTv) as TextView
                    val manange = convertView.findViewById<TextView>(R.id.manange) as TextView
                    val money = convertView.findViewById<TextView>(R.id.money) as TextView
                    override fun setData(view: View, data: FundCompany, position: Int) {
                        if (data.logo.isNullOrEmpty()) {
                            var bitmap = BitmapFactory.decodeResource(resources, R.drawable.default_icon)
                            var draw = RoundedBitmapDrawableFactory.create(resources, bitmap)
                            draw.cornerRadius = Utils.dpToPx(context, 4).toFloat()
                            icon.setBackgroundDrawable(draw)
                        } else {
                            Glide.with(context).asBitmap().load(data.logo).into(object : SimpleTarget<Bitmap>() {
                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    var draw = RoundedBitmapDrawableFactory.create(resources, resource)
                                    draw.cornerRadius = Utils.dpToPx(context, 4).toFloat()
                                    icon.setBackgroundDrawable(draw)
                                }
                            })
                        }

                        ColorUtil.setColorInFundProject(context, status, data.type)
                        title.text = data.company_name
                        //var time11 = DateUtils.timesFormat(data.invest_time, "yyyy年MM月dd日 HH:mm:ss")
                        if(data.invest_time.isNullOrEmpty()) {
                            time.text = "投资时间：无"
                        } else {
                            time.text = "投资时间：${data.invest_time}"
                        }

                        if(data.manager_name.isNullOrEmpty()) {
                            manange.text = "投资经理：无"
                        } else {
                            manange.text = "投资经理：${data.manager_name}"
                        }

                        if(data.had_invest.isNullOrEmpty()) {
                            money.text = "投资金额：无"
                        } else {
                            money.text = "投资金额：${data.had_invest}万元"
                        }
                    }

                }
            }
            val layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            fileList.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 10)))
            fileList.layoutManager = layoutManager
            fileList.adapter = mAdapter
            mAdapter.onItemClick = { v, p ->
                FundFinancialActivity.start(context, mAdapter.dataList.get(p))
            }
        }

        doRequest()
    }

    private fun doRequest() {
        SoguApi.getService(application,FundService::class.java)
                .getCompanyOnFund(bean.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        mAdapter.dataList.clear()
                        payload.payload?.apply {
                            mAdapter.dataList.addAll(this)
                        }
                        mAdapter.notifyDataSetChanged()
                        if (mAdapter.dataList.size == 0) {
                            fileList.visibility = View.GONE
                            iv_empty.visibility = View.VISIBLE
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        if (mAdapter.dataList.size == 0) {
                            fileList.visibility = View.GONE
                            iv_empty.visibility = View.VISIBLE
                        }
                    }
                }, { e ->
                    Trace.e(e)
                    if (mAdapter.dataList.size == 0) {
                        fileList.visibility = View.GONE
                        iv_empty.visibility = View.VISIBLE
                    }
                })
    }
}
