package com.sogukj.pe.module.score

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.bean.GradeCheckBean
import com.sogukj.pe.service.ScoreService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_gang_wei_list.*
import org.jetbrains.anko.textColor

class GangWeiListActivity : ToolbarActivity() {

    companion object {
        //type: Int  员工或者领导
        fun start(ctx: Context?, type: Int) {
            val intent = Intent(ctx, GangWeiListActivity::class.java)
            intent.putExtra(Extras.TYPE, type)
            ctx?.startActivity(intent)
        }
    }

    lateinit var adapter: RecyclerAdapter<GradeCheckBean.ScoreItem>

    var type = 0

    var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gang_wei_list)

        type = intent.getIntExtra(Extras.TYPE, 0)

        setBack(true)
        title = "岗位胜任力评价"
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        toolbar?.apply {
            val title = this.findViewById<TextView>(R.id.toolbar_title)
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById<ImageView>(R.id.toolbar_back) as ImageView
            back.visibility = View.VISIBLE
            back.setImageResource(R.drawable.icon_back_gray)
        }

        if (currentIndex == 0 && type == Extras.TYPE_EMPLOYEE) {
            tag_1.visibility = View.VISIBLE
            tag_2.visibility = View.VISIBLE
            tag_3.visibility = View.GONE
            tag_4.visibility = View.GONE
        } else if (currentIndex == 1 && type == Extras.TYPE_EMPLOYEE) {
            tag_1.visibility = View.VISIBLE
            tag_2.visibility = View.VISIBLE
            tag_3.visibility = View.VISIBLE
            tag_4.visibility = View.GONE
        } else if (currentIndex == 0 && type == Extras.TYPE_MANAGE) {
            tag_1.visibility = View.VISIBLE
            tag_2.visibility = View.VISIBLE
            tag_3.visibility = View.VISIBLE
            tag_4.visibility = View.GONE
        } else if (currentIndex == 1 && type == Extras.TYPE_MANAGE) {
            tag_1.visibility = View.VISIBLE
            tag_2.visibility = View.VISIBLE
            tag_3.visibility = View.GONE
            tag_4.visibility = View.VISIBLE
        }

        list_tabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0 && type == Extras.TYPE_EMPLOYEE) {
                    tag_1.visibility = View.VISIBLE
                    tag_2.visibility = View.VISIBLE
                    tag_3.visibility = View.GONE
                    tag_4.visibility = View.GONE
                } else if (tab.position == 1 && type == Extras.TYPE_EMPLOYEE) {
                    tag_1.visibility = View.VISIBLE
                    tag_2.visibility = View.VISIBLE
                    tag_3.visibility = View.VISIBLE
                    tag_4.visibility = View.GONE
                } else if (tab.position == 0 && type == Extras.TYPE_MANAGE) {
                    tag_1.visibility = View.VISIBLE
                    tag_2.visibility = View.VISIBLE
                    tag_3.visibility = View.VISIBLE
                    tag_4.visibility = View.GONE
                } else if (tab.position == 1 && type == Extras.TYPE_MANAGE) {
                    tag_1.visibility = View.VISIBLE
                    tag_2.visibility = View.VISIBLE
                    tag_3.visibility = View.GONE
                    tag_4.visibility = View.VISIBLE
                }
                if (tab.position == 0) {
                    currentIndex = 0
                    loadData()
                } else if (tab.position == 1) {
                    currentIndex = 1
                    loadData()
                }
            }
        })

        adapter = RecyclerAdapter<GradeCheckBean.ScoreItem>(context, { _adapter, parent, type0 ->
            val convertView = _adapter.getView(R.layout.item_judge, parent) as LinearLayout
            object : RecyclerHolder<GradeCheckBean.ScoreItem>(convertView) {

                val tvTag1 = convertView.findViewById<TextView>(R.id.tag1) as TextView
                val tvTag2 = convertView.findViewById<TextView>(R.id.tag2) as TextView
                val tvTag3 = convertView.findViewById<TextView>(R.id.tag3) as TextView
                val tvTag4 = convertView.findViewById<TextView>(R.id.tag4) as TextView

                override fun setData(view: View, data: GradeCheckBean.ScoreItem, position: Int) {
                    if (type == Extras.TYPE_MANAGE && currentIndex == 1) {
                        tvTag3.textColor = Color.parseColor("#FFA1CEA9")
                    } else if (type == Extras.TYPE_EMPLOYEE && currentIndex == 0) {
                        tvTag3.textColor = Color.parseColor("#FFCEA1A1")
                    }
                    if (currentIndex == 0 && type == Extras.TYPE_EMPLOYEE) {
                        tvTag1.visibility = View.VISIBLE
                        tvTag2.visibility = View.VISIBLE
                        tvTag3.visibility = View.GONE
                        tvTag4.visibility = View.GONE
                    } else if (currentIndex == 1 && type == Extras.TYPE_EMPLOYEE) {
                        tvTag1.visibility = View.VISIBLE
                        tvTag2.visibility = View.VISIBLE
                        tvTag3.visibility = View.GONE
                        tvTag4.visibility = View.VISIBLE
                    } else if (currentIndex == 0 && type == Extras.TYPE_MANAGE) {
                        tvTag1.visibility = View.VISIBLE
                        tvTag2.visibility = View.VISIBLE
                        tvTag3.visibility = View.VISIBLE
                        tvTag4.visibility = View.GONE
                    } else if (currentIndex == 1 && type == Extras.TYPE_MANAGE) {
                        tvTag1.visibility = View.VISIBLE
                        tvTag2.visibility = View.VISIBLE
                        tvTag3.visibility = View.GONE
                        tvTag4.visibility = View.VISIBLE
                    }
                    tvTag1.text = data.name
                    tvTag2.text = data.department
                    tvTag3.text = data.position
                    tvTag4.text = data.grade_date
                }
            }
        })
        adapter.onItemClick = { v, p ->
            //领导员工进同一个界面
            if (currentIndex == 0) {
                GangWeiShengRenLiActivity.start(context, adapter.dataList.get(p), false)
            } else if (currentIndex == 1) {
                GangWeiShengRenLiActivity.start(context, adapter.dataList.get(p), true)
            }
        }

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        list.layoutManager = layoutManager
        list.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 10)))
        list.adapter = adapter
    }

    fun loadData() {
        if (currentIndex == 0) {
            adapter.dataList.clear()
            adapter.dataList.addAll(unfinish)
            adapter.notifyDataSetChanged()
        } else if (currentIndex == 1) {
            adapter.dataList.clear()
            adapter.dataList.addAll(finish)
            adapter.notifyDataSetChanged()
        }
    }

    var unfinish = ArrayList<GradeCheckBean.ScoreItem>()
    var finish = ArrayList<GradeCheckBean.ScoreItem>()

    override fun onResume() {
        super.onResume()
        SoguApi.getService(application,ScoreService::class.java)
                .check(2)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            unfinish = ready_grade!!
                            finish = finish_grade!!
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
