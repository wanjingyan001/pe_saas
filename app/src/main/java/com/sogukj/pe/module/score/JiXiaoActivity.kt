package com.sogukj.pe.module.score

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.bean.EmployeeInteractBean
import com.sogukj.pe.bean.EmployeeInteractBean.EmployeeItem
import kotlinx.android.synthetic.main.activity_ji_xiao.*
import kotlinx.android.synthetic.main.item_empty.*
import org.jetbrains.anko.textColor

class JiXiaoActivity : ToolbarActivity() {

    companion object {
        // JIXIAO    RED_BLACK
        fun start(ctx: Context?, type: Int, data: EmployeeInteractBean? = null) {
            val intent = Intent(ctx, JiXiaoActivity::class.java)
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.DATA, data)
            ctx?.startActivity(intent)
        }
    }

    var type = 0
    lateinit var data: EmployeeInteractBean

    lateinit var adapter: RecyclerAdapter<EmployeeItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ji_xiao)

        type = intent.getIntExtra(Extras.TYPE, 0)
        var tmp = intent.getSerializableExtra(Extras.DATA)
        if (tmp != null) {
            data = tmp as EmployeeInteractBean
        }

        setBack(true)
        if (type == Extras.JIXIAO) {
            title = "关键绩效考核结果"
        } else if (type == Extras.RED_BLACK) {
            title = data.title
        }
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar?.apply {
            val title = this.findViewById<TextView>(R.id.toolbar_title)
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById<ImageView>(R.id.toolbar_back) as ImageView
            back.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
        }


        adapter = RecyclerAdapter(context, { _adapter, parent, type0 ->
            val convertView = _adapter.getView(R.layout.item_child, parent) as LinearLayout
            object : RecyclerHolder<EmployeeItem>(convertView) {

                var seq = convertView.findViewById<TextView>(R.id.seq) as TextView
                var depart = convertView.findViewById<TextView>(R.id.depart) as TextView
                var name = convertView.findViewById<TextView>(R.id.name) as TextView
                var score = convertView.findViewById<TextView>(R.id.score) as TextView

                override fun setData(view: View, data: EmployeeItem, position: Int) {
                    seq.text = "${data.sort}"
                    depart.text = data.department
                    name.text = data.name
                    score.text = data.grade_case
                }
            }
        })
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        jixiao_list.layoutManager = layoutManager
        jixiao_list.addItemDecoration(SpaceItemDecoration(10))
        jixiao_list.adapter = adapter

        adapter.dataList.addAll(data.data!!)
        adapter.notifyDataSetChanged()

        adapter.dataList = data.data!!
        adapter.notifyDataSetChanged()
        if (adapter.dataList.size == 0) {
            //暂无数据
            jixiao_list.visibility = View.GONE
            empty.visibility = View.VISIBLE
            tv_empty.visibility = View.GONE
        }
    }
}
