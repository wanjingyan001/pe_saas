package com.sogukj.pe.module.project.originpro

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.ApprovalHisInfo
import com.sogukj.pe.bean.ProjectPostBean
import com.sogukj.pe.widgets.indexbar.RecycleViewDivider
import kotlinx.android.synthetic.main.activity_project_show.*
import kotlinx.android.synthetic.main.commom_blue_title.*
import kotlinx.android.synthetic.main.project_show_bottom.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find

/**
 * Created by CH-ZH on 2018/9/19.
 */
class ProjectApprovalShowActivity : ToolbarActivity() {
    private lateinit var postAdapter: RecyclerAdapter<ProjectPostBean>
    private lateinit var approveAdapter:RecyclerAdapter<ApprovalHisInfo>
    private val postBeans = ArrayList<ProjectPostBean>()
    private val approveInfos = ArrayList<ApprovalHisInfo>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_show)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        setBack(true)
        setTitle("中兴资产股份有限公司")
        toolbar_title.maxEms = 12
    }

    private fun initData() {
        for (i in 0 .. 2){
            val postBean = ProjectPostBean()
            postBean.name = "中国证券监督管理委员….pdf${i}"
            postBean.time = "01/09 10:32:07${i}"
            postBeans.add(postBean)
        }
        postAdapter = RecyclerAdapter(this){_adapter,parent,_ ->
            ProjectPostHolder(_adapter.getView(R.layout.item_approval_post, parent))
        }
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.addItemDecoration(RecycleViewDivider(ctx,LinearLayoutManager.VERTICAL))
        postAdapter.dataList.addAll(postBeans)
        recycler_view.adapter = postAdapter


        for (i in 0 .. 2){
            val approveInfo = ApprovalHisInfo()
            approveInfos.add(approveInfo)
        }
        approveAdapter = RecyclerAdapter(this){_adapter,parent,_ ->
            ProjectApproveHolder(_adapter.getView(R.layout.item_approve_list, parent))
        }
        approve_list.layoutManager = LinearLayoutManager(this)
        approveAdapter.dataList.addAll(approveInfos)
        approve_list.adapter = approveAdapter
    }

    private fun bindListener() {
        postAdapter.onItemClick = {v,position ->
            //pdf预览
            showErrorToast("pdf预览")
        }

        tv_refuse.setOnClickListener {
            //退回修改
            showConfirmDialog()
        }

        tv_agree.setOnClickListener {
            //同意通过

        }

        tv_agree_lxh.setOnClickListener {
            //同意上立项会
        }
    }

    private fun showConfirmDialog() {
        val title =  "是否确认否决审批？"
        val build = MaterialDialog.Builder(this)
                .theme(Theme.DARK)
                .customView(R.layout.layout_confirm_approve, false)
                .canceledOnTouchOutside(false)
                .build()
        build.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val titleTv = build.find<TextView>(R.id.confirm_title)
        val cancel = build.find<TextView>(R.id.cancel_comment)
        val confirm = build.find<TextView>(R.id.confirm_comment)
        titleTv.text = title
        cancel.setOnClickListener {
            if (build.isShowing) {
                build.dismiss()
            }
        }
        confirm.setOnClickListener {
            if (build.isShowing) {
                build.dismiss()
            }
            //确认否决
        }
        build.show()
    }

    inner class ProjectPostHolder(itemView:View) : RecyclerHolder<ProjectPostBean>(itemView){
        val pdfIcon = itemView.findViewById<ImageView>(R.id.pdfIcon)
        val time = itemView.findViewById<TextView>(R.id.time)
        val pdfName = itemView.findViewById<TextView>(R.id.pdfName)
        override fun setData(view: View, data: ProjectPostBean, position: Int) {
            pdfName.text = data.name
            time.text = data.time

        }

    }

    inner class ProjectApproveHolder(itemView : View):RecyclerHolder<ApprovalHisInfo>(itemView){
        val iv_image = itemView.findViewById<ImageView>(R.id.iv_image)
        val tv_name = itemView.findViewById<TextView>(R.id.tv_name)
        val tv_agree = itemView.findViewById<TextView>(R.id.tv_agree)
        val tv_time = itemView.findViewById<TextView>(R.id.tv_time)
        val tv_agree_pro = itemView.findViewById<TextView>(R.id.tv_agree_pro)
        val tv_meel_time = itemView.findViewById<TextView>(R.id.tv_meel_time)
        val tv_meel_person = itemView.findViewById<TextView>(R.id.tv_meel_person)
        override fun setData(view: View, data: ApprovalHisInfo, position: Int) {

        }

    }
}