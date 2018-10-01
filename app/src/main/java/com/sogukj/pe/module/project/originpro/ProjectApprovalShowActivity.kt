package com.sogukj.pe.module.project.originpro

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.sogukj.pe.App
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.ApproveRecordInfo
import com.sogukj.pe.bean.ProjectApproveInfo
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.module.other.OnlinePreviewActivity
import com.sogukj.pe.module.project.originpro.callback.ProjectApproveCallBack
import com.sogukj.pe.module.project.originpro.presenter.ProjectApprovePresenter
import com.sogukj.pe.peUtils.FileTypeUtils
import com.sogukj.pe.service.OtherService
import com.sogukj.pe.widgets.BuildProjectDialog
import com.sogukj.pe.widgets.indexbar.RecycleViewDivider
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_project_show.*
import kotlinx.android.synthetic.main.commom_blue_title.*
import kotlinx.android.synthetic.main.project_show_bottom.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.startActivity
import java.io.File
import java.util.*

/**
 * Created by CH-ZH on 2018/9/19.
 */
class ProjectApprovalShowActivity : ToolbarActivity(),ProjectApproveCallBack{
    override fun setProApproveInfo(infos: List<ProjectApproveInfo>) {
        if (null != infos) {
            if (infos.size > 0) {
                val approveInfo = infos[0]
                if (null != approveInfo) {
                    class_id = approveInfo.class_id
                    val frames = approveInfo.frame
                    if (null != frames && frames.size > 0){
                        setAmountData(frames!!)
                    }
                }
            }
            if (infos.size > 1){
                val approveInfo = infos[1]
                if (null != approveInfo){
                    if (null != approveInfo.files){
                        setFilesData(approveInfo.files)
                    }
                }
            }
        }
    }

    private fun setFilesData(files: List<ProjectApproveInfo.ApproveFile>?) {
        postAdapter.dataList.clear()
        postAdapter.dataList.addAll(files!!)
        postAdapter.notifyDataSetChanged()
    }

    private fun setAmountData(frames: List<ProjectApproveInfo.ApproveInfo>) {
        if (null != piv){
            piv.setAmountData(frames)
        }
    }

    override fun createApproveSuccess() {

    }

    override fun goneCommitLodding() {

    }

