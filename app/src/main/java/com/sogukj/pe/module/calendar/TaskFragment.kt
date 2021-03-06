package com.sogukj.pe.module.calendar


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.scwang.smartrefresh.header.MaterialHeader
import com.scwang.smartrefresh.layout.api.RefreshHeader
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.BaseRefreshFragment
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.bean.TaskItemBean
import com.sogukj.pe.bean.TodoDay
import com.sogukj.pe.interf.ScheduleItemClickListener
import com.sogukj.pe.module.calendar.adapter.TaskAdapter
import com.sogukj.pe.peUtils.SupportEmptyView
import com.sogukj.pe.service.CalendarService
import com.sogukj.pe.widgets.TaskFilterWindow
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_task.*
import kotlinx.android.synthetic.main.layout_empty.*
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.find
import org.jetbrains.anko.textColor


/**
 * A simple [Fragment] subclass.
 * Use the [TaskFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TaskFragment : BaseRefreshFragment(), View.OnClickListener, TaskFilterWindow.FilterItemClickListener, ScheduleItemClickListener {
    override val containerViewId: Int
        get() = R.layout.fragment_task
    lateinit var taskAdapter: TaskAdapter
    val data = ArrayList<Any>()
    var page = 1
    var range = "w"
    var isFinish: Int = 2



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()

        dateFilter.setOnClickListener(this)
        taskFilter.setOnClickListener(this)
    }


    private fun initAdapter() {
        taskAdapter = TaskAdapter(activity, data)
        taskAdapter.setListener(this)
        taskList.layoutManager = LinearLayoutManager(context)
        taskList.adapter = taskAdapter
    }


    override fun doRefresh() {
        page = 1
        doRequest(page, range, isFinish)
    }

    override fun doLoadMore() {
        ++page
        doRequest(page, range, isFinish)
    }

    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = true
        config.autoLoadMoreEnable = true
        config.disableContentWhenRefresh = true
        return config
    }

    override fun initRefreshHeader(): RefreshHeader? {
        return MaterialHeader(ctx)
    }

    override fun onResume() {
        super.onResume()
        doRequest(page, range, isFinish)
//        if (baseActivity is CalendarMainActivity) {
//            val currentItem = (baseActivity as? CalendarMainActivity)?.adapter?.getCurrentItem()
//            currentItem?.let {
//                if (it::class.simpleName == TaskFragment::class.simpleName) {
//                    doRequest(page, range, isFinish)
//                }
//            }
//        }
    }

    fun doRequest(page: Int, range: String, isFinish: Int) {
        SoguApi.getService(ctx, CalendarService::class.java)
                .showTask(page = page, range = range, is_finish = isFinish)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (page == 1) {
                            data.clear()
                        }
                        Log.d("WJY", Gson().toJson(payload.payload))
                        payload.payload.let {
                            it?.forEachIndexed { index, taskItemBean ->
                                data.add(TodoDay(taskItemBean.date))
                                taskItemBean.data.forEach {
                                    data.add(it)
                                }
                            }
                        }

                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                }, {
                    if (data.size == 0) {
                        refresh.setVisible(false)
                        iv_empty.setVisible(true)
                    } else {
                        refresh.setVisible(true)
                        iv_empty.setVisible(false)
                    }
                    isLoadMoreEnable = (data.size - 1) % 20 == 0
                    taskAdapter.notifyDataSetChanged()
                    if (page == 1) {
                        finishRefresh()
                    } else {
                        finishLoadMore()
                    }
                })

    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.dateFilter -> {
                dateFilterTv.textColor = resources.getColor(R.color.color_main)
                val dateFilters = arrayListOf("一周内", "一月内", "一年内")
                val dateWindow = TaskFilterWindow(activity, dateFilters, this, "date")
                dateFilters.forEachIndexed { index, s ->
                    if (s == dateFilterTv.text) {
                        dateWindow.setSelectPosition(index)
                    }
                }
                dateWindow.setOnDismissListener {
                    dateFilterTv.textColor = resources.getColor(R.color.text_3)
                    arrow1.visibility = View.VISIBLE
                    arrow3.visibility = View.GONE
                }
                dateWindow.showAsDropDown(find(R.id.filterConditionLayout))
                arrow1.visibility = View.GONE
                arrow3.visibility = View.VISIBLE
            }
            R.id.taskFilter -> {
                taskFilterTv.textColor = resources.getColor(R.color.color_main)
                val taskFilters = arrayListOf("未完成任务", "已完成任务", "全部任务")
                val taskWindow = TaskFilterWindow(activity, taskFilters, this, "task")
                taskFilters.forEachIndexed { index, s ->
                    if (s == taskFilterTv.text) {
                        taskWindow.setSelectPosition(index)
                    }
                }
                taskWindow.setOnDismissListener {
                    taskFilterTv.textColor = resources.getColor(R.color.text_3)
                    arrow2.visibility = View.VISIBLE
                    arrow4.visibility = View.GONE
                }
                taskWindow.showAsDropDown(find(R.id.filterConditionLayout))
                arrow2.visibility = View.GONE
                arrow4.visibility = View.VISIBLE
            }

        }
    }

    override fun onItemClick(view: View, position: Int) {
        when (view.id) {
            R.id.taskItemLayout -> {
                val bean = data[position] as TaskItemBean.ItemBean
                TaskDetailActivity.start(activity, bean.data_id, bean.title, ModifyTaskActivity.Task)
            }
        }
    }


    override fun finishCheck(isChecked: Boolean, position: Int) {
        if (isChecked) {
            Toast.makeText(context, "您完成了该任务", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "您重新打开了该任务", Toast.LENGTH_SHORT).show()
        }
    }

    override fun itemClick(view: View?, position: Int, filter: String?) {
        when (view?.tag) {
            "date" -> {
                dateFilterTv.text = filter
                when (filter) {
                    "一周内" -> {
                        range = "w"
                    }
                    "一月内" -> {
                        range = "m"
                    }
                    "一年内" -> {
                        range = "y"
                    }
                }

            }
            "task" -> {
                taskFilterTv.text = filter
                when (filter) {
                    "未完成任务" -> {
                        isFinish = 0
                    }
                    "已完成任务" -> {
                        isFinish = 1
                    }
                    "全部任务" -> {
                        isFinish = 2
                    }
                }
            }
        }
        doRequest(page, range, isFinish)
    }


    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"
        val TAG = TaskFragment::class.java.simpleName

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TaskFragment.
         */
        fun newInstance(): TaskFragment {
            return TaskFragment()
        }
    }

}
