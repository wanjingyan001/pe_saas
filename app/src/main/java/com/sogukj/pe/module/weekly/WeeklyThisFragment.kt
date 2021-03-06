package com.sogukj.pe.module.weekly

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.baselibrary.widgets.MyListView
import com.sogukj.pe.bean.FundSmallBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.bean.WeeklyThisBean
import com.sogukj.pe.module.approve.SealApproveActivity
import com.sogukj.pe.module.approve.SignApproveActivity
import com.sogukj.pe.module.calendar.CalendarMainActivity
import com.sogukj.pe.module.calendar.ModifyTaskActivity
import com.sogukj.pe.module.calendar.TaskDetailActivity
import com.sogukj.pe.module.creditCollection.ShareholderCreditActivity
import com.sogukj.pe.module.fund.FundDetailActivity
import com.sogukj.pe.module.main.ContactsActivity
import com.sogukj.pe.module.project.ProjectDetailActivity
import com.sogukj.pe.module.project.archives.RecordTraceActivity
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.NewService
import com.sogukj.pe.service.WeeklyService
import com.sogukj.pe.widgets.CircleImageView
import com.sogukj.pe.widgets.WeeklyDotView
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_weekly.*
import kotlinx.android.synthetic.main.buchong_empty.*
import kotlinx.android.synthetic.main.buchong_full.*
import kotlinx.android.synthetic.main.fragment_weekly_this.*
import kotlinx.android.synthetic.main.send.*
import org.jetbrains.anko.support.v4.ctx
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

//import kotlinx.android.synthetic.main.layout_network_error.*


class WeeklyThisFragment : BaseFragment(), View.OnClickListener {
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.resetRefresh -> doRequest()
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

    fun hide() {
        LL_list.forEach {
            it.visibility = View.GONE
        }
    }

    fun show() {
        LL_list.forEach {
            it.visibility = View.VISIBLE
        }
    }

