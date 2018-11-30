package com.sogukj.pe.module.fund

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayout
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.Gson
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.FundSmallBean
import com.sogukj.pe.bean.FundStructure
import com.sogukj.pe.service.FundService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_fund_structure.*
import org.jetbrains.anko.find

class FundStructureActivity : ToolbarActivity() {
    lateinit var adapter: RecyclerAdapter<FundStructure.FundedRatio>

    companion object {
        val DATA: String = "DATA"
        val TAG = FundStructureActivity::class.java.simpleName
        fun start(ctx: Context?, bean: FundSmallBean) {
            val intent = Intent(ctx, FundStructureActivity::class.java)
            intent.putExtra(DATA, bean)
            ctx?.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fund_structure)
        val bean = intent.getSerializableExtra(DATA) as FundSmallBean
        setBack(true)
        title = "基金公司架构"
        fundCompanyName.text = bean.fundName
        run {
            adapter = RecyclerAdapter(this, { _adapter, parent, type ->
                val convertView = _adapter.getView(R.layout.item_fund_structure_ratio, parent)
                object : RecyclerHolder<FundStructure.FundedRatio>(convertView) {
                    override fun setData(view: View, data: FundStructure.FundedRatio, position: Int) {
                        convertView.find<TextView>(R.id.cardCompanyName).text = data.partnerName
                        convertView.find<TextView>(R.id.contributeTv).text = data.contribute
                        convertView.find<TextView>(R.id.investRateTv).text = data.investRate
                    }
                }
            })
            blList.layoutManager = LinearLayoutManager(this)
            blList.adapter = adapter
            blList.isNestedScrollingEnabled = false
        }

        getFundStructure(bean.id)
    }

    private fun addEntry(datas: Collection<String>, group: ViewGroup) {
        if (datas.isNotEmpty()) {
            for (it in datas) {
                val entry = layoutInflater.inflate(R.layout.item_fund_structure_list, null)
                val view = entry.find<TextView>(R.id.list_item_tv)
                view.text = it
                val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams.bottomMargin = Utils.dpToPx(this, 10)
                group.addView(entry, layoutParams)
            }
        } else {
            empty2.visibility = View.VISIBLE
        }
    }

    private fun addEntry1(datas: Collection<String>, group: ViewGroup) {
        if (datas.isNotEmpty()) {
            datas.forEachIndexed { index, s ->
                val entry = layoutInflater.inflate(R.layout.item_fund_structure_list, null)
                val view = entry.find<TextView>(R.id.list_item_tv)
                view.text = s
                view.gravity = Gravity.CENTER
                val rowSpec: GridLayout.Spec = GridLayout.spec(index / 4, 1f)
                val columnSpec: GridLayout.Spec = GridLayout.spec(index % 4, 1f)
                val gridManage: GridLayout.LayoutParams = GridLayout.LayoutParams(rowSpec, columnSpec)
                gridManage.width = 0
                gridManage.bottomMargin = Utils.dpToPx(this, 10)
                group.addView(entry, gridManage)
            }
        } else {
            empty1.visibility = View.VISIBLE
        }
    }


    private fun getFundStructure(fundId: Int) {
        SoguApi.getService(application,FundService::class.java)
                .getFundStructure(fundId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            if (director.isNullOrEmpty()) {
                                empty1.visibility = View.VISIBLE
                            } else {
                                empty1.visibility = View.GONE
                                addEntry1(director!!.split(","), directorList)
                            }
                            if (null == gd || gd!!.size == 0 ) {
                                empty2.visibility = View.VISIBLE
                            } else {
                                empty2.visibility = View.GONE
                                addEntry(gd!!, shareholderList)
                            }
                            Log.d("WJY", Gson().toJson(payload.payload))
                            if (bl != null && bl!!.isNotEmpty()) {
                                blList.visibility = View.VISIBLE
                                empty5.visibility = View.GONE
                                adapter.dataList.addAll(bl!!)
                                adapter.notifyDataSetChanged()
                            } else {
                                blList.visibility = View.GONE
                                empty5.visibility = View.VISIBLE
                            }
                            if (supervisor.isNullOrEmpty()) {
                                supervisorName.visibility = View.GONE
                                empty3.visibility = View.VISIBLE
                            } else {
                                supervisorName.visibility = View.VISIBLE
                                empty3.visibility = View.GONE
                                supervisorName.text = supervisor
                            }
                            if (total.isNullOrEmpty()) {
                                totalLayout.visibility = View.GONE
                                empty4.visibility = View.VISIBLE
                            } else {
                                find<LinearLayout>(R.id.totalLayout).visibility = View.VISIBLE
                                empty4.visibility = View.GONE
                                totalTv.text = total
                            }
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })
    }
}
