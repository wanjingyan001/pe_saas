package com.sogukj.pe.module.calendar


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.google.gson.Gson
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.bean.KeyNode
import com.sogukj.pe.bean.TodoDay
import com.sogukj.pe.bean.TodoYear
import com.sogukj.pe.interf.ScheduleItemClickListener
import com.sogukj.pe.module.approve.SealApproveActivity
import com.sogukj.pe.module.approve.SignApproveActivity
import com.sogukj.pe.module.calendar.adapter.TodoAdapter
import com.sogukj.pe.module.project.ProjectActivity
import com.sogukj.pe.module.project.archives.RecordTraceActivity
import com.sogukj.pe.service.CalendarService
import com.sogukj.pe.service.NewService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_todo.*


/**
 * A simple [Fragment] subclass.
 * Use the [TodoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TodoFragment : BaseFragment(), ScheduleItemClickListener {


    override val containerViewId: Int
        get() = R.layout.fragment_todo
    val data = ArrayList<Any>()
    lateinit var adapter: TodoAdapter
    private var companyId: String? = null
    private var mParam2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            companyId = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = TodoAdapter(data, context)
        adapter.setListener(this)
        todoList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        todoList.setHasFixedSize(true)
        todoList.adapter = adapter
        companyId?.let { doRequest(it) }
    }

    fun doRequest(id: String) {
        SoguApi.getService(baseActivity!!.application,CalendarService::class.java)
                .projectMatter2(id.toInt(), 3)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        data.clear()
                        payload.payload?.let {
                            Log.d("WJY", Gson().toJson(it))
                            val yearList = ArrayList<String>()
                            val dayList = ArrayList<String>()
                            val infoList = ArrayList<KeyNode>()
                            it.forEachIndexed { index, matterDetails ->
                                yearList.add(matterDetails.year)
                                matterDetails.data.forEach {
                                    val day = it.end_time?.split(" ")?.get(0)!!
                                    if (!dayList.contains(day)) {
                                        dayList.add(day)
                                    }
                                    infoList.add(it)
                                }
                            }
                            yearList.forEach {
                                data.add(TodoYear(it))
                                dayList.forEachIndexed { _, s ->
                                    if (s.substring(0, 4) == it) {
                                        data.add(TodoDay(s))
                                        infoList.forEachIndexed { _, keyNode ->
                                            if (keyNode.end_time?.split(" ")?.get(0).equals(s)) {
                                                data.add(keyNode)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e -> Trace.e(e) }, {
                    adapter.notifyDataSetChanged()
                })
    }

    override fun onItemClick(view: View, position: Int) {
        val bean = data[position] as KeyNode
        when (bean.type) {
            0 -> {
                //日程
                TaskDetailActivity.start(activity, bean.data_id!!, bean.title!!, ModifyTaskActivity.Task)
            }
            1 -> {
                //任务
                TaskDetailActivity.start(activity, bean.data_id!!, bean.title!!, ModifyTaskActivity.Task)
            }
            2 -> {
                //会议
            }
            3 -> {
                //用印审批
                SealApproveActivity.start(activity, bean.data_id!!, "用印审批")
            }
            4 -> {
                //签字审批
                SignApproveActivity.start(activity, bean.data_id!!, "签字审批")
            }
            5 -> {
                //跟踪记录
                getCompanyDetail(bean.data_id!!, 5)
            }
            6 -> {
                //项目
                getCompanyDetail(bean.data_id!!, 6)
            }
            7 -> {
                //请假
            }
            8 -> {
                // 出差
            }
        }
    }

    override fun finishCheck(isChecked: Boolean, position: Int) {

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
                })
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
         * @return A new instance of fragment TodoFragment.
         */
        fun newInstance(param1: String, param2: String): TodoFragment {
            val fragment = TodoFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}
