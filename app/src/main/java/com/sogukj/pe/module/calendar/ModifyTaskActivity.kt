package com.sogukj.pe.module.calendar

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.Extras.SCHEDULE_DRAFT
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.ifNotNull
import com.sogukj.pe.baselibrary.Extended.noSpace
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.bean.*
import com.sogukj.pe.interf.AddPersonListener
import com.sogukj.pe.module.calendar.adapter.CcPersonAdapter
import com.sogukj.pe.module.calendar.adapter.ExecutiveAdapter
import com.sogukj.pe.module.main.ContactsActivity
import com.sogukj.pe.module.other.CompanySelectActivity
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.CalendarService
import com.sogukj.pe.widgets.CalendarDingDing
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_modify_task.*
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk25.coroutines.textChangedListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

/**
 * 添加/修改任务界面
 */
class ModifyTaskActivity : ToolbarActivity(), View.OnClickListener, AddPersonListener {
    var type: Long by Delegates.notNull()
    var name: String? = null
    var selectT: Int by Delegates.notNull()
    var data_id: Int? = null
    var companyId: Int? = null
    lateinit var adapter: CcPersonAdapter//抄送人
    lateinit var exAdapter: ExecutiveAdapter//执行人
    private lateinit var xmlDb: XmlDb
    val data = ArrayList<UserBean>()
    val data2 = ArrayList<UserBean>()
    override val menuId: Int
        get() = R.menu.modify_submit


    companion object {
        const val CREATE = 1L
        const val MODIFY = 2L
        const val Schedule = "Schedule"
        const val Task = "Task"
        fun startForCreate(ctx: Activity?, name: String) {
            val intent = Intent(ctx, ModifyTaskActivity::class.java)
            intent.putExtra(Extras.NAME, name)
            intent.putExtra(Extras.TYPE, CREATE)
            ctx?.startActivity(intent)
        }

        fun startForModify(ctx: Activity?, data_id: Int, name: String) {
            val intent = Intent(ctx, ModifyTaskActivity::class.java)
            intent.putExtra(Extras.TYPE, MODIFY)
            intent.putExtra(Extras.NAME, name)
            intent.putExtra(Extras.DATA, data_id)
            ctx?.startActivity(intent)
        }
    }

    override fun onBackPressed() {
        //创建时保存草稿
        val reqBean = getReqBean()
        if (type == CREATE) {
            if (!isNeedSave) {
                super.onBackPressed()
            } else {
                var mDialog = MaterialDialog.Builder(this@ModifyTaskActivity)
                        .theme(Theme.LIGHT)
                        .canceledOnTouchOutside(true)
                        .customView(R.layout.dialog_yongyin, false).build()
                mDialog.show()
                val content = mDialog.find<TextView>(R.id.content)
                val cancel = mDialog.find<Button>(R.id.cancel)
                val yes = mDialog.find<Button>(R.id.yes)
                content.text = "是否需要保存草稿"
                cancel.text = "否"
                yes.text = "是"
                cancel.setOnClickListener {
                    super.onBackPressed()
                }
                yes.setOnClickListener {
                    showCustomToast(R.drawable.icon_toast_success, "草稿保存成功")
                    val draft = TaskDraft()
                    draft.taskReqBean = reqBean
                    draft.company = companyBean
                    draft.executorList = selectUser2.selectUsers
                    draft.watcherList = selectUser.selectUsers
                    xmlDb.set("${Store.store.getUser(this)?.uid}_${SCHEDULE_DRAFT}_$name", Gson().toJson(draft))
                    super.onBackPressed()
                }
            }
        } else {
            super.onBackPressed()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_task)
        setBack(true)
        adapter = CcPersonAdapter(context, data)
        adapter.setListener(this)
        copyList.layoutManager = GridLayoutManager(this, 6)
        copyList.adapter = adapter

        exAdapter = ExecutiveAdapter(context, data2)
        exAdapter.setListener(this)
        executiveList.layoutManager = GridLayoutManager(context, 6)
        executiveList.adapter = exAdapter
        type = intent.getLongExtra(Extras.TYPE, -1)
        name = intent.getStringExtra(Extras.NAME)
        when (type) {
            CREATE -> {
                when (name) {
                    Task -> {
                        executiveLayout.visibility = View.VISIBLE
                        copyLayout.visibility = View.VISIBLE
                        line.visibility = View.VISIBLE
                        selectTypeLayout.visibility = View.GONE
                        selectT = 1
                        title = "添加任务"
                        missionTV.text = "任务描述"
                    }
                    Schedule -> {
                        executiveLayout.visibility = View.GONE
                        copyLayout.visibility = View.GONE
                        line.visibility = View.GONE
                        selectTypeLayout.visibility = View.GONE
                        selectT = 0
                        title = "添加日程"
                        missionTV.text = "日程描述"
                    }
                    else -> {
                        title = "添加"
                        selectTypeLayout.visibility = View.VISIBLE
                    }
                }
                start = Date(System.currentTimeMillis())
                endTime = Date(System.currentTimeMillis() + 3600 * 1000)
                startTime.text = Utils.getTime(start, "MM月dd日 E HH:mm")
                deadline.text = Utils.getTime(endTime, "MM月dd日 E HH:mm")
            }
            MODIFY -> {
                when (name) {
                    Task -> {
                        executiveLayout.visibility = View.VISIBLE
                        copyLayout.visibility = View.VISIBLE
                        line.visibility = View.VISIBLE
                        selectTypeLayout.visibility = View.GONE
                        selectT = 1
                        title = "修改任务"
                        missionTV.text = "任务描述"
                    }
                    Schedule -> {
                        executiveLayout.visibility = View.GONE
                        copyLayout.visibility = View.GONE
                        line.visibility = View.GONE
                        selectTypeLayout.visibility = View.GONE
                        selectT = 0
                        title = "修改日程"
                        missionTV.text = "日程描述"
                    }
                    else -> {
                        title = "修改"
                        selectTypeLayout.visibility = View.VISIBLE
                    }
                }
                data_id = intent.getIntExtra(Extras.DATA, -1)
                doRequest()
            }
        }
        selectType.setOnClickListener(this)
        relatedProject.setOnClickListener(this)
        startTime.setOnClickListener(this)
        deadline.setOnClickListener(this)
        remind.setOnClickListener(this)
        missionDetails.textChangedListener {
            onTextChanged { charSequence, s, b, c ->
                if (missionDetails.textStr.isNotEmpty()) {
                    missionDetails.gravity = Gravity.START
                } else {
                    missionDetails.gravity = Gravity.END
                }
            }
        }
        xmlDb = XmlDb.open(application)
        startDD = CalendarDingDing(context)
        deadDD = CalendarDingDing(context)
        draftRecurrent()
    }

