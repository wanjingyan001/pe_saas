package com.sogukj.pe.module.score


import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.bean.GradeCheckBean
import com.sogukj.pe.bean.NormalItemBean
import com.sogukj.pe.bean.PFBZ
import com.sogukj.pe.bean.TouZiUpload
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.service.ScoreService
import com.sogukj.service.SoguApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_rate.*
import kotlinx.android.synthetic.main.header.*
import org.jetbrains.anko.support.v4.ctx


/**
 * a simple [Fragment] subclass.
 */
class RateFragment : BaseFragment() {

    lateinit var jixiao_adapter: RecyclerAdapter<NormalItemBean.NormalItem.BeanItem>
    lateinit var add_adapter: RecyclerAdapter<NormalItemBean.NormalItem.BeanItem>
    lateinit var minus_adapter: RecyclerAdapter<NormalItemBean.NormalItem.BeanItem>

    companion object {

        //isShow  = false 打分界面，true展示界面
        fun newInstance(check_person: GradeCheckBean.ScoreItem, isShow: Boolean, type: Int): RateFragment {
            val fragment = RateFragment()
            val intent = Bundle()
            intent.putBoolean(Extras.FLAG, isShow)
            intent.putSerializable(Extras.DATA, check_person)
            intent.putInt(Extras.TYPE, type)
            fragment.arguments = intent
            return fragment
        }
    }

    override val containerViewId: Int
        get() = R.layout.fragment_rate

    lateinit var person: GradeCheckBean.ScoreItem
    var isShown = false
    var type = 0
    var type111 = 0//

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        jixiao_list.layoutManager = layoutManager
        jixiao_list.addItemDecoration(SpaceItemDecoration(25))

        val layoutManager1 = LinearLayoutManager(context)
        layoutManager1.orientation = LinearLayoutManager.VERTICAL
        add_listview.layoutManager = layoutManager1
        add_listview.addItemDecoration(SpaceItemDecoration(25))

        val layoutManager2 = LinearLayoutManager(context)
        layoutManager2.orientation = LinearLayoutManager.VERTICAL
        minuse_listview.layoutManager = layoutManager2
        minuse_listview.addItemDecoration(SpaceItemDecoration(25))

        person = arguments!!.getSerializable(Extras.DATA) as GradeCheckBean.ScoreItem
        person.let {
            if(it.url.isNullOrEmpty()){
                icon.setChar(it.name?.first())
            } else {
                Glide.with(ctx).load(MyGlideUrl(it.url)).into(icon)
            }
            name.text = it.name
            depart.text = it.department
            position.text = it.position
        }
        isShown = arguments!!.getBoolean(Extras.FLAG) // false 打分界面，true展示界面
        type = arguments!!.getInt(Extras.TYPE)

