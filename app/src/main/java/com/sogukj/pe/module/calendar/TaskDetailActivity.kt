package com.sogukj.pe.module.calendar

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.withOutEmpty
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.ScheduleBean
import com.sogukj.pe.bean.TaskDetailBean
import com.sogukj.pe.interf.CommentListener
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.CalendarService
import com.sogukj.pe.widgets.CircleImageView
import com.sogukj.pe.widgets.CommentWindow
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_task_detail.*
import org.jetbrains.anko.find
import kotlin.properties.Delegates

class TaskDetailActivity : ToolbarActivity(), CommentListener, View.OnClickListener {
    lateinit var adapter: RecyclerAdapter<TaskDetailBean.Record>
    lateinit var window: CommentWindow
    var data_id: Int by Delegates.notNull()
    var titleStr: String = ""
    var name: String = ""


    companion object {
        fun start(ctx: Activity?, data_id: Int, title: String, name: String) {
            val intent = Intent(ctx, TaskDetailActivity::class.java)
            intent.putExtra(Extras.ID, data_id)
            intent.putExtra(Extras.NAME, name)
            intent.putExtra(Extras.TITLE, title)
            ctx?.startActivity(intent)
        }

        fun start(ctx: Context?, data_id: Int, title: String, name: String) {
            val intent = Intent(ctx, TaskDetailActivity::class.java)
            intent.putExtra(Extras.ID, data_id)
            intent.putExtra(Extras.NAME, name)
            intent.putExtra(Extras.TITLE, title)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx?.startActivity(intent)
        }

        fun startSchedule(ctx: Activity?, scheduleBean: ScheduleBean, name: String) {
            val intent = Intent(ctx, TaskDetailActivity::class.java)
            intent.putExtra(Extras.DATA, scheduleBean)
            intent.putExtra(Extras.NAME, name)
            ctx?.startActivity(intent)
        }
    }

