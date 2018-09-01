package com.sogukj.pe.module.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.Html
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nim.uikit.business.recent.TeamMemberAitHelper
import com.netease.nim.uikit.business.uinfo.UserInfoHelper
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.lucene.LuceneService
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.netease.nimlib.sdk.search.model.MsgIndexRecord
import com.scwang.smartrefresh.layout.footer.BallPulseFooter
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.MessageIndexBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.module.approve.ApproveListActivity
import com.sogukj.pe.module.im.ImSearchResultActivity
import com.sogukj.pe.module.other.GongGaoDetailActivity
import com.sogukj.pe.module.other.MessageListActivity
import com.sogukj.pe.module.user.UserActivity
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.OtherService
import com.sogukj.pe.widgets.CircleImageView
import com.sogukj.service.SoguApi
import com.xuexuan.zxing.android.activity.CaptureActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_msg_center.*
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.info
import org.jetbrains.anko.support.v4.ctx
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by qinfei on 17/10/11.
 */
class MainMsgFragment : BaseFragment() {
    var recentList = ArrayList<RecentContact>()
    override val containerViewId: Int
        get() = R.layout.fragment_msg_center

    lateinit var adapter: RecyclerAdapter<Any>
    lateinit var searchResult: RecyclerAdapter<MsgIndexRecord>
    val extMap = HashMap<String, Any>()

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {   // 不在最前端显示 相当于调用了onPause();

        } else {  // 在最前端显示 相当于调用了onResume();
            loadHead()
            add_layout.visibility = View.GONE
        }
    }

    fun loadHead() {
        var header = toolbar_back.getChildAt(0) as CircleImageView
        val user = Store.store.getUser(baseActivity!!)
        if (user?.url.isNullOrEmpty()) {
            val ch = user?.name?.first()
            header.setChar(ch)
        } else {
            Glide.with(ctx)
                    .load(MyGlideUrl(user?.url))
                    .listener(object : RequestListener<Drawable> {
                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            header.setImageDrawable(resource)
                            return true
                        }

                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            val ch = user?.name?.first()
                            header.setChar(ch)
                            return true
                        }
                    })
                    .into(header)
        }
    }

    private fun initSearchView() {
        search_edt.filters = Utils.getFilter(context)
        search_edt.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus || search_edt.textStr.isNotEmpty()) {
                search_hint.visibility = View.GONE
                search_icon.visibility = View.VISIBLE
                delete1.visibility = View.VISIBLE
            } else {
                search_hint.visibility = View.VISIBLE
                search_icon.visibility = View.GONE
                delete1.visibility = View.GONE
                search_edt.clearFocus()
            }
        }
        search_edt.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (search_edt.textStr.isNotEmpty()) {
                    searchKey = search_edt.text.toString()
                    searchWithName()
                }
                true
            } else {
                false
            }
        }
        delete1.setOnClickListener {
            Utils.toggleSoftInput(context, search_edt)
            search_edt.setText("")
            search_edt.clearFocus()
            recycler_view.adapter = adapter
            doRequest()
        }
        search_edt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (search_edt.text.toString().isEmpty()) {
                    searchKey = ""
                    adapter.dataList.clear()
                    adapter.dataList.add(zhushou)
                    adapter.dataList.add(sys_zhushou)
                    adapter.dataList.addAll(recentList)
                    recycler_view.adapter = adapter
                    adapter.notifyDataSetChanged()
                    if (adapter.dataList.size == 0) {
                        recycler_view.visibility = View.GONE
                        iv_empty.visibility = View.VISIBLE
                    } else {
                        recycler_view.visibility = View.VISIBLE
                        iv_empty.visibility = View.GONE
                    }
                }
                if (!search_edt.text.isNullOrEmpty()) {
                    delete1.visibility = View.VISIBLE
                } else {
                    delete1.visibility = View.GONE
                }
            }
        })
    }

    private fun searchWithName() {
        NIMClient.getService(LuceneService::class.java).searchAllSession(searchKey, 20)
                .setCallback(object : RequestCallback<List<MsgIndexRecord>> {
                    override fun onSuccess(param: List<MsgIndexRecord>?) {
                        searchResult.dataList.clear()
                        param?.let {
                            searchResult.dataList.addAll(it)
                            recycler_view.adapter = searchResult
                        }
                        if (searchResult.dataList.isEmpty()) {
                            recycler_view.visibility = View.GONE
                            iv_empty.visibility = View.VISIBLE
                        } else {
                            recycler_view.visibility = View.VISIBLE
                            iv_empty.visibility = View.GONE
                        }
                    }

                    override fun onException(exception: Throwable?) {
                        recycler_view.visibility = View.VISIBLE
                        iv_empty.visibility = View.GONE
                    }

                    override fun onFailed(code: Int) {
                        recycler_view.visibility = View.VISIBLE
                        iv_empty.visibility = View.GONE
                    }
                })
    }

    lateinit var searchKey: String

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()) {
            grantResults
                    .filter { it != PackageManager.PERMISSION_GRANTED }
                    .forEach { return }
            if (requestCode == 200) {
                val openCameraIntent = Intent(context, CaptureActivity::class.java)
                startActivityForResult(openCameraIntent, 0)
            }
        } else {
            showCustomToast(R.drawable.icon_toast_common, "该功能需要相机权限")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar_title.text = "消息"

        loadHead()
//        initSearchView()
        toolbar_back.setOnClickListener {
            if (add_layout.visibility == View.VISIBLE) {
                add_layout.visibility = View.GONE
            }
            val intent = Intent(context, UserActivity::class.java)
            startActivityForResult(intent, 0x789)
        }

        toolbar_menu.setOnClickListener {
            if (add_layout.visibility == View.VISIBLE) {
                add_layout.visibility = View.GONE
            } else {
                add_layout.visibility = View.VISIBLE
                add_layout.setOnClickListener {
                    add_layout.visibility = View.GONE
                }
            }
        }
        toolbar.setOnTouchListener { v, event ->
            if (add_layout.visibility == View.VISIBLE) {
                add_layout.visibility = View.GONE
            }
            true
        }
        start_chat.setOnClickListener {
            add_layout.visibility = View.GONE
            var alreadySelect = ArrayList<UserBean>()
            alreadySelect.add(Store.store.getUser(ctx)!!)
            ContactsActivity.start(ctx, alreadySelect, true, true)
        }
        scan.setOnClickListener {
            add_layout.visibility = View.GONE
            var per = "android.permission.CAMERA"
            if (ContextCompat.checkSelfPermission(ctx, per) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(per), 200)
            } else {
                val openCameraIntent = Intent(context, CaptureActivity::class.java)
                startActivityForResult(openCameraIntent, 0)
            }
        }
        rl_search.setOnClickListener {
            ImSearchResultActivity.invoke(activity!!, 0)
        }
        adapter = RecyclerAdapter(baseActivity!!, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_msg_index, parent)
            object : RecyclerHolder<Any>(convertView) {
                val msgIcon = convertView.findViewById<CircleImageView>(R.id.msg_icon) as CircleImageView
                val tvTitle = convertView.findViewById<TextView>(R.id.tv_title) as TextView
                val tvDate = convertView.findViewById<TextView>(R.id.tv_date) as TextView
                val tvTitleMsg = convertView.findViewById<TextView>(R.id.tv_title_msg) as TextView
                val tvNum = convertView.findViewById<TextView>(R.id.tv_num) as TextView
                val topTag = convertView.findViewById<ImageView>(R.id.topTag)
                val tv_flag = convertView.findViewById<TextView>(R.id.tv_flag)
                @SuppressLint("SetTextI18n")
                override fun setData(view: View, data: Any, position: Int) {
                    if (data is MessageIndexBean) {
                        tv_flag.visibility = View.GONE
                        if (!TextUtils.isEmpty(data.title))
                            tvTitleMsg.text = data.title
                        else
                            tvTitleMsg.text = "暂无数据"
                        tvDate.text = data.time
                        tvNum.text = "${data.count}"
                        tvTitle.text = "系统消息助手"
                        if (data.count > 0) {
                            tvNum.visibility = View.VISIBLE
                        } else {
                            tvNum.visibility = View.INVISIBLE
                        }
                        if (data.flag == 1) {
                            tvTitle.text = "审批消息助手"
                            msgIcon.imageResource = R.drawable.ic_sys_alert
                        } else if (data.flag == 2) {
                            tvTitle.text = "系统消息助手"
                            msgIcon.imageResource = R.drawable.ic_msg_alert
                        }
                        topTag.setVisible(false)
                    } else if (data is RecentContact) {
                        val titleName = UserInfoHelper.getUserTitleName(data.contactId, data.sessionType)
                        tvTitle.text = titleName
                        topTag.setVisible(data.tag == RECENT_TAG_STICKY)
                        if (data.sessionType == SessionTypeEnum.P2P) {
                            tv_flag.visibility = View.GONE
                            val value = data.msgStatus.value
                            when (value) {
                                3 -> tvTitleMsg.text = Html.fromHtml("<font color='#a0a4aa'>[已读]</font>${data.content}")
                                4 -> tvTitleMsg.text = Html.fromHtml("<font color='#1787fb'>[未读]</font>${data.content}")
                                else -> tvTitleMsg.text = data.content
                            }
                            val userInfo = NimUIKit.getUserInfoProvider().getUserInfo(data.contactId)
                            userInfo?.let {
                                Glide.with(this@MainMsgFragment)
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
                            tv_flag.visibility = View.VISIBLE
                            val value = data.msgStatus.value
                            val fromNick = if (data.fromNick.isNullOrEmpty()) "" else "${data.fromNick}: "
                            when (value) {
                                3 -> tvTitleMsg.text = Html.fromHtml("<font color='#a0a4aa'>[已读]</font>$fromNick${data.content}")
                                4 -> tvTitleMsg.text = Html.fromHtml("<font color='#1787fb'>[未读]</font>$fromNick${data.content}")
                                else -> tvTitleMsg.text = "$fromNick${data.content}"
                            }
                            val team = NimUIKit.getTeamProvider().getTeamById(data.contactId)
                            team?.let {
                                Glide.with(this@MainMsgFragment)
                                        .load(it.icon)
                                        .apply(RequestOptions().error(R.drawable.im_team_default))
                                        .into(msgIcon)

                                val extServer = it.extServer
                                if (null != extServer && !"".equals(extServer)){
                                    val jsonObject = JSONObject(extServer)
                                    val flag = jsonObject.getString("grouptype")
                                    if ("0".equals(flag)){
                                        //全员
                                        tv_flag.setBackgroundResource(R.drawable.shape_flag_bg)
                                        tv_flag.setTextColor(activity!!.resources.getColor(R.color.orange_f5))
                                        tv_flag.setText("全员")
                                    }else if ("1".equals(flag)){
                                        //部门
                                        tv_flag.setBackgroundResource(R.drawable.shape_flag_bg)
                                        tv_flag.setTextColor(activity!!.resources.getColor(R.color.orange_f5))
                                        tv_flag.setText("部门")
                                    }else{
                                        //内部群
                                        tv_flag.setBackgroundResource(R.drawable.shape_flag_bg_other)
                                        tv_flag.setTextColor(activity!!.resources.getColor(R.color.blue_43))
                                        tv_flag.setText("内部群")
                                    }
                                }else{
                                    //内部群
                                    tv_flag.setBackgroundResource(R.drawable.shape_flag_bg_other)
                                    tv_flag.setTextColor(activity!!.resources.getColor(R.color.blue_43))
                                    tv_flag.setText("内部群")
                                }
                            }
                        }
                        try {
                            val time = Utils.getTime(data.time, "yyyy-MM-dd HH:mm:ss")
                            tvDate.text = Utils.formatDingDate(time)
                        } catch (e: Exception) {
                        }
                        val mutableMap = data.extension
                        if (mutableMap != null && mutableMap.isNotEmpty() && mutableMap[data.contactId] == "Mute") {
                            tvNum.visibility = View.VISIBLE
                            tvNum.text = ""
                            tvNum.backgroundResource = R.drawable.im_team_shield
                        } else {
                            tvNum.backgroundResource = R.drawable.bg_tag_num
                            if (data.unreadCount > 0) {
                                tvNum.visibility = View.VISIBLE
                                tvNum.text = data.unreadCount.toString()
                            } else {
                                tvNum.visibility = View.INVISIBLE
                            }
                        }
                    }
                }

            }
        })
        adapter.onItemClick = { v, p ->
            if (search_edt.textStr.isEmpty()) {
                search_edt.clearFocus()
            }
            val data = adapter.dataList[p]
            if (data is MessageIndexBean) {
                MessageListActivity.start(baseActivity, data)
            } else if (data is RecentContact) {
                if (NimUIKit.getAccount().isNotEmpty()) {
                    if (data.sessionType == SessionTypeEnum.P2P) {
                        NimUIKit.startP2PSession(activity, data.contactId)
                    } else if (data.sessionType == SessionTypeEnum.Team) {
                        NimUIKit.startTeamSession(activity, data.contactId)
                    }
                }
            }
        }
        adapter.onItemLongClick = { v, positon ->
            val data = adapter.dataList[positon]
            if (data is RecentContact) {
                val top = if (isTagSet(data, RECENT_TAG_STICKY)) "取消置顶" else "置顶该聊天"
                MaterialDialog.Builder(ctx)
                        .theme(Theme.LIGHT)
                        .items(mutableListOf(top, "删除"))
                        .itemsCallback { dialog, itemView, position, text ->
                            when (position) {
                                0 -> {
                                    if (isTagSet(data, RECENT_TAG_STICKY)) {
                                        removeTag(data, RECENT_TAG_STICKY)
                                    } else {
                                        addTag(data, RECENT_TAG_STICKY)
                                    }
                                    NIMClient.getService(MsgService::class.java).updateRecent(data)
                                    doRequest()
                                }
                                1 -> {
                                    deleteRecentContact(data.contactId, data.sessionType)
                                }
                            }
                        }
                        .show()
            }
            true
        }
        val layoutManager = LinearLayoutManager(baseActivity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = adapter
        searchResult = RecyclerAdapter(ctx) { _adapter, parent, _ ->
            SearchResultHolder(_adapter.getView(R.layout.item_msg_index, parent))
        }
        searchResult.onItemClick = { v, position ->
            search_edt.setText("")
            search_edt.clearFocus()
            val record = searchResult.dataList[position]
            when (record.sessionType) {
                SessionTypeEnum.P2P -> {
                    NimUIKit.startP2PSession(ctx, record.sessionId, record.message)
                }
                SessionTypeEnum.Team -> {
                    NimUIKit.startTeamSession(ctx, record.sessionId, record.message)
                }
            }
        }
        refresh.setEnableLoadMoreWhenContentNotFull(true)
        refresh.setRefreshFooter(BallPulseFooter(ctx))
        refresh.isEnableLoadMore = false
        refresh.setOnRefreshListener {
            doRequest()
            refresh.finishRefresh(1000)
        }
        if (refresh.isEnableLoadMore) {
            refresh.setOnLoadMoreListener {
                doRequest()
                refresh.finishLoadMore(1000)
            }
        }

        registerObservers(true)

        mAppBarLayout.addOnOffsetChangedListener { _, verticalOffset ->
            if (mAppBarLayout.height > 0) {
                if (Math.abs(verticalOffset) > mAppBarLayout.height - 10) {
                    Utils.closeInput(context, search_edt)
                }
            }
        }

        Glide.with(ctx)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
        iv_loading?.visibility = View.VISIBLE
        loadPop()
    }

    private fun loadPop() {
        pop_layout.setOnClickListener { null }
        pop_layout.visibility = View.GONE
        SoguApi.getService(baseActivity!!.application, OtherService::class.java)
                .getNewPop()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload?.payload?.apply {
                            pop_layout.visibility = View.VISIBLE
                            tvGGTitle.text = title
                            tvGGContent.text = contents

                            tvConfirm.setOnClickListener {
                                pop_layout.visibility = View.GONE
                            }
                            tvDetail.setOnClickListener {
                                pop_layout.visibility = View.GONE
                                if (this.type == 1) {
                                    // 内部公告
                                    GongGaoDetailActivity.start(baseActivity, this)
                                } else {
                                    // 审批统计信息
                                    ApproveListActivity.start(baseActivity, 6, this.news_id, this)
                                }
                            }
                        }
                    } else {
                        Log.e("message", payload.message)
                        pop_layout.visibility = View.GONE
                    }
                }, { e ->
                    Trace.e(e)
                    pop_layout.visibility = View.GONE
                })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val bundle = data!!.extras
            val scanResult = bundle!!.getString("result")//       /api/qrlogin/notify
            if (scanResult.contains("/api/qrlogin/notify")) {
                var index = scanResult.indexOf("/api/")
                info { scanResult.substring(0, index) }
                RetrofitUrlManager.getInstance().putDomain("QRCode", scanResult.substring(0, index))
                SoguApi.getService(baseActivity!!.application, OtherService::class.java)
                        .qrNotify_saas(scanResult.substring(index), 1, Store.store.getUser(ctx)?.phone!!)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ payload ->
                            if (payload.isOk) {
                                ScanResultActivity.start(baseActivity, scanResult)
                                baseActivity?.overridePendingTransition(R.anim.activity_in, 0)
                                add_layout.visibility = View.GONE
                            } else {
                                showCustomToast(R.drawable.icon_toast_fail, payload.message)
                            }
                        }, { e ->
                            //showCustomToast(R.drawable.icon_toast_fail, "二维码错误")
                        })
            }
        } else if (requestCode == 0x789) {
            loadHead()
        }
    }

    override fun onResume() {
        super.onResume()
        doRequest()
    }

    fun doRequest() {
        SoguApi.getService(baseActivity!!.application, OtherService::class.java)
                .msgIndex()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        adapter.dataList.clear()
                        payload.payload?.apply {
                            zhushou = this
                            zhushou.flag = 1
                            adapter.dataList.add(zhushou)
                        }
                        getAnnouncement()
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        getAnnouncement()
                    }
                }, { e ->
                    Trace.e(e)
                    recycler_view.visibility = View.GONE
                    iv_empty.visibility = View.VISIBLE
                })
    }

    var zhushou = MessageIndexBean()
    var sys_zhushou = MessageIndexBean()

    private fun getAnnouncement() {
        SoguApi.getService(baseActivity!!.application, OtherService::class.java)
                .sysMsgIndex()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload.payload?.apply {
                            sys_zhushou = this
                            sys_zhushou.flag = 2
                            adapter.dataList.add(sys_zhushou)
                        }
                        getIMRecentContact()
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        getIMRecentContact()
                    }
                }, { e ->
                    Trace.e(e)
                    getIMRecentContact()
                })
    }

    private fun getIMRecentContact() {
        recentList.clear()
        NIMClient.getService(MsgService::class.java).queryRecentContacts().setCallback(object : RequestCallback<MutableList<RecentContact>> {
            override fun onSuccess(p0: MutableList<RecentContact>?) {
                p0?.forEach { recentContact ->
                    if (recentContact.sessionType == SessionTypeEnum.Team) {
                        extMap.clear()
                        val titleName = UserInfoHelper.getUserTitleName(recentContact.contactId, recentContact.sessionType)
                        if (titleName.isNotEmpty()) {
                            val team = NimUIKit.getTeamProvider().getTeamById(recentContact.contactId)
                            if (team.isMyTeam) {
                                extMap.put(recentContact.contactId, team.messageNotifyType)
                                recentContact.extension = extMap
                                recentList.add(recentContact)
                            }
                        }
                    } else if (recentContact.sessionType == SessionTypeEnum.P2P) {
                        recentList.add(recentContact)
                    }
                }
                Collections.sort(recentList) { o1, o2 ->
                    // 先比较置顶tag
                    val sticky = (o1.tag and RECENT_TAG_STICKY) - (o2.tag and RECENT_TAG_STICKY)
                    if (sticky != 0L) {
                        return@sort if (sticky > 0) -1 else 1
                    } else {
                        val time = o1.time - o2.time
                        return@sort if (time == 0L) 0 else if (time > 0) -1 else 1
                    }
                }
                refresh.isEnableLoadMore = recentList.size >= 10
                adapter.dataList.addAll(recentList)
                adapter.dataList.distinct()
                adapter.notifyDataSetChanged()
                iv_loading.visibility = View.GONE
                if (adapter.dataList.size == 0) {
                    recycler_view.visibility = View.GONE
                    iv_empty.visibility = View.VISIBLE
                } else {
                    recycler_view.visibility = View.VISIBLE
                    iv_empty.visibility = View.GONE
                }
            }

            override fun onException(p0: Throwable?) {
                iv_loading.visibility = View.GONE
            }

            override fun onFailed(p0: Int) {
                iv_loading.visibility = View.GONE
            }

        })
    }

    private fun deleteRecentContact(account: String, sessionType: SessionTypeEnum) {
        NIMClient.getService(MsgService::class.java).deleteRecentContact2(account, sessionType)
    }

    private fun registerObservers(register: Boolean) {
        val service = NIMClient.getService(MsgServiceObserve::class.java)
        service.observeRecentContact(messageObserver, register)
        service.observeRecentContactDeleted(deleteObserver, register)
    }


    private var messageObserver: Observer<List<RecentContact>> = Observer { recentContacts ->
        onRecentContactChanged(recentContacts)
    }

    private var deleteObserver: Observer<RecentContact> = Observer { recentContact ->
        doRequest()
    }

    // 暂存消息，当RecentContact 监听回来时使用，结束后清掉
    private val cacheMessages = HashMap<String, Set<IMMessage>>()

    private fun onRecentContactChanged(recentContacts: List<RecentContact>) {
        var index: Int
        for (r in recentContacts) {
            index = recentList.indices.firstOrNull { r.contactId == recentList[it].contactId && r.sessionType == recentList[it].sessionType }
                    ?: -1
            if (index >= 0) {
                recentList.removeAt(index)
            }
            recentList.add(r)
            if (r.sessionType == SessionTypeEnum.Team && cacheMessages[r.contactId] != null) {
                TeamMemberAitHelper.setRecentContactAited(r, cacheMessages[r.contactId])
            }
        }
        val iterator = adapter.dataList.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next is RecentContact) {
                iterator.remove()
            }
        }
        Collections.sort(recentList) { o1, o2 ->
            val time = o1.time - o2.time
            return@sort if (time == 0L) 0 else if (time > 0) -1 else 1
        }
        adapter.dataList.addAll(recentList)
        adapter.dataList.distinct()
        adapter.notifyDataSetChanged()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        registerObservers(false)
    }

    private fun addTag(recent: RecentContact, tag: Long) {
        recent.tag = recent.tag or tag
    }

    private fun removeTag(recent: RecentContact, tag: Long) {
        recent.tag = recent.tag and tag.inv()
    }

    private fun isTagSet(recent: RecentContact, tag: Long) = recent.tag and tag == tag


    companion object {
        val TAG = MainMsgFragment::class.java.simpleName
        val RECENT_TAG_STICKY = 1L

        fun newInstance(): MainMsgFragment {
            val fragment = MainMsgFragment()
            val intent = Bundle()
            fragment.arguments = intent
            return fragment
        }
    }

    inner class SearchResultHolder(itemView: View) : RecyclerHolder<MsgIndexRecord>(itemView) {
        val msgIcon = convertView.findViewById<CircleImageView>(R.id.msg_icon) as CircleImageView
        val tvTitle = convertView.findViewById<TextView>(R.id.tv_title) as TextView
        val tvDate = convertView.findViewById<TextView>(R.id.tv_date) as TextView
        val tvTitleMsg = convertView.findViewById<TextView>(R.id.tv_title_msg) as TextView
        val tvNum = convertView.findViewById<TextView>(R.id.tv_num) as TextView
        @SuppressLint("SetTextI18n")
        override fun setData(view: View, data: MsgIndexRecord, position: Int) {
            tvNum.setVisible(false)
            val titleName = UserInfoHelper.getUserTitleName(data.sessionId, data.sessionType)
            tvTitle.text = titleName
            val content = data.message.content
            if (data.sessionType == SessionTypeEnum.P2P) {
                tvTitleMsg.text = Html.fromHtml(content.replace(searchKey, "<font color='#1787fb'>$searchKey</font>"))
                val userInfo = NimUIKit.getUserInfoProvider().getUserInfo(data.sessionId)
                userInfo?.let {
                    Glide.with(this@MainMsgFragment)
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
                    Glide.with(this@MainMsgFragment)
                            .load(it.icon)
                            .apply(RequestOptions().error(R.drawable.im_team_default))
                            .into(msgIcon)
                }
            }
            try {
                val time = Utils.getTime(data.time, "yyyy-MM-dd HH:mm:ss")
                tvDate.text = Utils.formatDingDate(time)
            } catch (e: Exception) {
            }
        }
    }
}