package com.sogukj.pe.module.project.businessDev

import android.os.Bundle
import android.view.View
import android.widget.TableLayout
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.module.news.NewsListFragment
import com.sogukj.pe.service.ProjectService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_proj_finance_distribute.*


/**
 * Created by qinfei on 17/7/18.
 */
class FinanceDistributeListFragment : BaseFragment() {
    override val containerViewId: Int
        get() = R.layout.fragment_proj_finance_distribute //To change initializer of created properties use File | Settings | File Templates.

    lateinit var project: ProjectBean
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        project = arguments!!.getSerializable(Extras.DATA) as ProjectBean
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler.postDelayed({
            doRequest()
        }, 100)
    }


    var page = 1
    fun doRequest() {
        SoguApi.getService(baseActivity!!.application,ProjectService::class.java)
                .listInvestDistribute(project.company_id!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            val lunci = this["lunci"] as Map<String, Double>
                            lunci.apply { setData(tab1, this) }
                            val hangye = this["hangye"] as Map<String, Double>
                            hangye.apply { setData(tab2, this) }
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_common, "暂无可用数据")
                })
    }

    fun setData(tab1: TableLayout, map: Map<String, Double>) {
        val max = map.values.max()
        tab1.removeAllViews()
        val width = tab1
                .width - Utils.dpToPx(baseActivity, 160)
        val pw = width.toDouble() / max!!.toDouble()
        for ((k, v) in map) {
            val convertView = View.inflate(baseActivity, R.layout.item_proj_finance_distribute, null)
            tab1.addView(convertView)

            val tvTag = convertView.findViewById<TextView>(R.id.tv_tag) as TextView
            val bar = convertView.findViewById<View>(R.id.bar)
            val tvValue = convertView.findViewById<TextView>(R.id.tv_value) as TextView

            tvTag.text = k
            tvValue.text = v.toInt().toString()
            val lp = bar.layoutParams
            lp.width = (v * pw).toInt()
            bar.layoutParams = lp

        }
    }

    companion object {
        val TAG = NewsListFragment::class.java.simpleName

        fun newInstance(project: ProjectBean): FinanceDistributeListFragment {
            val fragment = FinanceDistributeListFragment()
            val intent = Bundle()
            intent.putSerializable(Extras.DATA, project)
            fragment.arguments = intent
            return fragment
        }
    }
}