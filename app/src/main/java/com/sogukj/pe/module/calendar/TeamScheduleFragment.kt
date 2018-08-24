package com.sogukj.pe.module.calendar

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.google.gson.Gson
import com.ldf.calendar.component.CalendarAttr
import com.ldf.calendar.component.CalendarViewAdapter
import com.ldf.calendar.interf.OnSelectDateListener
import com.ldf.calendar.model.CalendarDate
import com.ldf.calendar.view.Calendar
import com.ldf.calendar.view.MonthPager
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.ScheduleBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.interf.MonthSelectListener
import com.sogukj.pe.interf.ScheduleItemClickListener
import com.sogukj.pe.module.approve.SealApproveActivity
import com.sogukj.pe.module.approve.SignApproveActivity
import com.sogukj.pe.module.calendar.adapter.TeamAdapter
import com.sogukj.pe.module.main.ContactsActivity
import com.sogukj.pe.module.project.ProjectActivity
import com.sogukj.pe.module.project.ProjectDetailActivity
import com.sogukj.pe.module.project.archives.RecordTraceActivity
import com.sogukj.pe.service.CalendarService
import com.sogukj.pe.service.NewService
import com.sogukj.pe.widgets.CustomDayView
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_team_schedule.*
import org.jetbrains.anko.support.v4.ctx
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the [TeamScheduleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TeamScheduleFragment : BaseFragment(), ScheduleItemClickListener {


    override val containerViewId: Int
        get() = R.layout.fragment_team_schedule
    lateinit var teamAdapter: TeamAdapter
    val data = ArrayList<ScheduleBean>()

    private var mParam1: String? = null
    private var mParam2: String? = null
    lateinit var monthSelect: MonthSelectListener
    private lateinit var calendarAdapter: CalendarViewAdapter
    var page = 1
    var filter: StringBuilder = StringBuilder("")
    lateinit var date: String
    lateinit var selectDate: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }
        val format = SimpleDateFormat("yyyy年MM月").format(Date(System.currentTimeMillis()))
        date = format
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initCalendarView()
        initList()
        selectDate = SimpleDateFormat("yyyy-MM-dd").format(Date(System.currentTimeMillis()))
        calendarAdapter.notifyDataChanged(CalendarDate())

    }

    override fun onResume() {
        super.onResume()
        doRequest(page, selectDate)
    }


    private fun initCalendarView() {
        calendar_view.setViewheight(Utils.dpToPx(ctx, 270))
        val dayView = CustomDayView(ctx, R.layout.custom_day)
        calendarAdapter = CalendarViewAdapter(ctx, object : OnSelectDateListener {
            override fun onSelectDate(date: CalendarDate?) {
                //选中日期监听
                val calendar = java.util.Calendar.getInstance()
                calendar.set(date?.year!!, date.month - 1, date.day)
                selectDate = Utils.getTime(calendar.time.time, "yyyy-MM-dd")
                doRequest(page, selectDate, filter.toString())
            }

            override fun onSelectOtherMonth(offset: Int) {
                calendar_view.selectOtherMonth(offset)
            }

        }, CalendarAttr.CalendayType.MONTH, dayView)
        CalendarViewAdapter.weekArrayType = 1
        calendar_view.adapter = calendarAdapter
        calendar_view.currentItem = MonthPager.CURRENT_DAY_INDEX
        calendar_view.setPageTransformer(false) { page, position ->
            page.alpha = Math.sqrt((1 - Math.abs(position)).toDouble()).toFloat()
        }
        calendar_view.addOnPageChangeListener(object : MonthPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                val currentCalendars = calendarAdapter.pagers
                if (currentCalendars[position % currentCalendars.size] is Calendar) {
                    val date = currentCalendars[position % currentCalendars.size].seedDate
                    monthSelect.onMonthSelect(date)
                    this@TeamScheduleFragment.date = "${date.year}年${date.month}月"
                    val time = Utils.getSupportBeginDayofMonth(date.year, date.month - 1)
                    val time1 = Utils.getSupportBeginDayofMonth(date.year, date.month + 1)
                    showGreatPoint("${Utils.getTime(time[0], "yyyyMMdd")}-${Utils.getTime(time1[1], "yyyyMMdd")}")
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }

        })
        val timer = Utils.getSupportBeginDayofMonth(
                Utils.getTime(System.currentTimeMillis(), "yyyy").toInt(),
                Utils.getTime(System.currentTimeMillis(), "MM").toInt()
        )
        val timer1 = Utils.getSupportBeginDayofMonth(
                Utils.getTime(System.currentTimeMillis(), "yyyy").toInt(),
                Utils.getTime(System.currentTimeMillis(), "MM").toInt()
        )
        showGreatPoint("${Utils.getTime(timer[0], "yyyyMMdd")}-${Utils.getTime(timer1[1], "yyyyMMdd")}")
    }


    fun doRequest(page: Int, date: String, filter: String? = null) {
        SoguApi.getService(baseActivity!!.application,CalendarService::class.java)
                .showSchedule(page, stat = 2, time = date, filter = filter)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        Log.d("WJY", Gson().toJson(payload.payload))
                        if (page == 1) {
                            data.clear()
                        }
                        payload.payload?.let {
                            if (it.isNotEmpty()) {
                                val bean = ScheduleBean()
                                bean.start_time = date
                                data.add(bean)
                            }
                            data.addAll(it)
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                }, {
                    isLoading = false
                    isRefreshing = false
                    teamAdapter.notifyDataSetChanged()
                })
    }


    private fun showGreatPoint(timer: String) {
        SoguApi.getService(baseActivity!!.application,CalendarService::class.java)
                .showGreatPoint(timer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.let {
                            val map = HashMap<String, String>()
                            it.forEach {
                                map.put(it, "1")
                            }
                            calendarAdapter.setMarkData(map)
                            calendarAdapter.invalidateCurrentCalendar()
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })
    }


    var isLoading = false
    var isRefreshing = false
    private fun initList() {
        teamAdapter = TeamAdapter(context, data)
        teamList.layoutManager = LinearLayoutManager(context)
        teamList.adapter = teamAdapter
        teamAdapter.setListener(this)
        teamList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var lastItemPosition: Int by Delegates.notNull()
            var firstItemPosition: Int by Delegates.notNull()
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!isLoading && (lastItemPosition + 1) == teamAdapter.itemCount) {
                        page += 1
                        isLoading = true
                        doRequest(page, selectDate, filter.toString())
                    }
                    if (!isRefreshing && firstItemPosition == 0) {
                        page = 1
                        isRefreshing = true
                        doRequest(page, selectDate, filter.toString())
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val manager = teamList.layoutManager as LinearLayoutManager
                lastItemPosition = manager.findLastVisibleItemPosition()
                firstItemPosition = manager.findFirstVisibleItemPosition()
            }
        })
    }


    fun setListener(listener: MonthSelectListener) {
        this.monthSelect = listener
    }


    override fun onItemClick(view: View, position: Int) {
        when (view.id) {
            R.id.selectTv -> {
//                TeamSelectActivity.startForResult(this,true, selectUser,false,false)
                ContactsActivity.startFromFragment(this,selectUser,true,false)
            }
            R.id.teamItemLayout -> {
                val scheduleBean = data[position]
                when (scheduleBean.type) {
                    0 -> {
                        //日程
                        TaskDetailActivity.start(activity,  scheduleBean.data_id!!, scheduleBean.title!!, ModifyTaskActivity.Schedule)
                    }
                    1 -> {
                        //任务
                        TaskDetailActivity.start(activity, scheduleBean.data_id!!, scheduleBean.title!!, ModifyTaskActivity.Task)
                    }
                    2 -> {
                        //会议
                    }
                    3 -> {
                        //用印审批
                        SealApproveActivity.start(activity, scheduleBean.data_id!!, "用印审批")
                    }
                    4 -> {
                        //签字审批
                        SignApproveActivity.start(activity, scheduleBean.data_id!!, "签字审批")
                    }
                    5 -> {
                        //跟踪记录
                        getCompanyDetail(scheduleBean.data_id!!, 5)
                    }
                    6 -> {
                        //项目
                        getCompanyDetail(scheduleBean.data_id!!, 6)
                    }
                    7 -> {
                        //请假
                    }
                    8 -> {
                        // 出差
                    }
                }
            }
        }
    }

    private fun getCompanyDetail(cId: Int, type: Int) {
        SoguApi.getService(baseActivity!!.application, NewService::class.java)
                .singleCompany(cId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.let {
                            when (type) {
                                5 -> {
                                    RecordTraceActivity.start(activity, it)
                                }
                                6 -> {
                                    ProjectDetailActivity.start(activity, it)
                                }
                                else->{

                                }
                            }
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })
    }

    override fun finishCheck(isChecked: Boolean, position: Int) {

    }

    private var selectUser: ArrayList<UserBean>? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && resultCode == Extras.RESULTCODE && data != null) {
            selectUser = data.getSerializableExtra(Extras.DATA) as ArrayList<UserBean>
            selectUser?.let {
                it.forEachIndexed { index, userBean ->
                    filter = filter.append("${userBean.user_id},")
                }
            }
            val s = filter.toString()
            doRequest(page, selectDate, s.substring(0, s.length - 1))
        }
    }

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TeamScheduleFragment.
         */
        fun newInstance(param1: String, param2: String): TeamScheduleFragment {
            val fragment = TeamScheduleFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}
