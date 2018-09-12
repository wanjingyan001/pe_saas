package com.sogukj.pe.module.dataSource.lawcase

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.StatusBarUtil
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.LawCaseHisInfo
import com.sogukj.pe.peUtils.Store
import kotlinx.android.synthetic.main.commom_black_title.*
import kotlinx.android.synthetic.main.law_his_content.*
import kotlinx.android.synthetic.main.law_his_search.*
import org.jetbrains.anko.startActivity
import java.util.*

/**
 * Created by CH-ZH on 2018/9/7.
 */
class LowCaseHisActivity : ToolbarActivity(), TextWatcher {
    private var lawCaseAdapter: RecyclerAdapter<LawCaseHisInfo> ? = null
    private var lawHis = ArrayList<LawCaseHisInfo>()
    private var realLawHis = ArrayList<LawCaseHisInfo>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_low_case)
        StatusBarUtil.setTransparent(this)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        toolbar?.setBackgroundColor(resources.getColor(R.color.transparent))
        setTitle("法律案例大全")

        searchEdt.isFocusable = true
        searchEdt.isFocusableInTouchMode = true
        searchEdt.requestFocus()
        recycler_view.layoutManager = LinearLayoutManager(this)
    }

    private fun initData() {
        lawCaseAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            LawCaseHolder(_adapter.getView(R.layout.item_lawhis_list, parent))
        }
        lawHis = Store.store.getLawHis(this)
        fitData()
    }

    private fun fitData() {
        if (null != lawHis && lawHis.size > 0) {
            showLawEmpty(false)
            lawHis.reverse()
            if (lawHis.size > 5) {
                for (i in 0..4) {
                    realLawHis.add(lawHis[i])
                }
                lawCaseAdapter!!.dataList.addAll(realLawHis)
                recycler_view.adapter = lawCaseAdapter
                lawCaseAdapter!!.notifyDataSetChanged()
            } else {
                lawCaseAdapter!!.dataList.addAll(lawHis)
                recycler_view.adapter = lawCaseAdapter
                lawCaseAdapter!!.notifyDataSetChanged()
            }
        } else {
            showLawEmpty(true)
        }

    }

    private fun showLawEmpty(enable: Boolean) {
        if (enable) {
            empty_img.visibility = View.VISIBLE
        } else {
            empty_img.visibility = View.INVISIBLE
        }
    }

    override fun afterTextChanged(s: Editable?) {
        if (searchEdt.text.length > 0) {
            setDelectIcon(true)
        } else {
            setDelectIcon(false)
            searchEdt.setHint("请输入关键词")
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    private fun setDelectIcon(enable: Boolean) {
        if (enable) {
            clear.visibility = View.VISIBLE
        } else {
            clear.visibility = View.INVISIBLE
        }
    }

    private fun bindListener() {
        clearHistory.clickWithTrigger {
            //清除歷史
            Store.store.clearLawHis(this)
            showLawEmpty(true)
            lawCaseAdapter!!.dataList.clear()
            lawCaseAdapter!!.notifyDataSetChanged()
        }

        searchEdt.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val editable = searchEdt.textStr
                startActivity<LawSearchResultActivity>(Extras.DATA to searchEdt.textStr)
                this.finish()
                true
            }
            false
        }

        searchEdt.addTextChangedListener(this)
        toolbar_back.setOnClickListener {
            onBackPressed()
        }

        clear.setOnClickListener {
            setDelectIcon(false)
            searchEdt.setHint("请输入关键词")
            searchEdt.setText("")
        }

        lawCaseAdapter!!.onItemClick = {v,position ->
            val infos = lawCaseAdapter!!.dataList
            if (null != infos && infos.size > 0){
                if (null != infos[position]){
                    startActivity<LawResultDetailActivity>(Extras.DATA to infos[position].href)
                }
            }
        }
    }

    inner class LawCaseHolder(itemView: View) : RecyclerHolder<LawCaseHisInfo>(itemView) {
        val num = itemView.findViewById<TextView>(R.id.num)
        val tv_kind = itemView.findViewById<TextView>(R.id.tv_kind)
        val tv_title = itemView.findViewById<TextView>(R.id.tv_title)
        val tv_time = itemView.findViewById<TextView>(R.id.tv_time)
        override fun setData(view: View, data: LawCaseHisInfo, position: Int) {
            num.text = (position + 1).toString() + "."
            tv_kind.text = data.kind
            tv_title.text = data.title
            tv_time.text = data.time
        }
    }
}


