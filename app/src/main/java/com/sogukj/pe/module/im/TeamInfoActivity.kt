package com.sogukj.pe.module.im

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.huantansheng.easyphotos.EasyPhotos
import com.jakewharton.rxbinding2.widget.RxTextView
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nim.uikit.business.team.helper.TeamHelper
import com.netease.nim.uikit.common.ui.widget.SwitchButton
import com.netease.nim.uikit.support.glide.GlideEngine
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.RequestCallbackWrapper
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig
import com.netease.nimlib.sdk.team.TeamService
import com.netease.nimlib.sdk.team.TeamServiceObserver
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum
import com.netease.nimlib.sdk.team.constant.TeamMessageNotifyTypeEnum
import com.netease.nimlib.sdk.team.model.Team
import com.netease.nimlib.sdk.team.model.TeamMember
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo
import com.sogukj.pe.BuildConfig
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.TeamBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.module.main.ContactsActivity
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.ImService
import com.sogukj.service.SoguApi
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_team_info.*
import org.jetbrains.anko.info
import org.jetbrains.anko.sdk25.coroutines.textChangedListener
import org.jetbrains.anko.toast
import org.json.JSONObject
import java.io.Serializable
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates


class TeamInfoActivity : BaseActivity(), View.OnClickListener, SwitchButton.OnChangedListener {
    lateinit var sessionId: String
    lateinit var toolbar: Toolbar
    lateinit var teamMember: RecyclerView
    lateinit var teamLayout: RelativeLayout
    lateinit var profileToggle: SwitchButton
    var teamMembers = ArrayList<UserBean>()
    lateinit var adapter: MemberAdapter
    lateinit var team: Team
    private val mine by lazy { Store.store.getUser(this) ?: UserBean() }
    private var isMyTeam by Delegates.observable(false) { _, _, newValue ->
        adapter.isMyTeam = newValue
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_info)
        Utils.setWindowStatusBarColor(this, R.color.color_blue_0888ff)
        sessionId = intent.getStringExtra("sessionId")
        toolbar = findViewById(R.id.team_toolbar)
        toolbar.setNavigationIcon(R.drawable.sogu_ic_back)
        toolbar.setNavigationOnClickListener { finish() }
        teamMember = findViewById(R.id.team_member)
        teamLayout = findViewById(R.id.team_layout)
        profileToggle = findViewById(R.id.user_profile_toggle)
        val manager = GridLayoutManager(this, 7)
        teamMember.layoutManager = manager
        adapter = MemberAdapter(this)
        teamMember.adapter = adapter

        val teamPic = findViewById<TextView>(R.id.team_pic)
        val teamFile = findViewById<TextView>(R.id.team_file)
        val teamLink = findViewById<TextView>(R.id.team_link)
        val teamSearch = findViewById<TextView>(R.id.team_search)

