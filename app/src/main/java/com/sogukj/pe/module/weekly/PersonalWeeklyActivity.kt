package com.sogukj.pe.module.weekly

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.gson.JsonSyntaxException
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.StatusBarUtil
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.bean.WeeklyWatchBean
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.WeeklyService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_personal_weekly.*
import org.jetbrains.anko.textColor
import java.net.UnknownHostException
import kotlin.properties.Delegates

class PersonalWeeklyActivity : ToolbarActivity() {

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
                title = bean.name
//                if (bean.url.isNullOrEmpty()) {
//                    val ch = bean.name?.first()
//                    icon.setChar(ch)
//                } else {
//                    Glide.with(context).load(MyGlideUrl(bean.url)).into(icon)
//                }
            }
            "My" -> {
                week_id = intent.getIntExtra(Extras.ID, -1)
                startTime = intent.getStringExtra(Extras.TIME1)
                endTime = intent.getStringExtra(Extras.TIME2)
                val user = Store.store.getUser(context)
                title = user?.name
//                if (user?.headImage().isNullOrEmpty()) {
//                    val ch = user?.name?.first()
//                    icon.setChar(ch)
//                } else {
//                    Glide.with(context).load(MyGlideUrl(user?.headImage())).into(icon)
//                }
            }
            "Push" -> {
                week_id = intent.getIntExtra(Extras.ID, -1)
                user_id = intent.getIntExtra(Extras.TYPE1, -1)
                title = intent.getStringExtra(Extras.TYPE2)
            }
        }

        doRequest(user_id, null, startTime, endTime, week_id)

        toolbar_title.textColor = Color.parseColor("#282828")
        toolbar_menu.visibility = View.VISIBLE
        state = "FULL"
        toolbar_menu.setImageResource(R.drawable.full1)

        toolbar_menu.setOnClickListener {
            if (state == "FULL") {
                state = "ONLY"
                toolbar_menu.setImageResource(R.drawable.only)
                fragment.hide()
            } else {
                state = "FULL"
                toolbar_menu.setImageResource(R.drawable.full1)
                fragment.show()
            }
        }

        StatusBarUtil.setTranslucentForImageView(this, 0, toolbar)
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar_back.setImageResource(R.drawable.back_chevron)
        setBack(true)
    }

    var state = "FULL"
    lateinit var fragment: WeeklyThisFragment

    fun doRequest(user_id: Int?, issue: Int?, start_time: String?, end_time: String?, week_id: Int) {
        SoguApi.getService(application, WeeklyService::class.java)
                .getWeekly(user_id, issue, start_time, end_time, week_id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {

                            //                            fragments = arrayOf(
//                                    WeeklyThisFragment.newInstance("PERSONAL", this, intent.getStringExtra(Extras.TIME1), intent.getStringExtra(Extras.TIME2), week_id),
//                                    RecordBuChongFragment.newInstance(this.week)
//                            )

                            fragment = WeeklyThisFragment.newInstance("PERSONAL", this, intent.getStringExtra(Extras.TIME1), intent.getStringExtra(Extras.TIME2), week_id)

                            manager = supportFragmentManager
                            manager.beginTransaction().add(R.id.container, fragment).commit()
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                })

    }
}