        //非空（1=>绩效，3=>加减项）
        if (type == Extras.TYPE_JIXIAO) {
            type111 = 1
            final_score.text = "最后得分(总分80)"
        } else if (type == Extras.TYPE_TIAOZHENG) {
            type111 = 3
            standard.visibility = View.GONE
            final_score.text = "最后得分(最低-20，最高20)"
        }
        SoguApi.getService(baseActivity!!.application,ScoreService::class.java)
                .perAppraisal(person.user_id!!, type111)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            if (type == Extras.TYPE_JIXIAO) {
                                //
                                jixiao_adapter = RecyclerAdapter(ctx, { _adapter, parent, t ->
                                    ProjectHolderNoTitle(_adapter.getView(R.layout.item_rate, parent))
                                })
                                jixiao_list.adapter = jixiao_adapter

                                data?.forEach {
                                    jixiao_adapter.dataList.add(it)
                                }
                                jixiao_adapter.notifyDataSetChanged()

                                num = jixiao_adapter.dataList.size

                                ll_add.visibility = View.GONE
                                ll_minuse.visibility = View.GONE

                                pinfen = pfbz!!
                            } else if (type == Extras.TYPE_TIAOZHENG) {
                                //
                                add_adapter = RecyclerAdapter(ctx, { _adapter, parent, t ->
                                    ProjectHolderNoTitle(_adapter.getView(R.layout.item_rate, parent))
                                })
                                add_listview.adapter = add_adapter

                                jf?.forEach {
                                    add_adapter.dataList.add(it)
                                }
                                add_adapter.notifyDataSetChanged()

                                num += add_adapter.dataList.size

                                //
                                minus_adapter = RecyclerAdapter(ctx, { _adapter, parent, t ->
                                    ProjectHolderNoTitle(_adapter.getView(R.layout.item_rate, parent))
                                })
                                minuse_listview.adapter = minus_adapter

                                kf?.forEach {
                                    minus_adapter.dataList.add(it)
                                }
                                minus_adapter.notifyDataSetChanged()

                                num += minus_adapter.dataList.size

                                jixiao_list.visibility = View.GONE
                            }
                            if (isShown) {
                                tv_socre.text = payload.total as String
                                btn_commit.visibility = View.GONE
                            }
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                })
    }

    var num = 0
    var pinfen = ArrayList<PFBZ>()

    val observable_List = ArrayList<Observable<Int>>()
    val weight_list = ArrayList<Int>()
    var dataList = ArrayList<TouZiUpload>()

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
        params.put("user_id", person.user_id!!)
        params.put("type", type111)
        params.put("total", result)

        SoguApi.getService(baseActivity!!.application,ScoreService::class.java)
                .giveGrade(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        baseActivity?.finish()
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                })
    }

    //加分项，减分项---只有actual有用
    inner class ProjectHolderNoTitle(view: View)
        : RecyclerHolder<NormalItemBean.NormalItem.BeanItem>(view) {

        var bar = convertView.findViewById<ProgressBar>(R.id.progressBar) as ProgressBar
        var judge = convertView.findViewById<TextView>(R.id.text) as TextView
        var title = convertView.findViewById<TextView>(R.id.title) as TextView
        //jiajianfen
        var desc = convertView.findViewById<TextView>(R.id.desc) as TextView
        //jixiao
        var jixiao = convertView.findViewById<LinearLayout>(R.id.jixiao) as LinearLayout
        var title1 = convertView.findViewById<TextView>(R.id.title1) as TextView
        var title2 = convertView.findViewById<TextView>(R.id.title2) as TextView

        @SuppressLint("SetTextI18n")
        override fun setData(view: View, data: NormalItemBean.NormalItem.BeanItem, position: Int) {

            title.text = data.name
            bar.max = data.total_score!!

            if (type == Extras.TYPE_JIXIAO) {
                desc.visibility = View.GONE
                title1.text = data.info
                title2.text = data.actual
            } else if (type == Extras.TYPE_TIAOZHENG) {
                desc.text = data.actual
                jixiao.visibility = View.GONE
            }

            if (isShown) {
                var score = data.score?.toInt()!!
                bar.progress = score
                //1=>关键绩效指标评价 2=>岗位胜任力评价 3=>加分项 4=>减分项
                when {
                    data.type == 4 -> bar.progressDrawable = ctx.resources.getDrawable(R.drawable.pb_min)
                    data.type == 3 -> bar.progressDrawable = ctx.resources.getDrawable(R.drawable.pb_add)
                    else -> when (score) {
                        in 101..120 -> bar.progressDrawable = ctx.resources.getDrawable(R.drawable.pb_a)
                        in 81..100 -> bar.progressDrawable = ctx.resources.getDrawable(R.drawable.pb_b)
                        in 61..80 -> bar.progressDrawable = ctx.resources.getDrawable(R.drawable.pb_c)
                        in 0..60 -> bar.progressDrawable = ctx.resources.getDrawable(R.drawable.pb_d)
                    }
                }
                //3=>加分项 4=>减分项
                if (data.type == 4) {
                    judge.text = "-${data.score}"
                } else {
                    judge.text = data.score
                }
                judge.setTextColor(Color.parseColor("#ffa0a4aa"))
                judge.textSize = 16f
                judge.setBackgroundDrawable(null)
            } else {
                //1=>关键绩效指标评价 2=>岗位胜任力评价 3=>加分项 4=>减分项
                when {
                    data.type == 4 -> {
                        var obser = TextViewClickObservableAddOrMinus(context, judge, bar, data.total_score!!, data.offset!!, R.drawable.pb_min)
                        observable_List.add(obser)
                    }
                    data.type == 3 -> {
                        var obser = TextViewClickObservableAddOrMinus(context, judge, bar, data.total_score!!, data.offset!!, R.drawable.pb_add)
                        observable_List.add(obser)
                    }
                    else -> {
                        var obser = TextViewClickObservable(context, judge, bar, pinfen)
                        observable_List.add(obser)
                    }
                }

                if (data.type == 4) {//扣分项
                    weight_list.add(data.weight!!.toInt() * -1)
                } else {
                    weight_list.add(data.weight!!.toInt())
                }

                var upload = TouZiUpload()
                upload.performance_id = data.id!!.toInt()
                upload.type = data.type
                dataList.add(upload)

                if (observable_List.size == num) {
                    Observable.combineLatest(observable_List) { str ->
                        var result = 0.0
                        var date = ArrayList<Int>()//每项分数
                        str.mapTo(date) { it as Int }
                        for (i in weight_list.indices) {
                            dataList[i].score = date[i]
                            var single = date[i].toDouble() * weight_list[i] / 100
                            result += single
                        }
                        result//isEmailValid(str[0].toString()) && isPasswordValid(str[1].toString())
                    }.subscribe { t ->
                        tv_socre.text = "${String.format("%1$.2f", t)}"
                        btn_commit.setBackgroundColor(Color.parseColor("#FFE95C4A"))
                        btn_commit.setOnClickListener {
                            MaterialDialog.Builder(ctx)
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
                }
            }
        }
    }
}
