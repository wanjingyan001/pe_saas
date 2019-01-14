package com.sogukj.pe.module.main

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
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
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.team.TeamService
import com.netease.nimlib.sdk.team.model.Team
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.DepartmentBean
import com.sogukj.pe.bean.MechanismBasicInfo
import com.sogukj.pe.bean.TeamBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.module.im.ImSearchResultActivity
import com.sogukj.pe.module.im.PersonalInfoActivity
import com.sogukj.pe.module.im.TeamCreateActivity
import com.sogukj.pe.module.main.viewModel.OrganizationModel
import com.sogukj.pe.module.main.viewModel.TeamGroupModel
import com.sogukj.pe.module.user.UserActivity
import com.sogukj.pe.peExtended.getEnvironment
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.UserService
import com.sogukj.pe.widgets.CircleImageView
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_team_select.*
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.startActivity
import java.util.*
import kotlin.collections.ArrayList

class TeamSelectFragment : BaseFragment() {
    private val departList = ArrayList<DepartmentBean>() //组织架构
    private val contactList = ArrayList<UserBean>()//最近联系人
    private val resultData = ArrayList<UserBean>()//搜索结果
    var mine: UserBean? = null//自己
    var searchKey = ""
    private lateinit var orgAdapter: OrganizationAdapter
    private lateinit var contactAdapter: ContactAdapter
    private lateinit var resultAdapter: ContactAdapter
    private val selectMap = HashMap<String, Boolean>()
    private val model by lazy {
        ViewModelProviders.of(this).get(OrganizationModel::class.java)
    }

