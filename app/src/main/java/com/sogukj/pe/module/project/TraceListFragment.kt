package com.sogukj.pe.module.project

import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.utils.DateUtils
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.ProjectRecordBean
import com.sogukj.pe.bean.RecordInfoBean
import com.sogukj.pe.module.project.archives.RecordDetailActivity
import com.sogukj.pe.peUtils.SupportEmptyView
import com.sogukj.pe.service.ProjectService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_list_trace.*

/**
 * Created by qinfei on 17/7/18.
 */
class TraceListFragment : BaseFragment(), SupportEmptyView {
    override val containerViewId: Int
        get() = R.layout.fragment_list_trace
    lateinit var adapter: RecyclerAdapter<ProjectRecordBean>
    var type: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments?.getInt(Extras.TYPE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RecyclerAdapter<ProjectRecordBean>(baseActivity!!, { _adapter, parent, t ->
            ProjectHolder(_adapter.getView(R.layout.item_project_trace, parent))
        })
        adapter.onItemClick = { v, p ->
            var data = adapter.dataList.get(p)

            var project = ProjectBean()
            project.name = data.name
            project.company_id = null

            var item = RecordInfoBean.ListBean()
            item.start_time = data.start_time?.toLong()
            item.end_time = data.end_time?.toLong()
            item.visits = data.visits
            item.des = data.des
            item.important = data.important

            RecordDetailActivity.startView(baseActivity, project, item)
        }
        val layoutManager = LinearLayoutManager(baseActivity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_view.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 10)))
        //recycler_view.layoutManager = SnappingLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)
        recycler_view.adapter = adapter

        refresh.setOnRefreshListener {
            doRequest()
            refresh.finishRefresh(1000)
        }

        Glide.with(this)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
        iv_loading?.visibility = View.VISIBLE
//        handler.postDelayed({
//            doRequest()
//        }, 100)
        Log.e("onViewCreated", "${type}")
        isViewCreated = true
    }

    var isViewCreated = false


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && isViewCreated) {
            Log.e("setUserVisibleHint", "${type}")
            doRequest()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isViewCreated = false
    }

    fun doRequest() {
        SoguApi.getService(baseActivity!!.application,ProjectService::class.java)
                .projectRecords(type = type!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        adapter.dataList.clear()
                        payload.payload?.apply {
                            adapter.dataList.addAll(this)
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    iv_loading?.visibility = View.GONE
                }, { e ->
                    Trace.e(e)
                    iv_loading?.visibility = View.GONE
                    iv_empty.visibility = if (adapter.dataList.isEmpty()) View.VISIBLE else View.GONE
                }, {
                    iv_empty.visibility = if (adapter.dataList.isEmpty()) View.VISIBLE else View.GONE
                    adapter.notifyDataSetChanged()
                })
    }

    companion object {
        val TAG = TraceListFragment::class.java.simpleName
        const val TYPE_CB = 4
        const val TYPE_LX = 1
        const val TYPE_YT = 2
        const val TYPE_GZ = 3
        const val TYPE_DY = 6
        const val TYPE_TC = 7

        fun newInstance(type: Int): TraceListFragment {
            val fragment = TraceListFragment()
            val intent = Bundle()
            intent.putInt(Extras.TYPE, type)
            fragment.arguments = intent
            return fragment
        }
    }

    inner class ProjectHolder(convertView: View)
        : RecyclerHolder<ProjectRecordBean>(convertView) {

        val tvTitle: TextView
        val tvName: TextView
        val tvTime: TextView
        val tvCondition: TextView

        init {
            tvTitle = convertView.findViewById<TextView>(R.id.title) as TextView
            tvName = convertView.findViewById<TextView>(R.id.name) as TextView
            tvTime = convertView.findViewById<TextView>(R.id.time) as TextView
            tvCondition = convertView.findViewById<TextView>(R.id.condition) as TextView
        }

        override fun setData(view: View, data: ProjectRecordBean, position: Int) {
            tvTitle.text = data.name
            tvName.text = "填写人：${data.userName}"
            tvTime.text = "跟踪时间：${DateUtils.timedate(data.start_time.toString())}"
            tvCondition.text = "跟踪情况描述：${data.des}"
        }

    }
}