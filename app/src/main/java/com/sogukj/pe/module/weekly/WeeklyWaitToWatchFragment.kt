package com.sogukj.pe.module.weekly

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.bean.ReceiveSpinnerBean
import com.sogukj.pe.bean.WeeklyWatchBean
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.service.WeeklyService
import com.sogukj.pe.widgets.CircleImageView
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_weekly_wait_to_watch.*
import kotlinx.android.synthetic.main.layout_network_error.*
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.textColor
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class WeeklyWaitToWatchFragment : BaseFragment() {

    override val containerViewId: Int
        get() = R.layout.fragment_weekly_wait_to_watch

    lateinit var adapter: RecyclerAdapter<WeeklyWatchBean.BeanObj>
    var format = SimpleDateFormat("yyyy-MM-dd")
    var currentClick = 0

    var selected_depart_id: Int = 0
    var selected_depart_name: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var drawable = resources.getDrawable(R.drawable.iv_search_filter_gray)
        drawable.setBounds(0, 0, Utils.dpToPx(context, 16), Utils.dpToPx(context, 16))
        filter.setCompoundDrawables(drawable, null, null, null)
        filter.setOnClickListener {
            val intent = Intent(context, WeeklySelectActivity::class.java)
            intent.putExtra(Extras.DATA, true)
            intent.putExtra(Extras.TITLE, "我发出的")
            startActivityForResult(intent, 0x001)
        }

        adapter = RecyclerAdapter<WeeklyWatchBean.BeanObj>(ctx, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.senditem, parent) as RelativeLayout
            object : RecyclerHolder<WeeklyWatchBean.BeanObj>(convertView) {
                val icon = convertView.findViewById<CircleImageView>(R.id.circleImageView)
                val name = convertView.findViewById<TextView>(R.id.name)
                val time = convertView.findViewById<TextView>(R.id.time)
                val tvState = convertView.findViewById<TextView>(R.id.state)
                override fun setData(view: View, data: WeeklyWatchBean.BeanObj, position: Int) {
                    if (data.url.isNullOrEmpty()) {
                        val ch = data.name?.first()
                        icon.setChar(ch)
                    } else {
                        Glide.with(ctx)
                                .load(MyGlideUrl(data.url))
                                .listener(object : RequestListener<Drawable> {
                                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                        icon.setImageDrawable(resource)
                                        return true
                                    }

                                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                        val ch = data.name?.first()
                                        icon.setChar(ch)
                                        return true
                                    }
                                })
                                .into(icon)
                    }
                    name.text = "${data.name}的周报"

                    if (data.date.isNullOrEmpty()) {
                        time.visibility = View.INVISIBLE
                    } else {
                        var YMD = data.date!!.split(" ")[0]
                        var HMS = data.date!!.split(" ")[1]
                        time.text = "${YMD.split("-")[0]}年${YMD.split("-")[1]}月${YMD.split("-")[2]}日      ${HMS.substring(0, 5)}"
                    }

                    tvState.visibility = View.VISIBLE
                    if (data.is_read == 2) {
                        tvState.text = "已读"
                        tvState.textColor = Color.parseColor("#a0a4aa")
                        tvState.setBackgroundResource(R.drawable.bg_border_blue11)
                    } else {
                        tvState.text = "未读"
                        tvState.textColor = Color.parseColor("#1787fb")
                        tvState.setBackgroundResource(R.drawable.bg_border_blue1)
                    }
                }
            }
        }
        )
        adapter.onItemClick = { v, p ->
            var bean = adapter.dataList.get(p)
            val intent = Intent(context, PersonalWeeklyActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            intent.putExtra(Extras.TIME1, bean.start_time)
            intent.putExtra(Extras.TIME2, bean.end_time)
            intent.putExtra(Extras.NAME, "Other")
            startActivityForResult(intent, 0x011)

            selectPosition = p
        }
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        list.layoutManager = layoutManager
        list.addItemDecoration(SpaceItemDecoration(20))
        list.adapter = adapter

        currentClick = 0
        total.setClick(true)
        unread.setClick(false)
        readed.setClick(false)
        total.setOnClickListener {
            if (currentClick == 0) {
                return@setOnClickListener
            }
            currentClick = 0
            total.setClick(true)
            unread.setClick(false)
            readed.setClick(false)

            adapter.dataList.clear()
            adapter.notifyDataSetChanged()

            page = 1
            doRequest()
        }
        unread.setOnClickListener {
            if (currentClick == 1) {
                return@setOnClickListener
            }
            currentClick = 1
            total.setClick(false)
            unread.setClick(true)
            readed.setClick(false)

            adapter.dataList.clear()
            adapter.notifyDataSetChanged()

            page = 1
            doRequest()
        }
        readed.setOnClickListener {
            if (currentClick == 2) {
                return@setOnClickListener
            }
            currentClick = 2
            total.setClick(false)
            unread.setClick(false)
            readed.setClick(true)

            adapter.dataList.clear()
            adapter.notifyDataSetChanged()

            page = 1
            doRequest()
        }

        currentClick = 0
        selected_depart_id = 0
        page = 1
        doRequest()

        refresh.setOnRefreshListener {
            page = 1
            doRequest()
            refresh.finishRefresh(1000)
        }
        refresh.setOnLoadMoreListener {
            ++page
            doRequest()
            refresh.finishLoadMore(1000)
        }
    }

    /**
     * 每周的第一天和最后一天
     * @param dataStr
     * @param dateFormat
     * @param resultDateFormat
     * @return
     * @throws ParseException
     */
    fun getFirstAndLastOfWeek(): ArrayList<String> {
        val cal = Calendar.getInstance()
        cal.time = Date()
        var d = 0
        if (cal.get(Calendar.DAY_OF_WEEK) === Calendar.SUNDAY) {
            d = -6
        } else {
            d = 2 - cal.get(Calendar.DAY_OF_WEEK)
        }
        cal.add(Calendar.DAY_OF_WEEK, d)
        // 所在周开始日期
        val data1 = SimpleDateFormat("yyyy-MM-dd").format(cal.time)
        cal.add(Calendar.DAY_OF_WEEK, 6)
        // 所在周结束日期
        val data2 = SimpleDateFormat("yyyy-MM-dd").format(cal.time)
        return arrayListOf(data1, data2)
    }

    var page = 1
    var pageSize = 5
    var start_time: String? = null
    var end_time: String? = null

    fun doRequest() {
        var is_read: Int? = null
        if (currentClick == 1) {
            is_read = 1
        } else if (currentClick == 2) {
            is_read = 2
        }

        if (start_time.isNullOrEmpty() || end_time.isNullOrEmpty()) {
            start_time = getFirstAndLastOfWeek()[0]
            end_time = getFirstAndLastOfWeek()[1]
            selected_depart_id = 0
            selected_depart_name = "全部"
        }
        range.visibility = View.VISIBLE
        range.text = "${selected_depart_name}部门    ${start_time}到${end_time}的周报"
        range.setOnClickListener {
            range.visibility = View.GONE
        }

        SoguApi.getService(baseActivity!!.application, WeeklyService::class.java)
                .receive(is_read, selected_depart_id, start_time, end_time, page, pageSize)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (page == 1) {
                            adapter.dataList.clear()
                        }
                        payload.payload?.forEach { cell ->
                            cell.data?.forEach { item ->
                                item.start_time = cell.start_time
                                item.end_time = cell.end_time
                                adapter.dataList.add(item)
                            }
                            //adapter.notifyDataSetChanged()
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                    iv_empty.visibility = if (adapter.dataList.isEmpty()) View.VISIBLE else View.GONE
                }, {
                    iv_empty.visibility = if (adapter.dataList.isEmpty()) View.VISIBLE else View.GONE
                    adapter.notifyDataSetChanged()
                })
    }

    var selectPosition = -1

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x011) {
            if (selectPosition >= 0) {
                adapter.dataList.get(selectPosition).is_read = 2
                adapter.notifyDataSetChanged()
            }
        } else if (requestCode == 0x001) {
            data?.apply {
                selected_depart_id = getIntExtra(Extras.DATA, 0)
                selected_depart_name = getStringExtra(Extras.DATA2)
                start_time = getStringExtra(Extras.TIME1)
                end_time = getStringExtra(Extras.TIME2)

                adapter.dataList.clear()
                adapter.notifyDataSetChanged()

                page = 1
                doRequest()
            }
        }
    }

    class MyAdapter(var context: Context, val list: ArrayList<WeeklyWatchBean.BeanObj>) : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var viewHolder: ViewHolder
            var conView = convertView
            if (conView == null) {
                viewHolder = ViewHolder()
                conView = LayoutInflater.from(context).inflate(R.layout.watch_item, null) as LinearLayout
                viewHolder.icon = conView.findViewById<CircleImageView>(R.id.icon)
                viewHolder.name = conView.findViewById<TextView>(R.id.name)
                conView.setTag(viewHolder)
            } else {
                viewHolder = conView.tag as ViewHolder
            }
            viewHolder.icon?.setChar(list.get(position).name?.first())
            viewHolder.name?.text = list.get(position).name
            if (list.get(position).is_read == 2) {
                viewHolder.icon?.alpha = 0.8f
                viewHolder.name?.textColor = Color.parseColor("#A0A4AA")
            } else if (list.get(position).is_read == 1) {
                viewHolder.icon?.alpha = 1f
                viewHolder.name?.textColor = Color.parseColor("#282828")
            }
            return conView
        }

        override fun getItem(position: Int): WeeklyWatchBean.BeanObj {
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
        }
    }
}