    override val containerViewId: Int
        get() = R.layout.fragment_team_select

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {   // 不在最前端显示 相当于调用了onPause();

        } else {  // 在最前端显示 相当于调用了onResume();
            loadHead()
            doRequest()
        }
    }

    override fun onResume() {
        super.onResume()
        loadHead()
        initGroupDiscuss()
//        doRequest()
    }

    fun loadHead() {
        var header = toolbar_back.getChildAt(0) as CircleImageView
        val user = Store.store.getUser(baseActivity!!)
        if (user?.url.isNullOrEmpty()) {
            if (!user?.name.isNullOrEmpty()) {
                val ch = user?.name?.first()
                header.setChar(ch)
            }
        } else {
            Glide.with(ctx)
                    .load(MyGlideUrl(user?.url))
                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .listener(object : RequestListener<Drawable> {
                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            header.setImageDrawable(resource)
                            return true
                        }

                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            if (!user?.name.isNullOrEmpty()) {
                                val ch = user?.name?.first()
                                header.setChar(ch)
                            }
                            return true
                        }
                    })
                    .into(header)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x789) {
            loadHead()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mine = Store.store.getUser(ctx)
        initSearchView()
        initResultList()
        initOrganizationList()
        //initContactList()
        doRequest()
        initHeader()
        loadHead()
        toolbar_back.setOnClickListener {
            val intent = Intent(context, UserActivity::class.java)
            startActivityForResult(intent, 0x789)
        }

        isSelectUser = false

        loadByIsSelectUser()

        confirmTv.setOnClickListener {
            if (isSelectUser) {
                var alreadyList = ArrayList<UserBean>()//已选中的
                val map = selectMap.filterValues { it }
                if (map.isNotEmpty()) {
                    map.keys.forEach {
                        val id = it
                        departList.forEach {
                            it.data?.forEach {
                                if (id == it.uid.toString()) {
                                    alreadyList.add(it)
                                }
                            }
                        }
                    }
                }
                if (alreadyList.size == 0) {
                    return@setOnClickListener
                }
                mine?.accid?.let {
                    TeamCreateActivity.start(ctx, alreadyList)
                }

                isSelectUser = false
                var list = ArrayList<DepartmentBean>(departList)
                list.forEach {
                    it.data!!.forEach {
                        it.uid.let {
                            selectMap.put(it.toString(), false)
                        }
                    }
                }
                loadByIsSelectUser()
            }
        }

        listContent.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (et_layout.height > 0) {
                if (scrollY >= et_layout.height) {
                    Utils.closeInput(context, search_edt)
                    search_edt.clearFocus()
                }
            }
        }

        et_layout.setOnClickListener {
            ImSearchResultActivity.invoke(activity!!, 1)
        }
    }

    /*
     * false表示仅仅通讯录，只需要群组和组织架构
     * true表示创建群聊，需要常用联系人和组织架构
     */
    var isSelectUser = false

    fun loadByIsSelectUser() {
        if (isSelectUser) {
            team_toolbar_title.text = "创建群组"
            groupDiscuss1.visibility = View.GONE
            IMTeamGroupLayout.visibility = View.GONE
            contactLayout1.visibility = View.VISIBLE
            contactLayout.visibility = View.VISIBLE
            confirmSelectLayout.visibility = View.VISIBLE
        } else {
            team_toolbar_title.text = "通讯录"
            groupDiscuss1.visibility = View.VISIBLE
            IMTeamGroupLayout.visibility = View.VISIBLE
            contactLayout1.visibility = View.VISIBLE
            contactLayout.visibility = View.VISIBLE
            initContactList()
            confirmSelectLayout.visibility = View.GONE
        }

        listContent.smoothScrollTo(0, 0)

        orgAdapter.notifyDataSetChanged()
        contactAdapter.notifyDataSetChanged()
        resultAdapter.notifyDataSetChanged()

        toolbar_menu.setOnClickListener {
            search_edt.clearFocus()
            val alreadySelect = ArrayList<UserBean>()
            alreadySelect.add(Store.store.getUser(ctx)!!)
            startActivity<TeamCreateActivity>(Extras.FLAG to true, Extras.DATA to alreadySelect)
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
                searchKey = search_edt.text.toString()
                searchWithName()
                true
            } else {
                false
            }
        }
        delete1.setOnClickListener {
            Utils.toggleSoftInput(context, search_edt)
            search_edt.setText("")
            search_edt.clearFocus()
        }
        search_edt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (search_edt.text.toString().isEmpty()) {
                    searchKey = ""
                    result_layout.visibility = View.GONE
                }
                if (!search_edt.text.isNullOrEmpty()) {
                    delete1.visibility = View.VISIBLE
                } else {
                    delete1.visibility = View.GONE
                }
            }
        })
    }

    private fun initHeader() {
        val company = sp.getString(Extras.SAAS_BASIC_DATA, "")
        val detail = Gson().fromJson<MechanismBasicInfo?>(company)
        detail?.let {
            val defaultLogo = when (getEnvironment()) {
                "zgh" -> R.mipmap.ic_launcher_zgh
                else -> R.mipmap.ic_launcher_pe

            }
            if (it.logo.isNullOrEmpty()) {
                company_icon.imageResource = defaultLogo
            } else {
                Glide.with(this)
                        .load(MyGlideUrl(it.logo))
                        .apply(RequestOptions().placeholder(defaultLogo).error(defaultLogo).diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(company_icon)
            }
            companyName.text = it.mechanism_name
            allEmployees.setVisible(it.tid != null && it.tid != 0)
            allEmployees.clickWithTrigger {
                NimUIKit.startTeamSession(activity, detail.tid.toString())
            }
        }
    }

    /**
     * 群组
     */
    private fun initGroupDiscuss() {
        //groupDiscuss
        val model = ViewModelProviders.of(this).get(TeamGroupModel::class.java)
        model.requestTeamGroupData()
        model.teamGroup.observe(this, android.arch.lifecycle.Observer { teams ->
            teams?.let {
                TeamGroupName.text = "群聊（${it.size}）"
                it.isNotEmpty().yes {
                    TeamGroupLayout.clickWithTrigger {
                        startActivity<IMTeamGroupActivity>(Extras.TITLE to "群聊",
                                Extras.TYPE to 1)
                    }
                }
            }
        })
        model.discussionGroup.observe(this, android.arch.lifecycle.Observer { teams ->
            teams?.let {
                ProjectGroupName.text = "项目讨论组（${it.size}）"
                it.isNotEmpty().yes {
                    ProjectGroupLayout.clickWithTrigger {
                        startActivity<IMTeamGroupActivity>(Extras.TITLE to "项目讨论组",
                                Extras.TYPE to 2)
                    }
                }
            }
        })
    }

    private fun initOrganizationList() {
        orgAdapter = OrganizationAdapter(departList)
        organizationList.setAdapter(orgAdapter)
    }

    private fun initContactList() {
        contactLayout.layoutManager = LinearLayoutManager(context)
        contactAdapter = ContactAdapter(contactList)
        contactLayout.adapter = contactAdapter
    }

    private fun initResultList() {
        resultAdapter = ContactAdapter(resultData)
        resultList.layoutManager = LinearLayoutManager(context)
        resultList.adapter = resultAdapter
    }

    @SuppressLint("SetTextI18n")
    fun doRequest() {
        model.getUserDepart(ctx)
        model.datas.observe(this, android.arch.lifecycle.Observer {
            it?.let {
                var i = 0
                departList.clear()
                departList.addAll(it)
                orgAdapter.notifyDataSetChanged()
                it.forEach { depart ->
                    depart.data.let { userData ->
                        i += userData!!.size
                        userData.forEach {
                            it.uid.let {
                                selectMap.put(it.toString(), false)
                            }
                        }
                    }
                }
                num.text = "共${i}人"
            }
        })
        var user = Store.store.getUser(ctx)
        user?.accid?.let {
            SoguApi.getService(baseActivity!!.application, UserService::class.java)
                    .recentContacts(it)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            contactList.clear()
                            contactList.addAll(payload.payload!!)
                            contactAdapter.notifyDataSetChanged()
                        }
                    }, { e ->
                        Trace.e(e)
                    })
        }

    }

    private fun searchWithName() {
        val result = ArrayList<UserBean>()
        departList.forEach {
            it.data.let {
                it?.forEach {
                    if (it.name.contains(searchKey)) {//&& it.user_id != mine?.uid
                        result.add(it)
                    }
                }
            }
        }
        result_layout.visibility = View.VISIBLE
        resultData.clear()
        resultData.addAll(result)
        resultAdapter.notifyDataSetChanged()
        if (resultData.size == 0) {
            resultList.visibility = View.GONE
            iv_empty.visibility = View.VISIBLE
        } else {
            resultList.visibility = View.VISIBLE
            iv_empty.visibility = View.GONE
        }
    }

    /**
     * 不再使用展开列表的形式,会有卡顿
     */
