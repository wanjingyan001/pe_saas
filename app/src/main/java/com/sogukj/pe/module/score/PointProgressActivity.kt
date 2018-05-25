package com.sogukj.pe.module.score

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.bean.ProgressBean
import com.sogukj.pe.service.ScoreService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_point_progress.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.textColor


class PointProgressActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Context?) {
            val intent = Intent(ctx, PointProgressActivity::class.java)
            ctx?.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_point_progress)

        setBack(true)
        title = "全员打分进度"
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar?.apply {
            val title = this.findViewById<TextView>(R.id.toolbar_title)
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById<ImageView>(R.id.toolbar_back) as ImageView
            back.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
        }

        list_tabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    currentIndex = 0
                    loadData()
                } else if (tab.position == 1) {
                    currentIndex = 1
                    loadData()
                }
            }
        })

        adapter = RecyclerAdapter<ProgressBean.ProgressItem>(context, { _adapter, parent, type0 ->
            val convertView = _adapter.getView(R.layout.item_judge, parent) as LinearLayout
            object : RecyclerHolder<ProgressBean.ProgressItem>(convertView) {

                val tvTag1 = convertView.findViewById<TextView>(R.id.tag1) as TextView
                val tvTag2 = convertView.findViewById<TextView>(R.id.tag2) as TextView
                val tvTag3 = convertView.findViewById<TextView>(R.id.tag3) as TextView
                val tvTag4 = convertView.findViewById<TextView>(R.id.tag4) as TextView
                val tvTag5 = convertView.findViewById<TextView>(R.id.tag5) as TextView

                override fun setData(view: View, data: ProgressBean.ProgressItem, position: Int) {
                    tvTag1.visibility = View.VISIBLE
                    tvTag2.visibility = View.VISIBLE
                    tvTag3.visibility = View.VISIBLE
                    tvTag4.visibility = View.VISIBLE
                    tvTag5.visibility = View.VISIBLE

                    tvTag1.text = data.name
                    fill(tvTag2, data.wri!!)
                    fill(tvTag3, data.gws!!)
                    fill(tvTag4, data.jxs!!)
                    fill(tvTag5, data.les!!)

//                    var name: String? = null//	姓名
//                    var wri: Int? = null// 个人是否输入填写项    0=>未完成，1=>已完成，2=>延时完成
//                    var gws: Int? = null//是否为别人打岗位分    同上
//                    var jxs: Int? = null//上级是否为我打绩效分    同上
//                    var status: Int? = null//1--已完成，2-未完成
                }

                fun fill(view: TextView, dataIndex: Int) {
                    if (dataIndex == 0) {
                        view.text = "未完成"
                        view.textColor = Color.RED
                    } else if (dataIndex == 1) {
                        view.text = "已完成"
                        view.textColor = Color.parseColor("#FF282828")
                    } else if (dataIndex == 2) {
                        view.text = "延时完成"
                        view.textColor = Color.RED
                    } else if (dataIndex == -1) {
                        view.text = "无"
                        view.textColor = Color.parseColor("#FF282828")
                    }
                }
            }
        })

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        list.layoutManager = layoutManager
        list.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 10)))
        list.adapter = adapter

        toolbar_menu.setOnClickListener {
            var content1 = SpannableString("本页中的三个状态含义如下：\n" +
                    "工作结果：指员工自己是否填写了绩效考核中的工作结果。\n" +
                    "岗位互评：指员工自己是否为全公司人员评分完毕。\n" +
                    "直线上级评分：此处上级包含直线上级与班子两部分，只有两部分都完成打分才会显示完成打分。")
            content1.setSpan(ForegroundColorSpan(Color.parseColor("#ff282828")), 14, 19, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            content1.setSpan(ForegroundColorSpan(Color.parseColor("#ff282828")), 41, 46, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            content1.setSpan(ForegroundColorSpan(Color.parseColor("#ff282828")), 65, 72, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            var dialog = MaterialDialog.Builder(this)
                    .theme(Theme.LIGHT)
                    .customView(R.layout.help, false)
                    .show()

            val customeView = dialog.customView as LinearLayout
            customeView.backgroundResource = R.drawable.bg_help
            var content = customeView.findViewById<TextView>(R.id.content) as TextView
            content.text = content1
            var button = customeView.findViewById<TextView>(R.id.btn_OK) as TextView
            button.setOnClickListener { dialog.dismiss() }
        }
    }

    lateinit var adapter: RecyclerAdapter<ProgressBean.ProgressItem>
    var currentIndex = 0
    var _unfinish = ArrayList<ProgressBean.ProgressItem>()
    var _finish = ArrayList<ProgressBean.ProgressItem>()

    fun loadData() {
        if (currentIndex == 0) {
            adapter.dataList.clear()
            adapter.dataList.addAll(_unfinish)
            adapter.notifyDataSetChanged()
        } else if (currentIndex == 1) {
            adapter.dataList.clear()
            adapter.dataList.addAll(_finish)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
        SoguApi.getService(application,ScoreService::class.java)
                .GradeProgress()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            finish?.let {
                                _finish = it
                            }
                            unfinish?.let {
                                _unfinish = it
                            }
                            loadData()
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                })
    }
}
