package com.sogukj.pe.module.im

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nim.uikit.business.uinfo.UserInfoHelper
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.search.model.MsgIndexRecord
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.extraDelegate
import com.sogukj.pe.baselibrary.Extended.setDrawable
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.CloudFileBean
import com.sogukj.pe.bean.DepartmentBean
import com.sogukj.pe.bean.PlListInfos
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.module.im.clouddish.MineFileActivity
import com.sogukj.pe.module.lpassistant.PolicyExpressDetailActivity
import com.sogukj.pe.module.other.OnlinePreviewActivity
import com.sogukj.pe.peUtils.FileTypeUtils
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.presenter.ImSearchCallBack
import com.sogukj.pe.presenter.ImSearchPresenter
import com.sogukj.pe.widgets.CircleImageView
import com.sogukj.service.SoguApi
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter
import kotlinx.android.synthetic.main.activity_im_search.*
import kotlinx.android.synthetic.main.search_header.*
import kotlinx.android.synthetic.main.search_his.*
import kotlinx.android.synthetic.main.search_result.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.startActivity
import org.json.JSONObject
import java.util.*

/**
 * Created by CH-ZH on 2018/8/20.
 */
class ImSearchResultActivity : BaseActivity(), TextWatcher, ImSearchCallBack {
    override fun refreshPlList(infos: List<PlListInfos>) {
        setResultVisibility(true)
        plResultAdapter.dataList.clear()
        plResultAdapter.dataList.addAll(infos)
        search_recycler_view.adapter = plResultAdapter
        plResultAdapter.notifyDataSetChanged()

        val his = Store.store.getPlHis(this)
        if (null != his && his.size > 0) {
            var index = 0
            for (i in his) {
                if (searchKey.equals(i)) {
                    index++
                }
            }
            if (index == 0) {
                his.add(searchKey)
                Store.store.savePltHis(this, his)
            }
        } else {
            val keys = ArrayList<String>()
            keys.add(searchKey)
            Store.store.savePltHis(this, keys)
        }
    }

    override fun loadMoreData(moreData: List<PlListInfos>) {
        setResultVisibility(true)
        plResultAdapter.dataList.addAll(moreData)
        plResultAdapter.notifyDataSetChanged()
    }

    override fun setContractData(param: List<DepartmentBean>) {
        setResultVisibility(true)
        searchWithName(param)
    }

    private fun searchWithName(param: List<DepartmentBean>) {
        val result = ArrayList<UserBean>()
        param.forEach {
            it.data.let {
                it?.forEach {
                    if (it.name.contains(searchKey)) {//&& it.user_id != mine?.uid
                        result.add(it)
                    }
                }
            }
        }
        resultData.clear()
        resultData.addAll(result)
        resultAdapter.notifyDataSetChanged()
        if (resultData.size <= 0) {
            setEmpty(true)
        } else {
            setEmpty(false)
        }
    }

    override fun setFullData(param: List<MsgIndexRecord>) {
        setResultVisibility(true)
        searchResult.dataList.addAll(param)
        search_recycler_view.adapter = searchResult
    }