//    internal inner class DiscussAdapter(val parents: List<String>, val childs: ArrayList<List<Team>>) : BaseExpandableListAdapter() {
//
//        override fun getGroupCount(): Int = parents.size
//
//        override fun getChildrenCount(groupPosition: Int): Int {
//            return childs[groupPosition].size
//        }
//
//        override fun getGroup(groupPosition: Int): Any = parents[groupPosition]
//
//        override fun getChild(groupPosition: Int, childPosition: Int): Any? {
//            return childs[groupPosition][childPosition]
//        }
//
//        override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()
//
//        override fun getChildId(groupPosition: Int, childPosition: Int): Long =
//                childPosition.toLong()
//
//        override fun hasStableIds(): Boolean = true
//
//        override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {
//            var convertView = convertView
//            val holder: ParentHolder
//            if (convertView == null) {
//                convertView = LayoutInflater.from(context).inflate(R.layout.item_team_organization_parent, null)
//                holder = ParentHolder(convertView)
//                convertView!!.tag = holder
//            } else {
//                holder = convertView.tag as ParentHolder
//            }
//            val title = parents[groupPosition]
//            holder.departmentName.text = "$title (${childs[groupPosition].size})"
//            holder.departTeam.setVisible(false)
//            holder.indicator.isSelected = isExpanded
//            return convertView
//        }
//
//        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
//            var convertView = convertView
//            val holder: ChildHolder
//            convertView = LayoutInflater.from(context).inflate(R.layout.item_team_organization_chlid_2, null)
//            holder = ChildHolder(convertView)
//            convertView!!.tag = holder
//            childs[groupPosition].let {
//                val team = it[childPosition]
//                holder.userName.text = team.name
//                holder.selectIcon.visibility = View.GONE
//                if (team.icon.isNotEmpty()) {
//                    Glide.with(ctx)
//                            .load(MyGlideUrl(team.icon))
//                            .apply(RequestOptions().error(R.drawable.im_team_default).diskCacheStrategy(DiskCacheStrategy.ALL))
//                            .into(holder.userImg)
//                } else {
//                    holder.userImg.imageResource = R.drawable.im_team_default
//                }
//                holder.itemView.setOnClickListener {
//                    search_edt.clearFocus()
//                    NimUIKit.startTeamSession(context, team.id)
//                }
//            }
//            return convertView
//        }
//
//        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
//
//        internal inner class ParentHolder(view: View) {
//            val indicator: ImageView = view.find(R.id.indicator)
//            val departmentName: TextView = view.find(R.id.departmentName)
//            val departTeam: TextView = view.find(R.id.departTeam)
//        }
//
//        internal inner class ChildHolder(view: View) {
//            val selectIcon: ImageView
//            val userImg: CircleImageView
//            val userName: TextView
//            val userPosition: TextView
//            val itemView: View
//
//            init {
//                selectIcon = view.find(R.id.selectIcon)
//                userImg = view.find(R.id.userHeadImg)
//                userName = view.find(R.id.userName)
//                userPosition = view.find(R.id.userPosition)
//                itemView = view
//            }
//        }
//    }

    internal inner class OrganizationAdapter(private val parents: List<DepartmentBean>) : BaseExpandableListAdapter() {

        override fun getGroupCount(): Int = parents.size

        override fun getChildrenCount(groupPosition: Int): Int {
            return if (parents[groupPosition].data != null) {
                parents[groupPosition].data!!.size
            } else {
                0
            }
        }

        override fun getGroup(groupPosition: Int): Any = parents[groupPosition]

        override fun getChild(groupPosition: Int, childPosition: Int): Any? {
            return if (parents[groupPosition].data != null) {
                parents[groupPosition].data!![childPosition]
            } else {
                null
            }
        }

        override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

        override fun getChildId(groupPosition: Int, childPosition: Int): Long =
                childPosition.toLong()

        override fun hasStableIds(): Boolean = true

        override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val holder: ParentHolder
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_team_organization_parent, null)
                holder = ParentHolder(convertView)
                convertView!!.tag = holder
            } else {
                holder = convertView.tag as ParentHolder
            }
            val departmentBean = parents[groupPosition]
            val size = parents[groupPosition].data?.size ?: 0
            holder.departmentName.text = "${departmentBean.de_name} ($size)"

            holder.departTeam.setVisible(departmentBean.tid != null && departmentBean.tid != 0 && size > 2)
            holder.departTeam.clickWithTrigger {
                NimUIKit.startTeamSession(activity, departmentBean.tid.toString())
            }
            holder.indicator.isSelected = isExpanded
            return convertView
        }

        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val holder: ChildHolder
            convertView = LayoutInflater.from(context).inflate(R.layout.item_team_organization_chlid, null)
            holder = ChildHolder(convertView)
            convertView!!.tag = holder
            parents[groupPosition].data?.let {
                val userBean = it[childPosition]
                if (userBean.user_id == mine!!.uid) {
                    holder.selectIcon.imageResource = R.drawable.cannot_select
                }
                if (!userBean.headImage().isNullOrEmpty()) {
                    Glide.with(ctx)
                            .load(MyGlideUrl(userBean.headImage()))
                            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                            .listener(object : RequestListener<Drawable> {
                                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                    return false
                                }

                                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                    holder.userImg.setChar(userBean.name.first())
                                    return false
                                }

                            })
                            .into(holder.userImg)
                } else {
                    holder.userImg.setChar(userBean.name.first())
                }
                holder.userName.text = userBean.name
                holder.userPosition.text = userBean.position
                holder.selectIcon.visibility = View.GONE
            }
            if (isSelectUser) {
                holder.selectIcon.visibility = View.VISIBLE
                val userBean = parents[groupPosition].data!![childPosition]
                userBean.uid.let {
                    holder.selectIcon.isSelected = selectMap[it.toString()]!!
                }
            } else {
                holder.selectIcon.visibility = View.GONE
            }
            holder.itemView.setOnClickListener {
                search_edt.clearFocus()
                val userBean = parents[groupPosition].data!![childPosition]
                if (isSelectUser) {
                    //选人
                    if (userBean.uid != mine!!.uid) {
                        holder.selectIcon.isSelected = !holder.selectIcon.isSelected
                        userBean.uid.let {
                            selectMap.put(it.toString(), holder.selectIcon.isSelected)
                            val map = selectMap.filterValues { it }
                            selectNumber.text = "已选择: ${map.size}人"
                        }
                    }
                } else {
                    //查看详情
                    PersonalInfoActivity.start(ctx, userBean, null)
                }
            }
            return convertView
        }

        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true

        internal inner class ParentHolder(view: View) {
            val indicator: ImageView = view.find(R.id.indicator)
            val departmentName: TextView = view.find(R.id.departmentName)
            val departTeam: TextView = view.find(R.id.departTeam)
        }

        internal inner class ChildHolder(view: View) {
            val selectIcon: ImageView
            val userImg: CircleImageView
            val userName: TextView
            val userPosition: TextView
            val itemView: View

            init {
                selectIcon = view.find(R.id.selectIcon)
                userImg = view.find(R.id.userHeadImg)
                userName = view.find(R.id.userName)
                userPosition = view.find(R.id.userPosition)
                itemView = view
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
                Glide.with(ctx)
                        .load(MyGlideUrl(userBean.headImage()))
                        .apply(RequestOptions().error(R.drawable.nim_avatar_default).placeholder(R.drawable.nim_avatar_default).diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(holder.userImg)
            }
            var name = userBean.name
            if (searchKey.isNotEmpty()) {
                name = name.replaceFirst(searchKey, "<font color='#1787fb'>$searchKey</font>")
            }
            holder.userName.text = Html.fromHtml(name)
            holder.userPosition.text = userBean.position
            holder.selectIcon.visibility = View.GONE
            if (isSelectUser) {
                holder.selectIcon.visibility = View.VISIBLE
                userBean.uid?.let {
                    holder.selectIcon.isSelected = selectMap[it.toString()]!!
                }
            } else {
                holder.selectIcon.visibility = View.GONE
            }
            holder.itemView.setOnClickListener {
                search_edt.clearFocus()
                if (isSelectUser) {
                    //选人
                    if (userBean.uid != mine?.uid) {
                        holder.selectIcon.isSelected = !holder.selectIcon.isSelected
                        userBean.uid?.let {
                            selectMap.put(it.toString(), holder.selectIcon.isSelected)
                            val map = selectMap.filterValues { it }
                            selectNumber.text = "已选择: ${map.size}人"
                        }
                    }
                } else {
                    //查看详情
                    PersonalInfoActivity.start(ctx, userBean, null)
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
                selectIcon = itemView.find(R.id.selectIcon)
                userImg = itemView.find(R.id.userHeadImg)
                userName = itemView.find(R.id.userName)
                userPosition = itemView.find(R.id.userPosition)
            }
        }
    }

    companion object {
        fun newInstance(): TeamSelectFragment {
            val fragment = TeamSelectFragment()
            return fragment
        }
    }
}
