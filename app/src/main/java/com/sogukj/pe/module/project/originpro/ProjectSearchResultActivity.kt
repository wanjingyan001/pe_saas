package com.sogukj.pe.module.project.originpro

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.CompanySelectBean
import com.sogukj.pe.service.ProjectService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_project_search.*
/**
 * Created by CH-ZH on 2018/9/28.
 */
class ProjectSearchResultActivity : ToolbarActivity() {
    lateinit var mCompanyAdapter: RecyclerAdapter<CompanySelectBean>
    private var searchName : String ? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_search)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        searchName = intent.getStringExtra(Extras.DATA)

        search_view.et_search.setText(searchName)
        if (searchName!!.length <= 10){
            search_view.et_search.setSelection(searchName!!.length)
        }
    }

    private fun initData() {
        mCompanyAdapter = RecyclerAdapter<CompanySelectBean>(this, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_main_project_search, parent) as View
            object : RecyclerHolder<CompanySelectBean>(convertView) {
                val tv1 = convertView.findViewById<TextView>(R.id.tv1) as TextView
                override fun setData(view: View, data: CompanySelectBean, position: Int) {
                    tv1.text = "${position + 1}." + data.name
                }
            }
        })

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_result.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_result.layoutManager = layoutManager
        recycler_result.adapter = mCompanyAdapter

        doSearch(searchName!!)
    }

    private fun bindListener() {
        search_view.et_search.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchName = search_view.et_search.text.toString()
                doFinish()
                true
            }
            false
        }

        search_view.onTextChange = { text ->
            if (!TextUtils.isEmpty(text)) {
                handler.postDelayed({ doSearch(text) }, 500)
            } else {
                mCompanyAdapter.dataList.clear()
                mCompanyAdapter.notifyDataSetChanged()
            }
        }

        search_view.tv_cancel.setOnClickListener {
            Utils.closeInput(context, search_view.et_search)
            searchName = ""
            doFinish()
        }

        mCompanyAdapter.onItemClick = { v, p ->
            val data = mCompanyAdapter.dataList.get(p)
            searchName = data.name
            Utils.closeInput(context, search_view.et_search)
            doFinish()
        }
    }

    fun doSearch(text: String) {
        var map = HashMap<String, String>()
        map.put("name", text)
        recycler_result.visibility = View.VISIBLE
        iv_empty.visibility = View.GONE
        SoguApi.getService(application, ProjectService::class.java)
                .searchCompany(map)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        mCompanyAdapter.dataList.clear()

                        var bean = CompanySelectBean()
                        bean.name = search_view.search
                        mCompanyAdapter.dataList.add(bean)

                        payload?.payload?.apply {
                            mCompanyAdapter.dataList.addAll(this)
                        }
                        mCompanyAdapter.notifyDataSetChanged()
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                    if (mCompanyAdapter.dataList.size == 0) {
                        recycler_result.visibility = View.GONE
                        iv_empty.visibility = View.VISIBLE
                    }
                }, { e ->
                    Trace.e(e)
                    if (mCompanyAdapter.dataList.size == 0) {
                        recycler_result.visibility = View.GONE
                        iv_empty.visibility = View.VISIBLE
                    }
                })
    }

    fun doFinish(){
        val intent = Intent()
        intent.putExtra(Extras.DATA, searchName.toString().trim())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    companion object {
        fun start(ctx: Activity?,content: String, code: Int) {
            val intent = Intent(ctx, ProjectSearchResultActivity::class.java)
            intent.putExtra(Extras.DATA, content)
            ctx?.startActivityForResult(intent, code)
        }
    }

    override fun onPause() {
        super.onPause()
        if (null != search_view.et_search){
            Utils.forceCloseInput(this,search_view.et_search)
        }
    }
}