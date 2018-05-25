package com.sogukj.pe.module.score

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.bean.EmployeeInteractBean
import kotlinx.android.synthetic.main.activity_red_black.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.textColor

class RedBlackActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Context?, data: ArrayList<EmployeeInteractBean>) {
            val intent = Intent(ctx, RedBlackActivity::class.java)
            intent.putExtra(Extras.DATA, data)
            ctx?.startActivity(intent)
        }
    }

    var type = Extras.RED
    lateinit var adapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_red_black)

        setBack(true)
        title = "排行榜"
        toolbar?.setBackgroundColor(Color.TRANSPARENT)
        toolbar?.apply {
            val title = this.findViewById<TextView>(R.id.toolbar_title)
            title?.textColor = Color.parseColor("#ffffff")
            val back = this.findViewById<ImageView>(R.id.toolbar_back) as ImageView
            back.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
            var toolbar_menu = this.findViewById<TextView>(R.id.toolbar_menu) as TextView
            toolbar_menu.text = "黑榜"
            toolbar_menu.setOnClickListener {
                if (type == Extras.RED) {
                    type = Extras.BLACK
                    initView()
                } else if (type == Extras.BLACK) {
                    type = Extras.RED
                    initView()
                }
            }
        }

        var data = intent.getSerializableExtra(Extras.DATA) as ArrayList<EmployeeInteractBean>
        adapter = MyAdapter(context, data)
        listview.adapter = adapter
        adapter.red_or_black = type
        adapter.notifyDataSetChanged()
        //有数据
        scroll.visibility = View.VISIBLE
        empty.visibility = View.GONE
    }

    fun initView() {
        if (type == Extras.RED) {
            toolbar_menu.text = "黑榜"
            //root.backgroundResource = R.drawable.red
            root.backgroundColor = Color.parseColor("#FFD3513C")
            icon_title.backgroundResource = R.drawable.hong
        } else if (type == Extras.BLACK) {
            toolbar_menu.text = "红榜"
            //root.backgroundResource = R.drawable.black
            root.backgroundColor = Color.parseColor("#FF0C162E")
            icon_title.backgroundResource = R.drawable.hei
        }
        adapter.red_or_black = type
        adapter.notifyDataSetChanged()
    }

    class MyAdapter(val context: Context, val data: ArrayList<EmployeeInteractBean>) : BaseAdapter() {

        var red_or_black = 0

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            var holder: InnerHolder? = null
            if (view == null) {
                holder = InnerHolder()
                var inflate = LayoutInflater.from(context)
                view = inflate.inflate(R.layout.red_black_item, null)
                holder.title = view.findViewById<TextView>(R.id.title) as TextView
                holder.chakan = view.findViewById<TextView>(R.id.chakan) as TextView
                var ll = view.findViewById<LinearLayout>(R.id.addContent) as LinearLayout


                var first = inflate.inflate(R.layout.red_black_inner, null) as LinearLayout
                holder.list.add(inflateOneItem(first))
                ll.addView(first, 1)
                var second = inflate.inflate(R.layout.red_black_inner, null) as LinearLayout
                holder.list.add(inflateOneItem(second))
                ll.addView(second, 2)
                var third = inflate.inflate(R.layout.red_black_inner, null) as LinearLayout
                holder.list.add(inflateOneItem(third))
                ll.addView(third, 3)


                view.setTag(holder)
            } else {
                holder = view.tag as InnerHolder
            }


            holder.title?.text = data[position].title
            var dataList = data[position].data
            try {
                if (red_or_black == Extras.RED) {
                    holder.chakan?.textColor = Color.parseColor("#FFEA9C93")
                    var index = 0
                    fillOneItem(holder.list[0], dataList?.get(index)!!, red_or_black, 0)
                    fillOneItem(holder.list[1], dataList.get(index + 1), red_or_black, 1)
                    fillOneItem(holder.list[2], dataList.get(index + 2), red_or_black, 2)
                } else if (red_or_black == Extras.BLACK) {
                    holder.chakan?.textColor = Color.parseColor("#FFA0A4AA")
                    var index = dataList!!.size - 3
                    fillOneItem(holder.list[0], dataList.get(index), red_or_black, 0)
                    fillOneItem(holder.list[1], dataList.get(index + 1), red_or_black, 1)
                    fillOneItem(holder.list[2], dataList.get(index + 2), red_or_black, 2)
                }
            } catch (e: Exception) {
                Log.e("数据没满三个", "数据没满三个")
            }


            holder.chakan?.setOnClickListener {
                JiXiaoActivity.start(context, Extras.RED_BLACK, data[position])
            }
            return view!!
        }

        override fun getItem(position: Int): Any {
            return data.get(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return data.size
        }

        fun fillOneItem(item: InnerHolder.Item, itemData: EmployeeInteractBean.EmployeeItem, type: Int, index: Int) {
            if (type == Extras.RED) {
                item.seq?.text = ""
                if (index == 0) {
                    item.seq?.backgroundResource = R.drawable.no1
                } else if (index == 1) {
                    item.seq?.backgroundResource = R.drawable.no2
                } else if (index == 2) {
                    item.seq?.backgroundResource = R.drawable.no3
                }
            } else if (type == Extras.BLACK) {
                item.seq?.backgroundColor = Color.WHITE
                item.seq?.text = "${itemData.sort}"
            }
            //Glide.with(context).load(itemData.url).into(item.head_icon)
            item.name?.text = itemData.name
            item.depart?.text = itemData.department
            item.score?.text = itemData.grade_case
        }

        fun inflateOneItem(view: LinearLayout): InnerHolder.Item {
            var item = InnerHolder.Item()
            item.seq = view.findViewById<TextView>(R.id.seq) as TextView
            //item.head_icon = view.findViewById(R.id.head_icon) as CircleImageView
            item.name = view.findViewById<TextView>(R.id.name) as TextView
            item.depart = view.findViewById<TextView>(R.id.depart) as TextView
            item.score = view.findViewById<TextView>(R.id.score) as TextView
            return item
        }

        class InnerHolder {
            var title: TextView? = null
            var chakan: TextView? = null
            var addContent: LinearLayout? = null
            var list = ArrayList<Item>()

            class Item {
                var seq: TextView? = null
                //var head_icon: CircleImageView? = null
                var name: TextView? = null
                var depart: TextView? = null
                var score: TextView? = null
            }
        }
    }
}
