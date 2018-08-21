package com.sogukj.pe.module.calendar


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.ldf.calendar.component.CalendarAttr
import com.ldf.calendar.component.CalendarViewAdapter
import com.ldf.calendar.interf.OnSelectDateListener
import com.ldf.calendar.model.CalendarDate
import com.ldf.calendar.view.Calendar
import com.ldf.calendar.view.MonthPager
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.FundSmallBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.ScheduleBean
import com.sogukj.pe.interf.MonthSelectListener
import com.sogukj.pe.interf.ScheduleItemClickListener
import com.sogukj.pe.module.approve.SealApproveActivity
import com.sogukj.pe.module.approve.SignApproveActivity
import com.sogukj.pe.module.calendar.adapter.ScheduleAdapter
import com.sogukj.pe.module.creditCollection.ShareholderCreditActivity
import com.sogukj.pe.module.fund.FundDetailActivity
import com.sogukj.pe.module.project.ProjectActivity
import com.sogukj.pe.module.project.archives.RecordTraceActivity
import com.sogukj.pe.service.CalendarService
import com.sogukj.pe.service.NewService
import com.sogukj.pe.widgets.CustomDayView
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_schedule.*
import org.jetbrains.anko.support.v4.ctx
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates


/**
 * A simple [Fragment] subclass.
 * Use the [ScheduleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ScheduleFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_schedule

    val data = ArrayList<ScheduleBean>()
    lateinit var adapter: ScheduleAdapter
    var page = 1
    lateinit var monthSelect: MonthSelectListener
    lateinit var date: String
    lateinit var calendarAdapter: CalendarViewAdapter

    lateinit var selectDate: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val format = SimpleDateFormat("yyyy年MM月").format(Date(System.currentTimeMillis()))
        date = format
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        calendar_view.setViewheight(Utils.dpToPx(ctx, 270))
        initCalendarView()
        initList()
        selectDate = SimpleDateFormat("yyyy-MM-dd").format(Date(System.currentTimeMillis()))
        calendarAdapter.notifyDataChanged(CalendarDate())
    }


    private fun initCalendarView() {
        val dayView = CustomDayView(context, R.layout.custom_day)
        calendarAdapter = CalendarViewAdapter(context, object : OnSelectDateListener {
            override fun onSelectDate(date: CalendarDate?) {
                //选中日期监听
                val calendar = java.util.Calendar.getInstance()
                calendar.set(date?.year!!, date.month - 1, date.day)
                page = 1
                selectDate = Utils.getTime(calendar.time.time, "yyyy-MM-dd")
                doRequest(page, selectDate)
            }

            override fun onSelectOtherMonth(offset: Int) {
                calendar_view.selectOtherMonth(offset)
            }

        }, CalendarAttr.CalendayType.MONTH, dayView)
        CalendarViewAdapter.weekArrayType = 1
        initCalendar()
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

    private fun initCalendar() {
        calendar_view.adapter = calendarAdapter
        calendar_view.currentItem = MonthPager.CURRENT_DAY_INDEX
        calendar_view.setPageTransformer(false) { page, position ->
            page.alpha = Math.sqrt((1 - Math.abs(position)).toDouble()).toFloat()
        }
        calendar_view.addOnPageChangeListener(object : MonthPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                //切换月份
                val currentCalendars = calendarAdapter.pagers
                if (currentCalendars[position % currentCalendars.size] is Calendar) {
                    val date = currentCalendars[position % currentCalendars.size].seedDate
                    monthSelect.onMonthSelect(date)
                    this@ScheduleFragment.date = "${date.year}年${date.month}月"
                    val time = Utils.getSupportBeginDayofMonth(date.year, date.month - 1)
                    val time1 = Utils.getSupportBeginDayofMonth(date.year, date.month + 1)
                    showGreatPoint("${Utils.getTime(time[0], "yyyyMMdd")}-${Utils.getTime(time1[1], "yyyyMMdd")}")
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

        })

    }

    public fun load(date: String) {
        page = 1
        selectDate = date
        val year = Integer.parseInt(date.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
        val month = Integer.parseInt(date.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])
        val day = Integer.parseInt(date.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[2])
        val tmpdate = CalendarDate(year, month, day)
        calendarAdapter.notifyDataChanged(tmpdate)
        doRequest(page, selectDate)
    }

    override fun onResume() {
        super.onResume()
        doRequest(page, selectDate)
    }


    fun doRequest(page: Int, date: String) {
        SoguApi.getService(baseActivity!!.application,CalendarService::class.java)
                .showSchedule(page, stat = 1, time = date)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
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
                    ToastError(e)
                }, {
                    adapter.notifyDataSetChanged()
                    isLoading = false
                    isRefreshing = false
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
                    ToastError(e)
                })
    }

    fun finishTask(id: Int, isChecked: Boolean) {
        SoguApi.getService(baseActivity!!.application,CalendarService::class.java)
                .finishTask(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {

                        payload.payload?.let {
                            if (it == 1) {
                                showCustomToast(R.drawable.icon_toast_success, "您完成了该日程")
                            } else {
                                showCustomToast(R.drawable.icon_toast_success, "您重新打开了该日程")
                            }
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                })
    }

    var isLoading = false
    var isRefreshing = false
    private fun initList() {
        adapter = ScheduleAdapter(context, data)
        list.layoutManager = LinearLayoutManager(context)
        list.adapter = adapter
        adapter.setListener(object : ScheduleItemClickListener {
            override fun onItemClick(view: View, position: Int) {
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
                    9 -> {
                        //征信
                        var project = ProjectBean()
                        project.name = ""
                        project.company_id = 0
                        ShareholderCreditActivity.start(context, project)
                    }
                    10 -> {
                        //基金
                        var fund = FundSmallBean()
                        fund.fundName = ""
                        fund.id = scheduleBean.data_id!!
                        FundDetailActivity.start(context, fund)
                    }
                    11 -> {
                        // 被投企业大事件
                        // 2018-05-26 00:00:00
                        scheduleBean.start_time?.apply {
                            val date = this.split(" ")[0]
                            if (!date.isNullOrEmpty()) {
                                CalendarMainActivity.start(ctx, date)
                            }
                        }
                    }
                }
            }

            override fun finishCheck(isChecked: Boolean, position: Int) {
                val scheduleBean = data[position]
                scheduleBean.id?.let { finishTask(it, isChecked) }
            }
        })
        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var lastItemPosition: Int by Delegates.notNull()
            var firstItemPosition: Int by Delegates.notNull()
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val manager = list.layoutManager as LinearLayoutManager
                lastItemPosition = manager.findLastVisibleItemPosition()
                firstItemPosition = manager.findFirstVisibleItemPosition()
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!isLoading && adapter.itemCount == (lastItemPosition + 1)) {
                        page += 1
                        isLoading = true
                        doRequest(page, selectDate)
                    }
                    if (!isRefreshing && firstItemPosition == 0) {
                        page = 1
                        isRefreshing = true
                        doRequest(page, selectDate)
                    }
                }
            }
        })
    }


    fun getCompanyDetail(cId: Int, type: Int) {
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
                                    ProjectActivity.start(activity, it)
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
                    ToastError(e)
                })
    }


    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(): ScheduleFragment {
            return ScheduleFragment()
        }
    }

}
