package com.sogukj.pe.module.calendar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.isNullOrEmpty
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.WeeklyArrangeBean
import kotlinx.android.synthetic.main.activity_arrange_detail.*
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource

class ArrangeDetailActivity : ToolbarActivity() {
    lateinit var attendAdapter: RecyclerAdapter<WeeklyArrangeBean.Person>
    lateinit var participateAdapter: RecyclerAdapter<WeeklyArrangeBean.Person>
    override val menuId: Int
        get() = R.menu.menu_arrange_modify
    lateinit var data: WeeklyArrangeBean

    companion object {
        fun start(context: Context, data: WeeklyArrangeBean) {
            val intent = Intent(context, ArrangeDetailActivity::class.java)
            intent.putExtra(Extras.DATA, data)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arrange_detail)
        title = "班子工作安排详情"
        setBack(true)
        data = intent.getSerializableExtra(Extras.DATA) as WeeklyArrangeBean
        when (data.weekday) {
            "周一" -> {
                detailIcon.imageResource = R.drawable.icon_monday
            }
            "周二" -> {
                detailIcon.imageResource = R.drawable.icon_tuesday
            }
            "周三" -> {
                detailIcon.imageResource = R.drawable.icon_wednesday
            }
            "周四" -> {
                detailIcon.imageResource = R.drawable.icon_thursday
            }
            "周五" -> {
                detailIcon.imageResource = R.drawable.icon_friday
            }
            "周六" -> {
                detailIcon.imageResource = R.drawable.icon_saturday
            }
            "周日" -> {
                detailIcon.imageResource = R.drawable.icon_sunday
            }
        }


        attendAdapter = RecyclerAdapter(this, { _adapter, parent, type ->
            AttendHolder(_adapter.getView(R.layout.item_arrange_detail_person, parent))
        })
        participateAdapter = RecyclerAdapter(this, { _adapter, parent, type ->
            AttendHolder(_adapter.getView(R.layout.item_arrange_detail_person, parent))
        })
        attendList.layoutManager = GridLayoutManager(this, 4)
        participateList.layoutManager = GridLayoutManager(this, 4)
        attendList.adapter = attendAdapter
        participateList.adapter = participateAdapter
        setContentData(data)

    }

    private fun setContentData(data: WeeklyArrangeBean) {
        weeklyTv.text = data.weekday
        dayOfYear.text = data.date
        causeContent.text = data.reasons
        data.attendee?.let {
            attendAdapter.dataList.clear()
            attendAdapter.dataList.addAll(it)
            attendAdapter.notifyDataSetChanged()
        }
        data.participant?.let {
            participateAdapter.dataList.clear()
            participateAdapter.dataList.addAll(it)
            participateAdapter.notifyDataSetChanged()
        }
        if (data.place.isNullOrEmpty()) {
            address_icon.isEnabled = false
            addressTv.text = ""
            addressTv.hint = "暂无地址信息"
        } else {
            address_icon.isEnabled = true
            addressTv.text = data.place
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && resultCode == Extras.RESULTCODE && data != null) {
            val list = data.getSerializableExtra(Extras.DATA) as ArrayList<WeeklyArrangeBean>
            this.data = list[0]
            this.data.apply {
                if (attendee.isNullOrEmpty() && participant.isNullOrEmpty() && reasons.isNullOrEmpty() && place.isNullOrEmpty()){
                    finish()
                    return
                }
            }
            setContentData(this.data)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.modify -> {
                ArrangeEditActivity.start(this, arrayListOf(data), null)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    inner class AttendHolder(convertView: View) : RecyclerHolder<WeeklyArrangeBean.Person>(convertView) {
        val name = convertView.find<TextView>(R.id.personName)
        override fun setData(view: View, data: WeeklyArrangeBean.Person, position: Int) {
            name.text = data.name
        }
    }

}
