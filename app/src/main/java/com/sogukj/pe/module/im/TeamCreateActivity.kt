package com.sogukj.pe.module.im

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.text.TextUtils
import com.amap.api.mapcore.util.it
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.huantansheng.easyphotos.EasyPhotos
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nim.uikit.business.team.helper.TeamHelper
import com.netease.nim.uikit.common.ui.dialog.DialogMaker
import com.netease.nim.uikit.support.glide.GlideEngine
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.team.TeamService
import com.netease.nimlib.sdk.team.constant.*
import com.netease.nimlib.sdk.team.model.CreateTeamResult
import com.sogukj.pe.BuildConfig
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ActivityHelper
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.CustomSealBean
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.TeamBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.module.main.ContactsActivity
import com.sogukj.pe.module.other.CompanySelectActivity
import com.sogukj.pe.peExtended.removeTeamSelectActivity
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.ImService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_team_create.*
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import java.io.Serializable

class TeamCreateActivity : BaseActivity() {
    lateinit var adapter: MemberAdapter
    private var path: String? = null
    var bean: CustomSealBean.ValueBean? = null
    var project: ProjectBean? = null
    private val mine by lazy { Store.store.getUser(this) }
    private val data: ArrayList<UserBean>? by extraDelegate(Extras.DATA, null)
    private val teamMember by lazy {
        if (data != null) {
            data as ArrayList<UserBean>
        } else {
            ArrayList()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.getBooleanExtra(Extras.FLAG, false).yes {
            ContactsActivity.start(this, teamMember, true, true, Extras.REQUESTCODE)
        }
        setContentView(R.layout.activity_team_create)
        Utils.setWindowStatusBarColor(this, R.color.color_blue_0888ff)
        team_toolbar.setNavigationIcon(R.drawable.sogu_ic_back)
        team_toolbar.setNavigationOnClickListener { finish() }
        project = intent.getSerializableExtra(Extras.DATA2) as? ProjectBean
        adapter = MemberAdapter(this)
        adapter.isMyTeam = true
        adapter.refreshData(teamMember)
        adapter.onItemClick = { v, position ->
            if (v.getTag(R.id.member_headImg) == "ADD") {
                ContactsActivity.start(this, teamMember, true, true, Extras.REQUESTCODE)
            } else {
                RemoveMemberActivity.start(this, teamMember)
            }
        }
        val manager = GridLayoutManager(this, 7)
        team_member.layoutManager = manager
        team_member.adapter = adapter

        team_number.text = "${teamMember.size}人"
        team_name.filters = Utils.getFilter(this)
        related_items_layout.clickWithTrigger {
            CompanySelectActivity.start(this)
        }
        createTeam.clickWithTrigger {
            if (teamMember.size <= 2) {
                showCommonToast("建群人数不能少于3人")
            } else {
                createTeam()
            }
        }
        team_logo.clickWithTrigger {
            EasyPhotos.createAlbum(this, true, GlideEngine.getInstance())
                    .setFileProviderAuthority(BuildConfig.FILEPROVIDER)
                    .setPuzzleMenu(false)
                    .start(Extras.requestCode1)
        }
        teamNameLayout.clickWithTrigger {
            team_name.isFocusable = true
            team_name.isFocusableInTouchMode = true
            team_name.requestFocus()
            Utils.toggleSoftInput(this, team_name)
        }
        teamIntroductionLayout.clickWithTrigger {
            teamIntroduction.isFocusable = true
            teamIntroduction.isFocusableInTouchMode = true
            teamIntroduction.requestFocus()
            Utils.toggleSoftInput(this, teamIntroduction)
        }

        team_name.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus && team_name.textStr.isNotEmpty()) {
                team_name.setSelection(team_name.textStr.length)
            }
        }
        teamIntroduction.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus && teamIntroduction.textStr.isNotEmpty()) {
                teamIntroduction.setSelection(teamIntroduction.textStr.length)
            }
        }

        if (project != null) {
            team_project.text = project?.shortName ?: project?.name!!
            team_project.setVisible(team_project.text.isNotEmpty())
            team_name.setText(getDefaultName())
            path = project?.logo
            Glide.with(this)
                    .load(project?.logo)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            getTeamHeader(teamMember)
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            return false
                        }

                    }).into(team_logo)
            related_items_layout.setVisible(true)
            related_items.text = project?.name
            related_items_layout.isClickable = false
            team_name.isEnabled = false
            team_logo.isEnabled = false
        } else {
            team_project.setVisible(false)
            getTeamHeader(teamMember)
            team_name.setText(getDefaultName())
            team_title.text = getDefaultName()
        }
    }

    private fun getDefaultName(): String {
        //自动生成群名字
        var nameStr = ""
        if (project == null) {
            val nameList = ArrayList<UserBean>(teamMember)
            if (teamMember.size > 4) {
                nameList.clear()
                (0 until 4).mapTo(nameList) { teamMember[it] }
            }

            for (item in nameList) {
                nameStr = "${nameStr}、${item.name}"
            }
            if (nameList.size > 0) {
                nameStr = nameStr.removePrefix("、")
            }
        } else {
            nameStr = project?.shortName ?: project?.name!!
        }
        return nameStr
    }

    private fun getTeamHeader(teamMember: ArrayList<UserBean>) {
        if (path.isNullOrEmpty()) {
            if (teamMember.isNotEmpty()) {
                val uids = StringBuilder()
                teamMember.forEach { user ->
                    uids.append("${user.uid},")
                }
                SoguApi.getService(application, ImService::class.java).getTeamGroupHeader(uids.substring(0, uids.lastIndexOf(",")))
                        .execute {
                            onNext { payload ->
                                payload.payload?.let {
                                    path = it
                                    Glide.with(this@TeamCreateActivity)
                                            .load(it)
                                            .apply(RequestOptions().error(R.drawable.invalid_name2))
                                            .into(team_logo)
                                }
                            }
                        }
            }
        }
    }

    /**
     * 创建群组
     */
    private fun createTeam() {
        var teamName = team_name.text.toString().trim()
        if (teamName.isEmpty()) {
            teamName = getDefaultName()
        }
        if (teamMember.size == 1) {
            toast("请选择群成员")
            return
        }
        DialogMaker.showProgressDialog(this, "", true)
        val memberAccounts = teamMember.map { it.accid }.filter { !it.isNullOrEmpty() }
        val map = HashMap<TeamFieldEnum, Serializable>()
        map.put(TeamFieldEnum.Name, teamName)
        if (path != null && !TextUtils.isEmpty(path)) {
            map.put(TeamFieldEnum.ICON, path!!)
        }
        if (teamIntroduction.textStr.isNotEmpty()) {
            map.put(TeamFieldEnum.Introduce, teamIntroduction.textStr)
        }
        map.put(TeamFieldEnum.InviteMode, TeamInviteModeEnum.All)
        map.put(TeamFieldEnum.BeInviteMode, TeamBeInviteModeEnum.NoAuth)
        map.put(TeamFieldEnum.VerifyType, VerifyTypeEnum.Free)
        map.put(TeamFieldEnum.TeamUpdateMode, TeamUpdateModeEnum.All)
        project?.let {
            val teamBean = TeamBean()
            teamBean.project_id = it.company_id.toString()
            teamBean.project_name = it.name.toString()
            map.put(TeamFieldEnum.Extension, teamBean.toString())
        }
        NIMClient.getService(TeamService::class.java).createTeam(map, TeamTypeEnum.Advanced, "邀请你加入群组", memberAccounts)
                .setCallback(object : RequestCallback<CreateTeamResult> {
                    override fun onFailed(p0: Int) {
                        DialogMaker.dismissProgressDialog()
                        if (p0 == 802) {
                            toast("邀请失败，成员人数上限为200人")
                        } else {
                            toast("创建失败")
                        }
                    }

                    override fun onSuccess(p0: CreateTeamResult?) {
                        p0?.let {
                            DialogMaker.dismissProgressDialog()
                            val failedAccounts = it.failedInviteAccounts
                            if (failedAccounts != null && !failedAccounts.isEmpty()) {
                                TeamHelper.onMemberTeamNumOverrun(failedAccounts, this@TeamCreateActivity)
                            } else {
                                toast("创建成功")
                                project?.let { project ->
                                    SoguApi.getService(application, ImService::class.java)
                                            .saveGroupId(project.company_id!!, it.team.id)
                                            .execute { onNext { info { "项目群组创建成功" } } }
                                }
                            }
                            NimUIKit.startTeamSession(this@TeamCreateActivity, it.team.id, "${mine?.name}发起了群聊。本群成员来自同一组织，请放心安全沟通。")
                            finish()
                            //去掉中间的activity(TeamSelectActivity)
                            ActivityHelper.removeTeamSelectActivity()
                        }

                    }

                    override fun onException(p0: Throwable?) {
                        DialogMaker.dismissProgressDialog()
                    }

                })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && data != null) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    bean = data.getSerializableExtra(Extras.DATA) as CustomSealBean.ValueBean?
                    bean?.let {
                        related_items.text = it.name
                    }
                }
                Extras.RESULTCODE -> {
                    val list = data.getSerializableExtra(Extras.DATA) as ArrayList<UserBean>?
                    list?.let {
                        teamMember.clear()
                        teamMember.addAll(it)
                        adapter.refreshData(teamMember)
                        team_number.text = "${teamMember.size}人"
                        getTeamHeader(teamMember)
                    }
                }
                Extras.RESULTCODE2 -> {
                    val list = data.getSerializableExtra(Extras.LIST2) as? ArrayList<UserBean>
                    list?.let {
                        teamMember.clear()
                        teamMember.addAll(it)
                        adapter.refreshData(teamMember)
                        team_number.text = "${teamMember.size}人"
                        getTeamHeader(teamMember)
                    }
                }
            }
        } else if (requestCode == Extras.requestCode1 && data != null && resultCode == Activity.RESULT_OK) {
            val resultPaths = data.getStringArrayListExtra(EasyPhotos.RESULT_PATHS)
            path = resultPaths[0]
            Glide.with(this@TeamCreateActivity)
                    .load(resultPaths[0])
                    .apply(RequestOptions().error(R.drawable.invalid_name2))
                    .into(team_logo)
        }
    }

    companion object {
        fun start(ctx: Context, data: ArrayList<UserBean>?, project: ProjectBean? = null) {
            val intent = Intent(ctx, TeamCreateActivity::class.java)
            intent.putExtra(Extras.DATA, data)
            intent.putExtra(Extras.DATA2, project)
            ctx.startActivity(intent)
        }
    }
}
