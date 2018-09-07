package com.sogukj.pe.module.dataSource.lawcase

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.LawCaseHisInfo
import com.sogukj.pe.peUtils.Store
import kotlinx.android.synthetic.main.law_his_content.*
import java.util.*

/**
 * Created by CH-ZH on 2018/9/7.
 */
class LowCaseHisActivity : ToolbarActivity(){
    private lateinit var lawCaseAdapter: RecyclerAdapter<LawCaseHisInfo>
    private  var lawHis = ArrayList<LawCaseHisInfo>()
    private var realLawHis = ArrayList<LawCaseHisInfo>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_low_case)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        setTitle("法律案例大全")
    }

    private fun initData() {
        lawCaseAdapter =  RecyclerAdapter(this){ _adapter, parent, _ ->
            LawCaseHolder(_adapter.getView(R.layout.item_lawhis_list, parent))
        }
       lawHis = Store.store.getLawHis(this)
        fitData()

    }
    private fun fitData() {
        lawHis.let{
            if (null != it && it.size > 0){
                showLawEmpty(false)
                it.reverse()
                if (it.size > 5){
                    for (i in 0 .. 4){
                        realLawHis.add(it[i])
                    }
                }else{
                    lawCaseAdapter.dataList.addAll(it)
                }
                recycler_view.adapter = lawCaseAdapter
                lawCaseAdapter.notifyDataSetChanged()
            }else{
                showLawEmpty(true)
            }
        }

    }

    private fun showLawEmpty(enable: Boolean) {
        if (enable){
            empty_img.visibility = View.VISIBLE
        }else{
            empty_img.visibility = View.INVISIBLE
        }
    }

    private fun bindListener() {
        clearHistory.clickWithTrigger {
            //清除歷史
            Store.store.clearLawHis(this)
            showLawEmpty(true)
            lawCaseAdapter.dataList.clear()
            lawCaseAdapter.notifyDataSetChanged()
        }

    }
    inner class LawCaseHolder(itemView: View) : RecyclerHolder<LawCaseHisInfo>(itemView) {
        val num = itemView.findViewById<TextView>(R.id.num)
        val tv_kind = itemView.findViewById<TextView>(R.id.tv_kind)
        val tv_title = itemView.findViewById<TextView>(R.id.tv_title)
        val tv_time = itemView.findViewById<TextView>(R.id.tv_time)
        override fun setData(view: View, data: LawCaseHisInfo, position: Int) {
            num.text = (position + 1).toString()+"."
            tv_kind.text = data.kind
            tv_title.text = data.title
            tv_time.text = data.time
        }
    }
}