    var isViewCreate = false

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && isViewCreate) {
            clearView()
            doRequest()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isViewCreate = true

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
            //buchong_hint.visibility = View.VISIBLE
            user_id = null
            issue = null
            week_id = null
            clearView()
            doRequest()
        } else if (TYPE == "PERSONAL") {
            //buchong_hint.visibility = View.GONE
            spinner_this.visibility = View.VISIBLE
            var obj = arguments!!.getSerializable(Extras.DATA) as WeeklyThisBean
            //initView(obj)
        }
    }

    fun clearView() {
//        if (TYPE == "MAIN") {
//            //不存在这种情况
//        } else if (TYPE == "PERSONAL") {
//            var flag = true
//            while (flag) {
//                try {
//                    root.getChildAt(1) as TextView
//                    flag = false
//                } catch (e: Exception) {
//                    root.removeViewAt(1)
//                }
//            }
//        }
    }

    var isFresh = false

    override fun onResume() {
        super.onResume()
        if (isFresh) {
            isFresh = false
            clearView()
            doRequest()
        }
    }

    lateinit var user: UserBean

    fun doRequest() {
        var s_time: String? = null
        var e_time: String? = null
        if (TYPE == "MAIN") {
            //
            user = Store.store.getUser(baseActivity!!)!!
        } else if (TYPE == "PERSONAL") {
            s_time = arguments!!.getString(Extras.TIME1)
            e_time = arguments!!.getString(Extras.TIME2)
            week_id = arguments!!.getInt(Extras.CODE)
            user = arguments!!.getSerializable(Extras.DATA2) as UserBean
        }
        Glide.with(ctx).asGif().load(R.drawable.loading).into(iv_loading)
        iv_loading.visibility = View.VISIBLE
        if (TYPE == "MAIN") {
            //buchong_hint.visibility = View.GONE
        } else if (TYPE == "PERSONAL") {
            spinner_this.visibility = View.GONE
        }
        baseActivity?.let {
            SoguApi.getService(it.application, WeeklyService::class.java)
                    .getWeekly(user.user_id, issue, s_time, e_time, week_id)
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
//                        when (e) {
//                            is JsonSyntaxException -> showToast("后台数据出错")
//                            is UnknownHostException -> {
//                                showToast("网络出错")
//                                rootScroll.visibility = View.GONE
//                                networkErrorLayout.visibility = View.VISIBLE
//                                resetRefresh.setOnClickListener(this)
//                            }
//                            else -> showToast("未知错误")
//                        }
                    }, {
                        if (TYPE == "MAIN") {
                            //buchong_hint.visibility = View.VISIBLE
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

    var LL_list = ArrayList<LinearLayout>()

    fun initView(loaded: WeeklyThisBean) {
//        if (spinner_this.visibility == View.VISIBLE) {
//            childs = 1
//        }
//        if (TYPE == "MAIN") {
//            //buchong_hint.visibility = View.VISIBLE
//        } else if (TYPE == "PERSONAL") {
//            spinner_this.visibility = View.VISIBLE
//            childs = 1
//        }

        childs = 2

        while (true) {
            if (root.getChildAt(2) is LinearLayout) {
                root.removeViewAt(2)
            } else {
                break
            }
        }

        LL_list.clear()
        loaded.automatic?.let {
            for (items in it.iterator()) {
                val item = inflate.inflate(R.layout.weekly_item, null) as LinearLayout
                //val index = item.findViewById(R.id.tv_index) as TextView
                //val weekday = item.findViewById(R.id.tv_week) as TextView
                //val date = item.findViewById(R.id.tv_date) as TextView
                val event_list = item.findViewById<MyListView>(R.id.event_list)

//                var str = items.date!!.split("-")
//                index.text = str[2].toInt().toString()
//                date.text = "${str[0]}年${str[1]}月"
//                weekday.text = Utils.getTime(SimpleDateFormat("yyyy-MM-dd").parse(items.date), "E")

                val adapter = WeeklyEventAdapter(ctx, items.data!!)
                event_list.adapter = adapter
                event_list.setOnItemClickListener { parent, view, position, id ->
                    val weeklyData = items.data?.get(position)
                    isFresh = true
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
                            fund.id = weeklyData.data_id!!
                            FundDetailActivity.start(context, fund)
                        }
                        11 -> {
                            // 被投企业大事件
                            // 2018-05-28 08:31:04
                            weeklyData.start_time?.apply {
                                val date = this.split(" ")[0]
                                if (!date.isNullOrEmpty()) {
                                    CalendarMainActivity.start(ctx, date)
                                }
                            }
                        }
                    }
                }

                LL_list.add(item)
                root.addView(item, childs++)
            }
        }

//        if (TYPE == "PERSONAL") {
//            return
//        }

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
            //var time = buchong_full.findViewById(R.id.time) as TextView
            //var times = buchong_full.findViewById(R.id.times) as TextView
            var info = buchong_full.findViewById<TextView>(R.id.infoTv)
            var fl_edit = buchong_full.findViewById<FrameLayout>(R.id.fl_edit)

            var S_TIME = week.start_time?.split("-")
            var E_TIME = week.end_time?.split("-")

            loadHead(buchong_full.findViewById<CircleImageView>(R.id.circleImageView))
            buchong_full.findViewById<TextView>(R.id.name).setText("${user!!.name}的周报补充记录")
            var YMD = week.date!!.split(" ")[0]
            var HMS = week.date!!.split(" ")[1]
            buchong_full.findViewById<TextView>(R.id.timeTv).text = "${YMD.split("-")[1]}月${YMD.split("-")[2]}日      ${HMS.substring(0, 5)}"

            //time.text = week.time
            //times.text = "${S_TIME?.get(1)}.${S_TIME?.get(2)}-${E_TIME?.get(1)}.${E_TIME?.get(2)}"
            info.text = week.info

            fl_edit.setOnClickListener {
                val intent = Intent(context, WeeklyRecordActivity::class.java)
                intent.putExtra(Extras.FLAG, "EDIT")
                intent.putExtra(Extras.DATA, week)
                startActivityForResult(intent, EDIT)
            }

            buchong_full.setOnClickListener {
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
//                TeamSelectActivity.startForResult(this, true, send_adapter.getData(), false, false, SEND)
                ContactsActivity.startFromFragment(this, send_adapter.getData(), true, false, requestCode = SEND)
            } else {
                list.removeAt(position)
                send_adapter.notifyDataSetChanged()
                db.set(Extras.SEND_USERS, Gson().toJson(list))
            }
        }

        grid_chaosong_to.setOnItemClickListener { parent, view, position, id ->
            if (position == list1.size) {
//                TeamSelectActivity.startForResult(this, true, chaosong_adapter.getData(), false, false, CHAO_SONG)
                ContactsActivity.startFromFragment(this, chaosong_adapter.getData(), true, false, requestCode = CHAO_SONG)
            } else {
                list1.removeAt(position)
                chaosong_adapter.notifyDataSetChanged()
                db.set(Extras.COPY_FOR_USERS, Gson().toJson(list1))
            }
        }

        if (TYPE == "PERSONAL") {
            send_layout.visibility = View.GONE
        }

        btn_commit.setOnClickListener {

            if (send_adapter.list.size == 0) {
                showCustomToast(R.drawable.icon_toast_common, "发送人不可为空")
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

            var weekly_id: Int? = null
            try {
                if (week != null) {
                    weekly_id = week.week_id
                    SoguApi.getService(baseActivity!!.application, WeeklyService::class.java)
                            .sendReport(weekly_id, accept_uid, copy_uid)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ payload ->
                                if (payload.isOk) {
                                    payload.payload?.apply {

                                    }
                                    db.set(Extras.SEND_USERS, "")
                                    db.set(Extras.COPY_FOR_USERS, "")
                                    //baseActivity?.finish()
                                    kotlin.run {
                                        (activity as WeeklyActivity).view_pager.currentItem = 2
                                    }
                                } else
                                    showCustomToast(R.drawable.icon_toast_fail, payload.message)
                            }, { e ->
                                Trace.e(e)
                                ToastError(e)
                            })
                }
            } catch (e: Exception) {
                //week未初始化
//                showCustomToast(R.drawable.icon_toast_common, "补充记录不能为空")
//                return@setOnClickListener
                weekly_id = null
                SoguApi.getService(baseActivity!!.application, WeeklyService::class.java)
                        .addEditReport(start_time = loaded.week!!.start_time!!, end_time = loaded.week!!.end_time!!, content = "", week_id = null)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ payload ->
                            if (payload.isOk) {
                                payload.payload?.apply {

                                    weekly_id = this.toString().split(".")[0].toInt()
                                    SoguApi.getService(baseActivity!!.application, WeeklyService::class.java)
                                            .sendReport(weekly_id, accept_uid, copy_uid)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribeOn(Schedulers.io())
                                            .subscribe({ payload ->
                                                if (payload.isOk) {
                                                    payload.payload?.apply {

                                                    }
                                                    db.set(Extras.SEND_USERS, "")
                                                    db.set(Extras.COPY_FOR_USERS, "")
                                                    //baseActivity?.finish()
                                                    kotlin.run {
                                                        (activity as WeeklyActivity).view_pager.currentItem = 2
                                                    }
                                                } else
                                                    showCustomToast(R.drawable.icon_toast_fail, payload.message)
                                            }, { e ->
                                                Trace.e(e)
                                                ToastError(e)
                                            })
                                }
                            } else
                                showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }, { e ->
                            Trace.e(e)
                            ToastError(e)
                        })
            }
        }
    }

    var ADD = 0x005
    var EDIT = 0x006
    var SEND = 0x007
    var CHAO_SONG = 0x008

    internal fun loadHead(header: CircleImageView) {
        if (user?.url.isNullOrEmpty()) {
            val ch = user?.name?.first()
            header.setChar(ch)
        } else {
            Glide.with(ctx)
                    .load(MyGlideUrl(user?.url))
                    .listener(object : RequestListener<Drawable> {
                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            header.setImageDrawable(resource)
                            return true
                        }

                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            val ch = user?.name?.first()
                            header.setChar(ch)
                            return true
                        }
                    })
                    .into(header)
        }
    }

    internal fun fillAI(item: WeeklyThisBean.Automatic.WeeklyData, tv: TextView) {

        var cellW = Utils.dpToPx(context, 10)

        var drawable = resources.getDrawable(R.drawable.ai)
        drawable.setBounds(0, 0, cellW, cellW)
        tv.setCompoundDrawables(drawable, null, null, null)
        tv.setText("AI采集")
        // 0日程，1任务 2会议      5跟踪记录           6项目 3用印审批 4签字审批(AI)            7请假 8出差
        if (item.type == 0) {
            drawable = resources.getDrawable(R.drawable.huiyi)
            drawable.setBounds(0, 0, cellW, cellW)
            tv.setCompoundDrawables(drawable, null, null, null)
            tv.setText("日程")
        } else if (item.type == 1) {
            drawable = resources.getDrawable(R.drawable.huiyi)
            drawable.setBounds(0, 0, cellW, cellW)
            tv.setCompoundDrawables(drawable, null, null, null)
            tv.setText("任务")
        } else if (item.type == 2) {
            drawable = resources.getDrawable(R.drawable.huiyi)
            drawable.setBounds(0, 0, cellW, cellW)
            tv.setCompoundDrawables(drawable, null, null, null)
            tv.setText("会议")
        } else if (item.type == 3) {
            drawable = resources.getDrawable(R.drawable.ai)
            drawable.setBounds(0, 0, cellW, cellW)
            tv.setCompoundDrawables(drawable, null, null, null)
            tv.setText("用印审批")
        } else if (item.type == 4) {
            drawable = resources.getDrawable(R.drawable.ai)
            drawable.setBounds(0, 0, cellW, cellW)
            tv.setCompoundDrawables(drawable, null, null, null)
            tv.setText("签字审批")
        } else if (item.type == 5) {
            drawable = resources.getDrawable(R.drawable.gzjl)
            drawable.setBounds(0, 0, cellW, cellW)
            tv.setCompoundDrawables(drawable, null, null, null)
            tv.setText("拜访记录")
        } else if (item.type == 6) {
            drawable = resources.getDrawable(R.drawable.ai)
            drawable.setBounds(0, 0, cellW, cellW)
            tv.setCompoundDrawables(drawable, null, null, null)
            tv.setText("项目")
        } else if (item.type == 7) {
            drawable = resources.getDrawable(R.drawable.ccqj)
            drawable.setBounds(0, 0, cellW, cellW)
            tv.setCompoundDrawables(drawable, null, null, null)
            tv.setText("请假")
        } else if (item.type == 8) {
            drawable = resources.getDrawable(R.drawable.ccqj)
            drawable.setBounds(0, 0, cellW, cellW)
            tv.setCompoundDrawables(drawable, null, null, null)
            tv.setText("出差")
        }
    }

    // 2018-07-09 14:58:18
    internal fun loadTime(time: String, tv: TextView) {
        var YMD = time.split(" ")[0]
        var HMS = time.split(" ")[1]

        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val date = sdf.parse(YMD)
        val calendar = Calendar.getInstance()
        calendar.setTime(date)
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            tv.setText("周一      ${HMS.substring(0, 5)}")
        } else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) {
            tv.setText("周二      ${HMS.substring(0, 5)}")
        } else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
            tv.setText("周三      ${HMS.substring(0, 5)}")
        } else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
            tv.setText("周四      ${HMS.substring(0, 5)}")
        } else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            tv.setText("周五      ${HMS.substring(0, 5)}")
        } else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            tv.setText("周六      ${HMS.substring(0, 5)}")
        } else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            tv.setText("周日      ${HMS.substring(0, 5)}")
        }
    }

    inner class WeeklyEventAdapter(var context: Context, val list: ArrayList<WeeklyThisBean.Automatic.WeeklyData>) : BaseAdapter() {//  time=2017-10-06

        val EVENT = 0x001//AI采集
        val LEAVE = 0x002//非AI采集

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var conView = convertView
            var item = list.get(position)

            if (getItemViewType(position) == EVENT) {
                // 会议，跟踪记录
                conView = LayoutInflater.from(context).inflate(R.layout.weekly_event, null) as LinearLayout
                var dot = conView.findViewById<WeeklyDotView>(R.id.dot)
                var event = conView.findViewById<TextView>(R.id.event)
                var AI = conView.findViewById<TextView>(R.id.AI)
                //var tag = conView.findViewById(R.id.tag) as TextView

                //dot.setTime(item.time!!)
                event.text = item.title
                fillAI(item, AI)
                //AI.visibility = if (item.is_collect == 1) View.VISIBLE else View.INVISIBLE
                //tag.text = item.type_name

                loadHead(conView.findViewById<CircleImageView>(R.id.circleImageView))
                conView.findViewById<TextView>(R.id.name).setText("${user!!.name}的周报")
                loadTime(item.add_time!!, conView.findViewById<TextView>(R.id.timeTv))
            } else {
                // 请假，出差
                conView = LayoutInflater.from(context).inflate(R.layout.weekly_leave, null) as LinearLayout
                var dot = conView.findViewById<WeeklyDotView>(R.id.dot)
                var event = conView.findViewById<TextView>(R.id.event)
                var AI = conView.findViewById<TextView>(R.id.AI)
                var tv_start_time = conView.findViewById<TextView>(R.id.tv_start_time)
                var tv_end_time = conView.findViewById<TextView>(R.id.tv_end_time)
                //var tag = conView.findViewById(R.id.tag) as TextView

                //dot.setTime(item.time!!)
                event.text = item.title
                //AI.visibility = if (item.is_collect == 1) View.VISIBLE else View.INVISIBLE
                fillAI(item, AI)
                //tag.text = item.type_name
                tv_start_time.text = item.start_time
                tv_end_time.text = item.end_time

                loadHead(conView.findViewById<CircleImageView>(R.id.circleImageView))
                conView.findViewById<TextView>(R.id.name).setText("${user!!.name}的周报")
                loadTime(item.add_time!!, conView.findViewById<TextView>(R.id.timeTv))
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
        SoguApi.getService(activity!!.application, NewService::class.java)
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
                viewHolder.icon = conView.findViewById<CircleImageView>(R.id.icon)
                viewHolder.name = conView.findViewById<TextView>(R.id.name)
                viewHolder.cs_add = conView.findViewById<ImageView>(R.id.cs_add)
                viewHolder.cs_default = conView.findViewById<ImageView>(R.id.cs_default)
                conView.setTag(viewHolder)
            } else {
                viewHolder = conView.tag as ViewHolder
            }
            if (position == list.size) {
                //viewHolder.icon?.setBackgroundResource(R.drawable.send_add)
                Glide.with(context)
                        .load(R.drawable.send_add)
                        .into(viewHolder.icon!!)
                viewHolder.name?.text = "添加"
                viewHolder.cs_add?.visibility = View.GONE
                viewHolder.cs_default?.visibility = View.GONE
            } else {
//                viewHolder.icon?.setChar(list[position].name.first())
                if (list[position].headImage().isNullOrEmpty()) {
                    viewHolder.icon?.setChar(list[position].name.first())
                } else {
                    Glide.with(context)
                            .load(list[position].headImage())
                            .apply(RequestOptions().error(R.drawable.nim_avatar_default).fallback(R.drawable.nim_avatar_default))
                            .into(viewHolder.icon!!)
                }
                viewHolder.name?.text = list[position].name
                viewHolder.cs_add?.visibility = View.VISIBLE
                viewHolder.cs_default?.visibility = View.GONE
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
            var cs_add: ImageView? = null
            var cs_default: ImageView? = null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD && resultCode == Activity.RESULT_OK) {//ADD
            bu_chong_empty.visibility = View.GONE
            buchong_full.visibility = View.VISIBLE

            week = data?.getSerializableExtra(Extras.DATA) as WeeklyThisBean.Week
            //var time = buchong_full.findViewById(R.id.time) as TextView
            //var times = buchong_full.findViewById(R.id.times) as TextView
            var info = buchong_full.findViewById<TextView>(R.id.infoTv)

            var S_TIME = week.start_time?.split("-")
            var E_TIME = week.end_time?.split("-")

            loadHead(buchong_full.findViewById<CircleImageView>(R.id.circleImageView))
            buchong_full.findViewById<TextView>(R.id.name).setText("${user!!.name}的周报补充记录")
            var YMD = week.date!!.split(" ")[0]
            var HMS = week.date!!.split(" ")[1]
            buchong_full.findViewById<TextView>(R.id.timeTv).text = "${YMD.split("-")[1]}月${YMD.split("-")[2]}日      ${HMS.substring(0, 5)}"

            var fl_edit = buchong_full.findViewById<FrameLayout>(R.id.fl_edit)
            fl_edit.setOnClickListener {
                val intent = Intent(context, WeeklyRecordActivity::class.java)
                intent.putExtra(Extras.FLAG, "EDIT")
                intent.putExtra(Extras.DATA, week)
                startActivityForResult(intent, EDIT)
            }

            //time.text = week.time
            //times.text = "${S_TIME?.get(1)}.${S_TIME?.get(2)}-${E_TIME?.get(1)}.${E_TIME?.get(2)}"
            info.text = week.info
        } else if (requestCode == EDIT && resultCode == Activity.RESULT_OK) {//EDIT
            bu_chong_empty.visibility = View.GONE
            buchong_full.visibility = View.VISIBLE

            week = data?.getSerializableExtra(Extras.DATA) as WeeklyThisBean.Week
            var info = buchong_full.findViewById<TextView>(R.id.infoTv)
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

        fun newInstance(tag: String, data: WeeklyThisBean? = null, s_time: String? = null, e_time: String? = null, week_id: Int, bean: UserBean? = null): WeeklyThisFragment {
            val fragment = WeeklyThisFragment()
            var args = Bundle()
            args.putString(Extras.FLAG, tag)
            args.putSerializable(Extras.DATA, data)
            args.putString(Extras.TIME1, s_time)
            args.putString(Extras.TIME2, e_time)
            args.putInt(Extras.CODE, week_id)
            args.putSerializable(Extras.DATA2, bean)
            fragment.arguments = args
            return fragment
        }
    }
}
