package com.sogukj.pe.module.weekly

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.gson.JsonSyntaxException
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.lcodecore.tkrefreshlayout.footer.BallPulseView
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.widgets.MyGridView
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.bean.ReceiveSpinnerBean
import com.sogukj.pe.bean.WeeklyWatchBean
import com.sogukj.pe.service.UserService
import com.sogukj.pe.service.WeeklyService
import com.sogukj.pe.widgets.CalendarDingDing
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
class WeeklyWaitToWatchFragment : BaseFragment(), View.OnClickListener {
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.resetRefresh -> getDepartmentData()
        }
    }

    override val containerViewId: Int
        get() = R.layout.fragment_weekly_wait_to_watch

    lateinit var adapter: RecyclerAdapter<WeeklyWatchBean>
    var format = SimpleDateFormat("yyyy-MM-dd")
    lateinit var arr_adapter: ArrayAdapter<String>
    var currentClick = 0

    var loadedData = ArrayList<WeeklyWatchBean>()
    var spinner_data = ArrayList<ReceiveSpinnerBean>()
    var selected_depart_id: Long = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arr_adapter = ArrayAdapter(context, R.layout.spinner_item)
        arr_adapter.setDropDownViewResource(R.layout.spinner_dropdown)
        spinner.adapter = arr_adapter
        spinner.setSelection(0, true)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View,
                                        pos: Int, id: Long) {
                selected_depart_id = parent.selectedItemId
                Log.e("IIIIIDDDDDD", "${selected_depart_id}")
                var selected_depart = spinner_data.get(selected_depart_id.toInt()).name
                Log.e("IIIIIDDDDDD", selected_depart)
                Log.e("IIIIIDDDDDD", "${spinner_data.get(selected_depart_id.toInt()).id ?: 100000}")

                adapter.dataList.clear()
                adapter.notifyDataSetChanged()
                doRequest()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        adapter = RecyclerAdapter(ctx, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_wait_watch, parent) as LinearLayout
            object : RecyclerHolder<WeeklyWatchBean>(convertView) {
                val tv_title = convertView.findViewById<TextView>(R.id.title_date) as TextView
                val grid = convertView.findViewById<MyGridView>(R.id.grid_list) as MyGridView
                override fun setData(view: View, data: WeeklyWatchBean, position: Int) {
                    tv_title.text = data.date
                    data.data?.let {
                        var adapter = MyAdapter(ctx, it)
                        adapter.sort()
                        grid.adapter = adapter

                        grid.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                            (grid.adapter.getItem(position) as WeeklyWatchBean.BeanObj).is_read = 2
                            grid.tag = "CLICK"

                            val intent = Intent(context, PersonalWeeklyActivity::class.java)
                            intent.putExtra(Extras.DATA, grid.adapter.getItem(position) as WeeklyWatchBean.BeanObj)
                            intent.putExtra(Extras.TIME1, data.start_time)
                            intent.putExtra(Extras.TIME2, data.end_time)
                            intent.putExtra(Extras.NAME, "Other")
                            startActivityForResult(intent, 0x011)
                        }
                    }
                }
            }
        }
        )
        adapter.onItemClick = { v, p ->
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
            if (spinner_data.size == 0) {
                return@setOnClickListener
            }
            if (currentClick == 0) {
                return@setOnClickListener
            }
            currentClick = 0
            total.setClick(true)
            unread.setClick(false)
            readed.setClick(false)

            //sort()

            adapter.dataList.clear()
            adapter.notifyDataSetChanged()

            doRequest()
        }
        unread.setOnClickListener {
            if (spinner_data.size == 0) {
                return@setOnClickListener
            }
            if (currentClick == 1) {
                return@setOnClickListener
            }
            currentClick = 1
            total.setClick(false)
            unread.setClick(true)
            readed.setClick(false)

            //sort()

            adapter.dataList.clear()
            adapter.notifyDataSetChanged()

            doRequest()
        }
        readed.setOnClickListener {
            if (spinner_data.size == 0) {
                return@setOnClickListener
            }
            if (currentClick == 2) {
                return@setOnClickListener
            }
            currentClick = 2
            total.setClick(false)
            unread.setClick(false)
            readed.setClick(true)

            //sort()

            adapter.dataList.clear()
            adapter.notifyDataSetChanged()

            doRequest()
        }


        var calendar = Calendar.getInstance()

