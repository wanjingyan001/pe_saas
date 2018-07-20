package com.sogukj.pe.module.weekly


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.gson.JsonSyntaxException
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.widgets.MyListView
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.WeeklySendBean
import com.sogukj.pe.module.weekly.PersonalWeeklyActivity
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.WeeklyService
import com.sogukj.pe.widgets.CircleImageView
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_weekly_isend.*
import kotlinx.android.synthetic.main.layout_network_error.*
import org.jetbrains.anko.support.v4.ctx
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class WeeklyISendFragment : BaseFragment() {

    override val containerViewId: Int
        get() = R.layout.fragment_weekly_isend

    lateinit var adapter: RecyclerAdapter<WeeklySendBean>
    var format = SimpleDateFormat("yyyy-MM-dd")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RecyclerAdapter(ctx, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_week_send, parent) as LinearLayout
            object : RecyclerHolder<WeeklySendBean>(convertView) {
                val tv_title = convertView.findViewById<TextView>(R.id.title_date)
                val grid = convertView.findViewById<MyListView>(R.id.grid_list)
                override fun setData(view: View, data: WeeklySendBean, position: Int) {
                    tv_title.text = data.date
                    data.data?.let {
                        var adapter = MyAdapter(ctx, it)
                        grid.adapter = adapter
                        adapter.notifyDataSetChanged()
                        grid.setOnItemClickListener { parent, view, position, id ->
                            val sendBeanObj = it[position]
                            val intent = Intent(context, PersonalWeeklyActivity::class.java)
                            intent.putExtra(Extras.ID, sendBeanObj.week_id)
                            intent.putExtra(Extras.NAME, "My")
                            intent.putExtra(Extras.TIME1, sendBeanObj.start_time)
                            intent.putExtra(Extras.TIME2, sendBeanObj.end_time)
                            ctx.startActivity(intent)
                        }
                    }
                }
            }
        })
        adapter.onItemClick = { v, p ->
        }
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        list.layoutManager = layoutManager
        list.adapter = adapter

        refresh.setOnRefreshListener {
            page = 1
            doRequest(startTime, endTime)
            refresh.finishRefresh(1000)
        }
        refresh.setOnLoadMoreListener {
            ++page
            doRequest(startTime, endTime)
            refresh.finishLoadMore(1000)
        }

        doRequest("", "")
    }

    var page = 1
    var pageSize = 5
    var startTime = ""
    var endTime = ""

    fun doRequest(startT: String, endT: String) {
        startTime = startT
        endTime = endT
        if (startTime == "" || endTime == "") {
            iv_empty.visibility = View.VISIBLE
            range.visibility = View.GONE
            return
        }
        range.visibility = View.VISIBLE
        range.text = "${startTime}到${endTime}的周报"
        range.setOnClickListener {
            range.visibility = View.GONE
        }
        SoguApi.getService(baseActivity!!.application, WeeklyService::class.java)
                .send(page, pageSize, startTime, endTime)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (page == 1) {
                            adapter.dataList.clear()
                        }
                        payload.payload?.apply {
                            adapter.dataList.addAll(this)
                            //adapter.notifyDataSetChanged()
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    if (adapter.dataList.size == 0) {
                        iv_empty.visibility = View.VISIBLE
                    } else {
                        iv_empty.visibility = View.GONE
                    }
                }, {
                    if (adapter.dataList.size == 0) {
                        iv_empty.visibility = View.VISIBLE
                    } else {
                        iv_empty.visibility = View.GONE
                    }
                    adapter.notifyDataSetChanged()
                })
    }

    class MyAdapter(var context: Context, val list: ArrayList<WeeklySendBean.WeeklySendBeanObj>) : BaseAdapter() {


        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var viewHolder: ViewHolder
            var conView = convertView
            if (conView == null) {
                viewHolder = ViewHolder()
                conView = LayoutInflater.from(context).inflate(R.layout.senditem, null)
                viewHolder.icon = conView.findViewById<CircleImageView>(R.id.circleImageView)
                viewHolder.name = conView.findViewById<TextView>(R.id.name)
                viewHolder.time = conView.findViewById<TextView>(R.id.time)
                conView.setTag(viewHolder)
            } else {
                viewHolder = conView.tag as ViewHolder
            }

            val user = Store.store.getUser(context)
            if (user?.url.isNullOrEmpty()) {
                val ch = user?.name?.first()
                viewHolder.icon?.setChar(ch)
            } else {
                Glide.with(context)
                        .load(MyGlideUrl(user?.url))
                        .listener(object : RequestListener<Drawable> {
                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                viewHolder.icon?.setImageDrawable(resource)
                                return true
                            }

                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                val ch = user?.name?.first()
                                viewHolder.icon?.setChar(ch)
                                return true
                            }
                        })
                        .into(viewHolder.icon!!)
            }
            viewHolder.name?.text = "${user!!.name}的周报"
            //
            return conView!!
        }

        override fun getItem(position: Int): Any {
            return list.get(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return list.size
        }

        class ViewHolder {
            var icon: CircleImageView? = null
            var name: TextView? = null
            var time: TextView? = null
        }
    }
}
