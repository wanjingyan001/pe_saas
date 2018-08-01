package com.sogukj.pe.module.calendar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.amap.api.mapcore.util.it
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.extraDelegate
import com.sogukj.pe.baselibrary.Extended.isNullOrEmpty
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.ChildBean
import com.sogukj.pe.bean.NewArrangeBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.bean.WeeklyArrangeBean
import kotlinx.android.synthetic.main.activity_arrange_detail.*
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource

class ArrangeDetailActivity : ToolbarActivity() {
    lateinit var attendAdapter: RecyclerAdapter<UserBean>
    lateinit var participateAdapter: RecyclerAdapter<UserBean>
    override val menuId: Int
        get() = R.menu.menu_arrange_modify
    lateinit var data: NewArrangeBean
    private val position :Int by extraDelegate(Extras.INDEX,-1)

    companion object {
        fun start(context: Context, data: NewArrangeBean,position:Int) {
            val intent = Intent(context, ArrangeDetailActivity::class.java)
            intent.putExtra(Extras.DATA, data)
            intent.putExtra(Extras.INDEX,position)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arrange_detail)
        title = "班子工作安排详情"
        setBack(true)
        data = intent.getParcelableExtra(Extras.DATA)
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

    private fun setContentData(data: NewArrangeBean) {
        weeklyTv.text = data.weekday
        dayOfYear.text = data.date
        causeContent.text = data.child[position].reasons
        data.child[position].attendee?.let {
            attendAdapter.dataList.clear()
            attendAdapter.dataList.addAll(it)
            attendAdapter.notifyDataSetChanged()
        }
        data.child[position].participant?.let {
            participateAdapter.dataList.clear()
            participateAdapter.dataList.addAll(it)
            participateAdapter.notifyDataSetChanged()
        }
        if (data.child[position].place.isNullOrEmpty()) {
            address_icon.isEnabled = false
            addressTv.text = ""
            addressTv.hint = "暂无地址信息"
        } else {
            address_icon.isEnabled = true
            addressTv.text = data.child[position].place
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && resultCode == Extras.RESULTCODE && data != null) {
            val list = data.getSerializableExtra(Extras.DATA) as ArrayList<NewArrangeBean>
            this.data = list[0]
            this.data.child[position].apply {
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
                ArrangeEditActivity.start(this, arrayListOf(data), null,position)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    inner class AttendHolder(convertView: View) : RecyclerHolder<UserBean>(convertView) {
        val name = convertView.find<TextView>(R.id.personName)
        override fun setData(view: View, data: UserBean, position: Int) {
            name.text = data.name
        }
    }

}
