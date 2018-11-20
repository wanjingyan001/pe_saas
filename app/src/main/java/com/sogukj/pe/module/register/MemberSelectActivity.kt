package com.sogukj.pe.module.register

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import com.amap.api.mapcore.util.it
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.DepartmentBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.service.UserService
import com.sogukj.pe.widgets.CircleImageView
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_member_select.*
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import org.jetbrains.anko.info

class MemberSelectActivity : ToolbarActivity() {
    /**
     * 已选中人员
     */
    private lateinit var alreadySelected: MutableSet<UserBean>
    /**
     * 是否是单选
     */
    private var isSingle: Boolean = false
    /**
     * 是否是选择管理员
     */
    private var isAdmin: Boolean = false
    /**
     * 组织架构adapter
     */
    private lateinit var tissueAdapter: TissueAdapter
    private lateinit var memberAdapter: RecyclerAdapter<UserBean>
    private val departList = ArrayList<DepartmentBean>() //组织架构
    private val model by lazy { ViewModelProviders.of(this).get(OrganViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_select)
        setBack(true)
        title = "选择部门成员"
        getDataFromIntent()
        initTissueAdapter()
        memberAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            MemberHolder(_adapter.getView(R.layout.item_team_organization_chlid, parent))
        }
        memberList.apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = memberAdapter
        }
        getMemberList()
        confirmTv.clickWithTrigger {
            val list = ArrayList<UserBean>()
            list.addAll(alreadySelected)
            intent.putExtra(Extras.DATA, list)
            setResult(Extras.RESULTCODE, intent)
            finish()
        }
    }

    private fun getDataFromIntent() {
        val list = intent.getSerializableExtra(Extras.LIST) as ArrayList<UserBean?>
        alreadySelected = list.filter { it != null }.toMutableSet() as MutableSet<UserBean>
        isSingle = intent.getBooleanExtra(Extras.FLAG, false)
        isAdmin = intent.getBooleanExtra(Extras.FLAG2, false)
        if (alreadySelected.any { it?.user_id != null }) {
            selectNumber.text = "已选择: ${alreadySelected.size} 人"
        }
        title = if (isSingle){
            "选择部门负责人"
        }else{
            "选择部门成员"
        }
        var titleStr = intent.getStringExtra(Extras.TITLE)
        if (!titleStr.isNullOrEmpty()) {
            title = titleStr
        }
    }

    private fun initTissueAdapter() {
        tissueAdapter = TissueAdapter(this, departList)
        organizationList.setAdapter(tissueAdapter)
        organizationList.setVisible(false)
//        getTissueData()
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
                        showCustomToast(R.drawable.icon_toast_fail, "数据获取失败")
                    }
                }
    }

    private fun getMemberList() = runBlocking {
        val key = sp.getString(Extras.CompanyKey, "")
        model.loadMemberList(application, key).observe(this@MemberSelectActivity, Observer {
            val data = if (isAdmin) {
                it?.filter { it.is_admin == 0 }
            } else {
                it
            }
            data?.forEach {
                it.uid = it.user_id
            }
            data?.let {
                memberAdapter.refreshData(it)
                (::alreadySelected.isLateinit &&  alreadySelected.isNotEmpty()).yes {
                    it.forEach { user ->
                        val find = alreadySelected.find { it.user_id == user.user_id }
                        if (find != null) {
                            alreadySelected.remove(find)
                            alreadySelected.add(user)
                        }
                    }
                }
            }
        })
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
            holder.selectIcon.setVisible(true)
            employees[groupPosition].data?.let {
                val userBean = it[childPosition]
                val find = alreadySelected.find { it.uid == userBean.uid }
                holder.selectIcon.isSelected = find != null
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
                    if (alreadySelected.contains(userBean)) {
                        alreadySelected.remove(userBean)
                    } else {
                        if (isSingle) {
                            alreadySelected.clear()
                        }
                        alreadySelected.add(userBean)
                    }
                    selectNumber.text = "已选择: ${alreadySelected.size} 人"
                    tissueAdapter.notifyDataSetChanged()
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

    inner class MemberHolder(itemView: View) : RecyclerHolder<UserBean>(itemView) {
        val selectIcon = itemView.find<ImageView>(R.id.selectIcon)
        val userImg = itemView.find<CircleImageView>(R.id.userHeadImg)
        val userName = itemView.find<TextView>(R.id.userName)
        val userPosition = itemView.find<TextView>(R.id.userPosition)
        override fun setData(view: View, data: UserBean, position: Int) {
            selectIcon.setVisible(true)
            userPosition.setVisible(true)
            if (data.headImage().isNullOrEmpty()) {
                val ch = data.name.first()
                userImg.setChar(ch)
            } else {
                Glide.with(context)
                        .load(data.headImage())
                        .apply(RequestOptions().error(R.drawable.nim_avatar_default).placeholder(R.drawable.nim_avatar_default))
                        .into(userImg)
            }
            val find = alreadySelected.find { it.user_id == data.user_id }
            selectIcon.isSelected = find != null
            userName.text = data.name
            userPosition.text = data.depart_name
            view.clickWithTrigger {
                search_edt.clearFocus()
                if (alreadySelected.contains(data)) {
                    alreadySelected.remove(data)
                } else {
                    if (isSingle) {
                        alreadySelected.clear()
                    }
                    alreadySelected.add(data)
                }
                selectNumber.text = "已选择: ${alreadySelected.size} 人"
                memberAdapter.notifyDataSetChanged()
            }
        }
    }

}
