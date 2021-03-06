package com.sogukj.pe.module.dataSource.lawcase

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.bumptech.glide.Glide
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.bean.LawCaseHisInfo
import com.sogukj.pe.bean.LawSearchResultInfo
import com.sogukj.pe.module.dataSource.lawcase.presenter.LawSearchPresenter
import com.sogukj.pe.peUtils.Store
import kotlinx.android.synthetic.main.fragment_law_search.*
import kotlinx.android.synthetic.main.law_search_title.*
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.startActivity

/**
 * Created by CH-ZH on 2018/9/10.
 */
class LawSearchFragment : LawSearchBaseFragment(),LawSearchCallBack, TextWatcher {
    private var type : Int  = 1
    private var searchKey : String = ""
    private var presenter:LawSearchPresenter ? = null
    private lateinit var resultAdapter: LawSearchAdapter
    private var searchData  = ArrayList<LawSearchResultInfo>()
    private var realType : Int = 1
    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = true
        config.autoLoadMoreEnable = true
        config.disableContentWhenRefresh = true
        return config
    }

    override val containerViewId: Int
        get() = R.layout.fragment_law_search

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments!!.getInt("type", 1)
        realType = arguments!!.getInt("type",1)
        searchKey = arguments!!.getString("search_key")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        presenter = LawSearchPresenter(activity!!,this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initData()
        bindListener()
    }

    override fun onFragmentFirstVisible() {

    }

    override fun onFragmentVisibleChange(isVisible: Boolean) {
        realType = type
        val realSearchKey = XmlDb.open(activity!!).get("searchKey", searchKey)
        if (!realSearchKey.equals(searchKey)){
            searchKey = realSearchKey
        }
        XmlDb.open(activity!!).set("realType",realType)
        if (isVisible){
            getSearchData()
            searchResultListener()
        }
    }

    private fun initData() {
        Glide.with(ctx)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
        resultAdapter = LawSearchAdapter(searchData)
        recycler_view.layoutManager = LinearLayoutManager(context)
        if (null != activity!!.et_search.textStr){
            searchKey = activity!!.et_search.textStr
        }
        recycler_view.adapter = resultAdapter
        if (!XmlDb.open(activity!!).get("isFirst")){
            if (null != presenter){
                showLoadding()
                presenter!!.doLawSearchRequest(searchKey, type,true)
            }
            XmlDb.open(activity!!).set("isFirst",true)
        }
    }

    private fun getSearchData() {
        if (null != presenter){
            showLoadding()
            presenter!!.doLawSearchRequest(searchKey, realType,true)
        }
    }

    private fun getSearchData(searchType:Int) {
        if (null != presenter){
            showLoadding()
            presenter!!.doLawSearchRequest(searchKey, searchType,true)
        }
    }

    private fun showLoadding(){
        if (null != view_recover){
            view_recover.visibility = View.VISIBLE
        }
    }

    private fun goneLoadding(){
        if (null != view_recover){
            view_recover.visibility = View.INVISIBLE
        }
    }

    private fun bindListener() {
        activity!!.et_search.addTextChangedListener(this)
        activity!!.iv_del.setOnClickListener {
            //删除搜索
            setDelectIcon(false)
            activity!!.et_search.setHint(R.string.search_keyword)
            activity!!.et_search.setText("")
        }
    }

    private fun searchResultListener(){
        activity!!.et_search.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchKey = activity!!.et_search.textStr
                if (null != presenter){
                    Utils.closeInput(activity!!,activity!!.et_search)
                    XmlDb.open(activity!!).set("searchKey",searchKey)
                    val searchType = XmlDb.open(activity!!).get("realType", type)
                    getSearchData(searchType)
                }
                true
            }
            false
        }
    }

    override fun doRefresh() {
        if (null != activity!!.et_search){
            searchKey = activity!!.et_search.textStr
        }
        if (null != presenter){
            presenter!!.doLawSearchRequest(searchKey, realType,true)
        }
    }

    override fun doLoadMore() {
        if (null != activity!!.et_search){
            searchKey = activity!!.et_search.textStr
        }
        if (null != presenter){
            presenter!!.doLawSearchRequest(searchKey, realType,false)
        }
    }

    private fun setDelectIcon(enable: Boolean) {
        if (enable){
            activity!!.iv_del.visibility = View.VISIBLE
        }else{
            activity!!.iv_del.visibility = View.INVISIBLE
        }
    }
    var inputTime = 0L
    private val INTERVAL = 300 //输入间隔时间
    override fun afterTextChanged(s: Editable?) {
        if (activity!!.et_search.text.length > 0){
            setDelectIcon(true)
        }else{
            setDelectIcon(false)
            activity!!.et_search.setHint(R.string.search_keyword)
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        if (!activity!!.et_search.isCursorVisible){
            activity!!.et_search.isCursorVisible = true
        }
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    private var inputRunnable:Runnable = object : Runnable{
        override fun run() {
            val inval = System.currentTimeMillis() - inputTime
            if (inval < INTERVAL){
                activity!!.et_search.postDelayed(this,INTERVAL - inval)
            }else{
                requestData(activity!!.et_search.text.toString())
            }
        }
    }

    private fun requestData(query: String) {
        searchKey = activity!!.et_search.textStr
        if (null != presenter){
            showLoadding()
            presenter!!.doLawSearchRequest(searchKey, type,true)
        }
    }

    override fun refreshLawList(it: List<LawSearchResultInfo>?, total: Any) {
        if (null != it && it.size > 0){
            if (null != iv_empty){
                iv_empty.visibility = View.INVISIBLE
            }
            if (null != recycler_view){
                recycler_view.visibility = View.VISIBLE
            }
            setTotalCountEnable(true)
            searchData.clear()
            searchData.addAll(it)
            resultAdapter.notifyDataSetChanged()
            if (null != tv_total && null != total){
                tv_total.text = (total as Double).toInt().toString()
            }
        }else{
            if (null != iv_empty){
                iv_empty.visibility = View.VISIBLE
            }
            if (null != recycler_view){
                recycler_view.visibility = View.INVISIBLE
            }
            setTotalCountEnable(false)
        }
    }

    fun setTotalCountEnable(enable:Boolean){
        if (enable){
            if (null != ll_total){
                ll_total.visibility = View.VISIBLE
            }
            if (null != view_cut){
                view_cut.visibility = View.VISIBLE
            }
        }else{
            if (null != ll_total){
                ll_total.visibility = View.GONE
            }
            if (null != view_cut){
                view_cut.visibility = View.GONE
            }
        }
    }

    override fun loadMoreData(it: List<LawSearchResultInfo>?) {
        searchData.addAll(it!!)
        resultAdapter.notifyDataSetChanged()
    }

    override fun loadError() {
        goneLoadding()
        if (null != iv_empty){
            iv_empty.visibility = View.VISIBLE
        }
        setTotalCountEnable(false)
    }

    override fun dofinishRefresh() {
        goneLoadding()
        if (null != refresh && refresh.isRefreshing) {
            refresh.finishRefresh()
        }
    }

    override fun dofinishLoadMore() {
        if (null != refresh && refresh.isLoading) {
            refresh.finishLoadMore(0)
        }
    }

    internal inner class LawSearchAdapter(private val datas: List<LawSearchResultInfo>) : RecyclerView.Adapter<LawSearchAdapter.LawHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LawHolder =
                LawHolder(LayoutInflater.from(context).inflate(R.layout.item_law_result, parent, false))

        @SuppressLint("StringFormatMatches")
        override fun onBindViewHolder(holder: LawHolder, position: Int) {
            val resultInfo = datas[position]
            if (null == resultInfo) return
            holder.tv_title.text = replaceText("${resultInfo.title}")
            holder.tv_content.text = "${resultInfo.fwzh}/${resultInfo.sxx}/${resultInfo.fbrq}/${resultInfo.ssrq}"

            holder.itemView.setOnClickListener {
                var hisInfo = LawCaseHisInfo()
                var kind = ""
                when(type){
                    1 -> {
                        kind = "党内法规"
                    }
                    2 -> {
                        kind = "部门规章"
                    }
                    3 -> {
                        kind = "行政法规"
                    }
                    4 -> {
                        kind = "法律"
                    }
                    5 -> {
                        kind = "法规规章"
                    }
                    6 -> {
                        kind = "行业规定"
                    }
                    7 -> {
                       kind = "团体规定"
                    }
                    8 -> {
                        kind = "司法解释"
                    }
                }
                startActivity<LawResultDetailActivity>(Extras.DATA to resultInfo.href, Extras.TITLE to kind)
                hisInfo.kind = kind
                hisInfo.title = if (null == resultInfo.title){""}else{resultInfo.title}
                hisInfo.hao = if (null == resultInfo.fwzh){""}else{resultInfo.fwzh}
                hisInfo.href = if (null == resultInfo.href){""}else{resultInfo.href}
                hisInfo.key_word = searchKey
                if (null != resultInfo.fbrq && resultInfo.fbrq.contains("发")){
                    val split = resultInfo.fbrq.split("发")
                    if (null != split && split.size > 1){
                        hisInfo.time = split[0]
                    }
                }
                val lawHis = Store.store.getLawHis(activity!!)
                if (null != lawHis && lawHis.size > 0){
                    var index = 0
                    for (i in lawHis){
                        if (resultInfo.href.equals(i.href)){
                            index ++
                        }
                    }
                    if (index == 0){
                        lawHis.add(hisInfo)
                        Store.store.saveLawtHis(activity!!,lawHis)
                    }
                }else{
                    val realHis = ArrayList<LawCaseHisInfo>()
                    realHis.add(hisInfo)
                    Store.store.saveLawtHis(activity!!,realHis)
                }
            }
        }

        override fun getItemCount(): Int = datas.size

        internal inner class LawHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tv_title: TextView
            val tv_content: TextView

            init {
                tv_title = itemView.findViewById<TextView>(R.id.tv_title)
                tv_content = itemView.findViewById<TextView>(R.id.tv_content)
            }
        }
    }

    private fun replaceText(str: String): SpannableString {
        val spannable = SpannableString(str)
        searchKey.let {
            str.contains(it).yes {
                spannable.setSpan(ForegroundColorSpan(Color.parseColor("#1787FB"))
                        , str.indexOf(it), str.indexOf(it) + it.length,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            }
        }
        return spannable
    }

    companion object {
        fun newInstance(type : Int,search_key:String) : LawSearchFragment {
            val fragment = LawSearchFragment()
            val bundle = Bundle()
            bundle.putInt("type",type)
            bundle.putString("search_key",search_key)
            fragment.arguments = bundle
            return fragment
        }
    }
}