    var seconds: Int? = null
    fun doRequest() {
        data_id?.let {
            SoguApi.getService(application, CalendarService::class.java)
                    .showEditTask(it)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            payload.payload?.let {
                                companyId = it.company_id
                                relatedProject.text = it.cName
                                missionDetails.setText(it.info)
                                start = SimpleDateFormat("yyyy-MM-dd HH:mm").parse(it.start_time)
                                endTime = SimpleDateFormat("yyyy-MM-dd HH:mm").parse(it.end_time)
                                startTime.text = Utils.getTime(start, "MM月dd日 E HH:mm")
                                deadline.text = Utils.getTime(endTime, "MM月dd日 E HH:mm")
                                if (it.clock == null) {
                                    remind.text = "不提醒"
                                } else {
                                    remind.text = when (it.clock!!.div(60)) {
                                        0 -> "不提醒"
                                        5 -> "提前5分钟"
                                        15 -> "提前15分钟"
                                        30 -> "提前30分钟"
                                        60 -> "提前1小时"
                                        60 * 24 -> "提前1天"
                                        else -> "提前${it.clock!!.div(60)}分钟"
                                    }
                                }
                                seconds = it.clock
                                it.executor?.forEach {
                                    val bean = UserBean()
                                    bean.uid = it.id
                                    bean.name = it.name
                                    bean.url = it.url
                                    selectUser2.selectUsers.add(bean)
                                    exAdapter.addData(bean)
                                }
                                it.watcher?.forEach {
                                    val bean = UserBean()
                                    bean.uid = it.id
                                    bean.name = it.name
                                    bean.url = it.url
                                    selectUser.selectUsers.add(bean)
                                    adapter.addData(bean)
                                }
                            }
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
                    })
        }
    }


    private fun submitChange() {
        if (missionDetails.noSpace.isEmpty()) {
            showCustomToast(R.drawable.icon_toast_common, "请填写日程描述")
            return
        }
        if (start.time == 0L || endTime.time == 0L) {
            showCustomToast(R.drawable.icon_toast_common, "请选择时间")
            return
        }
        if (start.time - endTime.time > 0) {
            showCustomToast(R.drawable.icon_toast_common, "开始时间不能大于结束时间")
            return
        }
        SoguApi.getService(application, CalendarService::class.java)
                .aeCalendarInfo(getReqBean())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        Utils.closeInput(this, missionDetails)
                        xmlDb.set("${Store.store.getUser(this)?.uid}_${SCHEDULE_DRAFT}_$name", "{}")
                        showCustomToast(R.drawable.icon_toast_success, "提交成功")
                        finish()
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                    hideProgress()
                }, {
                    hideProgress()
                }, {
                    showProgress("正在提交")
                })

    }

    var isNeedSave = false

    /**
     * 获取要提交的数据对象
     */
    private fun getReqBean(): TaskModifyBean {
        val reqBean = TaskModifyBean()
        val bean = reqBean.ae
        reqBean.data_id = data_id
        reqBean.type = selectT
        bean.info = missionDetails.noSpace
        if (companyBean != null) {
            bean.company_id = companyBean?.id
        } else {
            bean.company_id = companyId
        }
        bean.start_time = Utils.getTime(start, "yyyy-MM-dd HH:mm")
        bean.end_time = Utils.getTime(endTime, "yyyy-MM-dd HH:mm")
        bean.clock = seconds
        val exusers = StringBuilder()
        if (data2.isNotEmpty()) {
            data2.forEach {
                if (it.uid != null) {
                    exusers.append(it.uid.toString() + ",")
                }
            }
        }
        if (exusers.isNotEmpty()) {
            bean.executor = exusers.toString().substring(0, exusers.length - 1)
        }
        val watchusers = StringBuilder()
        if (data.isNotEmpty()) {
            data.forEach {
                if (it.uid != null) {
                    watchusers.append(it.uid.toString() + ",")
                }
            }
        }
        if (watchusers.isNotEmpty()) {
            bean.watcher = watchusers.toString().substring(0, watchusers.length - 1)
        }
        Log.d("WJY", Gson().toJson(reqBean))

        if (!bean.info.isNullOrEmpty()) {
            isNeedSave = true
        }
        if (bean.company_id != null) {
            isNeedSave = true
        }
        if (!bean.executor.isNullOrEmpty()) {
            isNeedSave = true
        }
        if (!bean.watcher.isNullOrEmpty()) {
            isNeedSave = true
        }

        return reqBean
    }

    lateinit var startDD: CalendarDingDing
    lateinit var deadDD: CalendarDingDing

    var start: Date by Delegates.observable(Date(), { property, oldValue, newValue ->
        if (newValue.time > endTime.time) {
            endTime = Date(newValue.time + 3600 * 1000)
            deadline.text = Utils.getTime(endTime, "MM月dd日 E HH:mm")
        }
    })
    var endTime: Date by Delegates.observable(Date(), { property, oldValue, newValue ->
        if (newValue.time < start.time) {
            start = Date(newValue.time - 3600 * 1000)
            startTime.text = Utils.getTime(start, "MM月dd日 E HH:mm")
        }
    })

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.selectType -> {
                MaterialDialog.Builder(this)
                        .theme(Theme.LIGHT)
                        .title("请选择类型")
                        .items(arrayListOf("日程", "任务"))
                        .itemsCallbackSingleChoice(-1) { dialog, itemView, which, text ->
                            if (text == "日程") {
                                selectT = 0
                                executiveLayout.visibility = View.GONE
                                copyLayout.visibility = View.GONE
                                line.visibility = View.GONE
                            } else if (text == "任务") {
                                selectT = 1
                                executiveLayout.visibility = View.VISIBLE
                                copyLayout.visibility = View.VISIBLE
                                line.visibility = View.VISIBLE
                            }
                            selectType.text = text
                            true
                        }
                        .positiveText("确定")
                        .negativeText("取消")
                        .show()
            }
            R.id.relatedProject -> {
                CompanySelectActivity.start(this)
            }
            R.id.startTime -> {
                Utils.closeInput(context, missionDetails)
                val selectedDate = Calendar.getInstance()//系统当前时间
                val startDate = Calendar.getInstance()
                startDate.set(1949, 10, 1)
                val endDate = Calendar.getInstance()
                endDate.set(2020, 12, 31)
                startDD.show(2, selectedDate) { date ->
                    if (date != null) {
                        start = date
                        startTime.text = Utils.getTime(date, "MM月dd日 E HH:mm")
                    }
                }
            }
            R.id.deadline -> {
                Utils.closeInput(context, missionDetails)
                val selectedDate = Calendar.getInstance()//系统当前时间
                val startDate = Calendar.getInstance()
                startDate.set(1949, 10, 1)
                val endDate = Calendar.getInstance()
                endDate.set(2020, 12, 31)
                deadDD.show(2, selectedDate) { date ->
                    if (date != null) {
                        endTime = date
                        deadline.text = Utils.getTime(date, "MM月dd日 E HH:mm")
                    }
                }
            }
            R.id.remind -> {
                if (start.time == 0L) {
                    showCustomToast(R.drawable.icon_toast_common, "请选择开始时间")
                    //showToast("请选择开始时间")
                    return
                }
                if (endTime.time == 0L) {
                    showCustomToast(R.drawable.icon_toast_common, "请选择结束时间")
                    //showToast("请选择结束时间")
                    return
                }
                if (seconds == null) {
                    seconds = 0
                }
                RemindTimeActivity.start(this, seconds.toString())
            }
        }
    }

    var companyBean: CustomSealBean.ValueBean? = null
    var selectUser: Users = Users()//抄送人
    var selectUser2: Users = Users()//执行人
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            if (resultCode == Extras.RESULTCODE) {
                when (requestCode) {
                    1005 -> {
                        selectUser.selectUsers = data.getSerializableExtra(Extras.DATA) as ArrayList<UserBean>
                        adapter.addAllData(selectUser.selectUsers)
                        this.data.clear()
                        this.data.addAll(selectUser.selectUsers)
                    }
                    1006 -> {
                        selectUser2.selectUsers = data.getSerializableExtra(Extras.DATA) as ArrayList<UserBean>
                        exAdapter.addAllData(selectUser2.selectUsers)
                        this.data2.clear()
                        this.data2.addAll(selectUser2.selectUsers)
                    }
                    Extras.REQUESTCODE -> {
                        val str = data.getStringExtra(Extras.DATA)
                        remind.text = str
                        seconds = when (remind.text) {
                            "不提醒" -> 0
                            "提前5分钟" -> 5 * 60
                            "提前15分钟" -> 15 * 60
                            "提前30分钟" -> 30 * 60
                            "提前1小时" -> 60 * 60
                            "提前1天" -> 24 * 60 * 60
                            else -> null
                        }
                    }
                }

            } else if (resultCode == Activity.RESULT_OK) {
                companyBean = data.getSerializableExtra(Extras.DATA) as CustomSealBean.ValueBean
                companyBean?.let {
                    relatedProject.text = it.name
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.modify_submit -> {
                submitChange()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun addPerson(tag: String) {
        when (tag) {
            "CcPersonAdapter" -> {
                ContactsActivity.start(this, selectUser.selectUsers, true, false, requestCode = 1005)
            }
            "ExecutiveAdapter" -> {
                ContactsActivity.start(this, selectUser2.selectUsers, true, false, requestCode = 1006)
            }
        }
    }

    override fun remove(tag: String, user: UserBean) {
        when (tag) {
            "CcPersonAdapter" -> {
                selectUser.selectUsers.remove(user)
            }
            "ExecutiveAdapter" -> {
                selectUser2.selectUsers.remove(user)
            }
        }
    }

    /**
     *  草稿复现
     */
    private fun draftRecurrent() {
        if (type == CREATE) {
            val draftJson = xmlDb.get("${Store.store.getUser(this)?.uid}_${SCHEDULE_DRAFT}_$name", "")
            if (draftJson.isNotEmpty()) {
                val draft = Gson().fromJson(draftJson, TaskDraft::class.java)
                draft?.let {
                    it.taskReqBean?.ae?.apply {
                        missionDetails.setText(info)
                        start = SimpleDateFormat("yyyy-MM-dd HH:mm").parse(start_time)
                        endTime = SimpleDateFormat("yyyy-MM-dd HH:mm").parse(end_time)
                        startTime.text = Utils.getTime(start, "MM月dd日 E HH:mm")
                        deadline.text = Utils.getTime(endTime, "MM月dd日 E HH:mm")
                        if (clock == null) {
                            remind.text = "不提醒"
                        } else {
                            seconds = clock
                            remind.text = when (clock!!.div(60)) {
                                0 -> "不提醒"
                                5 -> "提前5分钟"
                                15 -> "提前15分钟"
                                30 -> "提前30分钟"
                                60 -> "提前1小时"
                                60 * 24 -> "提前1天"
                                else -> "提前${clock!!.div(60)}分钟"
                            }
                        }
                    }
                    it.company?.let {
                        companyBean = it
                        relatedProject.text = it.name
                    }
                    it.executorList?.let {
                        selectUser2.selectUsers = it
                        exAdapter.addAllData(selectUser2.selectUsers)
                    }
                    it.watcherList?.let {
                        selectUser.selectUsers = it
                        adapter.addAllData(selectUser.selectUsers)
                    }
                }
            }
        }
    }

//    override fun onStop() {
//        super.onStop()
//        //创建时保存草稿
//        if (type == CREATE) {
//            val reqBean = getReqBean()
//            val draft = TaskDraft()
//            draft.taskReqBean = reqBean
//            draft.company = companyBean
//            draft.executorList = selectUser2.selectUsers
//            draft.watcherList = selectUser.selectUsers
//            xmlDb.set("${Store.store.getUser(this)?.uid}_${SCHEDULE_DRAFT}_$name", Gson().toJson(draft))
//        }
//    }

}
