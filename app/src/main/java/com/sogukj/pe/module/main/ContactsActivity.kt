package com.sogukj.pe.module.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import com.amap.api.mapcore.util.it
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nimlib.sdk.team.model.Team
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.fromJson
import com.sogukj.pe.baselibrary.Extended.setOnClickFastListener
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.DepartmentBean
import com.sogukj.pe.bean.MechanismBasicInfo
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.module.im.TeamCreateActivity
import com.sogukj.pe.peExtended.getEnvironment
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.UserService
import com.sogukj.pe.widgets.CircleImageView
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_contacts.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.info

/**
通讯录Activity(所有选择人员的都跳转到这里)
 */
class ContactsActivity : ToolbarActivity() {
    /**
     * 已选中人员
     */
    private lateinit var alreadySelected: MutableSet<UserBean>
    /**
     * 已选中人员是否可编辑
     */
    private var selectedCanModify = true

    /**
     * 是否是创建群聊
     */
    private var isCreateTeam = false
    /**
     * 最近联系人adapter
     */
    private lateinit var contactsAdapter: RecyclerAdapter<UserBean>

    /**
     * 群组adapter
     */
    private lateinit var groupsAdapter: GroupAdapter

    /**
     * 组织架构adapter
     */
    private lateinit var tissueAdapter: TissueAdapter
    private val departList = ArrayList<DepartmentBean>() //组织架构

    /**
     * 搜索结果adapter
     */
    private lateinit var searchAdapter: RecyclerAdapter<UserBean>

    /**
     * 分享文件
     */
    private var pathByUri: String? = null

    /**
     * 无法修改的抄送人名单
     */
    var default: ArrayList<Int>? = null
    var searchKey = ""
    private val mine = Store.store.getUser(this)


