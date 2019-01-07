package com.sogukj.pe.module.score

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.jakewharton.rxbinding2.widget.RxTextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.GradeCheckBean
import com.sogukj.pe.bean.JinDiaoItem
import com.sogukj.pe.bean.TouHouManageItem
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.ScoreService
import com.sogukj.pe.widgets.CircleImageView
import com.sogukj.service.SoguApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_feng_kong.*
import kotlinx.android.synthetic.main.header.*
import org.jetbrains.anko.textColor


class FengKongActivity : ToolbarActivity() {

    companion object {
        fun start(ctx: Activity) {
            val intent = Intent(ctx, FengKongActivity::class.java)
            ctx.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feng_kong)

        val bean = Store.store.getUser(context)
        bean?.let {
            //Glide.with(context).load(it.url).into(icon)
            val icon = findViewById<CircleImageView>(R.id.icon) as CircleImageView
            if (it.url.isNullOrEmpty()) {
                val ch = it.name.first()
                icon.setChar(ch)
            } else {
                Glide.with(context)
                        .load(MyGlideUrl(it.url))
                        .apply(RequestOptions().error(R.drawable.nim_avatar_default).fallback(R.drawable.nim_avatar_default))
                        .into(icon)
            }
            name.text = it.name
            depart.text = it.depart_name
            position.text = it.position
        }

        setBack(true)
        title = "风控部门评分标准填写"
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        toolbar?.apply {
            val title = this.findViewById<TextView>(R.id.toolbar_title)
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById<ImageView>(R.id.toolbar_back) as ImageView
            back.visibility = View.VISIBLE
            back.setImageResource(R.drawable.icon_back_gray)
        }

        btn_commit.setOnClickListener {
            if (canClick == false) {
                return@setOnClickListener
            }

            MaterialDialog.Builder(context)
                    .theme(Theme.LIGHT)
                    .title("提示")
                    .content("确定要提交此标准?")
                    .onPositive { materialDialog, dialogAction ->
                        var jin__ = ArrayList<HashMap<String, String>>()
                        for (item in jin) {
                            var jin_item = HashMap<String, String>()
                            jin_item.put("target", item.title!!)
                            jin_item.put("info", item.info!!)
                            jin__.add(jin_item)
                        }

                        var touhou__ = ArrayList<HashMap<String, String>>()
                        for (item in touhou) {
                            var touhou_item = HashMap<String, String>()
                            touhou_item.put("performance_id", "${item.performance_id}")
                            touhou_item.put("info", item.info!!)
                            touhou__.add(touhou_item)
                        }

                        var total = HashMap<String, ArrayList<HashMap<String, String>>>()
                        total.put("jdxm", jin__)
                        total.put("thgl", touhou__)

                        SoguApi.getService(application,ScoreService::class.java)
                                .risk_add(total)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe({ payload ->
                                    if (payload.isOk) {
                                        GangWeiListActivity.start(context, Extras.TYPE_EMPLOYEE)
                                        //JudgeActivity.start(context, TYPE_EMPLOYEE, FK)
                                        finish()
                                    } else
                                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                                }, { e ->
                                    Trace.e(e)
                                    ToastError(e)
                                })
                    }
                    .positiveText("确定")
                    .negativeText("取消")
                    .show()
        }

        SoguApi.getService(application,ScoreService::class.java)
                .check(3)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            fengkong?.let {
                                //jin diao
                                var std = findViewById<TextView>(R.id.std1) as TextView
                                std.text = initString(it.get(0).biaozhun!!)

                                var listView = findViewById<RecyclerView>(R.id.myList1) as RecyclerView
                                var jindiao_adapter = RecyclerAdapter<GradeCheckBean.FengKongItem.FengKongInnerItem>(context, { _adapter, parent, t ->
                                    JinDiaoHolder(_adapter.getView(R.layout.fengkong_item, parent))
                                })
                                val layoutManager = LinearLayoutManager(context)
                                layoutManager.orientation = LinearLayoutManager.VERTICAL
                                listView.layoutManager = layoutManager
                                listView.adapter = jindiao_adapter
                                jindiao_adapter.dataList.add(GradeCheckBean.FengKongItem.FengKongInnerItem())
                                jindiao_adapter.notifyDataSetChanged()

                                var add_item = findViewById<TextView>(R.id.add_item) as TextView
                                add_item.setOnClickListener {
                                    jin.clear()
                                    isJinDiaoReady = false
                                    observable_List_jindiao_zb.clear()
                                    observable_List_jindiao_cond.clear()
                                    jindiao_adapter.dataList.add(GradeCheckBean.FengKongItem.FengKongInnerItem())
                                    jindiao_adapter.notifyDataSetChanged()
                                    max_jindiao = jindiao_adapter.dataList.size
                                }
                                max_jindiao = 1

                                //tou hou
                                var std2 = findViewById<TextView>(R.id.std2) as TextView
                                std2.text = initString(it.get(1).biaozhun!!)

                                var listView2 = findViewById<RecyclerView>(R.id.mylist2) as RecyclerView
                                var touhou_adapter = RecyclerAdapter<GradeCheckBean.FengKongItem.FengKongInnerItem>(context, { _adapter, parent, t ->
                                    TouHouHolder(_adapter.getView(R.layout.item_fengkong, parent))
                                })
                                val layoutManager1 = LinearLayoutManager(context)
                                layoutManager1.orientation = LinearLayoutManager.VERTICAL
                                listView2.layoutManager = layoutManager1
                                listView2.adapter = touhou_adapter

                                touhou_adapter.dataList.addAll(it.get(1).data!!)
                                touhou_adapter.notifyDataSetChanged()
                                max_touhou = it.get(1).data!!.size
                            }
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                })
    }

    fun initString(str: String): SpannableString {
        val spannable1 = SpannableString("评分标准:${str}")
        spannable1.setSpan(ForegroundColorSpan(Color.parseColor("#FFE95C4A")), 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        spannable1.setSpan(ForegroundColorSpan(Color.parseColor("#FF323232")), 5, spannable1.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        return spannable1
    }

    var max_touhou = 0
    val observable_List_touhou = ArrayList<Observable<String>>()
    var touhou = ArrayList<TouHouManageItem>()
    var isTouHouReady = false

    var max_jindiao = 0
    val observable_List_jindiao_zb = ArrayList<Observable<String>>()
    val observable_List_jindiao_cond = ArrayList<Observable<String>>()
    var jin = ArrayList<JinDiaoItem>()
    var isJinDiaoReady = false

    var canClick = false

    /**
     * 0--touhou
     */
    inner class TouHouHolder(view: View)
        : RecyclerHolder<GradeCheckBean.FengKongItem.FengKongInnerItem>(view) {

        var title = view.findViewById<TextView>(R.id.item_title) as TextView
        var content = view.findViewById<EditText>(R.id.item_content) as EditText

        override fun setData(view: View, data: GradeCheckBean.FengKongItem.FengKongInnerItem, position: Int) {
            Log.e("TouHouHolder", "TouHouHolder")
            title.text = data.zhibiao
            content.filters = Utils.getFilter(context)
            var item = TouHouManageItem()
            item.performance_id = data.performance_id
            item.info = ""
            touhou.add(item)
            var obser = RxTextView.textChanges(content).map { t -> t.toString() }
            observable_List_touhou.add(obser)

            if (max_touhou == observable_List_touhou.size) {
                Observable.combineLatest(observable_List_touhou) { str ->
                    for (item in 0 until touhou.size) {
                        touhou[item].info = str[item] as String
                    }
                    true
                }.subscribe {
                    val flag = touhou.any { it.info.toString() == "" }

                    if (!flag) {
                        isTouHouReady = true
                    }

                    if (!flag && isJinDiaoReady) {
                        btn_commit.setBackgroundColor(Color.parseColor("#FFE95C4A"))
                        canClick = true
                    } else {
                        btn_commit.setBackgroundColor(Color.parseColor("#FFD9D9D9"))
                    }
                }
            }
        }
    }

    /**
     * 1 jindiao
     */
    inner class JinDiaoHolder(view: View)
        : RecyclerHolder<GradeCheckBean.FengKongItem.FengKongInnerItem>(view) {

        var zhibiao = view.findViewById<EditText>(R.id.zhibiao) as EditText
        var condition = view.findViewById<EditText>(R.id.condi) as EditText

        override fun setData(view: View, data: GradeCheckBean.FengKongItem.FengKongInnerItem, position: Int) {
            Log.e("JinDiaoHolder", "JinDiaoHolder")
            zhibiao.filters = Utils.getFilter(context)
            condition.filters = Utils.getFilter(context)
            var item = JinDiaoItem()
            item.info = ""
            item.title = ""
            jin.add(item)

            var obser = RxTextView.textChanges(zhibiao).map { t -> t.toString() }
            observable_List_jindiao_zb.add(obser)

            var obser1 = RxTextView.textChanges(condition).map { t -> t.toString() }
            observable_List_jindiao_cond.add(obser1)

            if (max_jindiao == observable_List_jindiao_zb.size) {
                Observable.combineLatest(observable_List_jindiao_zb) { str ->
                    for (item in 0 until jin.size) {
                        jin[item].title = str[item] as String
                    }
                    true
                }.subscribe {
                    Observable.combineLatest(observable_List_jindiao_cond) { str ->
                        for (item in 0 until jin.size) {
                            jin[item].info = str[item] as String
                        }
                        true
                    }.subscribe {
                        val flag = jin.any { it.title.toString() == "" || it.info.toString() == "" }

                        if (flag == false) {
                            isJinDiaoReady = true
                        }

                        if (flag == false && isTouHouReady == true) {
                            btn_commit.setBackgroundColor(Color.parseColor("#FFE95C4A"))
                            canClick = true
                        } else {
                            btn_commit.setBackgroundColor(Color.parseColor("#FFD9D9D9"))
                        }
                    }
                }
            }
        }
    }
}
