package com.sogukj.pe.module.calendar

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import kotlinx.android.synthetic.main.activity_remind_time.*
import org.jetbrains.anko.find

class RemindTimeActivity : BaseActivity() {
    lateinit var adapter: RecyclerAdapter<String>


    companion object {
        fun start(context: Activity, seconds: String?) {
            val intent = Intent(context, RemindTimeActivity::class.java)
            intent.putExtra(Extras.DATA, seconds)
            context.startActivityForResult(intent, Extras.REQUESTCODE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remind_time)
        Utils.setWindowStatusBarColor(this, R.color.white)
        val seconds = intent.getStringExtra(Extras.DATA)
        adapter = RecyclerAdapter(this, { _adapter, parent, type ->
            RemindHolder(_adapter.getView(R.layout.item_remind_time, parent))
        })

        if (seconds == null || seconds == "null") {
            noRemindLayout.isSelected = true
            noRemindIcon.visibility = View.VISIBLE
        } else {
            noRemindIcon.visibility = View.INVISIBLE
            when (seconds.toInt().div(60)) {
                0 -> {
                    noRemindLayout.isSelected = true
                    noRemindIcon.visibility = View.VISIBLE
                    adapter.selectedPosition = -1
                }
                5 -> {
                    adapter.selectedPosition = 0
                }
                15 -> {
                    adapter.selectedPosition = 1
                }
                30 -> {
                    adapter.selectedPosition = 2
                }
                60 -> {
                    adapter.selectedPosition = 3
                }
                else -> {
                    adapter.selectedPosition = 4
                }
            }
        }

        val array = resources.getStringArray(R.array.remind_times)
        array.forEach {
            adapter.dataList.add(it)
        }
        remindList.layoutManager = LinearLayoutManager(this)
        remindList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        remindList.adapter = adapter

        noRemindLayout.setOnClickListener {
            noRemindLayout.isSelected = !noRemindLayout.isSelected
            if (noRemindLayout.isSelected) {
                noRemindIcon.visibility = View.VISIBLE
            } else {
                noRemindIcon.visibility = View.INVISIBLE
            }
            adapter.selectedPosition = -1
            adapter.notifyDataSetChanged()
        }
        back.setOnClickListener {
            onBackPressed()
        }
    }


    override fun onBackPressed() {
        val intent = Intent()
        val str = when (adapter.selectedPosition) {
            -1 -> "不提醒"
            else -> adapter.dataList[adapter.selectedPosition]
        }
        intent.putExtra(Extras.DATA, str)
        setResult(Extras.RESULTCODE, intent)
        finish()
    }


    inner class RemindHolder(convertView: View) : RecyclerHolder<String>(convertView) {
        val remindTv = convertView.find<TextView>(R.id.remindTv)
        val remindIcon = convertView.find<ImageView>(R.id.remindIcon)
        override fun setData(view: View, data: String, position: Int) {
            if (adapter.selectedPosition == position) {
                view.isSelected = true
                remindIcon.visibility = View.VISIBLE
            } else {
                view.isSelected = false
                remindIcon.visibility = View.INVISIBLE
            }
            remindTv.text = data
            view.setOnClickListener {
                if (noRemindLayout.isSelected) {
                    noRemindLayout.isSelected = false
                    noRemindIcon.visibility = View.INVISIBLE
                }
                view.isSelected = !view.isSelected
                if (view.isSelected) {
                    adapter.selectedPosition = position
                    remindIcon.visibility = View.VISIBLE
                } else {
                    adapter.selectedPosition = -1
                    remindIcon.visibility = View.INVISIBLE
                }
                adapter.notifyDataSetChanged()
            }
        }
    }
}
