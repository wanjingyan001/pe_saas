package com.sogukj.pe.module.approve

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.bean.VacationBean
import com.sogukj.pe.service.ApproveService
import com.sogukj.pe.util.ColorUtil
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_my_holiday.*

class MyHolidayActivity : ToolbarActivity() {

    lateinit var adapter: RecyclerAdapter<VacationBean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_holiday)
        setBack(true)
        title = "我的假期"

        adapter = RecyclerAdapter(context, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_vacation, parent)
            object : RecyclerHolder<VacationBean>(convertView) {
                val image = convertView.findViewById<ImageView>(R.id.color) as ImageView
                val vac_type = convertView.findViewById<TextView>(R.id.type) as TextView
                val shengyu = convertView.findViewById<TextView>(R.id.shengyu) as TextView
                override fun setData(view: View, data: VacationBean, position: Int) {
                    vac_type.text = data.name
                    if (data.status == 0) {//0=>已请了多长时间，1=>还剩多长时间
                        shengyu.text = "已使用${data.hours}小时"
                    } else if (data.status == 1) {
                        shengyu.text = "剩余${data.hours}小时"
                    }
                    ColorUtil.setColorStatus(image, data)
                }
            }
        })
        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 10)))
        recycler_view.adapter = adapter

        SoguApi.getService(application, ApproveService::class.java)
                .showVacation()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        adapter.dataList.clear()
                        adapter.dataList.addAll(payload.payload!!)
                        adapter.notifyDataSetChanged()
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        empty.visibility = View.VISIBLE
                    }
                }, { e ->
                    Trace.e(e)
                    empty.visibility = View.VISIBLE
                })

        record_detail.setOnClickListener {
            VacationRecordActivity.start(context, type = 1)
        }
    }

    companion object {
        fun start(ctx: Activity?) {
            val intent = Intent(ctx, MyHolidayActivity::class.java)
            ctx?.startActivity(intent)
        }
    }
}