        teamPic.setOnClickListener(this)
        teamFile.setOnClickListener(this)
        teamLink.setOnClickListener(this)
        teamSearch.setOnClickListener(this)
        teamLayout.setOnClickListener(this)
        exit_team.setOnClickListener(this)
        team_logo.setOnClickListener(this)
        teamNameLayout.setOnClickListener(this)
        teamIntroductionLayout.setOnClickListener(this)
        dismissTeam.setOnClickListener(this)
        profileToggle.setOnChangedListener(this)
        team_name.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                team_name.setSelection(team_name.textStr.length)
            }
        }
        teamIntroduction.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                teamIntroduction.setSelection(teamIntroduction.textStr.length)
            }
        }
        teamIntroduction.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateIntroduction()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        adapter.onItemClick = { v, p ->
            if (v.getTag(R.id.member_headImg) == "ADD") {
                ContactsActivity.start(this, teamMembers, false, false)
            } else if (v.getTag(R.id.member_headImg) == "REMOVE") {
                RemoveMemberActivity.start(this, teamMembers,0,team)
            }
        }
    }


    fun doRequest() {
        NIMClient.getService(TeamService::class.java).queryTeam(sessionId).setCallback(object : RequestCallback<Team> {
            override fun onSuccess(p0: Team?) {
                p0?.let {
                    refreshTeamView(it)
                }
            }

            override fun onFailed(p0: Int) {
            }

            override fun onException(p0: Throwable?) {
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun refreshTeamView(it: Team) {
        team = it
        profileToggle.check = it.messageNotifyType == TeamMessageNotifyTypeEnum.Mute
        Glide.with(this@TeamInfoActivity)
                .load(it.icon)
                .apply(RequestOptions().error(R.drawable.invalid_name2))
                .into(team_logo)
        team_title.text = it.name
        val extServer = it.extServer
        if (null != extServer && "" != extServer && """"{}"""" != extServer) {
            val jsonObject = JSONObject(extServer)
            val flag = jsonObject.getString("grouptype")
            when (flag) {
                "0" -> {
                    //全员
                    team_title.setDrawable(team_title, 2, getDrawable(R.mipmap.ic_flag_qy))
                }
                "1" -> {
                    //部门
                    team_title.setDrawable(team_title, 2, getDrawable(R.mipmap.ic_flag_bm))
                }
                else -> {
                    //内部
                    team_title.setDrawable(team_title, 2, getDrawable(R.mipmap.ic_flag_nb))
                }
            }
        } else {
            //内部
            team_title.setDrawable(team_title, 2, getDrawable(R.mipmap.ic_flag_nb))
        }
        val bean = Gson().fromJson(it.extension, TeamBean::class.java)
        if (bean != null) {
            team_project.text = bean.project_name
        }
        team_name.setText(it.name)
        if (it.introduce != "这是群介绍") {
            teamIntroduction.setText(it.introduce)
        } else {
            teamIntroduction.hint = "暂无介绍"
        }
        isMyTeam = it.creator == mine.accid
        if (isMyTeam) {
            exit_team.text = "转让群组"
            dismissTeam.setVisible(true)
        } else {
            exit_team.text = "退出群组"
            dismissTeam.setVisible(false)
        }
        getTeamMember(team.id)
    }

    /**
     * 获取用户列表
     */
    private fun getTeamMember(teamId: String) {
        NIMClient.getService(TeamService::class.java).queryMemberList(teamId).setCallback(object : RequestCallback<List<TeamMember>> {
            override fun onSuccess(p0: List<TeamMember>?) {
                val accounts = ArrayList<String>()
                p0?.let {
                    it.filter { it.isInTeam }.mapTo(accounts) { it.account }
                }
                getUsersInfoAsync(accounts)
            }

            override fun onFailed(p0: Int) {

            }

            override fun onException(p0: Throwable?) {
            }
        })
    }

    /**
     * 获取用户资料
     */
    fun getUsersInfoAsync(accounts: List<String>) {
        NimUIKit.getUserInfoProvider().getUserInfoAsync(accounts) { success, result, code ->
            teamMembers.clear()
            result.forEach {
                val info = it as NimUserInfo
                if (null != info.extensionMap &&
                        info.extensionMap.isNotEmpty() &&
                        info.extensionMap["uid"] != null &&
                        info.extensionMap["uid"] != "null") {
                    println(info.extensionMap.jsonStr)
                    val uid = info.extensionMap["uid"].toString().toInt()
                    val user = UserBean()
                    user.uid = uid
                    user.name = info.name
                    user.user_id = uid
                    user.url = info.avatar
                    user.accid = info.account
                    teamMembers.add(user)
                }
            }
            adapter.refreshData(teamMembers)
            team_number.text = "${teamMembers.size}人"
        }
    }


    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.team_pic -> TeamPictureActivity.start(this, sessionId.toInt())
            R.id.team_file -> TeamHistoryFileActivity.start(this, sessionId.toInt())
            R.id.team_link -> {
                TeamHistoryFileActivity.start(this, sessionId.toInt(), 2)
            }
            R.id.team_search -> {
                val intent = Intent(this, TeamSearchActivity::class.java)
                intent.putExtra("sessionId", getIntent().getStringExtra("sessionId"))
                startActivity(intent)
            }
            R.id.team_logo -> {
                EasyPhotos.createAlbum(this, true, GlideEngine.getInstance())
                        .setFileProviderAuthority(BuildConfig.FILEPROVIDER)
                        .setPuzzleMenu(false)
                        .start(Extras.requestCode1)
            }
            R.id.team_layout -> {
                MemberEditActivity.start(this, teamMembers, team)
            }
            R.id.exit_team -> {
                if (isMyTeam) {
                    MemberEditActivity.start(this, teamMembers, team, true)
                } else {
                    exitTeam()
                }
            }
            R.id.teamNameLayout -> {
                team_name.isFocusable = true
                team_name.isFocusableInTouchMode = true
                team_name.requestFocus()
                Utils.toggleSoftInput(this, team_name)
            }
            R.id.teamIntroductionLayout -> {
                teamIntroduction.isFocusable = true
                teamIntroduction.isFocusableInTouchMode = true
                teamIntroduction.requestFocus()
                Utils.toggleSoftInput(this, teamIntroduction)
            }
            R.id.dismissTeam -> {
                dismissTeam()
            }
        }
    }

    private fun exitTeam() {
        MaterialDialog.Builder(this)
                .theme(Theme.LIGHT)
                .title("确定退出?")
                .content("是否退出${team_title.text}")
                .positiveText("确认")
                .negativeText("取消")
                .onPositive { dialog, which ->
                    NIMClient.getService(TeamService::class.java).quitTeam(sessionId).setCallback(object : RequestCallback<Void> {
                        override fun onFailed(p0: Int) {
                            info { "错误码$p0" }
                        }

                        override fun onException(p0: Throwable?) {
                            info { "错误码${p0?.printStackTrace()}" }
                        }

                        override fun onSuccess(p0: Void?) {
                            toast("您已退出该群")
                            finish()
                        }

                    })
                }
                .onNegative { dialog, which ->
                    dialog.dismiss()
                }
                .show()
    }

    private fun dismissTeam() {
        MaterialDialog.Builder(this)
                .theme(Theme.LIGHT)
                .title("确定解散群?")
                .content("是否解散该群")
                .positiveText("确认")
                .negativeText("取消")
                .onPositive { dialog, which ->
                    NIMClient.getService(TeamService::class.java).dismissTeam(sessionId).setCallback(object : RequestCallback<Void> {
                        override fun onFailed(p0: Int) {
                            info { "错误码$p0" }
                        }

                        override fun onException(p0: Throwable?) {
                            info { "错误码${p0?.printStackTrace()}" }
                        }

                        override fun onSuccess(p0: Void?) {
                            toast("您已退出该群")
                            val bean = Gson().fromJson(team.extension, TeamBean::class.java)
                            if (bean != null) {
                                SoguApi.getService(application, ImService::class.java).deleteTeam(bean.project_id!!, sessionId)
                                        .execute {
                                            onNext { payload ->
                                                if (payload.isOk) {
                                                    finish()
                                                } else {
                                                    showErrorToast(payload.message)
                                                }
                                            }
                                        }
                            } else {
                                finish()
                            }
                        }
                    })
                }
                .onNegative { dialog, which ->
                    dialog.dismiss()
                }
                .show()

    }


    override fun OnChanged(v: View?, checkState: Boolean) {
        val typeEnum = if (checkState) TeamMessageNotifyTypeEnum.Mute else TeamMessageNotifyTypeEnum.All
        NIMClient.getService(TeamService::class.java).muteTeam(sessionId, typeEnum).setCallback(
                object : RequestCallback<Void> {
                    override fun onFailed(code: Int) {
                        if (code == 408) {
                            toast(R.string.network_is_not_available)
                        } else {
                            toast("未知错误")
                        }
                        profileToggle.check = !checkState
                    }

                    override fun onSuccess(param: Void?) {

                    }

                    override fun onException(exception: Throwable?) {
                    }

                }
        )
    }

    override fun onResume() {
        super.onResume()
        doRequest()
    }

    override fun onPause() {
        super.onPause()
        Utils.closeInput(this, team_name)
        Utils.closeInput(this, teamIntroduction)
    }

    private fun updateIntroduction() {
        val map = HashMap<TeamFieldEnum, Serializable>()
        val name = team_name.text.trim().toString()
        if (name.isNotEmpty() && team_title.text != name) {
            map[TeamFieldEnum.Name] = name
        }
        if (team.introduce != teamIntroduction.textStr && teamIntroduction.text.isNotEmpty()) {
            map[TeamFieldEnum.Introduce] = teamIntroduction.textStr
        }
        map.isNotEmpty().yes {
            NIMClient.getService(TeamService::class.java).updateTeamFields(sessionId, map)
        }
    }

    override fun onStart() {
        super.onStart()
        NIMClient.getService(TeamServiceObserver::class.java).observeTeamUpdate(teamUpdateObserver, true)
    }

    override fun onStop() {
        super.onStop()
        NIMClient.getService(TeamServiceObserver::class.java).observeTeamUpdate(teamUpdateObserver, false)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && data != null) {
            when (resultCode) {
                Extras.RESULTCODE -> {
                    val newMembers = data.getSerializableExtra(Extras.DATA) as ArrayList<UserBean>
                    teamMembers.forEach { oldMember ->
                        val bean = newMembers.find { it.accid == oldMember.accid }
                        newMembers.remove(bean)
                    }
                    val accounts = ArrayList<String>()
                    newMembers.forEach {
                        it.accid?.let {
                            accounts.add(it)
                        }
                    }
                    if (accounts.isNotEmpty()) {
                        NIMClient.getService(TeamService::class.java).addMembers(sessionId, accounts)
                                .setCallback(object : RequestCallback<List<String>> {
                                    override fun onSuccess(p0: List<String>?) {
                                        if (p0 != null && !p0.isEmpty()) {
                                            TeamHelper.onMemberTeamNumOverrun(p0, this@TeamInfoActivity)
                                        } else {
                                            toast("邀请成功")
                                            val msg = MessageBuilder.createTipMessage(sessionId, SessionTypeEnum.Team)
                                            msg.content = "欢迎${newMembers.joinToString { it.name }}加入"
                                            val config = CustomMessageConfig()
                                            config.enableUnreadCount = false
                                            msg.config = config
                                            NIMClient.getService(MsgService::class.java).sendMessage(msg, false)
                                        }
                                        getTeamMember(team.id)
                                    }

                                    override fun onFailed(p0: Int) {
                                        if (p0 == 408) {
                                            toast("请检查网络")
                                        } else {
                                            toast("邀请失败")
                                        }
                                    }

                                    override fun onException(p0: Throwable?) {
                                    }
                                })
                    }
                }
                Extras.RESULTCODE2 -> {
                    val newMembers = data.getSerializableExtra(Extras.LIST2) as ArrayList<UserBean>
                    adapter.refreshData(newMembers)
                    val removes = mutableListOf<UserBean>()
                    teamMembers.forEach { user ->
                        if (newMembers.find { it.uid == user.uid } == null) {
                            removes.add(user)
                        }
                    }
                    removes.forEach {
                        NIMClient.getService(TeamService::class.java).removeMember(team.id, it.accid)
                    }
                }
            }
        } else if (requestCode == Extras.requestCode1 && resultCode == Activity.RESULT_OK && data != null) {
            val resultPaths = data.getStringArrayListExtra(EasyPhotos.RESULT_PATHS)
            Glide.with(this@TeamInfoActivity)
                    .load(resultPaths[0])
                    .apply(RequestOptions().error(R.drawable.invalid_name2))
                    .into(team_logo)
            NIMClient.getService(TeamService::class.java)
                    .updateTeam(sessionId, TeamFieldEnum.ICON, resultPaths[0])
                    .setCallback(object : RequestCallback<Void> {
                        override fun onFailed(code: Int) {
                            showCustomToast(R.drawable.icon_toast_fail, "修改群头像失败")
                        }

                        override fun onSuccess(param: Void?) {
                            showCustomToast(R.drawable.icon_toast_success, "修改群头像成功")
                        }

                        override fun onException(exception: Throwable?) {
                            showCustomToast(R.drawable.icon_toast_common, "修改群头像失败")
                        }
                    })
        }
    }

    /**
     * 群组资料变动监听
     */
    private val teamUpdateObserver = Observer<List<Team>> {
        refreshTeamView(it[0])
    }

    companion object {

        fun start(context: Context, sessionId: String) {
            val intent = Intent(context, TeamInfoActivity::class.java)
            intent.putExtra("sessionId", sessionId)
            context.startActivity(intent)
        }
    }

}