    private fun setResultVisibility(enable: Boolean) {
        if (enable) {
            ll_search_result.visibility = View.VISIBLE
            search_his.visibility = View.INVISIBLE
        } else {
            ll_search_result.visibility = View.INVISIBLE
            search_his.visibility = View.VISIBLE
            if (0 == type) {
                searchHis = Store.store.getSearchHis(this)
                searchHis!!.reverse()
                if (null != searchAdapter) {
                    searchAdapter!!.notifyDataChanged()
                }
                if (null != searchHis && searchHis!!.size > 0) {
                    ll_empty_his.visibility = View.INVISIBLE
                } else {
                    ll_empty_his.visibility = View.VISIBLE
                }
            } else if (1 == type) {
                contractHis = Store.store.getContractHis(this)
                contractHis!!.reverse()
                if (null != contractAdapter) {
                    contractAdapter!!.notifyDataChanged()
                }
                if (null != contractHis && contractHis!!.size > 0) {
                    ll_empty_his.visibility = View.INVISIBLE
                } else {
                    ll_empty_his.visibility = View.VISIBLE
                }
            } else if (2 == type) {
                plHis = Store.store.getPlHis(this)
                plHis!!.reverse()
                if (null != plAdapter) {
                    plAdapter!!.notifyDataChanged()
                }
                if (null != plHis && plHis!!.size > 0) {
                    ll_empty_his.visibility = View.INVISIBLE
                } else {
                    ll_empty_his.visibility = View.VISIBLE
                }
            } else if (3 == type) {
                cloudFileHis = Store.store.getCloudHis(this)
                cloudFileHis!!.reverse()
                if (null != cloudHisAdapter) {
                    cloudHisAdapter!!.notifyDataChanged()
                }
                if (null != cloudFileHis && cloudFileHis!!.size > 0) {
                    ll_empty_his.visibility = View.INVISIBLE
                } else {
                    ll_empty_his.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun clearData() {
        searchResult.dataList.clear()
    }

    override fun setEmpty(isEmty: Boolean) {
        setResultVisibility(true)
        if (isEmty) {
            search_recycler_view.visibility = View.GONE
            search_iv_empty.visibility = View.VISIBLE
        } else {
            search_recycler_view.visibility = View.VISIBLE
            search_iv_empty.visibility = View.GONE
        }
    }

    lateinit var searchResult: RecyclerAdapter<MsgIndexRecord>
    private lateinit var resultAdapter: ContactAdapter
    private lateinit var plResultAdapter: RecyclerAdapter<PlListInfos>
    private lateinit var cloudResultAdapter: RecyclerAdapter<CloudFileBean>
    private val resultData = ArrayList<UserBean>()//搜索结果
    private var searchKey = ""
    private val INTERVAL = 300 //输入间隔时间
    private var presenter: ImSearchPresenter? = null
    private var searchHis: ArrayList<String>? = null
    private var contractHis: ArrayList<UserBean>? = null
    private var plHis: ArrayList<String>? = null
    private var cloudFileHis: ArrayList<String>? = null
    private var type = 0  //0:消息 1：联系人 2:政策速递 3 : 云盘文件搜索
    private var searchAdapter: SearchAdapter? = null
    private var contractAdapter: ContractAdapter? = null
    private var plAdapter: PlAdapter? = null
    private var cloudHisAdapter: CloudHisAdapter? = null
    private var page = 1
    private val isBusinessFile: Boolean? by extraDelegate(Extras.FLAG, null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_im_search)
        Utils.setWindowStatusBarColor(this, R.color.white)
        initData()
        bindListener()
    }

    private fun initData() {
        type = intent.getIntExtra("type", 0)
        presenter = ImSearchPresenter(this, this)
        et_search.filters = Utils.getFilter(context)
        searchHis = Store.store.getSearchHis(this)
        contractHis = Store.store.getContractHis(this)
        plHis = Store.store.getPlHis(this)
        cloudFileHis = Store.store.getCloudHis(this)
        if (type == 0) {
            refresh.isEnableRefresh = false
            refresh.isEnableLoadMore = false
            if (null != searchHis && searchHis!!.size > 0) {
                setResultVisibility(false)
                ll_empty_his.visibility = View.INVISIBLE
            } else {
                ll_empty_his.visibility = View.VISIBLE
            }
            initSearchHisData()
        } else if (type == 1) {
            refresh.isEnableRefresh = false
            refresh.isEnableLoadMore = false
            initContractHisData()
        } else if (type == 2) {
            refresh.isEnableRefresh = false
            refresh.isEnableLoadMore = true
            refresh.isEnableAutoLoadMore = true
            refresh.setRefreshFooter(ClassicsFooter(this))
            if (null != plHis && plHis!!.size > 0) {
                setResultVisibility(false)
                ll_empty_his.visibility = View.INVISIBLE
            } else {
                ll_empty_his.visibility = View.VISIBLE
            }
            initPlHisData()
        } else if (type == 3) {
            refresh.isEnableRefresh = false
            refresh.isEnableLoadMore = true
            refresh.isEnableAutoLoadMore = true
            refresh.setRefreshFooter(ClassicsFooter(this))
            if (null != cloudFileHis && cloudFileHis!!.size > 0) {
                setResultVisibility(false)
                ll_empty_his.visibility = View.INVISIBLE
            } else {
                ll_empty_his.visibility = View.VISIBLE
            }
            initCloudFileHisData()
        }
        if (type == 0) {
            searchResult = RecyclerAdapter(this) { _adapter, parent, _ ->
                SearchResultHolder(_adapter.getView(R.layout.item_msg_index, parent))
            }
        } else if (type == 1) {
            initContractResult()
        } else if (type == 2) {
            initLpResult()
        } else if (type == 3) {
            initCloudFileResult()
        }
    }

    private fun initCloudFileHisData() {
        cloudHisAdapter = CloudHisAdapter()
        tfl.adapter = cloudHisAdapter
    }

    private fun initPlHisData() {
        plAdapter = PlAdapter()
        tfl.adapter = plAdapter
    }

    private fun initContractHisData() {
        if (null != contractHis && contractHis!!.size > 0) {
            setResultVisibility(false)
            ll_empty_his.visibility = View.INVISIBLE
        } else {
            contractAdapter = ContractAdapter()
            ll_empty_his.visibility = View.VISIBLE
        }
        initContractHis()
    }

    private fun initContractHis() {
        contractAdapter = ContractAdapter()
        tfl.adapter = contractAdapter
    }

    inner class SearchAdapter : TagAdapter<String>(searchHis) {
        override fun getView(parent: FlowLayout?, position: Int, key: String?): View {
            val itemView = View.inflate(this@ImSearchResultActivity, R.layout.search_his_item, null)
            val tv_item = itemView.findViewById<TextView>(R.id.tv_item)
            tv_item.text = key
            return itemView
        }
    }

    inner class ContractAdapter : TagAdapter<UserBean>(contractHis) {
        override fun getView(parent: FlowLayout?, position: Int, userBean: UserBean?): View {
            val itemView = View.inflate(this@ImSearchResultActivity, R.layout.search_his_item, null)
            val tv_item = itemView.findViewById<TextView>(R.id.tv_item)
            tv_item.text = userBean!!.name
            return itemView
        }
    }

    inner class PlAdapter : TagAdapter<String>(plHis) {
        override fun getView(parent: FlowLayout?, position: Int, key: String?): View {
            val itemView = View.inflate(this@ImSearchResultActivity, R.layout.search_his_item, null)
            val tv_item = itemView.findViewById<TextView>(R.id.tv_item)
            tv_item.text = key
            return itemView
        }
    }

    inner class CloudHisAdapter : TagAdapter<String>(cloudFileHis) {
        override fun getView(parent: FlowLayout?, position: Int, key: String?): View {
            val itemView = View.inflate(this@ImSearchResultActivity, R.layout.search_his_item, null)
            val tv_item = itemView.findViewById<TextView>(R.id.tv_item)
            if (!key.isNullOrEmpty()) {
                tv_item.text = key
            }
            return itemView
        }

    }

    private fun initContractResult() {
        resultAdapter = ContactAdapter(resultData)
        search_recycler_view.layoutManager = LinearLayoutManager(context)
        search_recycler_view.adapter = resultAdapter
    }

    private fun initLpResult() {
        plResultAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            LpResultHolder(_adapter.getView(R.layout.pl_search_item, parent))
        }

    }

    private fun initCloudFileResult() {
        search_recycler_view.layoutManager = LinearLayoutManager(context)
        search_recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        cloudResultAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            CloudResultHolder(_adapter.getView(R.layout.item_cloud_result, parent))
        }
        search_recycler_view.adapter = cloudResultAdapter
    }

    override fun onDestroy() {
        Utils.closeInput(this, et_search)
        super.onDestroy()
    }

    override fun finishLoadMore() {
        if (refresh.isLoading) {
            refresh.finishLoadMore(0)
        }
    }

    private fun initSearchHisData() {
        searchAdapter = SearchAdapter()
        tfl.adapter = searchAdapter
    }

    private fun bindListener() {
        iv_search.setOnClickListener {
            Utils.showSoftInputFromWindow(this, et_search)
        }
        et_search.addTextChangedListener(this)
        et_search.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Utils.closeInput(ctx, et_search)
                true
            }
            false
        }
        iv_del.setOnClickListener {
            //删除搜索
            setDelectIcon(false)
            et_search.setHint(R.string.search)
            et_search.setText("")
            setResultVisibility(false)
        }