    override val menuId: Int
        get() = R.menu.task_modify

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)
        name = intent.getStringExtra(Extras.NAME)
        data_id = intent.getIntExtra(Extras.ID, -1)
        titleStr = intent.getStringExtra(Extras.TITLE)
        setBack(true)
        when (name) {
            ModifyTaskActivity.Schedule -> {
                title = "日程详情"
                delete.text = "删除日程"
                commentLayout.visibility = View.GONE
                taskExecutive.visibility = View.GONE
                doRequest2(data_id)
            }
            ModifyTaskActivity.Task -> {
                title = "任务详情"
                adapter = RecyclerAdapter(this) { _adapter, parent, position ->
                    val convertView = _adapter.getView(R.layout.item_comment_list, parent)
                    object : RecyclerHolder<TaskDetailBean.Record>(convertView) {
                        val headerImage = convertView.find<CircleImageView>(R.id.headerImage)
                        val commentName = convertView.find<TextView>(R.id.commentName)
                        val info = convertView.find<TextView>(R.id.infoTv)
                        val time = convertView.find<TextView>(R.id.timeTv)
                        val type = convertView.find<TextView>(R.id.type)
                        override fun setData(view: View, data: TaskDetailBean.Record, position: Int) {

                            if(data.url.isNullOrEmpty()){
                                headerImage.setChar(data.name?.first())
                            } else {
                                Glide.with(this@TaskDetailActivity)
                                        .load(MyGlideUrl(data.url))
                                        .apply(RequestOptions().error(R.drawable.nim_avatar_default).fallback(R.drawable.nim_avatar_default))
                                        .into(headerImage)
                            }

                            commentName.text = data.name
                            when (data.type) {
                                "1" -> {
                                    info.text = "布置任务"
                                    type.visibility = View.GONE
                                }
                                "2" -> {
                                    info.text = "接受任务"
                                    type.text = TextStrSplice("意见:${data.content}", 3)
                                }
                                "3" -> {
                                    info.text = "拒绝任务"
                                    type.text = TextStrSplice("原因:${data.content}", 3)
                                }
                                else -> {
                                    info.text = "评价任务"
                                    type.text = TextStrSplice("评论:${data.content}", 3)
                                }
                            }
                            time.text = data.time
                        }
                    }
                }
                commentList.layoutManager = LinearLayoutManager(this)
                commentList.adapter = adapter
                window = CommentWindow(this, this)
                commentTv.setOnClickListener {
                    window.showAtLocation(find(R.id.task_detail_main), Gravity.BOTTOM, 0, 0)
                }
                doRequest(data_id)
            }
        }
        taskTitle.text = titleStr
        delete.setOnClickListener(this)

    }

    fun doRequest(data_id: Int) {
        if (data_id == 0) {
            return
        }
        SoguApi.getService(application,CalendarService::class.java)
                .showTaskDetail(data_id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.let {
                            val user = Store.store.getUser(this)
                            if (user?.uid != it.info?.pub_id) {
                                mMenu.getItem(0).isVisible = false
                                mMenu.getItem(0).isEnabled = false
                                delete.visibility = View.GONE
                            } else {
                                mMenu.getItem(0).isVisible = true
                                mMenu.getItem(0).isEnabled = true
                                delete.visibility = View.VISIBLE
                            }
                            it.info?.let {
                                taskNumber.text = TextStrSplice("任务编号: ${it.number.withOutEmpty}", 5)
                                arrangeTime.text = TextStrSplice("安排时间: ${it.timing.withOutEmpty}", 5)
                                taskPublisher.text = TextStrSplice("任务发布者: ${it.publisher.withOutEmpty}", 6)
                                related_project.text = TextStrSplice("关联项目: ${it.cName.withOutEmpty}", 5)
                                taskExecutive.text = TextStrSplice("任务执行者: ${it.executor.withOutEmpty}", 6)
                                taskCcPerson.text = TextStrSplice("抄送人: ${it.watcher.withOutEmpty}", 4)
                                taskDetail.text = TextStrSplice("任务详情: ${it.info.withOutEmpty}", 5)
                            }
                            it.record?.let {
                                adapter.dataList.addAll(it)
                                adapter.notifyDataSetChanged()
                            }
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })

    }

    private fun doRequest2(data_id: Int){
        if (data_id == 0) {
            return
        }
        SoguApi.getService(application,CalendarService::class.java)
                .showScheduleDetail(data_id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.let {
                            val user = Store.store.getUser(this)
                            if (user?.uid != it.info?.pub_id) {
                                mMenu.getItem(0).isVisible = false
                                mMenu.getItem(0).isEnabled = false
                                delete.visibility = View.GONE
                            } else {
                                mMenu.getItem(0).isVisible = true
                                mMenu.getItem(0).isEnabled = true
                                delete.visibility = View.VISIBLE
                            }
                            it.info?.let {
                                taskNumber.text = TextStrSplice("日程编号: ${it.number.withOutEmpty}", 5)
                                arrangeTime.text = TextStrSplice("安排时间: ${it.timing.withOutEmpty}", 5)
                                taskPublisher.text = TextStrSplice("日程发布者: ${it.publisher.withOutEmpty}", 6)
                                related_project.text = TextStrSplice("关联项目: ${it.cName.withOutEmpty}", 5)
                                taskExecutive.text = TextStrSplice("日程执行者: ${it.executor.withOutEmpty}", 6)
                                taskCcPerson.visibility = View.GONE
                                taskDetail.text = TextStrSplice("日程详情: ${it.info.withOutEmpty}", 5)
                            }
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })
    }

    private fun TextStrSplice(text: String, start: Int): SpannableString {
        val spStr = SpannableString(text)
        spStr.setSpan(ForegroundColorSpan(Color.parseColor("#808080")), start, text.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        return spStr
    }

    override fun confirmListener(comment: String) {
        SoguApi.getService(application,CalendarService::class.java)
                .addComment(data_id, comment)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.let {
                            adapter.dataList.add(it)
                            adapter.notifyDataSetChanged()
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.delete -> {
                MaterialDialog.Builder(this)
                        .theme(Theme.LIGHT)
                        .title("删除")
                        .content("确认删除?")
                        .positiveText("确认")
                        .negativeText("取消")
                        .onPositive { dialog, which ->
                            SoguApi.getService(application,CalendarService::class.java)
                                    .deleteTask(data_id)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(Schedulers.io())
                                    .subscribe({ payload ->
                                        if (payload.isOk) {
                                            showCustomToast(R.drawable.icon_toast_success, "删除成功")
                                            finish()
                                        } else {
                                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                                        }
                                    }, { e ->
                                        Trace.e(e)
                                    })
                        }
                        .show()

            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.task_modify -> {
                ModifyTaskActivity.startForModify(this, data_id, intent.getStringExtra(Extras.NAME))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
