package com.sogukj.pe.module.score

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.bean.GradeCheckBean
import com.sogukj.pe.bean.JobPageBean
import com.sogukj.pe.bean.PFBZ
import com.sogukj.pe.bean.TouZiUpload
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.service.ScoreService
import com.sogukj.service.SoguApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_gang_wei_sheng_ren_li.*
import kotlinx.android.synthetic.main.header.*
import org.jetbrains.anko.textColor

class GangWeiShengRenLiActivity : ToolbarActivity() {

    companion object {
        //id person.id, isShow-- 是否展示页面  true为展示页面，false是打分界面
        fun start(ctx: Context?, person: GradeCheckBean.ScoreItem, isShow: Boolean) {
            val intent = Intent(ctx, GangWeiShengRenLiActivity::class.java)
            intent.putExtra(Extras.DATA, person)
            intent.putExtra(Extras.FLAG, isShow)
            ctx?.startActivity(intent)
        }
    }

    lateinit var sub_adapter: RecyclerAdapter<JobPageBean.PageItem>
    var id = -1
    var isShow = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gang_wei_sheng_ren_li)

        toolbar_menu.setOnClickListener {
            RuleActivity.start(context)
        }
        var person = intent.getSerializableExtra(Extras.DATA) as GradeCheckBean.ScoreItem
        id = person.user_id!!
        person.let {
            if(it.url.isNullOrEmpty()){
                val ch = it.name?.first()
                icon.setChar(ch)
            } else {
                Glide.with(context).load(MyGlideUrl(it.url)).into(icon)
            }
            name.text = it.name
            depart.text = it.department
            position.text = it.position
        }

        isShow = intent.getBooleanExtra(Extras.FLAG, false)

        setBack(true)
        title = "岗位胜任力考核评分"
        toolbar?.setBackgroundColor(Color.WHITE)
        toolbar?.apply {
            val title = this.findViewById<TextView>(R.id.toolbar_title)
            title?.textColor = Color.parseColor("#282828")
            val back = this.findViewById<ImageView>(R.id.toolbar_back) as ImageView
            back.visibility = View.VISIBLE
            back.setImageResource(R.drawable.grey_back)
        }

        sub_adapter = RecyclerAdapter(context, { _adapter, parent, t ->
            ProjectHolderNoTitle(_adapter.getView(R.layout.item_rate, parent))
        })
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rate_list.layoutManager = layoutManager
        rate_list.addItemDecoration(SpaceItemDecoration(30))
        rate_list.adapter = sub_adapter
        doRequest()
    }

    var pinfen = ArrayList<PFBZ>()


    fun doRequest() {
        SoguApi.getService(application,ScoreService::class.java)
                .showJobPage(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            data?.forEach {
                                sub_adapter.dataList.add(it)
                            }
                            sub_adapter.notifyDataSetChanged()

                            pfbz?.forEach {
                                pinfen.add(it)
                            }
                        }
                        if (isShow) {
                            tv_socre.text = payload.total as String
                            btn_commit.visibility = View.GONE
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                })
    }

    val observable_List = ArrayList<Observable<Int>>()
    val weight_list = ArrayList<Int>()
    var dataList = ArrayList<TouZiUpload>()

    inner class ProjectHolderNoTitle(view: View)
        : RecyclerHolder<JobPageBean.PageItem>(view) {

        var bar = convertView.findViewById<ProgressBar>(R.id.progressBar) as ProgressBar
        var judge = convertView.findViewById<TextView>(R.id.text) as TextView
        var title = convertView.findViewById<TextView>(R.id.title) as TextView
        var desc = convertView.findViewById<TextView>(R.id.desc) as TextView
        var jixiao = convertView.findViewById<LinearLayout>(R.id.jixiao) as LinearLayout

        override fun setData(view: View, data: JobPageBean.PageItem, position: Int) {

            title.text = data.name
            desc.visibility = View.GONE
            jixiao.visibility = View.GONE
            bar.max = data.total_score!!


            if (isShow) {
                var score = data.score?.toInt()!!
                bar.progress = score
                if (score >= pinfen.get(0).ss!!.toInt() && score <= pinfen.get(0).es!!.toInt()) {
                    bar.progressDrawable = context.resources.getDrawable(R.drawable.pb_a)
                } else if (score >= pinfen.get(1).ss!!.toInt() && score <= pinfen.get(1).es!!.toInt()) {
                    bar.progressDrawable = context.resources.getDrawable(R.drawable.pb_b)
                } else if (score >= pinfen.get(2).ss!!.toInt() && score <= pinfen.get(2).es!!.toInt()) {
                    bar.progressDrawable = context.resources.getDrawable(R.drawable.pb_c)
                } else if (score >= pinfen.get(3).ss!!.toInt() && score <= pinfen.get(3).es!!.toInt()) {
                    bar.progressDrawable = context.resources.getDrawable(R.drawable.pb_d)
                }
                judge.text = data.score
                judge.setTextColor(Color.parseColor("#ffa0a4aa"))
                judge.textSize = 16f
                judge.setBackgroundDrawable(null)
            } else {

                var obser = TextViewClickObservable(context, judge, bar, pinfen)

                observable_List.add(obser)

                weight_list.add(data.weight!!.toInt())

                var upload = TouZiUpload()
                upload.performance_id = data.id!!.toInt()
                upload.type = data.type
                dataList.add(upload)

                if (observable_List.size == sub_adapter.dataList.size) {
                    Observable.combineLatest(observable_List, object : Function<Array<Any>, Double> {
                        override fun apply(str: Array<Any>): Double {
                            var result = 0.0
                            var date = ArrayList<Int>()//每项分数
                            for (ites in str) {
                                date.add(ites as Int)
                            }
                            for (i in weight_list.indices) {
                                dataList[i].score = date[i]
                                var single = date[i].toDouble() * weight_list[i] / 100
                                result += single
                            }
                            return result//isEmailValid(str[0].toString()) && isPasswordValid(str[1].toString())
                        }
                    }).subscribe(object : Consumer<Double> {
                        override fun accept(t: Double) {
                            tv_socre.text = "${String.format("%1$.2f", t)}"
                            btn_commit.setBackgroundColor(Color.parseColor("#FFE95C4A"))
                            btn_commit.setOnClickListener {

                                MaterialDialog.Builder(context)
                                        .theme(Theme.LIGHT)
                                        .title("提示")
                                        .content("确定要提交分数?")
                                        .onPositive { materialDialog, dialogAction ->
                                            upload(t)
                                        }
                                        .positiveText("确定")
                                        .negativeText("取消")
                                        .show()
                            }
                        }
                    })
                }
            }
        }

        fun upload(result: Double) {

            var data = ArrayList<HashMap<String, Int>>()
            for (item in dataList) {
                val inner = HashMap<String, Int>()
                inner.put("performance_id", item.performance_id!!)
                inner.put("score", item.score!!)
                inner.put("type", item.type!!)
                data.add(inner)
            }

            val params = HashMap<String, Any>()
            params.put("data", data)
            params.put("user_id", id)
            params.put("type", 2)
            params.put("total", result)

            SoguApi.getService(application,ScoreService::class.java)
                    .giveGrade(params)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            finish()
                        } else
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }, { e ->
                        Trace.e(e)
                        ToastError(e)
                    })
        }
    }
}