        tv_cancel.setOnClickListener {
            //取消
            onBackPressed()
        }
        if (type == 0) {
            searchResult.onItemClick = { v, position ->
                val record = searchResult.dataList[position]
                when (record.sessionType) {
                    SessionTypeEnum.P2P -> {
                        NimUIKit.startP2PSession(this, record.sessionId, record.message)
                    }
                    SessionTypeEnum.Team -> {
                        NimUIKit.startTeamSession(this, record.sessionId, record.message)
                    }
                }

                val his = Store.store.getSearchHis(this)
                if (null != his && his.size > 0) {
                    var index = 0
                    for (i in his.iterator()) {
                        if (searchKey.equals(i)) {
                            index++
                        }
                    }
                    if (index == 0) {
                        his.add(searchKey)
                        Store.store.saveSearchHis(this, his)
                    }
                } else {
                    val keys = ArrayList<String>()
                    keys.add(searchKey)
                    Store.store.saveSearchHis(this, keys)
                }
            }
        }
        if (type == 2) {
            plResultAdapter.onItemClick = { v, position ->
                val info = plResultAdapter.dataList[position]
                if (null != info) {
                    PolicyExpressDetailActivity.invoke(this, info.id)
                }
            }
        }

        if (type == 3) {
            cloudResultAdapter.onItemClick = { v, position ->
                //预览页面
                val fileBean = cloudResultAdapter.dataList[position]
                if (null != fileBean) {
                    //预览
                    if (fileBean.file_type == "Folder") {
                        //文件夾
                        val b = isBusinessFile ?: false
                        startActivity<MineFileActivity>(Extras.TITLE to fileBean.file_name, Extras.DIR to "/${fileBean.file_path}",
                                Extras.TYPE to b)
                    } else {
                        OnlinePreviewActivity.start(this@ImSearchResultActivity, fileBean.preview_url, fileBean.file_name, false)
                    }
                }
            }
        }