//        start.text = formatTime(startBean)
//        end.text = formatTime(endBean)
        start.text = "开始时间"
        end.text = "结束时间"

        var startDD = CalendarDingDing(context)
        start.setOnClickListener {
            if (spinner_data.size == 0) {
                return@setOnClickListener
            }
            if (start.text != "开始时间") {
                calendar.time = format.parse(start.text.toString())
            } else {
                calendar = Calendar.getInstance()
            }
//            val timePicker = TimePickerView.Builder(context, { date, view ->
//                if (end.text.trim() == "结束时间") {
//                    start.text = format.format(date)
//                    return@Builder
//                }
//
//                var startBean = date
//                var endBean = format.parse(end.text.toString())
//                if (startBean.compareTo(endBean) > 0) {
//                    showToast("日期选择错误")
//                    return@Builder
//                }
//
//                start.text = format.format(date)
//
//                adapter.dataList.clear()
//                adapter.notifyDataSetChanged()
//                doRequest()
//            })
//                    //年月日时分秒 的显示与否，不设置则默认全部显示
//                    .setType(booleanArrayOf(true, true, true, false, false, false))
//                    .setDividerColor(Color.DKGRAY)
//                    .setContentSize(20)
//                    .setDate(calendar)
//                    .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
//                    .build()
//            timePicker.show()
            startDD.show(1, calendar, CalendarDingDing.onTimeClick { date ->
                if(date != null){
                    if (end.text.trim() == "结束时间") {
                        start.text = format.format(date)
                        return@onTimeClick
                    }

                    var startBean = date
                    var endBean = format.parse(end.text.toString())
                    if (startBean > endBean) {
                        showCustomToast(R.drawable.icon_toast_common, "日期选择错误")
                        return@onTimeClick
                    }

                    start.text = format.format(date)

                    adapter.dataList.clear()
                    adapter.notifyDataSetChanged()
                    doRequest()
                }
            })
        }

        var deadDD = CalendarDingDing(context)
        end.setOnClickListener {
            if (spinner_data.size == 0) {
                return@setOnClickListener
            }
            if (end.text != "结束时间") {
                calendar.time = format.parse(end.text.toString())
            } else {
                calendar = Calendar.getInstance()
            }
//            val timePicker = TimePickerView.Builder(context, { date, view ->
//                if (start.text.trim() == "开始时间") {
//                    end.text = format.format(date)
//                    return@Builder
//                }
//
//                var startBean = format.parse(start.text.toString())
//                var endBean = date
//                if (startBean.compareTo(endBean) > 0) {
//                    showToast("日期选择错误")
//                    return@Builder
//                }
//
//                end.text = format.format(date)
//
//                adapter.dataList.clear()
//                adapter.notifyDataSetChanged()
//                doRequest()
//            })
//                    //年月日时分秒 的显示与否，不设置则默认全部显示
//                    .setType(booleanArrayOf(true, true, true, false, false, false))
//                    .setDividerColor(Color.DKGRAY)
//                    .setContentSize(20)
//                    .setDate(calendar)
//                    .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
//                    .build()
//            timePicker.show()
            deadDD.show(1, calendar, CalendarDingDing.onTimeClick { date ->
                if(date != null){
                    if (start.text.trim() == "开始时间") {
                        end.text = format.format(date)
                        return@onTimeClick
                    }

                    var startBean = format.parse(start.text.toString())
                    var endBean = date
                    if (startBean.compareTo(endBean) > 0) {
                        showCustomToast(R.drawable.icon_toast_common, "日期选择错误")
                        return@onTimeClick
                    }

                    end.text = format.format(date)

                    adapter.dataList.clear()
                    adapter.notifyDataSetChanged()
                    doRequest()
                }
            })
        }

        currentClick = 0
        selected_depart_id = 0

        getDepartmentData()

        val header = ProgressLayout(baseActivity)
        header.setColorSchemeColors(ContextCompat.getColor(baseActivity!!, R.color.color_main))
        refresh.setHeaderView(header)
        val footer = BallPulseView(baseActivity)
        footer.setAnimatingColor(ContextCompat.getColor(baseActivity!!, R.color.color_main))
        refresh.setBottomView(footer)
        refresh.setOverScrollRefreshShow(false)
        refresh.setOnRefreshListener(object : RefreshListenerAdapter() {
            override fun onRefresh(refreshLayout: TwinklingRefreshLayout?) {
                page = 1
                doRequest()
            }

            override fun onLoadMore(refreshLayout: TwinklingRefreshLayout?) {
                ++page
                doRequest()
            }

        })
        refresh.setAutoLoadMore(true)
    }

    private fun getDepartmentData() {
        SoguApi.getService(baseActivity!!.application,UserService::class.java)
                .getDepartment()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        root.visibility = View.VISIBLE
                        networkErrorLayout.visibility = View.GONE
                        payload.payload?.apply {
                            spinner_data.clear()
                            spinner_data = this
                            var total = ReceiveSpinnerBean()
                            total.id = null
                            total.name = "全部"
                            spinner_data.add(0, total)

                            for (item in spinner_data) {
                                arr_adapter.add(item.name)
                            }
                            arr_adapter.notifyDataSetChanged()
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    //ToastError(e)
                    when (e) {
                        is JsonSyntaxException -> showCustomToast(R.drawable.icon_toast_fail, "后台数据出错")
                        is UnknownHostException -> {
                            showCustomToast(R.drawable.icon_toast_fail, "网络出错")
                            root.visibility = View.GONE
                            networkErrorLayout.visibility = View.VISIBLE
                            resetRefresh.setOnClickListener(this)
                        }
                        else -> showCustomToast(R.drawable.icon_toast_fail, "未知错误")
                    }
                })
    }

    var page = 1
    var pageSize = 5

    fun doRequest() {
        var is_read: Int? = null
        if (currentClick == 1) {
            is_read = 1
        } else if (currentClick == 2) {
            is_read = 2
        }

        var de_id = spinner_data.get(selected_depart_id.toInt()).id

        var start_time: String? = null
        var end_time: String? = null
        if (start.text.trim() != "") {
            start_time = start.text.toString()
            end_time = end.text.toString()
        }

        SoguApi.getService(baseActivity!!.application,WeeklyService::class.java)
                .receive(is_read, de_id, start_time, end_time, page, pageSize)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (page == 1) {
                            adapter.dataList.clear()
                        }
                        payload.payload?.apply {
                            loadedData = this
                            adapter.dataList.addAll(this)
                            //adapter.notifyDataSetChanged()
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                }, {
                    refresh.setEnableLoadmore(adapter.dataList.size % pageSize == 0)
                    adapter.notifyDataSetChanged()
                    if (page == 1)
                        refresh.finishRefreshing()
                    else
                        refresh.finishLoadmore()
                })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x011) {
            var grid = root.findViewWithTag<MyGridView>("CLICK") as MyGridView
            (grid.adapter as MyAdapter).sort()
            (grid.adapter as MyAdapter).notifyDataSetChanged()
            grid.tag = ""
        }
    }

    class MyAdapter(var context: Context, val list: ArrayList<WeeklyWatchBean.BeanObj>) : BaseAdapter() {

        // click=true放前面
        fun sort() {
            for (i in 0 until list.size) {
                if (list[i].is_read == 1) {
                    for (j in (i + 1) until list.size) {
                        if (list[j].is_read == 2) {
                            var tmp = list[i]
                            list[i] = list[j]
                            list[j] = tmp
                            break
                        }
                    }
                    break
                }
            }
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var viewHolder: ViewHolder
            var conView = convertView
            if (conView == null) {
                viewHolder = ViewHolder()
                conView = LayoutInflater.from(context).inflate(R.layout.watch_item, null) as LinearLayout
                viewHolder.icon = conView.findViewById<CircleImageView>(R.id.icon) as CircleImageView
                viewHolder.name = conView.findViewById<TextView>(R.id.name) as TextView
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
