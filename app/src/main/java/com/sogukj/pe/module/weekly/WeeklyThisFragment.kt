package com.sogukj.pe.module.weekly

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.baselibrary.widgets.MyListView
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.bean.WeeklyThisBean
import com.sogukj.pe.module.approve.SealApproveActivity
import com.sogukj.pe.module.approve.SignApproveActivity
import com.sogukj.pe.module.calendar.ModifyTaskActivity
import com.sogukj.pe.module.calendar.TaskDetailActivity
import com.sogukj.pe.module.main.ContactsActivity
import com.sogukj.pe.module.project.ProjectActivity
import com.sogukj.pe.module.project.archives.RecordTraceActivity
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.service.NewService
import com.sogukj.pe.service.WeeklyService
import com.sogukj.pe.widgets.CircleImageView
import com.sogukj.pe.widgets.WeeklyDotView
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.buchong_empty.*
import kotlinx.android.synthetic.main.buchong_full.*
import kotlinx.android.synthetic.main.fragment_weekly_this.*
import kotlinx.android.synthetic.main.send.*
import org.jetbrains.anko.support.v4.ctx
import java.text.SimpleDateFormat

//import kotlinx.android.synthetic.main.layout_network_error.*


class WeeklyThisFragment : BaseFragment(), View.OnClickListener {
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.resetRefresh->doRequest()
        }
    }

    override val containerViewId: Int
        get() = R.layout.fragment_weekly_this

    lateinit var inflate: LayoutInflater
    var user_id: Int? = null//可空（如果为空，显示自己的周报）
    var week_id: Int? = null//可空（如果为空，显示自己的周报）
    var issue: Int? = null//可空（1=>个人事务,2=>项目事务）
    var TYPE: String = ""
    lateinit var db: XmlDb

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inflate = LayoutInflater.from(context)
        db = XmlDb.open(ctx)
        var mItems = resources.getStringArray(R.array.spinner_this)
        val arr_adapter = ArrayAdapter<String>(context, R.layout.spinner_item, mItems)
        arr_adapter.setDropDownViewResource(R.layout.spinner_dropdown)
        spinner_this.adapter = arr_adapter
