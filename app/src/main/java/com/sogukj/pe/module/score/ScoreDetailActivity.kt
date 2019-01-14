package com.sogukj.pe.module.score

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.MyListView
import com.sogukj.pe.bean.EmployeeInteractBean
import com.sogukj.pe.bean.ScoreBean
import com.sogukj.pe.service.ScoreService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_score_detail.*
import kotlinx.android.synthetic.main.item_empty.*
import org.jetbrains.anko.textColor

/**
 * 员工互评考核结果
 */
class ScoreDetailActivity : ToolbarActivity() {

    companion object {
        //type员工互评结果
        // TYPE_INTERACT---第三个参数无用
        // TYPE_LISTITEM---第三个参数有用
        fun start(ctx: Activity?, type: Int, bean: ScoreBean? = null) {
            val intent = Intent(ctx, ScoreDetailActivity::class.java)
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.DATA, bean)
            ctx?.startActivity(intent)
        }
    }

    //设置组视图的显示文字
    val group = ArrayList<String>()
    val average = ArrayList<String>()

    //子视图显示文字
    val childs = ArrayList<ArrayList<EmployeeInteractBean.EmployeeItem>>()

    var type = 0
    var bean: ScoreBean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score_detail)

        type = intent.getIntExtra(Extras.TYPE, 0)
        var data = intent.getSerializableExtra(Extras.DATA)
        if (data == null) {
            bean = null
        } else {
            bean = data as ScoreBean
        }

        setBack(true)
        if (type == Extras.TYPE_INTERACT) {
            title = "员工互评考核结果"
        } else if (type == Extras.TYPE_LISTITEM) {
            title = "打分详情页"
        }
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        toolbar?.apply {
            val title = this.findViewById<TextView>(R.id.toolbar_title)
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById<ImageView>(R.id.toolbar_back) as ImageView
            back.visibility = View.VISIBLE
            back.setImageResource(R.drawable.icon_back_gray)
        }

        score_list.setGroupIndicator(null)
        score_list.setOnChildClickListener { _, _, _, _, _ -> false }
        //员工互评考核结果不用传，领导打分详情页要传
        SoguApi.getService(application,ScoreService::class.java)
                .grade_info(if (bean == null) null else bean!!.user_id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.forEach {
                            group.add(it.title!!)
                            if (bean != null) {//员工互评考核=null，不需要平均分
                                average.add(it.pev_grade!!)
                            }
                            childs.add(it.data!!)
                        }
                        var adapter = MyExpAdapter(context, group, average, childs, type)
                        score_list.setAdapter(adapter)
                        if (payload.payload == null || group.size == 0) {
                            //暂无数据
                            score_list.visibility = View.GONE
                            empty.visibility = View.VISIBLE
                            tv_empty.visibility = View.GONE
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                })
    }

    class MyExpAdapter(val context: Context, val group: ArrayList<String>, val average: ArrayList<String>,
                       val childs: ArrayList<ArrayList<EmployeeInteractBean.EmployeeItem>>, val type: Int) : BaseExpandableListAdapter() {

        override fun getGroup(groupPosition: Int): Any {
            return group[groupPosition]
        }

        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
            return true
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            var holder: GroupHolder? = null
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.item_group, null)
                holder = GroupHolder()
                holder.title = view.findViewById<TextView>(R.id.title) as TextView
                holder.average = view.findViewById<TextView>(R.id.average) as TextView
                holder.direction = view.findViewById<ImageView>(R.id.direction) as ImageView
                view.tag = holder
            } else {
                holder = view.tag as GroupHolder
            }

            if (type == Extras.TYPE_INTERACT) {
                holder.average?.visibility = View.GONE
            } else if (type == Extras.TYPE_LISTITEM) {
                holder.average?.visibility = View.VISIBLE
                holder.average?.text = average[groupPosition]
            }

            holder.title?.text = group[groupPosition]
            if (isExpanded) {
                holder.direction?.setBackgroundResource(R.drawable.up)
            } else {
                holder.direction?.setBackgroundResource(R.drawable.down)
            }

            return view!!
        }

        class GroupHolder {
            var title: TextView? = null
            var average: TextView? = null
            var direction: ImageView? = null
        }

        override fun getChildrenCount(groupPosition: Int): Int {
            //return childs[groupPosition].size
            return 1
        }

        override fun getChild(groupPosition: Int, childPosition: Int): Any {
            return childs[groupPosition][childPosition]
        }

        override fun getGroupId(groupPosition: Int): Long {
            return groupPosition.toLong()
        }

        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            var holder: FatherHolder? = null
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.item_child, null)
                holder = FatherHolder()
                holder.seq = view.findViewById<TextView>(R.id.seq) as TextView
                holder.depart = view.findViewById<TextView>(R.id.depart) as TextView
                holder.name = view.findViewById<TextView>(R.id.name) as TextView
                holder.score = view.findViewById<TextView>(R.id.score) as TextView
                holder.list = view.findViewById<MyListView>(R.id.listview) as MyListView
                view.setTag(holder)
            } else {
                holder = view.tag as FatherHolder
            }

            holder.seq?.setBackgroundColor(Color.TRANSPARENT)
            holder.depart?.setBackgroundColor(Color.TRANSPARENT)
            holder.name?.setBackgroundColor(Color.TRANSPARENT)
            holder.score?.setBackgroundColor(Color.TRANSPARENT)
            holder.list?.adapter = MyListAdapter(context, childs.get(groupPosition), type)

            if (type == Extras.TYPE_INTERACT) {
                holder.seq?.visibility = View.VISIBLE
                holder.depart?.visibility = View.VISIBLE
            } else if (type == Extras.TYPE_LISTITEM) {
                holder.seq?.visibility = View.GONE
                holder.depart?.visibility = View.GONE
            }

            return view!!
        }

        class FatherHolder {
            var seq: TextView? = null
            var depart: TextView? = null
            var name: TextView? = null
            var score: TextView? = null
            var list: MyListView? = null
        }

        override fun getChildId(groupPosition: Int, childPosition: Int): Long {
            return childPosition.toLong()
        }

        override fun getGroupCount(): Int {
            return group.size
        }

        class MyListAdapter(val context: Context, val datalist: ArrayList<EmployeeInteractBean.EmployeeItem>, val type: Int) : BaseAdapter() {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                var view = convertView
                var holder: ChildHolder? = null
                if (view == null) {
                    view = LayoutInflater.from(context).inflate(R.layout.item_child, null)
                    holder = ChildHolder()
                    holder.seq = view.findViewById<TextView>(R.id.seq) as TextView
                    holder.depart = view.findViewById<TextView>(R.id.depart) as TextView
                    holder.name = view.findViewById<TextView>(R.id.name) as TextView
                    holder.score = view.findViewById<TextView>(R.id.score) as TextView
                    view.setTag(holder)
                } else {
                    holder = view.tag as ChildHolder
                }

                holder.seq?.text = "${datalist[position].sort}"
                holder.depart?.text = datalist[position].department
                holder.name?.text = datalist[position].name
                holder.score?.text = datalist[position].grade_case

                holder.seq?.setBackgroundColor(Color.WHITE)
                holder.depart?.setBackgroundColor(Color.WHITE)
                holder.name?.setBackgroundColor(Color.WHITE)
                holder.score?.setBackgroundColor(Color.WHITE)

                if (type == Extras.TYPE_INTERACT) {
                    holder.seq?.visibility = View.VISIBLE
                    holder.depart?.visibility = View.VISIBLE
                } else if (type == Extras.TYPE_LISTITEM) {
                    holder.seq?.visibility = View.GONE
                    holder.depart?.visibility = View.GONE
                }

                return view!!
            }

            override fun getItem(position: Int): Any {
                return datalist.get(position)
            }

            override fun getItemId(position: Int): Long {
                return position.toLong()
            }

            override fun getCount(): Int {
                return datalist.size
            }

            class ChildHolder {
                var seq: TextView? = null
                var depart: TextView? = null
                var name: TextView? = null
                var score: TextView? = null
            }
        }
    }
}