        tfl.setOnTagClickListener { view, position, parent ->
            if (type == 0) {
                if (null != searchHis && searchHis!!.size > 0) {
                    val key = searchHis!![position]
                    if (null != presenter) {
                        presenter!!.doRequest(key)
                    }
                    et_search.setText(key)
                    et_search.setSelection(key.length)
                }
            } else if (type == 1) {
                if (null != contractHis && contractHis!!.size > 0) {
                    PersonalInfoActivity.start(this@ImSearchResultActivity, contractHis!![position], null)
                }
            } else if (type == 2) {
                if (null != plHis && plHis!!.size > 0) {
                    val key = plHis!![position]
                    if (null != presenter) {
                        presenter!!.getPlExpressResultData(true, key)
                    }
                    et_search.setText(key)
                    et_search.setSelection(key.length)
                }
            } else if (type == 3) {
                if (null != cloudFileHis && cloudFileHis!!.size > 0) {
                    val key = cloudFileHis!![position]
                    getCloudSearchData(false, key)
                    et_search.setText(key)
                    et_search.setSelection(key.length)
                }
            }
            true
        }

        tv_his.setOnClickListener {
            if (type == 0) {
                if (null != searchHis && searchHis!!.size > 0) {
                    Store.store.clearSearchHis(this)
                    searchHis = Store.store.getSearchHis(this)
                    if (null != searchAdapter) {
                        searchAdapter!!.notifyDataChanged()
                    }
                }
                ll_empty_his.visibility = View.VISIBLE
            } else if (type == 1) {
                if (null != contractHis && contractHis!!.size > 0) {
                    Store.store.clearContractHis(this)
                    contractHis = Store.store.getContractHis(this)
                    if (null != contractAdapter) {
                        contractAdapter!!.notifyDataChanged()
                    }
                }
                ll_empty_his.visibility = View.VISIBLE
            } else if (type == 2) {
                if (null != plHis && plHis!!.size > 0) {
                    Store.store.clearPlHis(this)
                    plHis = Store.store.getPlHis(this)
                    if (null != plAdapter) {
                        plAdapter!!.notifyDataChanged()
                    }
                }
                ll_empty_his.visibility = View.VISIBLE
            } else if (type == 3) {
                if (null != cloudFileHis && cloudFileHis!!.size > 0) {
                    Store.store.clearCloudHis(this)
                    cloudFileHis = Store.store.getCloudHis(this)
                    if (null != cloudHisAdapter) {
                        cloudHisAdapter!!.notifyDataChanged()
                    }
                }
                ll_empty_his.visibility = View.VISIBLE
            }
        }


