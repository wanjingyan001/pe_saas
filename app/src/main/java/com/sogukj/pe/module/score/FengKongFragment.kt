package com.sogukj.pe.module.score

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.bean.FKItem
import com.sogukj.pe.bean.GradeCheckBean
import com.sogukj.pe.bean.PFBZ
import com.sogukj.pe.bean.TouZiUpload
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.service.ScoreService
import com.sogukj.service.SoguApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_feng_kong.*
import kotlinx.android.synthetic.main.header.*
import org.jetbrains.anko.support.v4.ctx

/**
 * A simple [Fragment] subclass.
 */
class FengKongFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_feng_kong

    lateinit var sub_adapter: RecyclerAdapter<FKItem.THGL.ItemData>

    companion object {
        //isShow  = false 打分界面，true展示界面
        fun newInstance(check_person: GradeCheckBean.ScoreItem, isShow: Boolean): FengKongFragment {
            val fragment = FengKongFragment()
            val intent = Bundle()
            intent.putBoolean(Extras.FLAG, isShow)
            intent.putSerializable(Extras.DATA, check_person)
            fragment.arguments = intent
            return fragment
        }
    }

    lateinit var person: GradeCheckBean.ScoreItem
    var isShown = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var adapter = InnerAdapter(ctx)
        list_.adapter = adapter


        sub_adapter = RecyclerAdapter(ctx, { _adapter, parent, t ->
            ProjectHolderNoTitle(_adapter.getView(R.layout.item_rate, parent))
        })
        val layoutManager = LinearLayoutManager(ctx)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rate_list_FK.layoutManager = layoutManager
        rate_list_FK.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 30)))
        rate_list_FK.adapter = sub_adapter

        person = arguments!!.getSerializable(Extras.DATA) as GradeCheckBean.ScoreItem
        person.let {
            if(it.url.isNullOrEmpty()){

            } else {
                Glide.with(ctx).load(MyGlideUrl(it.url)).into(icon)
            }
            name.text = it.name
            depart.text = it.department
            position.text = it.position
        }
        isShown = arguments!!.getBoolean(Extras.FLAG) // false 打分界面，true展示界面

        SoguApi.getService(baseActivity!!.application,ScoreService::class.java)
                .perAppraisal_FK(person.user_id!!, person.type!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            jdxm?.data?.forEach {
                                adapter.datalist.add(it)
                            }
                            adapter.notifyDataSetChanged()

                            fk_score.text = "${jdxm?.selfScore}"
                            selfCore = jdxm?.selfScore!!

                            thgl?.data?.forEach {
                                sub_adapter.dataList.add(it)
                            }
                            sub_adapter.notifyDataSetChanged()
                            if (isShown) {
                                tv_socre.text = payload.total as String
                                btn_commit.visibility = View.GONE
                            }
                            pfbz?.forEach {
                                pinfen.add(it)
                            }
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    ToastError(e)
                })
    }

    var selfCore = 0
    val weight_list = ArrayList<Int>()
    val observable_List = ArrayList<Observable<Int>>()
    var pinfen = ArrayList<PFBZ>()

    inner class ProjectHolderNoTitle(view: View)
        : RecyclerHolder<FKItem.THGL.ItemData>(view) {

        var bar = convertView.findViewById<ProgressBar>(R.id.progressBar) as ProgressBar
        var judge = convertView.findViewById<TextView>(R.id.text) as TextView
        var title = convertView.findViewById<TextView>(R.id.title) as TextView
        var sub_title = convertView.findViewById<TextView>(R.id.subtitle) as TextView
        var desc = convertView.findViewById<TextView>(R.id.desc) as TextView
        var lll = convertView.findViewById<LinearLayout>(R.id.lll) as LinearLayout

        override fun setData(view: View, data: FKItem.THGL.ItemData, position: Int) {

            title.text = data.target
            //lll.visibility = View.GONE
            desc.text = data.info

            if (isShown) {
                var score = data.score?.toInt()!!
                bar.progress = score
                if (score >= 101 && score <= 120) {
                    bar.progressDrawable = ctx.resources.getDrawable(R.drawable.pb_a)
                } else if (score >= 81 && score <= 100) {
                    bar.progressDrawable = ctx.resources.getDrawable(R.drawable.pb_b)
                } else if (score >= 61 && score <= 80) {
                    bar.progressDrawable = ctx.resources.getDrawable(R.drawable.pb_c)
                } else if (score >= 0 && score <= 60) {
                    bar.progressDrawable = ctx.resources.getDrawable(R.drawable.pb_d)
                }
                judge.text = data.score
                judge.setTextColor(Color.parseColor("#ffa0a4aa"))
                judge.textSize = 16f
                judge.setBackgroundDrawable(null)
            } else {
                var obser = TextViewClickObservable(context, judge, bar, pinfen)
                observable_List.add(obser)

                weight_list.add(data.weight?.toInt()!!)

                var upload = TouZiUpload()
                upload.performance_id = data.id
                upload.type = data.type
                dataList.add(upload)

                if (observable_List.size == sub_adapter.dataList.size) {
                    Observable.combineLatest(observable_List) { str ->
                        var result = 0.0
                        var date = ArrayList<Int>()//每项分数
                        str.mapTo(date) { it as Int }
                        for (i in weight_list.indices) {
                            dataList[i].score = date[i]
                            var single = date[i].toDouble() * weight_list[i] / 100
                            result += single
                        }
                        //立项尽调
                        result += selfCore
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
        params.put("type", 1)
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

    class InnerAdapter(val context: Context) : BaseAdapter() {

        val datalist = ArrayList<FKItem.JDXM.InnerData>()

        fun addAll(list: ArrayList<FKItem.JDXM.InnerData>) {
            datalist.addAll(list)
            notifyDataSetChanged()
        }

        fun add(item: FKItem.JDXM.InnerData) {
            datalist.add(item)
            notifyDataSetChanged()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            var holder: Holder? = null
            if (view == null) {
                holder = Holder()
                view = LayoutInflater.from(context).inflate(R.layout.item_fengkong, null)
                holder.title = view.findViewById<TextView>(R.id.item_title) as TextView
                holder.content = view.findViewById<EditText>(R.id.item_content) as EditText
                view?.tag = holder
            } else {
                holder = view.tag as Holder
            }
            holder.content?.filters = Utils.getFilter(context)
            holder.zhibiao?.filters = Utils.getFilter(context)
            holder.condition?.filters = Utils.getFilter(context)
            holder.title?.text = datalist[position].target
            holder.content?.setText(datalist[position].info)
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

        class Holder {
            var title: TextView? = null
            var content: EditText? = null

            var zhibiao: EditText? = null
            var condition: EditText? = null
        }
    }
}