    companion object {
        fun start(context: Context, alreadyList: ArrayList<UserBean>?,
                  canModify: Boolean? = false,
                  isCreateTeam: Boolean? = false,
                  requestCode: Int? = null,
                  project: ProjectBean? = null) {
            val intent = Intent(context, ContactsActivity::class.java)
            intent.putExtra(Extras.LIST, alreadyList)
            intent.putExtra(Extras.FLAG, canModify)
            intent.putExtra(Extras.CREATE_TEAM, isCreateTeam)
            intent.putExtra(Extras.DATA, project)
            val code = requestCode ?: Extras.REQUESTCODE
            if (context is Fragment) {
                context.startActivityForResult(intent, code)
            } else if (context is Activity) {
                context.startActivityForResult(intent, code)
            }
        }


        fun startFromFragment(context: Fragment, alreadyList: ArrayList<UserBean>?,
                              canModify: Boolean? = false,
                              isCreateTeam: Boolean? = false,
                              requestCode: Int? = null) {
            val intent = Intent(context.activity, ContactsActivity::class.java)
            intent.putExtra(Extras.LIST, alreadyList)
            intent.putExtra(Extras.FLAG, canModify)
            intent.putExtra(Extras.CREATE_TEAM, isCreateTeam)
            val code = requestCode ?: Extras.REQUESTCODE
            context.startActivityForResult(intent, code)
        }

        fun startWithDefault(context: Context, alreadyList: ArrayList<UserBean>?,
                             canModify: Boolean? = false,
                             isCreateTeam: Boolean? = false,
                             noModification: ArrayList<Int>? = null,
                             requestCode: Int? = null) {
            val intent = Intent(context, ContactsActivity::class.java)
            intent.putExtra(Extras.LIST, alreadyList)
            intent.putExtra(Extras.FLAG, canModify)
            intent.putExtra(Extras.CREATE_TEAM, isCreateTeam)
            intent.putExtra(Extras.DEFAULT, noModification)
            val code = requestCode ?: Extras.REQUESTCODE
            if (context is Fragment) {
                context.startActivityForResult(intent, code)
            } else if (context is Activity) {
                context.startActivityForResult(intent, code)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)
        Utils.setWindowStatusBarColor(this, R.color.color_blue_0888ff)
        setBack(true)

        getDataFromIntent()
        initHeader()
        initSearchView()
        initContactsAdapter()
        initTissueAdapter()
        getTissueData()
        confirmTv.setOnClickFastListener {
            val list = ArrayList<UserBean>()
            list.addAll(alreadySelected)
            if (isCreateTeam) {
                TeamCreateActivity.start(this@ContactsActivity, list)
                finish()
            } else {
                val intent = Intent()
                intent.putExtra(Extras.DATA, list)
                setResult(Extras.RESULTCODE, intent)
                finish()
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun getDataFromIntent() {
        val list = intent.getSerializableExtra(Extras.LIST) as? ArrayList<UserBean>
        alreadySelected = list?.toMutableSet() ?: ArrayList<UserBean>().toMutableSet()
        selectedCanModify = intent.getBooleanExtra(Extras.FLAG, true)
        isCreateTeam = intent.getBooleanExtra(Extras.CREATE_TEAM, false)
        default = intent.getSerializableExtra(Extras.DEFAULT) as ArrayList<Int>?

        if (alreadySelected.isNotEmpty()) {
            selectNumber.text = "已选择: ${alreadySelected.size} 人"
        }
        title = if (isCreateTeam) "创建群聊" else "选择联系人"
        if (intent.action == Intent.ACTION_SEND && intent.extras.containsKey(Intent.EXTRA_STREAM)) {
            val uri = intent.extras.getParcelable<Uri>(Intent.EXTRA_STREAM)
            pathByUri = Utils.getFileAbsolutePathByUri(this, uri)
            AnkoLogger("WJY").info {
                "分享的文件:${Gson().toJson(uri)}\n" +
                        "path:${uri.path}--${uri.encodedPath}\n" +
                        "pathByUri:$pathByUri"
            }
        }
    }

    private fun initHeader() {
        when (getEnvironment()) {
            "civc" -> {
                company_icon.imageResource = R.mipmap.ic_launcher_zd
                companyName.text = "中缔资本"
            }
            "ht" -> {
                company_icon.imageResource = R.mipmap.ic_launcher_ht
                companyName.text = "海通创新"
            }
            "kk" -> {
                company_icon.imageResource = R.mipmap.ic_launcher_kk
                companyName.text = "夸克"
            }
            "yge" -> {
                company_icon.imageResource = R.mipmap.ic_launcher_yge
                companyName.text = "雅戈尔"
            }
            "sr" -> {
                company_icon.imageResource = R.mipmap.ic_launcher_sr
                companyName.text = "尚融资本"
            }
            else -> {
                val company = sp.getString(Extras.CompanyDetail, "")
                val detail = Gson().fromJson<MechanismBasicInfo?>(company)
                Glide.with(this)
                        .load(detail?.logo)
                        .apply(RequestOptions().placeholder(R.mipmap.ic_launcher_pe).error(R.mipmap.ic_launcher_pe))
                        .into(company_icon)
                companyName.text = detail?.mechanism_name ?: "搜股X-PE"
            }
        }
    }

    private fun initSearchView() {
        search_edt.filters = Utils.getFilter(this)
        search_edt.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                search_hint.visibility = View.GONE
                search_icon.visibility = View.VISIBLE
            } else {
                search_hint.visibility = View.VISIBLE
                search_icon.visibility = View.GONE
                search_edt.clearFocus()
            }
        }
        search_edt.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchKey = search_edt.text.toString()
                searchWithName()
                true
            } else {
                false
            }
        }
        search_edt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (search_edt.text.toString().isEmpty()) {
                    searchKey = ""
                    listContent.visibility = View.VISIBLE
                    resultList.visibility = View.GONE
                }
            }
        })
    }

    private fun initContactsAdapter() {
        contactsAdapter = RecyclerAdapter(this) { _adapter, parent, type ->
            ContactHolder(_adapter.getView(R.layout.item_team_organization_chlid, parent))
        }
        contactsAdapter.onItemClick = { v, position ->

        }
        contactList.apply {
            layoutManager = LinearLayoutManager(this@ContactsActivity)
            adapter = contactsAdapter
        }

        searchAdapter = RecyclerAdapter(this) { _adapter, parent, type ->
            ContactHolder(_adapter.getView(R.layout.item_team_organization_chlid, parent))
        }
        searchAdapter.onItemClick = { v, position ->

        }
        resultList.apply {
            layoutManager = LinearLayoutManager(this@ContactsActivity)
            adapter = searchAdapter
        }

        val user = Store.store.getUser(context)
        user?.accid?.let {
            SoguApi.getService(application, UserService::class.java)
                    .recentContacts(it)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            contactsAdapter.dataList.clear()
                            contactsAdapter.dataList.addAll(payload.payload!!)
                            contactsAdapter.notifyDataSetChanged()
                        } else
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }, { e ->
                        Trace.e(e)
                        showCustomToast(R.drawable.icon_toast_fail, "最近联系人数据获取失败")
                    })
        }
    }

    private fun initTissueAdapter() {
        tissueAdapter = TissueAdapter(this, departList)
        organizationList.setAdapter(tissueAdapter)
    }

    private fun searchWithName() {
        val result = ArrayList<UserBean>()
        departList.forEach {
            it.data?.let {
                it.forEach {
                    if (it.name.contains(searchKey)) {
                        result.add(it)
                    }
                }
            }
        }
        listContent.visibility = View.GONE
        resultList.visibility = View.VISIBLE
        searchAdapter.dataList.clear()
        searchAdapter.dataList.addAll(result)
        searchAdapter.notifyDataSetChanged()
    }


    private fun getTissueData() {
        SoguApi.getService(application, UserService::class.java)
                .userDepart()
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            payload.payload?.let {
                                departList.addAll(it)
                                tissueAdapter.notifyDataSetChanged()
                                departList.forEachIndexed { index, departmentBean ->
                                    departmentBean.data?.forEach { userBean ->
                                        val find = alreadySelected.find { it.uid == userBean.uid }
                                        if (find != null) {
                                            //这里是防止传递过来的已选中用户对象不规范
                                            alreadySelected.remove(find)
                                            alreadySelected.add(userBean)
                                            if (!organizationList.expandGroup(index)) {
                                                organizationList.expandGroup(index)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            showCustomToast(R.drawable.icon_toast_common, payload.message)
                        }
                    }
                    onError { error ->
                        Trace.e(error)
                        showCustomToast(R.drawable.icon_toast_fail, "数据获取失败")
                    }
                }
    }


    private fun getGroup() {
        val allTeams = NimUIKit.getTeamProvider().allTeams
        val group = ArrayList<List<Team>>()
        val myTeam = allTeams.filter { it.isMyTeam }
        val chatTeam = myTeam.filterNot { it.extension.isNotEmpty() }
        val projectTeam = myTeam.filter { it.extension.isNotEmpty() }
        group.add(chatTeam)
        group.add(projectTeam)
        groupsAdapter = GroupAdapter(this, group)
        groupDiscuss.setAdapter(groupsAdapter)
    }

    /**
     * 群组Adapter
     */
    internal inner class GroupAdapter(private val context: Context, private val groups: List<List<Team>>) : BaseExpandableListAdapter() {
        override fun getGroup(groupPosition: Int): Any = groups[groupPosition]
        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
        override fun hasStableIds(): Boolean = true
        override fun getChildrenCount(groupPosition: Int): Int = groups[groupPosition].size
        override fun getChild(groupPosition: Int, childPosition: Int): Any = groups[groupPosition][childPosition]
        override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()
        override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()
        override fun getGroupCount(): Int = groups.size

        override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
            var convertView = convertView
            val holder: ParentHolder
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_team_organization_parent, null)
                holder = ParentHolder(convertView)
                convertView!!.tag = holder
            } else {
                holder = convertView.tag as ParentHolder
            }
            val team = groups[groupPosition]
            if (groupPosition == 0) {
                holder.departmentName.text = "群聊 (${team.size})"
            } else {
                holder.departmentName.text = "项目组 (${team.size})"
            }
            holder.indicator.isSelected = isExpanded
            return convertView
        }

        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
            var convertView = convertView
            val holder: ChildHolder
            convertView = LayoutInflater.from(context).inflate(R.layout.item_team_organization_chlid, null)
            holder = ChildHolder(convertView)
            val team = groups[groupPosition][childPosition]
            holder.userName.text = team.name
            if (team.icon.isNullOrEmpty()) {
                holder.userImg.setImageResource(R.drawable.im_team_default)
            } else {
                Glide.with(context)
                        .load(team.icon)
                        .apply(RequestOptions().error(R.drawable.im_team_default))
                        .into(holder.userImg)
            }
            return convertView
        }

    }

    /**
     * 组织架构adapter
     */
    internal inner class TissueAdapter(private val context: Context, private val employees: List<DepartmentBean>) : BaseExpandableListAdapter() {
        override fun getGroup(groupPosition: Int): Any = employees[groupPosition]
        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
        override fun hasStableIds(): Boolean = true
        override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()
        override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()
        override fun getGroupCount(): Int = employees.size
        override fun getChildrenCount(groupPosition: Int): Int {
            return if (employees[groupPosition].data != null) {
                employees[groupPosition].data!!.size
            } else {
                0
            }
        }

        override fun getChild(groupPosition: Int, childPosition: Int): Any? {
            return if (employees[groupPosition].data != null) {
                employees[groupPosition].data!![childPosition]
            } else {
                null
            }
        }

        override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
            var convertView = convertView
            val holder: ParentHolder
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_team_organization_parent, null)
                holder = ParentHolder(convertView)
                convertView!!.tag = holder
            } else {
                holder = convertView.tag as ParentHolder
            }
            val departmentBean = employees[groupPosition]
            holder.departmentName.text = "${departmentBean.de_name} (${employees[groupPosition].data?.size})"
            holder.indicator.isSelected = isExpanded
            return convertView
        }

        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
            var convert = convertView
            val holder: ChildHolder
            convert = LayoutInflater.from(context).inflate(R.layout.item_team_organization_chlid, null)
            holder = ChildHolder(convert)
            if (pathByUri == null) {
                holder.selectIcon.setVisible(true)
            } else {
                holder.selectIcon.setVisible(false)
            }
            employees[groupPosition].data?.let {
                val userBean = it[childPosition]
                val find = alreadySelected.find { it.uid == userBean.uid }
                if (find != null) {
                    if (!selectedCanModify) {
                        holder.selectIcon.imageResource = R.drawable.cannot_select
                    } else {
                        holder.selectIcon.isSelected = true
                    }
                }
                //创建群聊且是自己时,灰色勾选
                if (userBean.uid == mine?.uid && isCreateTeam) {
                    holder.selectIcon.isSelected = false
                    holder.selectIcon.imageResource = R.drawable.cannot_select
                }
                if (userBean.headImage().isNullOrEmpty()) {
                    val ch = userBean.name.first()
                    holder.userImg.setChar(ch)
                } else {
                    Glide.with(context)
                            .load(userBean.headImage())
                            .apply(RequestOptions().error(R.drawable.nim_avatar_default).placeholder(R.drawable.nim_avatar_default))
                            .into(holder.userImg)
                }
                holder.userName.text = userBean.name
                holder.userPosition.text = userBean.position

                holder.itemView.setOnClickFastListener {
                    search_edt.clearFocus()
                    if (pathByUri == null) {
                        //点击的item是本人并且是创建群聊时不响应点击事件
                        if (userBean.uid == mine?.uid && isCreateTeam) {
                            return@setOnClickFastListener
                        }
                        //已选中而且不能编辑的情况下不响应点击事件
                        if (!selectedCanModify && find != null) {
                            return@setOnClickFastListener
                        } else {
                            if (alreadySelected.contains(userBean)) {
                                alreadySelected.remove(userBean)
                                holder.selectIcon.isSelected = false
                            } else {
                                alreadySelected.add(userBean)
                                holder.selectIcon.isSelected = true
                            }
                        }
                        selectNumber.text = "已选择: ${alreadySelected.size} 人"

                        contactsAdapter.notifyDataSetChanged()
                    } else {
                        //发送文件消息
                        userBean.accid?.let {
                            NimUIKit.startP2PSession(this@ContactsActivity, userBean.accid, pathByUri)
                        }
                    }
                }
            }
            return convert
        }
    }

    internal inner class ParentHolder(view: View) {
        val indicator: ImageView = view.findViewById<ImageView>(R.id.indicator)
        val departmentName: TextView = view.findViewById(R.id.departmentName)

    }

    internal inner class ChildHolder(view: View) {
        val selectIcon: ImageView = view.findViewById<ImageView>(R.id.selectIcon) as ImageView
        val userImg: CircleImageView = view.findViewById<CircleImageView>(R.id.userHeadImg) as CircleImageView
        val userName: TextView = view.findViewById<TextView>(R.id.userName) as TextView
        val userPosition: TextView = view.findViewById<TextView>(R.id.userPosition) as TextView
        val itemView: View = view
    }

    internal inner class ContactHolder(view: View) : RecyclerHolder<UserBean>(view) {
        val selectIcon = view.find<ImageView>(R.id.selectIcon)
        val userImg = view.find<CircleImageView>(R.id.userHeadImg)
        val userName = view.find<TextView>(R.id.userName)
        val userPosition = view.find<TextView>(R.id.userPosition)
        override fun setData(view: View, userBean: UserBean, position: Int) {
            val find = alreadySelected.find { it.uid == userBean.uid }
            selectIcon.visibility = View.VISIBLE
            if (find != null) {
                if (!selectedCanModify) {
                    selectIcon.imageResource = R.drawable.cannot_select
                } else {
                    selectIcon.isSelected = true
                }
            }
            //创建群聊且是自己时,灰色勾选
            if (userBean.uid == mine?.uid && isCreateTeam) {
                selectIcon.isSelected = false
                selectIcon.imageResource = R.drawable.cannot_select
            }
            if (userBean.headImage().isNullOrEmpty()) {
                val ch = userBean.name.first()
                userImg.setChar(ch)
            } else {
                Glide.with(this@ContactsActivity)
                        .load(userBean.headImage())
                        .apply(RequestOptions().error(R.drawable.nim_avatar_default).placeholder(R.drawable.nim_avatar_default))
                        .into(userImg)
            }
            var name = userBean.name
            if (searchKey.isNotEmpty()) {
                name = name.replaceFirst(searchKey, "<font color='#1787fb'>$searchKey</font>")
            }
            userName.text = Html.fromHtml(name)
            userPosition.text = userBean.position
            view.setOnClickFastListener {
                search_edt.clearFocus()
                if (pathByUri == null) {
                    //点击的item是本人并且是创建群聊时不响应点击事件
                    if (userBean.uid == mine?.uid && isCreateTeam) {
                        return@setOnClickFastListener
                    }
                    //已选中而且不能编辑的情况下不响应点击事件
                    if (!selectedCanModify && find != null) {
                        return@setOnClickFastListener
                    } else {
                        if (alreadySelected.contains(userBean)) {
                            alreadySelected.remove(userBean)
                            selectIcon.isSelected = false
                        } else {
                            alreadySelected.add(userBean)
                            selectIcon.isSelected = true
                        }
                    }
                    selectNumber.text = "已选择: ${alreadySelected.size} 人"

                    tissueAdapter.notifyDataSetChanged()
                } else {
                    //发送文件消息
                    userBean.accid?.let {
                        NimUIKit.startP2PSession(this@ContactsActivity, userBean.accid, pathByUri)
                    }
                }
            }
        }
    }
}
