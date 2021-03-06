package com.sogukj.pe.module.calendar

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.ldf.calendar.model.CalendarDate
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.interf.MonthSelectListener
import com.sogukj.pe.module.calendar.adapter.ContentAdapter
import kotlinx.android.synthetic.main.activity_calendar_mian.*
import org.jetbrains.anko.find
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

@Route(path = ARouterPath.CalendarMainActivity)
class CalendarMainActivity : ToolbarActivity(), MonthSelectListener, ViewPager.OnPageChangeListener {

    companion object {
        fun start(ctx: Activity?) {
            ctx?.startActivity(Intent(ctx, CalendarMainActivity::class.java))
        }

        // time   日期
        fun start(ctx: Context, time: String) {
            ctx.startActivity(Intent(ctx, CalendarMainActivity::class.java).putExtra(Extras.DATA, time))
        }
    }

    override val menuId: Int
        get() = R.menu.calendar_menu
    private var currentPosition: Int by Delegates.notNull()
    lateinit var adapter: ContentAdapter
    val fragments = ArrayList<Fragment>()
    private val titles = arrayListOf("周工作安排", "日历", "任务", "项目事项", "团队日程")
    private val arrangeFragment by lazy { ArrangeListFragment() }
    private val scheduleFragment by lazy { ScheduleFragment() }
    private val taskFragment by lazy { TaskFragment() }
    private val pmFragment by lazy { ProjectMattersFragment() }
    private val teamScheduleFragment by lazy { TeamScheduleFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_mian)
        setBack(true)
        currentPosition = 0
        scheduleFragment.monthSelect = this
        teamScheduleFragment.monthSelect = this
        fragments.add(arrangeFragment)
        fragments.add(scheduleFragment)
        fragments.add(taskFragment)
        fragments.add(pmFragment)
        fragments.add(teamScheduleFragment)
        initPager()
        title = SimpleDateFormat("yyyy年MM月").format(Date(System.currentTimeMillis()))
        addSchedule.visibility = View.GONE
        addSchedule.setOnClickListener {
            ModifyTaskActivity.startForCreate(this, ModifyTaskActivity.Schedule)
        }
    }

    //从推送进入，该界面已存在
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (!intent?.getStringExtra(Extras.DATA).isNullOrEmpty()) {
            contentPager.currentItem = 1
            scheduleFragment.load(intent!!.getStringExtra(Extras.DATA))
        }
    }


    private fun initPager() {
        adapter = ContentAdapter(supportFragmentManager, fragments, titles)
        contentPager.adapter = adapter
        tabLayout.setViewPager(contentPager)
        tabLayout.setTabViewFactory { parent, _ ->
            parent.removeAllViews()
            for (i in 0 until titles.size) {
                val view = LayoutInflater.from(this).inflate(R.layout.item_calendar_indicator, parent, false)
                view.find<TextView>(R.id.indicatorTv).text = titles[i]
                parent.addView(view)
            }
        }
        contentPager.addOnPageChangeListener(this)
        //推送
        contentPager.post {
            if (!intent.getStringExtra(Extras.DATA).isNullOrEmpty()) {
                Thread.sleep(1000)
                contentPager.currentItem = 1
                scheduleFragment.load(intent.getStringExtra(Extras.DATA))
            }
        }
    }

    override fun onMonthSelect(date: CalendarDate) {
        title = "${date.year}年${date.month}月"
    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }


    override fun onPageSelected(position: Int) {
        currentPosition = position
        when (position) {
            0 -> {
                mMenu.getItem(0).isVisible = true
                title = scheduleFragment.date
                addSchedule.visibility = View.GONE
            }
            1 -> {
                mMenu.getItem(0).isVisible = true
                title = scheduleFragment.date
                addSchedule.visibility = View.GONE
                addSchedule.setOnClickListener {
                    ModifyTaskActivity.startForCreate(this, ModifyTaskActivity.Schedule)
                }
            }
            2 -> {
                mMenu.getItem(0).isVisible = true
                title = "日历"
                addSchedule.visibility = View.GONE
                addSchedule.setOnClickListener {
                    ModifyTaskActivity.startForCreate(this, ModifyTaskActivity.Task)
                }
            }
            3 -> {
                mMenu.getItem(0).isVisible = false
                title = "日历"
                addSchedule.visibility = View.GONE
            }
            4 -> {
                mMenu.getItem(0).isVisible = false
                title = teamScheduleFragment.date
                addSchedule.visibility = View.GONE
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.calendar_menu -> {
                when (currentPosition) {
                    0 -> ArrangeEditActivity.start(this, arrangeFragment.getWeeklyData(), arrangeFragment.offset.toString())
                    1 -> ModifyTaskActivity.startForCreate(this, ModifyTaskActivity.Schedule)
                    2 -> ModifyTaskActivity.startForCreate(this, ModifyTaskActivity.Task)
                }

            }
        }
        return super.onOptionsItemSelected(item)
    }
}