    private lateinit var postAdapter: RecyclerAdapter<ProjectApproveInfo.ApproveFile>
    private lateinit var approveAdapter:RecyclerAdapter<ApproveRecordInfo.ApproveFlow>
    private var approveInfos = ArrayList<ApproveRecordInfo.ApproveFlow>()
    private var project: ProjectBean? = null
    private var presenter : ProjectApprovePresenter? = null
    private var class_id : Int ? = null
    private var dialog : BuildProjectDialog ? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_show)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        setBack(true)
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean?
        setTitle(project!!.name)
        toolbar_title.maxEms = 12

        presenter = ProjectApprovePresenter(this,this)
        dialog = BuildProjectDialog()
    }

    private fun initData() {

        postAdapter = RecyclerAdapter(this){_adapter,parent,_ ->
            ProjectPostHolder(_adapter.getView(R.layout.item_approval_post, parent))
        }
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.addItemDecoration(RecycleViewDivider(ctx,LinearLayoutManager.VERTICAL))
        recycler_view.adapter = postAdapter

        approveAdapter = RecyclerAdapter(this){_adapter,parent,_ ->
            ProjectApproveHolder(_adapter.getView(R.layout.item_approve_list, parent))
        }
        approve_list.layoutManager = LinearLayoutManager(this)
        approveAdapter.dataList.addAll(approveInfos)
        approve_list.adapter = approveAdapter
        if (null != project){
            getApprevoRecordInfo()
        }

        if (null != presenter){
            presenter!!.getProApproveInfo(project!!.company_id!!,project!!.floor!!)
        }
    }

    private fun getApprevoRecordInfo() {
        SoguApi.getService(App.INSTANCE,OtherService::class.java)
                .getApproveRecord(project!!.company_id!!,project!!.floor!!)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val recordInfo = payload.payload
                            if (null != recordInfo){
                                val flow = recordInfo.flow
                                if (null != flow && flow.size > 0){
                                    approveInfos = (flow as ArrayList<ApproveRecordInfo.ApproveFlow>?)!!
                                    approveAdapter.dataList.clear()
                                    approveAdapter.dataList.addAll(flow)
                                    approveAdapter.notifyDataSetChanged()
                                }
                                val click = recordInfo.click
                                if (null != click){
                                    setEditStatus(click)
                                }
                            }
                        }else{
                            showErrorToast(payload.message)
                        }
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("获取数据失败")
                    }
                }
    }

    private fun setEditStatus(click: Int) {
        if (click == 1){
            //审批人
            tv_edit.visibility = View.GONE
            ps_bottom.visibility = View.VISIBLE
        }else if (click == 2){
            //发起人
            tv_edit.visibility = View.VISIBLE
            tv_edit.text = "编辑"
            ps_bottom.visibility = View.GONE
        }else{
            tv_edit.visibility = View.GONE
            ps_bottom.visibility = View.GONE
        }
    }

    private fun bindListener() {
        postAdapter.onItemClick = {v,position ->
            //预览
            val dataList = postAdapter.dataList
            if (null != dataList && dataList.size > 0 && null != dataList[position]){
                OnlinePreviewActivity.start(this,dataList[position].url,dataList[position].file_name)
            }
        }

        tv_refuse.setOnClickListener {
            //退回修改
            showConfirmDialog()
        }

        tv_agree.setOnClickListener {
            //同意通过
            if (null != dialog){
                dialog!!.showAgreeBuildProDialog(this,project)
            }else{
                BuildProjectDialog().showAgreeBuildProDialog(this,project)
            }
        }

        tv_agree_lxh.setOnClickListener {
            //同意上立项会
            if (null != dialog){
                dialog!!.showAgreeBuildLxh(this,project)
            }else{
                BuildProjectDialog().showAgreeBuildLxh(this,project)
            }
        }

        tv_edit.setOnClickListener {
            //编辑
            startActivity<ProjectApprovalActivity>(Extras.DATA to project)
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

    inner class ProjectPostHolder(itemView:View) : RecyclerHolder<ProjectApproveInfo.ApproveFile>(itemView){
        val pdfIcon = itemView.findViewById<ImageView>(R.id.pdfIcon)
        val time = itemView.findViewById<TextView>(R.id.time)
        val pdfName = itemView.findViewById<TextView>(R.id.pdfName)
        override fun setData(view: View, data: ProjectApproveInfo.ApproveFile, position: Int) {
            pdfName.text = data.file_name
            time.text = data.date
            pdfIcon.imageResource = FileTypeUtils.getFileType(data.file_name).icon
        }

    }

    inner class ProjectApproveHolder(itemView : View):RecyclerHolder<ApproveRecordInfo.ApproveFlow>(itemView){
        val iv_image = itemView.findViewById<ImageView>(R.id.iv_image) //头像
        val tv_name = itemView.findViewById<TextView>(R.id.tv_name) //名称
        val tv_agree = itemView.findViewById<TextView>(R.id.tv_agree)  //审批状态 -2=>失效审批，-1=否决，0=待审批，1=同意通过，2=同意上立项会 ,3=>重新发起
        val tv_time = itemView.findViewById<TextView>(R.id.tv_time) //时间
        val tv_agree_pro = itemView.findViewById<TextView>(R.id.tv_agree_pro) //同意上立项会
        val tv_meel_time = itemView.findViewById<TextView>(R.id.tv_meel_time) //会议时间
        val tv_meel_person = itemView.findViewById<TextView>(R.id.tv_meel_person) //会议人员
        val view_bg = itemView.find<View>(R.id.view_bg) //会议背景
        val tv_meel_plan = itemView.find<TextView>(R.id.tv_meel_plan)
        val view = itemView.find<View>(R.id.view) //间隔
        val view_line2 = itemView.find<View>(R.id.view_line2) //有会议线
        val view_line1 = itemView.find<View>(R.id.view_line1) //无会议线
        val ll_bottom = itemView.find<LinearLayout>(R.id.ll_bottom)
        val rl_suggest = itemView.find<RelativeLayout>(R.id.rl_suggest)
        val tv_suggest = itemView.find<TextView>(R.id.tv_suggest)
        val tv_file = itemView.find<TextView>(R.id.tv_file)
        val ll_files = itemView.find<LinearLayout>(R.id.ll_files)
        val fl_assign_approve = itemView.find<FrameLayout>(R.id.fl_assign_approve)
        val ll_assign = itemView.find<LinearLayout>(R.id.ll_assign)
        val tv_assign_person = itemView.find<TextView>(R.id.tv_assign_person)
        override fun setData(view: View, data: ApproveRecordInfo.ApproveFlow, position: Int) {
            if (null == data) return
            Glide.with(this@ProjectApprovalShowActivity).load(data.url).into(iv_image)
            tv_name.text = data.name
            tv_time.text = data.approve_time
            when(data.status){
                -1 -> {
                    //否决
                    tv_agree.text = "已否决"
                    tv_agree.setTextColor(Color.parseColor("#FF5858"))
                    tv_agree_pro.visibility = View.GONE
                    tv_meel_time.visibility = View.GONE
                    tv_meel_person.visibility = View.GONE
                    view_bg.visibility = View.GONE
                    tv_meel_plan.visibility = View.GONE
                    view.visibility = View.GONE
                    view_line2.visibility = View.GONE
                    view_line1.visibility = View.VISIBLE
                    ll_bottom.visibility = View.GONE
                }
                0 -> {
                    //待审批
                    tv_agree.text = "待审批"
                    tv_agree.setTextColor(Color.parseColor("#FFA715"))
                    tv_agree_pro.visibility = View.GONE
                    tv_meel_time.visibility = View.GONE
                    tv_meel_person.visibility = View.GONE
                    view_bg.visibility = View.GONE
                    tv_meel_plan.visibility = View.GONE
                    view.visibility = View.GONE
                    view_line2.visibility = View.GONE
                    view_line1.visibility = View.VISIBLE
                    ll_bottom.visibility = View.GONE
                }
                3 -> {
                    //重新发起
                    tv_agree.text = "重新发起"
                    tv_agree.setTextColor(Color.parseColor("#808080"))
                    tv_agree_pro.visibility = View.GONE
                    tv_meel_time.visibility = View.GONE
                    tv_meel_person.visibility = View.GONE
                    view_bg.visibility = View.GONE
                    tv_meel_plan.visibility = View.GONE
                    view.visibility = View.GONE
                    view_line2.visibility = View.GONE
                    view_line1.visibility = View.VISIBLE
                    ll_bottom.visibility = View.GONE
                }
                1 -> {
                    //同意通过
                    tv_agree.text = "同意通过"
                    tv_agree.setTextColor(Color.parseColor("#50D59D"))
                    tv_agree_pro.visibility = View.GONE
                    tv_meel_time.visibility = View.GONE
                    tv_meel_person.visibility = View.GONE
                    view_bg.visibility = View.GONE
                    tv_meel_plan.visibility = View.GONE
                    view.visibility = View.GONE
                    view_line2.visibility = View.GONE
                    view_line1.visibility = View.VISIBLE
                    if (!data.content.isNullOrEmpty()){
                        val span = SpannableStringBuilder("意见意见${data.content}")
                        span.setSpan(ForegroundColorSpan(Color.TRANSPARENT),0,4, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                        tv_suggest.text = span
                        rl_suggest.visibility = View.VISIBLE
                    }else{
                        rl_suggest.visibility = View.GONE
                    }
                    if (null != data.file && data.file!!.size > 0){
                        //有文件
                        for (file in data.file!!){
                            val item = View.inflate(context,R.layout.file_item,null)
                            val iv_image = item.find<ImageView>(R.id.iv_image)
                            val tv_name = item.find<TextView>(R.id.tv_name)
                            Glide.with(context).load(file.url).into(iv_image)
                            tv_name.text = file.file_name
                            ll_files.addView(item)
                        }
                    }else{
                        //无文件
                        tv_file.visibility = View.GONE
                        ll_files.visibility = View.GONE
                    }

                }
                2 -> {
                    //同意上立项会
                }

            }

            fl_assign_approve.setOnClickListener {
                //分配审批
                if (null != dialog){
                    dialog!!.allocationApprove(this@ProjectApprovalShowActivity,project)
                }else{
                    BuildProjectDialog().allocationApprove(this@ProjectApprovalShowActivity,project)
                }
            }
        }

    }
    companion object {
        val REQ_SELECT_FILE = 0x1005
        val REQ_LXH_FILE = 0x1006
        val REW_SELECT_TIME = 0x1007
        val REQ_ADD_MEMBER = 0x1008
        val REQ_ADD_ALLOCATION = 0x1009
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (null != data){
            when(requestCode){
                REQ_SELECT_FILE -> {
                    val paths = data?.getStringArrayListExtra(Extras.LIST)
                    paths?.forEach {
                        val info = ProjectApproveInfo.ApproveFile()
                        val file = File(it)
                        info.file = file
                        info.file_name = file.name
                        if (null != dialog){
                            dialog!!.addFileData(info,file,0)
                        }else{
                            BuildProjectDialog().addFileData(info,file,0)
                        }
                    }
                }

                REQ_LXH_FILE -> {
                    val paths = data?.getStringArrayListExtra(Extras.LIST)
                    paths?.forEach {
                        val info = ProjectApproveInfo.ApproveFile()
                        val file = File(it)
                        info.file = file
                        info.file_name = file.name
                        if (null != dialog){
                            dialog!!.addFileData(info,file,1)
                        }else{
                            BuildProjectDialog().addFileData(info,file,1)
                        }
                    }
                }

                REW_SELECT_TIME -> {
                    val date = data.getSerializableExtra(Extras.DATA)
                    if (null != dialog){
                        dialog!!.setTime(date as Date?)
                    }else{
                        BuildProjectDialog().setTime(date as Date?)
                    }
                }

                REQ_ADD_MEMBER -> {
                    val list = data.getSerializableExtra(Extras.DATA) as ArrayList<UserBean>
                    val infos = ArrayList<UserBean>()
                    infos.clear()
                    infos.addAll(list)
                    infos.add(UserBean())
                    if (null != dialog){
                        dialog!!.setMemberData(infos)
                    }else{
                        BuildProjectDialog().setMemberData(infos)
                    }
                }

                REQ_ADD_ALLOCATION -> {
                    val list = data.getSerializableExtra(Extras.DATA) as ArrayList<UserBean>
                    val infos = ArrayList<UserBean>()
                    infos.clear()
                    infos.addAll(list)
                    infos.add(UserBean())
                    if (null != dialog){
                        dialog!!.setAllocationData(infos)
                    }else{
                        BuildProjectDialog().setAllocationData(infos)
                    }
                }
            }
        }
    }

}