//        spinner_this.setSelection(0, true) 会重复请求导致列表数据重复
        spinner_this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View,
                                        pos: Int, id: Long) {
                clearView()
                when (pos) {
                    0 -> issue = null
                    1 -> issue = 2
                    2 -> issue = 1
                }
                doRequest()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        //buchong_hint.visibility = View.GONE
        bu_chong_empty.visibility = View.GONE
        buchong_full.visibility = View.GONE
        send_layout.visibility = View.GONE

        TYPE = arguments!!.getString(Extras.FLAG)
        if (TYPE == "MAIN") {
            spinner_this.visibility = View.GONE
            buchong_hint.visibility = View.VISIBLE
            user_id = null
            issue = null
            week_id = null
            doRequest()
        } else if (TYPE == "PERSONAL") {
            buchong_hint.visibility = View.GONE
            spinner_this.visibility = View.VISIBLE
            var obj = arguments!!.getSerializable(Extras.DATA) as WeeklyThisBean
            //initView(obj)
        }
    }

    fun clearView() {
        if (TYPE == "MAIN") {
            //不存在这种情况
        } else if (TYPE == "PERSONAL") {
            var flag = true
            while (flag) {
                try {
                    root.getChildAt(1) as TextView
                    flag = false
                } catch (e: Exception) {
                    root.removeViewAt(1)
                }
            }
        }
    }

    fun doRequest() {
        var s_time: String? = null
        var e_time: String? = null
        if (TYPE == "MAIN") {
            //
        } else if (TYPE == "PERSONAL") {
            s_time = arguments!!.getString(Extras.TIME1)
            e_time = arguments!!.getString(Extras.TIME2)
            week_id = arguments!!.getInt(Extras.CODE)
        }
        Glide.with(ctx).asGif().load(R.drawable.loading).into(iv_loading)
        iv_loading.visibility = View.VISIBLE
        if (TYPE == "MAIN") {
            buchong_hint.visibility = View.GONE
        } else if (TYPE == "PERSONAL") {
            spinner_this.visibility = View.GONE
        }
        baseActivity?.let {
            SoguApi.getService(it.application,WeeklyService::class.java)
                    .getWeekly(user_id, issue, s_time, e_time, week_id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            payload.payload?.apply {
                                rootScroll.visibility = View.VISIBLE
//                                networkErrorLayout.visibility = View.GONE
                                initView(this)
                            }
                        } else
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }, { e ->
                        Trace.e(e)
                        ToastError(e)
                    }, {
                        if (TYPE == "MAIN") {
                            buchong_hint.visibility = View.VISIBLE
                        } else if (TYPE == "PERSONAL") {
                            spinner_this.visibility = View.VISIBLE
                        }
                        iv_loading.visibility = View.GONE
                    })
        }
    }

    var childs = 0
    lateinit var week: WeeklyThisBean.Week
    lateinit var send_adapter: MyAdapter
    lateinit var chaosong_adapter: MyAdapter

    fun initView(loaded: WeeklyThisBean) {
//        if (spinner_this.visibility == View.VISIBLE) {
//            childs = 1
//        }
        if (TYPE == "MAIN") {
            buchong_hint.visibility = View.VISIBLE
        } else if (TYPE == "PERSONAL") {
            spinner_this.visibility = View.VISIBLE
            childs = 1
        }

        loaded.automatic?.let {
            for (items in it.iterator()) {
                val item = inflate.inflate(R.layout.weekly_item, null) as LinearLayout
                val index = item.findViewById<TextView>(R.id.tv_index) as TextView
                val weekday = item.findViewById<TextView>(R.id.tv_week) as TextView
                val date = item.findViewById<TextView>(R.id.tv_date) as TextView
                val event_list = item.findViewById<MyListView>(R.id.event_list) as MyListView

                var str = items.date!!.split("-")
                index.text = str[2].toInt().toString()
                date.text = "${str[0]}年${str[1]}月"
                weekday.text = Utils.getTime(SimpleDateFormat("yyyy-MM-dd").parse(items.date), "E")

                val adapter = WeeklyEventAdapter(ctx, items.data!!)
                event_list.adapter = adapter
                event_list.setOnItemClickListener { parent, view, position, id ->
                    val weeklyData = items.data?.get(position)
                    when (weeklyData?.type) {
                        0 -> {
                            //日程
                            TaskDetailActivity.start(activity, weeklyData.data_id!!, weeklyData.title!!, ModifyTaskActivity.Schedule)
                        }
                        1 -> {
                            //任务
                            TaskDetailActivity.start(activity, weeklyData.data_id!!, weeklyData.title!!, ModifyTaskActivity.Task)
                        }
                        2 -> {
                            //会议
                        }
                        3 -> {
                            //用印审批
                            SealApproveActivity.start(activity, weeklyData.data_id!!, "用印审批")
                        }
                        4 -> {
                            //签字审批
                            SignApproveActivity.start(activity, weeklyData.data_id!!, "签字审批")
                        }
                        5 -> {
                            //跟踪记录
                            getCompanyDetail(weeklyData.data_id!!, 5)
                        }
                        6 -> {
                            //项目
                            getCompanyDetail(weeklyData.data_id!!, 6)
                        }
                        7 -> {
                            //请假
                        }
                        8 -> {
                            // 出差
                        }
                    }
                }

                root.addView(item, childs++)
            }
        }

        if (TYPE == "PERSONAL") {
            return
        }

        if (loaded.week?.is_send_week == 1) {
            send_layout.visibility = View.GONE
        } else {
            send_layout.visibility = View.VISIBLE
        }

        if (loaded.week?.week_id == null) {
            bu_chong_empty.visibility = View.VISIBLE
            buchong_full.visibility = View.GONE

            bu_chong_empty.setOnClickListener {
                val intent = Intent(context, WeeklyRecordActivity::class.java)
                intent.putExtra(Extras.FLAG, "ADD")
                intent.putExtra(Extras.DATA, loaded.week)
                startActivityForResult(intent, ADD)
            }
        } else {
            bu_chong_empty.visibility = View.GONE
            buchong_full.visibility = View.VISIBLE
            week = loaded.week as WeeklyThisBean.Week
            var time = buchong_full.findViewById<TextView>(R.id.time) as TextView
            var times = buchong_full.findViewById<TextView>(R.id.times) as TextView
            var info = buchong_full.findViewById<TextView>(R.id.info) as TextView
            var buchong_edit = buchong_full.findViewById<ImageView>(R.id.buchong_edit) as ImageView

            var S_TIME = week.start_time?.split("-")
            var E_TIME = week.end_time?.split("-")

            time.text = week.time
            times.text = "${S_TIME?.get(1)}.${S_TIME?.get(2)}-${E_TIME?.get(1)}.${E_TIME?.get(2)}"
            info.text = week.info

            buchong_edit.setOnClickListener {
                val intent = Intent(context, WeeklyRecordActivity::class.java)
                intent.putExtra(Extras.FLAG, "EDIT")
                intent.putExtra(Extras.DATA, week)
                startActivityForResult(intent, EDIT)
            }
        }


        var list = ArrayList<UserBean>()
        val send = db.get(Extras.SEND_USERS, "")
        if (send.isNotEmpty()) {
            list = Gson().fromJson(send, object : TypeToken<List<UserBean>>() {}.type)
        }
        send_adapter = MyAdapter(ctx, list)
        grid_send_to.adapter = send_adapter

        var list1 = ArrayList<UserBean>()
        val copy = db.get(Extras.COPY_FOR_USERS, "")
        if (copy.isNotEmpty()) {
            list1 = Gson().fromJson(copy, object : TypeToken<List<UserBean>>() {}.type)
        }
        chaosong_adapter = MyAdapter(ctx, list1)
        grid_chaosong_to.adapter = chaosong_adapter

        grid_send_to.setOnItemClickListener { parent, view, position, id ->
            if (position == list.size) {
                ContactsActivity.startFromFragment(this,send_adapter.getData(),true,false,requestCode = SEND)
            } else {
                list.removeAt(position)
                send_adapter.notifyDataSetChanged()
                db.set(Extras.SEND_USERS, Gson().toJson(list))
            }
        }

        grid_chaosong_to.setOnItemClickListener { parent, view, position, id ->
            if (position == list1.size) {
                ContactsActivity.startFromFragment(this,chaosong_adapter.getData(),true,false,requestCode = CHAO_SONG)
            } else {
                list1.removeAt(position)
                chaosong_adapter.notifyDataSetChanged()
                db.set(Extras.COPY_FOR_USERS, Gson().toJson(list1))
            }
        }

        btn_commit.setOnClickListener {

            if (send_adapter.list.size == 0) {
                showCustomToast(R.drawable.icon_toast_common, "发送人不可为空")
                return@setOnClickListener
            }

            var weekly_id: Int? = null
            try {
                if (week != null) {
                    weekly_id = week.week_id
                }
            } catch (e: Exception) {
                //week未初始化
                showCustomToast(R.drawable.icon_toast_common, "补充记录不能为空")
                return@setOnClickListener
            }
            var accept_uid: String = ""
            for (item in send_adapter.list) {
                if (item.name == "添加") {
                    continue
                }
                accept_uid = "${accept_uid},${item.user_id}"
            }
            if (accept_uid != "") {
                accept_uid = accept_uid.substring(1)
            }

            var copy_uid: String? = null
            if (chaosong_adapter.list.size > 1) {
                copy_uid = ""
                for (item in chaosong_adapter.list) {
                    if (item.name == "添加") {
                        continue
                    }
                    copy_uid = "${copy_uid},${item.user_id}"
                }
                if (copy_uid != "") {
                    copy_uid = copy_uid?.substring(1)
                }
            }

            SoguApi.getService(baseActivity!!.application,WeeklyService::class.java)
                    .sendReport(weekly_id, accept_uid, copy_uid)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            payload.payload?.apply {

                            }
                            baseActivity?.finish()
                        } else
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }, { e ->
                        Trace.e(e)
                        ToastError(e)
                    })
        }
    }

    var ADD = 0x005
    var EDIT = 0x006
    var SEND = 0x007
    var CHAO_SONG = 0x008

    inner class WeeklyEventAdapter(var context: Context, val list: ArrayList<WeeklyThisBean.Automatic.WeeklyData>) : BaseAdapter() {

        val EVENT = 0x001//AI采集
        val LEAVE = 0x002//非AI采集

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var conView = convertView
            var item = list.get(position)

            if (getItemViewType(position) == EVENT) {
                // 会议，跟踪记录
                conView = LayoutInflater.from(context).inflate(R.layout.weekly_event, null) as LinearLayout
                var dot = conView.findViewById<WeeklyDotView>(R.id.dot) as WeeklyDotView
                var event = conView.findViewById<TextView>(R.id.event) as TextView
                var AI = conView.findViewById<TextView>(R.id.AI) as TextView
                var tag = conView.findViewById<TextView>(R.id.tag) as TextView

                dot.setTime(item.time!!)
                event.text = item.title
                AI.visibility = if (item.is_collect == 1) View.VISIBLE else View.INVISIBLE
                tag.text = item.type_name
            } else {
                // 请假，出差
                conView = LayoutInflater.from(context).inflate(R.layout.weekly_leave, null) as LinearLayout
                var dot = conView.findViewById<WeeklyDotView>(R.id.dot) as WeeklyDotView
                var event = conView.findViewById<TextView>(R.id.event) as TextView
                var AI = conView.findViewById<TextView>(R.id.AI) as TextView
                var tv_start_time = conView.findViewById<TextView>(R.id.tv_start_time) as TextView
                var tv_end_time = conView.findViewById<TextView>(R.id.tv_end_time) as TextView
                var tag = conView.findViewById<TextView>(R.id.tag) as TextView

                dot.setTime(item.time!!)
                event.text = item.title
                AI.visibility = if (item.is_collect == 1) View.VISIBLE else View.INVISIBLE
                tag.text = item.type_name
                tv_start_time.text = item.start_time
                tv_end_time.text = item.end_time
            }
            return conView
        }

        override fun getItemViewType(position: Int): Int {
            if (list[position].is_collect == 1) {
                return EVENT
            }
            return LEAVE
        }

        override fun getViewTypeCount(): Int {
            return 0x003
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

    class MyAdapter(var context: Context, val list: ArrayList<UserBean>) : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var viewHolder: ViewHolder
            var conView = convertView
            if (conView == null) {
                viewHolder = ViewHolder()
                conView = LayoutInflater.from(context).inflate(R.layout.send_item, null) as LinearLayout
                viewHolder.icon = conView.findViewById<CircleImageView>(R.id.icon) as CircleImageView
                viewHolder.name = conView.findViewById<TextView>(R.id.name) as TextView
                conView.setTag(viewHolder)
            } else {
                viewHolder = conView.tag as ViewHolder
            }
            if (position == list.size) {
                viewHolder.icon?.setImageResource(R.drawable.send_add)
                viewHolder.name?.text = "添加"
            } else {
//                viewHolder.icon?.setChar(list[position].name.first())
                if (list[position].headImage().isNullOrEmpty()){
                    viewHolder.icon?.setChar(list[position].name.first())
                }else{
                    Glide.with(context)
                            .load(MyGlideUrl(list[position].headImage()))
                            .apply(RequestOptions().error(R.drawable.nim_avatar_default).fallback(R.drawable.nim_avatar_default))
                            .into(viewHolder.icon!!)
                }
                viewHolder.name?.text = list[position].name
            }
            return conView
        }

        override fun getItem(position: Int): Any {
            return list.get(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return list.size + 1
        }

        fun getData(): ArrayList<UserBean> = list

        class ViewHolder {
            var icon: CircleImageView? = null
            var name: TextView? = null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD && resultCode == Activity.RESULT_OK) {//ADD
            bu_chong_empty.visibility = View.GONE
            buchong_full.visibility = View.VISIBLE

            week = data?.getSerializableExtra(Extras.DATA) as WeeklyThisBean.Week
            var time = buchong_full.findViewById<TextView>(R.id.time) as TextView
            var times = buchong_full.findViewById<TextView>(R.id.times) as TextView
            var info = buchong_full.findViewById<TextView>(R.id.info) as TextView

            var S_TIME = week.start_time?.split("-")
            var E_TIME = week.end_time?.split("-")

            time.text = week.time
            times.text = "${S_TIME?.get(1)}.${S_TIME?.get(2)}-${E_TIME?.get(1)}.${E_TIME?.get(2)}"
            info.text = week.info
        } else if (requestCode == EDIT && resultCode == Activity.RESULT_OK) {//EDIT
            bu_chong_empty.visibility = View.GONE
            buchong_full.visibility = View.VISIBLE

            week = data?.getSerializableExtra(Extras.DATA) as WeeklyThisBean.Week
            var info = buchong_full.findViewById<TextView>(R.id.info) as TextView
            info.text = week.info
        } else if (requestCode == SEND && resultCode == Extras.RESULTCODE) {//SEND
            var beanObj = data?.getSerializableExtra(Extras.DATA) as ArrayList<UserBean>
            send_adapter.list.clear()
            send_adapter.list.addAll(beanObj)
            send_adapter.notifyDataSetChanged()
            db.set(Extras.SEND_USERS, Gson().toJson(beanObj))
        } else if (requestCode == CHAO_SONG && resultCode == Extras.RESULTCODE) {//CHAO_SONG
            var adapter = grid_chaosong_to.adapter as MyAdapter
            var beanObj = data?.getSerializableExtra(Extras.DATA) as ArrayList<UserBean>
            adapter.list.clear()
            adapter.list.addAll(beanObj)
            adapter.notifyDataSetChanged()
            db.set(Extras.COPY_FOR_USERS, Gson().toJson(beanObj))
        }
    }

    companion object {

        fun newInstance(tag: String, data: WeeklyThisBean? = null, s_time: String? = null, e_time: String? = null, week_id: Int): WeeklyThisFragment {
            val fragment = WeeklyThisFragment()
            var args = Bundle()
            args.putString(Extras.FLAG, tag)
            args.putSerializable(Extras.DATA, data)
            args.putString(Extras.TIME1, s_time)
            args.putString(Extras.TIME2, e_time)
            args.putInt(Extras.CODE, week_id)
            fragment.arguments = args
            return fragment
        }
    }
}
