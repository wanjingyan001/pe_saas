package com.sogukj.pe.module.weekly

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.widget.TextView
import com.bumptech.glide.Glide
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.bean.WeeklyWatchBean
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.WeeklyService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_personal_weekly.*
import org.jetbrains.anko.textColor

class PersonalWeeklyActivity : BaseActivity() {

    lateinit var fragments: Array<Fragment>

    lateinit var manager: FragmentManager
    var user_id: Int? = null
    var startTime: String? = null
    var endTime: String? = null
    var week_id: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_weekly)
        val nameStr = intent.getStringExtra(Extras.NAME)
        when (nameStr) {
            "Other" -> {
                val bean = intent.getSerializableExtra(Extras.DATA) as WeeklyWatchBean.BeanObj
                user_id = bean.user_id
                startTime = intent.getStringExtra(Extras.TIME1)
                endTime = intent.getStringExtra(Extras.TIME2)
                bean.week_id?.let {
                    week_id = it
                }
                name.text = bean.name
                if (bean.url.isNullOrEmpty()) {
                    val ch = bean.name?.first()
                    icon.setChar(ch)
                } else {
                    Glide.with(context).load(MyGlideUrl(bean.url)).into(icon)
                }
            }
            "My" -> {
                week_id = intent.getIntExtra(Extras.ID, -1)
                startTime = intent.getStringExtra(Extras.TIME1)
                endTime = intent.getStringExtra(Extras.TIME2)
                val user = Store.store.getUser(context)
                user?.let {
                    name.text = user.name
                    if (user.headImage().isNullOrEmpty()) {
                        val ch = user.name.first()
                        icon.setChar(ch)
                    } else {
                        Glide.with(context).load(MyGlideUrl(user.headImage())).into(icon)
                    }
                }
            }
            "Push" -> {
                week_id = intent.getIntExtra(Extras.ID, -1)
                user_id = intent.getIntExtra(Extras.TYPE1, -1)
                name.text = intent.getStringExtra(Extras.TYPE2)
            }
        }



        back.setOnClickListener {
            finish()
        }

        clicked(weekly, true)
        clicked(record_buchong, false)

        weekly.setOnClickListener {
            replace(0)
        }

        record_buchong.setOnClickListener {
            replace(1)
        }
        doRequest(user_id, null, startTime, endTime, week_id)
    }

    fun doRequest(user_id: Int?, issue: Int?, start_time: String?, end_time: String?, week_id: Int) {
        SoguApi.getService(application,WeeklyService::class.java)
                .getWeekly(user_id, issue, start_time, end_time, week_id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {

                            fragments = arrayOf(
                                    WeeklyThisFragment.newInstance("PERSONAL", this, intent.getStringExtra(Extras.TIME1), intent.getStringExtra(Extras.TIME2), week_id),
                                    RecordBuChongFragment.newInstance(this.week)
                            )

                            manager = supportFragmentManager
                            manager.beginTransaction().add(R.id.container, fragments[0]).commit()
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                })

    }

    fun switchContent(from: Int, to: Int) {
        if (!fragments[to].isAdded) { // 先判断是否被add过
            manager.beginTransaction().hide(fragments[from])
                    .add(R.id.container, fragments[to]).commit() // 隐藏当前的fragment，add下一个到Activity中
        } else {
            manager.beginTransaction().hide(fragments[from]).show(fragments[to]).commit() // 隐藏当前的fragment，显示下一个
        }
    }

    var current = 0

    fun replace(checkedId: Int) {
        if (checkedId == current) {
            return
        }
        switchContent(current, checkedId)
        current = checkedId
        if (checkedId == 0) {
            clicked(weekly, true)
            clicked(record_buchong, false)
        } else if (checkedId == 1) {
            clicked(weekly, false)
            clicked(record_buchong, true)
        }
    }

    fun clicked(view: TextView, flag: Boolean) {
        if (flag) {
            view.textColor = Color.parseColor("#FF282828")
            view.setBackgroundResource(R.drawable.weekly_selected)
        } else {
            view.textColor = Color.parseColor("#FFc7c7c7")
            view.setBackgroundResource(R.drawable.weekly_unselected)
        }
    }
}
