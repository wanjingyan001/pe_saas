package com.sogukj.pe.module.other

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import kotlinx.android.synthetic.main.activity_pay_expansion.*
import kotlinx.android.synthetic.main.item_pay_expansion_list.view.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.find

class PayExpansionActivity : BaseActivity() {
    private lateinit var pjAdapter: RecyclerAdapter<Any>
    private lateinit var calenderAdapter: RecyclerAdapter<Any>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_expansion)
        toolbar_title.text = "扩容套餐购买"
        pjAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            ExpansionHolder(_adapter.getView(R.layout.item_pay_expansion_list, parent), pjAdapter)
        }
        calenderAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            ExpansionHolder(_adapter.getView(R.layout.item_pay_expansion_list, parent),calenderAdapter)
        }
        pjAdapter.onItemClick = { v, position ->
            pjAdapter.selectedPosition = position
        }
        calenderAdapter.onItemClick = { v, position ->
            calenderAdapter.selectedPosition = position
        }
        pjPackageList.apply {
            layoutManager = LinearLayoutManager(this@PayExpansionActivity)
            adapter = pjAdapter
            addItemDecoration(SpaceItemDecoration(dip(10)))
        }
        calenderPackageList.apply {
            layoutManager = LinearLayoutManager(this@PayExpansionActivity)
            adapter = calenderAdapter
            addItemDecoration(SpaceItemDecoration(dip(10)))
        }
        initData()
    }

    private fun initData(){
        (0..5).forEach {
            pjAdapter.dataList.add("")
            calenderAdapter.dataList.add("")
        }
        pjAdapter.notifyDataSetChanged()
        calenderAdapter.notifyDataSetChanged()
    }

    inner class ExpansionHolder(itemView: View, val adapter: RecyclerAdapter<Any>) : RecyclerHolder<Any>(itemView) {
        override fun setData(view: View, data: Any, position: Int) {
            view.addNumberTv.text = ""
            view.itemLayout.isSelected = adapter.selectedPosition == position
        }
    }
}
