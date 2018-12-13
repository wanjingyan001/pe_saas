package com.sogukj.pe.module.approve

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.bean.LeaveRecordBean
import com.sogukj.pe.module.approve.baseView.viewBean.ApproveListBean
import com.sogukj.pe.module.approve.baseView.viewBean.ApproveRecord
import com.sogukj.pe.peUtils.SupportEmptyView
import com.sogukj.pe.service.ApproveService
import com.sogukj.pe.widgets.CircleImageView
import com.sogukj.pe.widgets.UserRequestListener
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_vacation_record.*
import org.jetbrains.anko.backgroundResource

class VacationRecordActivity : BaseRefreshActivity() {
    lateinit var adapter: RecyclerAdapter<ApproveRecord>
    var typeCCQJ: Int = 0
    var id: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vacation_record)
        title = "记录"
        setBack(true)

        typeCCQJ = intent.getIntExtra(Extras.TYPE, 0)
        id = intent.getIntExtra(Extras.ID, -1)
        id = if (id == -1) null else id

        adapter = RecyclerAdapter(context) { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.qj_record_item, parent)
            object : RecyclerHolder<ApproveRecord>(convertView) {

                val icon = convertView.findViewById<CircleImageView>(R.id.icon) as CircleImageView
                val title = convertView.findViewById<TextView>(R.id.title) as TextView
                val type1 = convertView.findViewById<TextView>(R.id.type) as TextView
                val start_time = convertView.findViewById<TextView>(R.id.start_time) as TextView
                val end_time = convertView.findViewById<TextView>(R.id.end_time) as TextView
                val time = convertView.findViewById<TextView>(R.id.timeTv) as TextView
                val status = convertView.findViewById<TextView>(R.id.status) as TextView

                override fun setData(view: View, data: ApproveRecord, position: Int) {
                    val firstName = if (data.mark == "new") {
                        data.name?.first()
                    } else {
                        data.title.first()
                    }
                    Glide.with(context).load(data.url).listener(object :RequestListener<Drawable>{
                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            icon.setImageDrawable(resource)
                            return false
                        }

                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            icon.setChar(firstName)
                            return false
                        }
                    }).into(icon)
                    title.text = data.title
                    if (typeCCQJ == 1) {
                        type1.text = "请假类型：${data.holidayname}"
                    } else {
                        type1.visibility = View.GONE
                    }
                    start_time.text = "开始时间：${data.start_time}"
                    end_time.text = "结束时间：${data.end_time}"
                    time.text = Utils.getTime(data.add_time * 1000, "yyyy/MM/dd HH:mm")
                    status.text = when (data.status) {//-1=>不通过，0=>待审批，1=>审批中，4=>审批通过
                        -1 -> "审批未通过"
                        0 -> "待审批"
                        1 -> "审批中"
                        4 -> "审批通过"
                        5 -> "已撤销"
                        else -> "未知状态"
                    }
                    status.backgroundResource = when (data.status) {//-1=>不通过，0=>待审批，1=>审批中，4=>审批通过
                        -1 -> R.drawable.qj_bg1
                        0 -> R.drawable.qj_bg2
                        1 -> R.drawable.qj_bg3
                        4 -> R.drawable.qj_bg
                        5 -> R.drawable.qj_bg4
                        else -> R.drawable.qj_bg
                    }
                }
            }
        }
        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 10)))
        recycler_view.adapter = adapter
        doRequest()
    }

    var page = 1


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
        config.loadMoreEnable = true
        config.autoLoadMoreEnable = true
        config.disableContentWhenRefresh = true
        return config
    }

    fun doRequest() {
        SoguApi.getService(application, ApproveService::class.java)
                .showApproveRecode(page = page)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            if (page == 1)
                                adapter.dataList.clear()
                            payload.payload?.apply {
                                adapter.dataList.addAll(this.data)
                            }
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }
                    onError { e ->
                        Trace.e(e)
                        showCustomToast(R.drawable.icon_toast_common, "暂无可用数据")
                        finishLoad(page)
                    }
                    onComplete {
                        SupportEmptyView.checkEmpty(this@VacationRecordActivity, adapter)
                        isLoadMoreEnable = adapter.dataList.size % 20 == 0
                        adapter.notifyDataSetChanged()
                        if (page == 1)
                            finishRefresh()
                        else
                            finishLoadMore()
                    }
                }

    }

    companion object {
        //我的出差记录  user_id查看别人的	可空，空则是自己的
        //type 0--出差，1--请假
        fun start(ctx: Context, user_id: Int? = null, type: Int) {
            val intent = Intent(ctx, VacationRecordActivity::class.java)
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.ID, user_id)
            ctx.startActivity(intent)
        }
    }
}
