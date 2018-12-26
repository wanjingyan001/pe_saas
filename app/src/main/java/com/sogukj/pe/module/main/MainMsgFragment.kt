package com.sogukj.pe.module.main

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.util.Log
import android.view.View
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
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.netease.nimlib.sdk.team.constant.TeamMessageNotifyTypeEnum
import com.scwang.smartrefresh.layout.footer.BallPulseFooter
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.module.approve.ApproveListActivity
import com.sogukj.pe.module.im.ImSearchResultActivity
import com.sogukj.pe.module.im.TeamCreateActivity
import com.sogukj.pe.module.im.msg_viewholder.*
import com.sogukj.pe.module.other.GongGaoDetailActivity
import com.sogukj.pe.module.other.MsgAssistantActivity
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
import org.jetbrains.anko.info
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.startActivity
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by qinfei on 17/10/11.
 */
class MainMsgFragment : BaseFragment() {
    var recentList = mutableSetOf<RecentContact>()
    override val containerViewId: Int
        get() = R.layout.fragment_msg_center

    lateinit var adapter: RecyclerAdapter<RecentContact>


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
            if (null != user?.name && !"".equals(user.name)) {
                val ch = user.name.first()
                header.setChar(ch)
            }
        } else {
            Glide.with(ctx)
                    .load(MyGlideUrl(user?.url))
                    .listener(object : RequestListener<Drawable> {
                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            header.setImageDrawable(resource)
                            return true
                        }

                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            if (null != user?.name && !"".equals(user.name)) {
                                val ch = user.name.first()
                                header.setChar(ch)
                            }
                            return true
                        }
                    })
                    .into(header)
        }
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
        toolbar_title.text = "享聊"
        loadHead()
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
            startActivity<TeamCreateActivity>(Extras.FLAG to true, Extras.DATA to alreadySelect)
