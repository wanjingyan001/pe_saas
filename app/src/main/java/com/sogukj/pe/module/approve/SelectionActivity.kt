package com.sogukj.pe.module.approve

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.module.approve.baseView.viewBean.*
import com.sogukj.pe.service.ApproveService
import com.sogukj.pe.widgets.CircleImageView
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_selection.*
import kotlinx.android.synthetic.main.item_approval_selection_list.view.*
import org.jetbrains.anko.ctx

class SelectionActivity : ToolbarActivity() {
    /**
     * 选择类型
     */
    private val skipSite: String by extraDelegate(Extras.TYPE, "")
    /**
     * 是否多选
     */
    private val multiple: Boolean by extraDelegate(Extras.FLAG, false)
    /**
     * 基金项目关联控件中获取项目需要传基金id
     */
    private val selectedFundId: Int? by extraDelegate(Extras.ID)
    private var requestUrl = ""
    private lateinit var listAdapter: RecyclerAdapter<Any>
    /**
     * 城市adapter
     */
    private lateinit var cityAdapter: CityAdapter
    private val citys by lazy { mutableListOf<MultiItemEntity>() }

    /**
     * 成员adapter
     */
    private lateinit var userAdapter: UserAdapter
    private val users by lazy { mutableListOf<MultiItemEntity>() }
    private lateinit var selectedUsers: MutableList<ApproveValueBean>
    private lateinit var selectedCities: MutableList<ApproveValueBean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selection)
        setBack(true)
        when (skipSite.toInt()) {
            7 -> {
                title = "请选择城市"
                cityAdapter = CityAdapter(citys)
                selectedCities = intent.getSerializableExtra(Extras.LIST) as MutableList<ApproveValueBean>
                recyclerList.apply {
                    layoutManager = LinearLayoutManager(ctx)
                    adapter = cityAdapter
                    addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
                }
            }

            9 -> {
                title = "请选择成员"
                userAdapter = UserAdapter(users)
                selectedUsers = intent.getSerializableExtra(Extras.LIST) as MutableList<ApproveValueBean>

                recyclerList.apply {
                    layoutManager = LinearLayoutManager(ctx)
                    adapter = userAdapter
                    addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
                }
            }

            else -> {
                when (skipSite.toInt()) {
                    1 -> {
                        requestUrl = "/api/Skip/projectList"
                        title = "请选择项目"
                    }
                    2 -> {
                        requestUrl = "/api/Skip/fundList"
                        title = "请选择基金"
                    }
                    3 -> {
                        requestUrl = "/api/Skip/foreignList"
                        title = "请选择外资"
                    }
                    10 -> {
                        requestUrl = "/api/Skip/fundProject"
                        title = "请选择项目"
                    }
                    11 -> {
                        requestUrl = "/api/Skip/departList"
                        title = "请选择部门"
                    }
                    12 -> {
                        requestUrl = "/api/Skip/relateApprove"
                        title = "请选择审批单"
                    }
                    else -> throw ClassCastException("类型错误")
                }
                listAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
                    ListHolder(_adapter.getView(R.layout.item_approval_selection_list, parent))
                }
                listAdapter.onItemClick = { v, position ->
                    val bean = listAdapter.dataList[position]
                    val intent = Intent()
                    if (bean is ApproveValueBean) {
                        intent.putExtra(Extras.BEAN, bean)
                    }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
                recyclerList.apply {
                    layoutManager = LinearLayoutManager(ctx)
                    adapter = listAdapter
                    addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
                }
            }
        }
        getList()
    }


    private fun getList() {
        requestUrl.isNotEmpty().yes {
            SoguApi.getService(application, ApproveService::class.java)
                    .selectionList(requestUrl, fund_id = selectedFundId.toString())
                    .execute {
                        onNext { payload ->
                            payload.isOk.yes {
                                payload.payload?.let {
                                    listAdapter.refreshData(it)
                                }
                            }.otherWise {
                                showErrorToast(payload.message)
                            }
                        }
                    }
        }.otherWise {
            when (skipSite.toInt()) {
                7 -> {
                    SoguApi.getService(application, ApproveService::class.java)
                            .selectionCity()
                            .execute {
                                onNext { payload ->
                                    payload.isOk.yes {
                                        payload.payload?.let {
                                            val list = mutableListOf<MultiItemEntity>()
                                            it.forEach { city ->
                                                city.children.forEach { cc ->
                                                    city.addSubItem(cc)
                                                    if (selectedCities.find { it.id == cc.id } != null) {
                                                        cityAdapter.selected.add(cc)
                                                    }
                                                }
                                                list.add(city)
                                            }
                                            citys.addAll(list)
                                            cityAdapter.notifyDataSetChanged()
                                            cityAdapter.expandAll()
                                        }
                                    }.otherWise {
                                        showErrorToast(payload.message)
                                    }
                                }
                            }
                }
                9 -> {
                    SoguApi.getService(application, ApproveService::class.java)
                            .approvalUsers()
                            .execute {
                                onNext { payload ->
                                    payload.isOk.yes {
                                        payload.payload?.let {
                                            val list = mutableListOf<MultiItemEntity>()
                                            it.forEach { dep ->
                                                dep.children.forEach { uc ->
                                                    dep.addSubItem(uc)
                                                    if (selectedUsers.find { it.id == uc.id } != null) {
                                                        userAdapter.selected.add(uc)
                                                    }
                                                }
                                                list.add(dep)
                                            }
                                            users.addAll(list)
                                            userAdapter.notifyDataSetChanged()
                                            userAdapter.expandAll()
                                        }
                                    }.otherWise {
                                        showErrorToast(payload.message)
                                    }
                                }
                            }
                }
            }
        }
    }

    override fun onBackPressed() {
        when (skipSite.toInt()) {
            7 -> {
                multiple.yes {
                    val list = ArrayList<ApproveValueBean>()
                    val map1 = cityAdapter.selected.map {
                        it as CChildren
                        ApproveValueBean(name = it.name, id = it.id)
                    }
                    list.addAll(map1)
                    val intent = Intent()
                    intent.putExtra(Extras.BEAN, list)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }.otherWise {
                    super.onBackPressed()
                }
            }
            9 -> {
                multiple.yes {
                    val list = ArrayList<ApproveValueBean>()
                    val map1 = userAdapter.selected.map {
                        it as UChild
                        ApproveValueBean(name = it.name, id = it.id)
                    }
                    list.addAll(map1)
                    val intent = Intent()
                    intent.putExtra(Extras.BEAN, list)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }.otherWise {
                    super.onBackPressed()
                }
            }
            else -> {
                super.onBackPressed()
            }
        }

    }

    /**
     * 项目,基金,外资,基金项目关联,部门
     */
    inner class ListHolder(itemView: View) : RecyclerHolder<Any>(itemView) {
        @SuppressLint("SetTextI18n")
        override fun setData(view: View, data: Any, position: Int) {
            if (data is ApproveValueBean) {
                ifNotNullReturnBlo(data.title, data.number) { v1, v2 ->
                    view.itemTv.text = v1 + v2
                }.no {
                    view.itemTv.text = data.name
                }
            }
        }
    }

    /**
     * 城市
     */
    inner class CityAdapter(data: List<MultiItemEntity>) : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(data) {
        val selected = mutableListOf<MultiItemEntity>()

        init {
            addItemType(1, R.layout.item_approve_province)
            addItemType(2, R.layout.item_approve_city)
        }

        override fun convert(helper: BaseViewHolder, item: MultiItemEntity) {
            when (helper.itemViewType) {
                1 -> {
                    item as CityBean
                    helper.setText(R.id.provinceName, item.name)
                            .setImageResource(R.id.indicator, if (item.isExpanded) R.drawable.jt_copy2 else R.drawable.jt_copy)
                    helper.itemView.clickWithTrigger {
                        val pos = helper.adapterPosition
                        item.isExpanded.yes {
                            collapse(pos)
                        }.otherWise {
                            expand(pos)
                        }
                    }
                }
                2 -> {
                    item as CChildren
                    helper.setText(R.id.cityName, item.name)
                    val icon = helper.getView<ImageView>(R.id.selectIcon)
                    icon.setVisible(multiple)
                    icon.isSelected = selected.contains(item)
                    helper.itemView.clickWithTrigger {
                        multiple.yes {
                            if (selected.contains(item)) {
                                selected.remove(item)
                            } else {
                                if (selected.size >= 6) {
                                    showErrorToast("最多选择6个城市")
                                } else {
                                    selected.add(item)
                                }
                            }
                            notifyItemChanged(helper.adapterPosition)
                        }.otherWise {
                            selected.clear()
                            selected.add(item)
                            val list = ArrayList<ApproveValueBean>()
                            val map1 = selected.map {
                                it as CChildren
                                ApproveValueBean(name = it.name, id = it.id)
                            }
                            list.addAll(map1)
                            val intent = Intent()
                            intent.putExtra(Extras.BEAN, list)
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        }
                    }
                }
            }
        }
    }

    /**
     * 成员选择
     */
    inner class UserAdapter(data: List<MultiItemEntity>) : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(data) {
        val selected = mutableListOf<MultiItemEntity>()

        init {
            addItemType(1, R.layout.item_approval_user_dep)
            addItemType(2, R.layout.item_approval_user)
        }

        override fun convert(holder: BaseViewHolder, item: MultiItemEntity) {
            when (holder.itemViewType) {
                1 -> {
                    item as ApprovalUser
                    holder.setText(R.id.depName, item.name)
                            .setImageResource(R.id.indicator, if (item.isExpanded) R.drawable.jt_copy2 else R.drawable.jt_copy)
                    holder.itemView.clickWithTrigger {
                        val pos = holder.adapterPosition
                        item.isExpanded.yes {
                            collapse(pos)
                        }.otherWise {
                            expand(pos)
                        }
                    }
                }
                2 -> {
                    item as UChild
                    holder.setText(R.id.userName, item.name)
                    val header = holder.getView<CircleImageView>(R.id.userHeadImg)
                    Glide.with(this@SelectionActivity)
                            .load(item.url)
                            .into(header)
                    val selectIcon = holder.getView<ImageView>(R.id.selectIcon)
                    selectIcon.setVisible(multiple)
                    selectIcon.isSelected = selected.contains(item)
                    holder.itemView.clickWithTrigger {
                        multiple.yes {
                            selected.contains(item).yes {
                                selected.remove(item)
                            }.otherWise {
                                selected.add(item)
                            }
                            notifyItemChanged(holder.adapterPosition)
                        }.otherWise {
                            selected.clear()
                            selected.add(item)
                            val list = ArrayList<ApproveValueBean>()
                            val map1 = selected.map {
                                it as UChild
                                ApproveValueBean(name = it.name, id = it.id)
                            }
                            list.addAll(map1)
                            val intent = Intent()
                            intent.putExtra(Extras.BEAN, list)
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        }
                    }
                }
            }
        }
    }
}