        refresh.setOnLoadMoreListener {
            doLoadMore()
        }
    }

    private fun doLoadMore() {
        if (type == 2) {
            if (null != presenter) {
                presenter!!.getPlExpressResultData(false, searchKey)
            }
        } else if (type == 3) {
            getCloudSearchData(true, searchKey)
        }
    }

    inner class SearchResultHolder(itemView: View) : RecyclerHolder<MsgIndexRecord>(itemView) {
        val msgIcon = convertView.findViewById<CircleImageView>(R.id.msg_icon) as CircleImageView
        val tvTitle = convertView.findViewById<TextView>(R.id.tv_title) as TextView
        val tvDate = convertView.findViewById<TextView>(R.id.tv_date) as TextView
        val tvTitleMsg = convertView.findViewById<TextView>(R.id.tv_title_msg) as TextView
        val tvNum = convertView.findViewById<TextView>(R.id.tv_num) as TextView
        @SuppressLint("SetTextI18n", "NewApi")
        override fun setData(view: View, data: MsgIndexRecord, position: Int) {
            if (null == data || null == data.message) return
            tvNum.setVisible(false)
            val titleName = UserInfoHelper.getUserTitleName(data.sessionId, data.sessionType)
            tvTitle.text = titleName
            val content = data.message.content
            if (data.sessionType == SessionTypeEnum.P2P) {
                tvTitle.setDrawable(tvTitle, -1, getDrawable(R.mipmap.ic_flag_qy))
                tvTitleMsg.text = Html.fromHtml(content.replace(searchKey, "<font color='#1787fb'>$searchKey</font>"))
                val userInfo = NimUIKit.getUserInfoProvider().getUserInfo(data.sessionId)
                userInfo?.let {
                    Glide.with(this@ImSearchResultActivity)
                            .load(it.avatar)
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                    val ch = userInfo.name.first()
                                    msgIcon.setChar(ch)
                                    return false
                                }

                                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                    return false
                                }

                            })
                            .into(msgIcon)
                }
            } else if (data.sessionType == SessionTypeEnum.Team) {
                val fromNick = if (data.message.fromNick.isNullOrEmpty()) "" else "${data.message.fromNick}:"
                tvTitleMsg.text = Html.fromHtml("$fromNick${content.replace(searchKey, "<font color='#1787fb'>$searchKey</font>")}")
                val team = NimUIKit.getTeamProvider().getTeamById(data.sessionId)
                team?.let {
                    Glide.with(this@ImSearchResultActivity)
                            .load(it.icon)
                            .apply(RequestOptions().error(R.drawable.im_team_default))
                            .into(msgIcon)

                    val extServer = it.extServer
                    if (null != extServer && "" != extServer && """"{}"""" != extServer) {
                        val jsonObject = JSONObject(extServer)
                        val flag = jsonObject.getString("grouptype")
                        when (flag) {
                            "0" -> {
                                //全员
                                tvTitle.setDrawable(tvTitle, 2, getDrawable(R.mipmap.ic_flag_qy))
                            }
                            "1" -> {
                                //部门
                                tvTitle.setDrawable(tvTitle, 2, getDrawable(R.mipmap.ic_flag_bm))
                            }
                            else -> {
                                //内部
                                tvTitle.setDrawable(tvTitle, 2, getDrawable(R.mipmap.ic_flag_nb))
                            }
                        }
                    } else {
                        //内部
                        tvTitle.setDrawable(tvTitle, 2, getDrawable(R.mipmap.ic_flag_nb))
                    }
                }
            }
            try {
                val time = Utils.getTime(data.time, "yyyy-MM-dd HH:mm:ss")
                tvDate.text = Utils.formatDingDate(time)
            } catch (e: Exception) {
            }
        }
    }

    inner class CloudResultHolder(itemView: View) : RecyclerHolder<CloudFileBean>(itemView) {
        val iv_head = itemView.find<ImageView>(R.id.iv_head)
        val tv_name = itemView.find<TextView>(R.id.tv_name)
        val tv_dir = itemView.find<TextView>(R.id.tv_dir)
        override fun setData(view: View, data: CloudFileBean, position: Int) {
            if (null == data) return
            if (data.file_type == "Folder") {
                iv_head.setImageResource(R.drawable.folder_zip)
            } else {
                iv_head.imageResource = FileTypeUtils.getFileType(data.file_name).icon
            }
            tv_name.text = data.file_name
            tv_dir.text = data.file_path
        }

    }

    inner class LpResultHolder(itemView: View) : RecyclerHolder<PlListInfos>(itemView) {
        val title1 = convertView.findViewById<TextView>(R.id.tv_title1)
        val title2 = convertView.findViewById<TextView>(R.id.tv_title2)
        val tag1 = convertView.findViewById<TextView>(R.id.tv_tag1)
        val tag2 = convertView.findViewById<TextView>(R.id.tv_tag2)
        val time = convertView.findViewById<TextView>(R.id.tv_time)
        val tv_time2 = convertView.findViewById<TextView>(R.id.tv_time2)
        val image = convertView.findViewById<ImageView>(R.id.iv_image)
        val rl_normal = convertView.findViewById<RelativeLayout>(R.id.rl_normal)
        val rl_image = convertView.findViewById<RelativeLayout>(R.id.rl_image)
        override fun setData(view: View, plListInfos: PlListInfos, position: Int) {
            if (null != plListInfos.img && !"".equals(plListInfos.img)) {
                rl_image.visibility = View.VISIBLE
                rl_normal.visibility = View.GONE
                if (null != plListInfos.title) {
                    title2.text = plListInfos.title
                }
                if (null != plListInfos.img) {
                    Glide.with(context).load(plListInfos.img).apply(RequestOptions.bitmapTransform(RoundedCorners(10)))
                            .thumbnail(0.5f).into(image)
                }
                if (null != plListInfos.source) {
                    tag2.text = plListInfos.source
                } else {
                    tag2.visibility = View.INVISIBLE
                }
            } else {
                rl_image.visibility = View.GONE
                rl_normal.visibility = View.VISIBLE
                if (null != plListInfos.title) {
                    title1.text = plListInfos.title
                }

                if (null != plListInfos.source) {
                    tag1.text = plListInfos.source
                } else {
                    tag1.visibility = View.INVISIBLE
                }
            }

            if (null != plListInfos.time) {
                time.text = plListInfos.time
                tv_time2.text = plListInfos.time
            }
        }

    }

    internal inner class ContactAdapter(private val datas: List<UserBean>) : RecyclerView.Adapter<ContactAdapter.ContactHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder =
                ContactHolder(LayoutInflater.from(context).inflate(R.layout.item_team_organization_chlid, parent, false))

        override fun onBindViewHolder(holder: ContactHolder, position: Int) {
            val userBean = datas[position]
            if (userBean.headImage().isNullOrEmpty()) {
                val ch = userBean.name.first()
                holder.userImg.setChar(ch)
            } else {
                Glide.with(this@ImSearchResultActivity)
                        .load(userBean.headImage())
                        .apply(RequestOptions().error(R.drawable.nim_avatar_default).placeholder(R.drawable.nim_avatar_default))
                        .into(holder.userImg)
            }
            var name = userBean.name
            if (searchKey.isNotEmpty()) {
                name = name.replaceFirst(searchKey, "<font color='#1787fb'>$searchKey</font>")
            }
            holder.userName.text = Html.fromHtml(name)
            holder.userPosition.text = userBean.position
            holder.selectIcon.visibility = View.GONE

            holder.itemView.setOnClickListener {
                //查看详情
                PersonalInfoActivity.start(this@ImSearchResultActivity, userBean, null)
                val contractHis = Store.store.getContractHis(this@ImSearchResultActivity)
                if (null != contractHis && contractHis.size > 0) {
                    var index = 0
                    for (i in contractHis) {
                        if (userBean.name.equals(i.name)) {
                            index++
                        }
                    }
                    if (index == 0) {
                        contractHis.add(userBean)
                        Store.store.saveContractHis(this@ImSearchResultActivity, contractHis)
                    }
                } else {
                    val infos = ArrayList<UserBean>()
                    infos.add(userBean)
                    Store.store.saveContractHis(this@ImSearchResultActivity, infos)
                }
            }
        }

        override fun getItemCount(): Int = datas.size

        internal inner class ContactHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val selectIcon: ImageView
            val userImg: CircleImageView
            val userName: TextView
            val userPosition: TextView

            init {
                selectIcon = itemView.findViewById(R.id.selectIcon)
                userImg = itemView.findViewById(R.id.userHeadImg)
                userName = itemView.findViewById(R.id.userName)
                userPosition = itemView.findViewById(R.id.userPosition)
            }
        }
    }


    override fun afterTextChanged(s: Editable?) {
        if (et_search.text.length > 0) {
            setDelectIcon(true)
            setResultVisibility(true)
        } else {
            setDelectIcon(false)
            et_search.setHint(R.string.search)
            setResultVisibility(false)
        }
    }

    private fun setDelectIcon(enable: Boolean) {
        if (enable) {
            iv_del.visibility = View.VISIBLE
        } else {
            iv_del.visibility = View.INVISIBLE
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    var inputTime = 0L
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (!et_search.text.toString().isEmpty()) {
            inputTime = System.currentTimeMillis()
            et_search.post(inputRunnable)
        }
    }

    private var inputRunnable: Runnable = object : Runnable {
        override fun run() {
            val inval = System.currentTimeMillis() - inputTime
            if (inval < INTERVAL) {
                et_search.postDelayed(this, INTERVAL - inval)
            } else {
                requestData(et_search.text.toString())
            }
        }
    }

    private fun requestData(query: String) {
        Log.e("TAG", "im search requestData -- query ==" + query)
        searchKey = et_search.text.toString()
        if (null != presenter) {
            if (type == 0) {
                presenter!!.doRequest(query)
            } else if (type == 1) {
                presenter!!.getDepartData()
            } else if (type == 2) {
                presenter!!.getPlExpressResultData(true, query)
            } else if (type == 3) {
                //获取云文件
                if (!query.isNullOrEmpty()) {
                    getCloudSearchData(false, query)
                }
            }
        }
    }

    private fun getCloudSearchData(isLoadMore: Boolean, query: String) {
        if (isLoadMore) {
            page++
        } else {
            page = 1
        }
        SoguApi.getStaticHttp(application)
                .getFileSearchData(page, Store.store.getUser(this)!!.phone, query)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            val infos = payload.payload
                            if (null != infos && infos.size > 0) {
                                setEmpty(false)
                                if (isLoadMore) {
                                    setResultVisibility(true)
                                    cloudResultAdapter.dataList.addAll(infos)
                                    cloudResultAdapter.notifyDataSetChanged()
                                } else {
                                    cloudResultAdapter.dataList.clear()
                                    cloudResultAdapter.dataList.addAll(infos)
                                    cloudResultAdapter.notifyDataSetChanged()
                                    saveCloudHis()
                                }
                            } else {
                                if (!isLoadMore) {
                                    setEmpty(true)
                                }
                            }
                        } else {
                            showErrorToast(payload.message)
                        }
                    }

                    onComplete {
                        if (isLoadMore) {
                            finishLoadMore()
                        }
                    }
                    onError {
                        it.printStackTrace()
                        showErrorToast("获取数据失败")
                        if (isLoadMore) {
                            finishLoadMore()
                        } else {
                            setEmpty(true)
                        }
                    }
                }
    }

    private fun saveCloudHis() {
        val his = Store.store.getCloudHis(this)
        if (null != his && his.size > 0) {
            var index = 0
            for (i in his) {
                if (searchKey.equals(i)) {
                    index++
                }
            }
            if (index == 0) {
                if (!searchKey.isNullOrEmpty()) {
                    his.add(searchKey)
                }
                Store.store.saveCloudtHis(this, his)
            }
        } else {
            val keys = ArrayList<String>()
            if (!searchKey.isNullOrEmpty()) {
                keys.add(searchKey)
            }
            Store.store.saveCloudtHis(this, keys)
        }
    }

    companion object {
        fun invoke(context: Context, type: Int, isBusinessFile: Boolean? = null) {
            var intent = Intent(context, ImSearchResultActivity::class.java)
            intent.putExtra("type", type)
            if (type == 3 && isBusinessFile != null) {
                intent.putExtra(Extras.FLAG, isBusinessFile)
            }
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