//            ContactsActivity.start(ctx, alreadySelect, true, true)
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
        adapter = RecyclerAdapter(baseActivity!!) { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.item_msg_index, parent)
            object : RecyclerHolder<RecentContact>(convertView) {
                val msgIcon = convertView.findViewById(R.id.msg_icon) as CircleImageView
                val tvTitle = convertView.findViewById(R.id.tv_title) as TextView
                val tvDate = convertView.findViewById(R.id.tv_date) as TextView
                val tvTitleMsg = convertView.findViewById(R.id.tv_title_msg) as TextView
                val tvNum = convertView.findViewById(R.id.tv_num) as TextView
                val topTag = convertView.findViewById<ImageView>(R.id.topTag)
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @SuppressLint("SetTextI18n")
                override fun setData(view: View, data: RecentContact, position: Int) {
                    val titleName = UserInfoHelper.getUserTitleName(data.contactId, data.sessionType)
                    tvTitle.text = titleName
                    topTag.setVisible(data.tag == RECENT_TAG_STICKY)
                    if (data.sessionType == SessionTypeEnum.P2P) {
                        tvTitle.setDrawable(tvTitle, -1, activity!!.getDrawable(R.mipmap.ic_flag_qy))
                        val value = data.msgStatus.value
                        when (value) {
                            3 -> tvTitleMsg.text = Html.fromHtml("<font color='#a0a4aa'>[已读]</font>${data.content}")
                            4 -> tvTitleMsg.text = Html.fromHtml("<font color='#1787fb'>[未读]</font>${data.content}")
                            else -> tvTitleMsg.text = if (data.content == "欢迎使用系统消息助手" ||
                                    data.content == "欢迎使用审批消息助手") "" else data.content
                        }
                        if (data.content == "[自定义消息]") {
                            val attachment = data.attachment as? CustomAttachment
                            attachment?.let {
                                when (attachment) {
                                    is ApproveAttachment -> {
                                        tvTitleMsg.text = attachment.messageBean.title
                                    }
                                    is SystemAttachment -> {
                                        if (attachment.remindBean != null) {
                                            tvTitleMsg.text = attachment.remindBean!!.title
                                        }
                                        if (attachment.systemBean != null) {
                                            tvTitleMsg.text = attachment.systemBean!!.title
                                        }
                                    }
                                    is ProcessAttachment -> {
                                        tvTitleMsg.text = attachment.bean.title
                                    }
                                    is PayPushAttachment -> {
                                        tvTitleMsg.text = attachment.payPushBean.title
                                    }
                                    else -> {
                                        tvTitleMsg.text = ""
                                    }
                                }
                            }
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
                        tvNum.backgroundResource = R.drawable.bg_tag_num
                        if (data.content == "欢迎使用系统消息助手" ||
                                data.content == "欢迎使用审批消息助手" || data.unreadCount <= 0) {
                            tvNum.visibility = View.INVISIBLE
                        } else {
                            tvNum.visibility = View.VISIBLE
                            tvNum.text = data.unreadCount.toString()
                        }
                    } else if (data.sessionType == SessionTypeEnum.Team) {
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
                            if (null != extServer && "" != extServer && """"{}"""" != extServer) {
                                val jsonObject = JSONObject(extServer)
                                val flag = jsonObject.getString("grouptype")
                                when (flag) {
                                    "0" -> {
                                        //全员
                                        tvTitle.setDrawable(tvTitle, 2, activity!!.getDrawable(R.mipmap.ic_flag_qy))
                                    }
                                    "1" -> {
                                        //部门
                                        tvTitle.setDrawable(tvTitle, 2, activity!!.getDrawable(R.mipmap.ic_flag_bm))
                                    }
                                    else -> {
                                        //内部
                                        tvTitle.setDrawable(tvTitle, 2, activity!!.getDrawable(R.mipmap.ic_flag_nb))
                                    }
                                }
                            } else {
                                //内部
                                tvTitle.setDrawable(tvTitle, 2, activity!!.getDrawable(R.mipmap.ic_flag_nb))
                            }
                            when (it.messageNotifyType) {
                                TeamMessageNotifyTypeEnum.Mute -> {
                                    tvNum.visibility = View.VISIBLE
                                    tvNum.text = ""
                                    tvNum.backgroundResource = R.drawable.im_team_shield
                                    if (data.unreadCount > 0) {
                                        tvNum.setCompoundDrawables(null, null, resources.getDrawable(R.drawable.icon_im_mute_unread), null)
                                    } else {
                                        tvNum.setCompoundDrawables(null, null, null, null)
                                    }
                                }
                                else -> {
                                    tvNum.backgroundResource = R.drawable.bg_tag_num
                                    (data.unreadCount <= 0).yes {
                                        tvNum.visibility = View.INVISIBLE
                                    }.otherWise {
                                        tvNum.visibility = View.VISIBLE
                                        tvNum.text = data.unreadCount.toString()
                                    }
                                }
                            }
                        }
                    }
                    try {
                        val time = Utils.getTime(data.time, "yyyy-MM-dd HH:mm:ss")
                        tvDate.text = Utils.formatDingDate(time)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

//                    val mutableMap = data.extension
//                    if (mutableMap != null && mutableMap.isNotEmpty() && mutableMap[data.contactId] == "Mute") {
//                        tvNum.visibility = View.VISIBLE
//                        tvNum.text = ""
//                        tvNum.backgroundResource = R.drawable.im_team_shield
//                        if (data.unreadCount > 0) {
//                            tvNum.setCompoundDrawables(null, null, resources.getDrawable(R.drawable.icon_im_mute_unread), null)
//                        } else {
//                            tvNum.setCompoundDrawables(null, null, null, null)
//                        }
//                    } else {
//                        tvNum.backgroundResource = R.drawable.bg_tag_num
//                        if (data.content == "欢迎使用系统消息助手" ||
//                                data.content == "欢迎使用审批消息助手" || data.unreadCount <= 0) {
//                            tvNum.visibility = View.INVISIBLE
//                        } else {
//                            tvNum.visibility = View.VISIBLE
//                            tvNum.text = data.unreadCount.toString()
//                        }
//                    }
                }

            }
        }
        adapter.onItemClick = { v, p ->
            if (search_edt.textStr.isEmpty()) {
                search_edt.clearFocus()
            }
            val data = adapter.dataList[p]
            if (NimUIKit.getAccount().isNotEmpty()) {
                if (data.contactId == "58d0c67d134fbc6c") {
                    //审批消息助手
                    startActivity<MsgAssistantActivity>(Extras.TYPE to 1, Extras.ID to "58d0c67d134fbc6c")
                } else if (data.contactId == "50a0500b1773be39") {
                    //系统消息助手
                    startActivity<MsgAssistantActivity>(Extras.TYPE to 2, Extras.ID to "50a0500b1773be39")
                } else {
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
            if (data.contactId != "58d0c67d134fbc6c" && data.contactId != "50a0500b1773be39") {
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
                                    getIMRecentContact()
                                }
                                1 -> {
                                    deleteRecentContact(data.contactId, data.sessionType)
                                }
                            }
                        }
                        .show()
                true
            } else {
                false
            }
        }
        val layoutManager = LinearLayoutManager(baseActivity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = adapter
        refresh.setEnableLoadMoreWhenContentNotFull(true)
        refresh.setRefreshFooter(BallPulseFooter(ctx))
        refresh.isEnableLoadMore = false
        refresh.setOnRefreshListener {
            getIMRecentContact()
            refresh.finishRefresh(1000)
        }
        if (refresh.isEnableLoadMore) {
            refresh.setOnLoadMoreListener {
                getIMRecentContact()
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

        baseActivity?.networkBlock = { isAvail ->
            networkError.setVisible(!isAvail)
        }
        refresh.autoRefresh(1500)
    }

    private fun loadPop() {
        if (null == pop_layout) return
        pop_layout.setOnClickListener { null }
        pop_layout.visibility = View.GONE
        SoguApi.getService(baseActivity!!.application, OtherService::class.java)
                .getNewPop()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        payload?.payload?.apply {
                            pop_layout?.visibility = View.VISIBLE
                            tvGGTitle?.text = title
                            tvGGContent?.text = contents

                            tvConfirm.setOnClickListener {
                                pop_layout?.visibility = View.GONE
                            }
                            tvDetail.setOnClickListener {
                                pop_layout?.visibility = View.GONE
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
                        pop_layout?.visibility = View.GONE
                    }
                }, { e ->
                    Trace.e(e)
                    pop_layout?.visibility = View.GONE
                }).let { }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val bundle = data!!.extras
            val scanResult = bundle!!.getString("result")//       /api/qrlogin/notify
            if (scanResult.contains("/api/qrlogin/notify")) {
                val index = scanResult.indexOf("/api/")
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
                        }).let { }
            }
        } else if (requestCode == 0x789) {
            loadHead()
        }
    }

    override fun onResume() {
        super.onResume()
        getIMRecentContact()
    }


    private fun getIMRecentContact() {
        adapter.dataList.clear()
        recentList.clear()
        NIMClient.getService(MsgService::class.java).queryRecentContacts().setCallback(object : RequestCallback<MutableList<RecentContact>> {
            override fun onSuccess(p0: MutableList<RecentContact>?) {
                p0?.let {
                    it.forEach {
                        info { it.jsonStr }
                    }
                    recentList.addAll(it)
                }
                val list = recentList.toList()
                if (list.size > 1) {
                    Collections.sort(list) { o1, o2 ->
                        if (o1.contactId == "58d0c67d134fbc6c" || o1.contactId == "50a0500b1773be39") {
                            return@sort -1
                        }
                        if (o2.contactId == "58d0c67d134fbc6c" || o2.contactId == "50a0500b1773be39") {
                            return@sort 1
                        }
                        if (o1.contactId == "58d0c67d134fbc6c" && o2.contactId == "50a0500b1773be39") {
                            return@sort -1
                        }
                        if (o2.contactId == "58d0c67d134fbc6c" && o1.contactId == "50a0500b1773be39") {
                            return@sort 1
                        }
                        // 比较置顶tag
                        val sticky = (o1.tag and RECENT_TAG_STICKY) - (o2.tag and RECENT_TAG_STICKY)
                        if (sticky != 0L) {
                            return@sort if (sticky > 0) -1 else 1
                        } else {
                            val time = o1.time - o2.time
                            return@sort if (time == 0L) 0 else if (time > 0) -1 else 1
                        }
                    }
                }
                refresh.isEnableLoadMore = list.size >= 10
                adapter.dataList.addAll(list)
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
        service.observeReceiveMessage(messageReceiverObserver, register)
        service.observeRecentContact(messageObserver, register)
        service.observeRecentContactDeleted(deleteObserver, register)
    }


    private var messageObserver: Observer<List<RecentContact>> = Observer { recentContacts ->
        onRecentContactChanged(recentContacts)
    }

    private var deleteObserver: Observer<RecentContact> = Observer { recentContact ->
        getIMRecentContact()
    }
    //监听在线消息中是否有@我
    private val messageReceiverObserver = Observer<List<IMMessage>> { imMessages ->
        if (imMessages != null) {
            for (imMessage in imMessages) {
                if (!TeamMemberAitHelper.isAitMessage(imMessage)) {
                    continue
                }
                var cacheMessageSet: MutableSet<IMMessage>? = cacheMessages[imMessage.sessionId]?.toMutableSet()
                if (cacheMessageSet == null) {
                    cacheMessageSet = HashSet()
                    cacheMessages.put(imMessage.sessionId, cacheMessageSet)
                }
                cacheMessageSet.add(imMessage)
            }
        }
    }

    // 暂存消息，当RecentContact 监听回来时使用，结束后清掉
    private val cacheMessages = HashMap<String, Set<IMMessage>>()

    private fun onRecentContactChanged(recentContacts: List<RecentContact>) {
        handler.postDelayed({
            var index: Int
            for (r in recentContacts) {
                index = adapter.dataList.indices.firstOrNull {
                    r.contactId == adapter.dataList[it].contactId && r.sessionType == adapter.dataList[it].sessionType
                }
                        ?: -1
                if (index >= 0) {
                    adapter.dataList.removeAt(index)
                }

                adapter.dataList.add(r)
                if (r.sessionType == SessionTypeEnum.Team && cacheMessages[r.contactId] != null) {
                    TeamMemberAitHelper.setRecentContactAited(r, cacheMessages[r.contactId])
                }
            }
            cacheMessages.clear()
            if (adapter.dataList.size > 1) {
                adapter.dataList.sortWith(Comparator { o1, o2 ->
                    if (o1.contactId == "58d0c67d134fbc6c" || o1.contactId == "50a0500b1773be39") {
                        return@Comparator -1
                    }
                    if (o2.contactId == "58d0c67d134fbc6c" || o2.contactId == "50a0500b1773be39") {
                        return@Comparator 1
                    }
                    if (o1.contactId == "58d0c67d134fbc6c" && o2.contactId == "50a0500b1773be39") {
                        return@Comparator -1
                    }
                    if (o2.contactId == "58d0c67d134fbc6c" && o1.contactId == "50a0500b1773be39") {
                        return@Comparator 1
                    }
                    val time = o1.time - o2.time
                    return@Comparator if (time == 0L) 0 else if (time > 0) -1 else 1
                })
            }
            adapter.dataList.distinct()
            adapter.notifyDataSetChanged()
        }, 200)
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
}