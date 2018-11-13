package com.sogukj.pe.module.receipt

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.SearchReceiptBean
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_bill_search.*
import kotlinx.android.synthetic.main.search_header.*

/**
 * Created by CH-ZH on 2018/11/8.
 * 抬头搜索
 */
class BillHeadSearchActivity : BaseRefreshActivity(), TextWatcher {
    private var inputTime = 0L
    private val INTERVAL = 300 //输入间隔时间
    private lateinit var searchAdaper : RecyclerAdapter<SearchReceiptBean>
    private var page = 1
    private var searchKey = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_search)
        Utils.setWindowStatusBarColor(this, R.color.white)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        searchKey = intent.getStringExtra("searchKey")
    }

    private fun initData() {
        searchAdaper = RecyclerAdapter(this){_adapter,parent,_ ->
            ResultBillHolder(_adapter.getView(R.layout.bill_search_item,parent))
        }
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_view.adapter = searchAdaper
        if (!searchKey.isNullOrEmpty()){
            et_search.setText(searchKey)
            et_search.setSelection(searchKey.length)
            setDelectIcon(true)
            requestData(searchKey,false)
        }
    }

    private fun bindListener() {
        iv_search.clickWithTrigger {
            Utils.showSoftInputFromWindow(this,et_search)
        }

        tv_cancel.clickWithTrigger {
            onBackPressed()
        }
        et_search.addTextChangedListener(this)

        iv_del.clickWithTrigger {
            setDelectIcon(false)
            et_search.setHint(R.string.search)
            et_search.setText("")
        }

        searchAdaper.onItemClick = {v,p ->
            val dataList = searchAdaper.dataList
            if (null != dataList && dataList.size > 0){
                val receiptBean = dataList[p]
                if (null != receiptBean){
                    val intent = Intent()
                    intent.putExtra(Extras.DATA,receiptBean)
                    setResult(Activity.RESULT_OK,intent)
                    finish()
                }
            }
        }
    }

    override fun afterTextChanged(s: Editable?) {
       if (et_search.textStr.isNullOrEmpty()){
           setDelectIcon(false)
       }else{
           setDelectIcon(true)
       }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (!et_search.textStr.isNullOrEmpty()){
            inputTime = System.currentTimeMillis()
            et_search.post(inputRunnable)
        }
    }

    private var inputRunnable:Runnable = object : Runnable{
        override fun run() {
            val inval = System.currentTimeMillis() - inputTime
            if (inval < INTERVAL){
                et_search.postDelayed(this,INTERVAL-inval)
            }else{
                requestData(et_search.textStr,false)
            }
        }
    }

    private fun requestData(search: String,isLoadMore:Boolean) {
        if (isLoadMore){
            page++
        }else{
            page = 1
        }
        SoguApi.getStaticHttp(application)
                .searchReceiptTitle(search,page)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val infos = payload.payload
                                if (!isLoadMore){
                                    searchAdaper.dataList.clear()
                                    if (!et_search.textStr.isNullOrEmpty()){
                                        val bean = SearchReceiptBean()
                                        bean.title = et_search.textStr
                                        searchAdaper.dataList.add(bean)
                                    }
                                }
                            if (null != infos && infos.size > 0){
                                searchAdaper.dataList.addAll(infos)
                            }
                            searchAdaper.notifyDataSetChanged()
                        }else{
                           showErrorToast(payload.message)
                            if (!isLoadMore){
                                searchAdaper.dataList.clear()
                                if (!et_search.textStr.isNullOrEmpty()){
                                    val bean = SearchReceiptBean()
                                    bean.title = et_search.textStr
                                    searchAdaper.dataList.add(bean)
                                    searchAdaper.notifyDataSetChanged()
                                }
                            }
                        }
                        if (isLoadMore){
                            dofinishLoadMore()
                        }
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("获取数据失败")
                        if (!isLoadMore){
                            searchAdaper.dataList.clear()
                            if (!et_search.textStr.isNullOrEmpty()){
                                val bean = SearchReceiptBean()
                                bean.title = et_search.textStr
                                searchAdaper.dataList.add(bean)
                                searchAdaper.notifyDataSetChanged()
                            }
                        }else{
                            dofinishLoadMore()
                        }
                    }
                }
    }

    private fun setDelectIcon(enable: Boolean) {
        if (enable){
            iv_del.visibility = View.VISIBLE
        }else{
            iv_del.visibility = View.INVISIBLE
        }
    }

    inner class ResultBillHolder(itemView: View) : RecyclerHolder<SearchReceiptBean>(itemView) {
        val tv_search_title = itemView.findViewById<TextView>(R.id.tv_search_title)
        val tv_search_duty = itemView.findViewById<TextView>(R.id.tv_search_duty)
        override fun setData(view: View, data: SearchReceiptBean, position: Int) {
            if (null == data) return
            tv_search_title.text = data.title
            tv_search_duty.text = data.tax_no
            if (data.tax_no.isNullOrEmpty()){
                tv_search_duty.setVisible(false)
            }else{
                tv_search_duty.setVisible(true)
            }
        }

    }

    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = true
        config.autoLoadMoreEnable = true
        config.scrollContentWhenLoaded = true
        config.disableContentWhenRefresh = true
        config.refreshEnable = false
        return config
    }

    override fun doRefresh() {

    }

    override fun doLoadMore() {
        requestData(et_search.textStr,true)
    }

    fun dofinishLoadMore() {
        if (this::refresh.isLateinit && refresh.isLoading) {
            refresh.finishLoadMore()
        }
    }
    companion object {
        fun invokeForResult(context:Fragment,requestCode:Int,searchKey:String){
            val intent = Intent(context.activity,BillHeadSearchActivity::class.java)
            intent.putExtra("searchKey",searchKey)
            context.startActivityForResult(intent,requestCode)
        }

        fun invokeForResult(context:Context){

        }
    }
}