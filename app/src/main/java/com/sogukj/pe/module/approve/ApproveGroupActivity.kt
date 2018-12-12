package com.sogukj.pe.module.approve

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sogukj.pe.ARouterPath
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.module.approve.baseView.viewBean.AGroup
import com.sogukj.pe.module.approve.baseView.viewBean.ApproveGroup
import com.sogukj.pe.service.ApproveService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_approve.*
import kotlinx.android.synthetic.main.activity_approve_group.*
import kotlinx.android.synthetic.main.layout_approve_group_header.view.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity

/**
 * 审批分组界面
 */
@Route(path = ARouterPath.EntryApproveActivity)
class ApproveGroupActivity : ToolbarActivity(), View.OnClickListener {
    private lateinit var groupAdapter: ApproveGroupAdapter
    private val groups = mutableListOf<AGroup>()
    private lateinit var header: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_approve_group)
        title = "审批"
        setBack(true)
        groupAdapter = ApproveGroupAdapter(groups)
        groupList.apply {
            layoutManager = GridLayoutManager(ctx, 4)
            adapter = groupAdapter
        }
        header = layoutInflater.inflate(R.layout.layout_approve_group_header, null)
        header.item_dwsp.setOnClickListener(this)
        header.item_wysp.setOnClickListener(this)
        header.item_wfqd.setOnClickListener(this)
        header.item_cswd.setOnClickListener(this)
        groupAdapter.addHeaderView(header)
        groupAdapter.setOnItemClickListener { _, _, position ->
            val bean = groups[position].t
            bean?.let {
                startActivity<ApproveInitiateActivity>(Extras.ID to bean.id, Extras.NAME to bean.name)
            }
        }
        getApproveGroup()
    }

    private fun getApproveGroup() {
        SoguApi.getService(application, ApproveService::class.java)
                .approveGroup()
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                it.forEach { ag ->
                                    groups.add(AGroup(true, ag.title))
                                    groups.addAll(ag.children.map { AGroup(it) })
                                }
                                groupAdapter.notifyDataSetChanged()
                                hideEmptyView()
                            }
                        }.otherWise {
                            showEmptyView { getApproveGroup() }
                            showErrorToast(payload.message)
                        }
                    }
                    onError {
                        showEmptyView { getApproveGroup() }
                    }
                }
    }

    private fun newApproveListNum() {
        SoguApi.getService(application, ApproveService::class.java)
                .newApproveListNum()
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                header.point.setVisible(it.waitMe != 0)
                                header.point1.setVisible(it.overMe != 0)
                                header.point2.setVisible(it.happenMe != 0)
                                header.point3.setVisible(it.copyMe != 0)
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }

    override fun onResume() {
        super.onResume()
        newApproveListNum()
    }

    override fun onClick(v: View) {
        val type = when (v.id) {
            R.id.item_dwsp -> 1
            R.id.item_wysp -> 2
            R.id.item_wfqd -> 3
            R.id.item_cswd -> 4
            else -> throw Exception("数据有误")
        }
        startActivity<NewApproveListActivity>(Extras.TYPE to type)
    }


    inner class ApproveGroupAdapter(data: List<AGroup>) :
            BaseSectionQuickAdapter<AGroup, BaseViewHolder>(R.layout.item_approve_group, R.layout.item_approve_group_header, data) {
        override fun convertHead(helper: BaseViewHolder, item: AGroup) {
            helper.setText(R.id.tv_title, item.header)
        }

        override fun convert(helper: BaseViewHolder, item: AGroup) {
            helper.setText(R.id.tv_label, item.t.name)
            val icon = helper.getView<ImageView>(R.id.iv_icon)
            Glide.with(ctx).load(item.t.url).apply(RequestOptions().optionalCircleCrop()).thumbnail(0.1f).into(icon)
        }

    }
}
