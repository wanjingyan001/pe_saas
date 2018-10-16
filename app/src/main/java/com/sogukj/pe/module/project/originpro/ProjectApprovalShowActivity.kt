package com.sogukj.pe.module.project.originpro

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sogukj.pe.App
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.base.BaseRefreshActivity
import com.sogukj.pe.baselibrary.utils.RefreshConfig
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.*
import com.sogukj.pe.module.other.OnlinePreviewActivity
import com.sogukj.pe.module.project.originpro.callback.ProjectApproveCallBack
import com.sogukj.pe.module.project.originpro.presenter.ProjectApprovePresenter
import com.sogukj.pe.peUtils.FileTypeUtils
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.peUtils.ToastUtil
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
class ProjectApprovalShowActivity : BaseRefreshActivity(),ProjectApproveCallBack{
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
    private var project: ProjectBean? = null
    private var presenter : ProjectApprovePresenter? = null
    private var class_id : Int ? = null
    private var dialog : BuildProjectDialog ? = null
    private var user : UserBean ? = null
    private var floor : Int ? = null
    private var approveDatas = ArrayList<ApproveRecordInfo.ApproveFlow>()
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
        floor = intent.getIntExtra(Extras.FLAG,-1)
        toolbar_title.maxEms = 11
        setTitle(project!!.name)
        presenter = ProjectApprovePresenter(this,this)
        dialog = BuildProjectDialog()
        user = Store.store.getUser(this)
        Glide.with(this)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
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
        if (null != project){
            setLoadding()
            getProjectComBase()
        }

        if (null != presenter){
            presenter!!.getProApproveInfo(project!!.company_id!!,floor!!)
        }
    }

    private fun getProjectComBase() {
        SoguApi.getService(App.INSTANCE,OtherService::class.java)
                .getProBuildInfo(project!!.company_id!!)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val projectInfo = payload.payload
                            if (null != projectInfo){
                               setEditStatus(projectInfo)
                            }
                            getApprevoRecordInfo()
                        }else{
                            ToastUtil.showCustomToast(R.drawable.icon_toast_fail, payload.message, ctx!!)
                        }
                    }
                    onComplete {

                    }

                    onError {
                        it.printStackTrace()
                        ToastUtil.showCustomToast(R.drawable.icon_toast_fail, "获取数据失败", ctx!!)
                    }
                }
    }
    private var enableEdit = false
    private fun setEditStatus(projectInfo: NewProjectInfo) {
        var pm = 0
        var pl = 0
        if (null != projectInfo.duty){
            pm = projectInfo.duty!!.principal!!
        }
        if (null != projectInfo.lead){
            pl = projectInfo.lead!!.leader!!
        }

        if (user!!.uid == pm || user!!.uid == pl){
            //可编辑
            tv_edit.visibility = View.VISIBLE
            tv_edit.text = "编辑"
            enableEdit = true
        }else{
            //不可编辑
            tv_edit.visibility = View.GONE
            enableEdit = false
        }
    }

    open fun getApprevoRecordInfo() {
        SoguApi.getService(App.INSTANCE,OtherService::class.java)
                .getApproveRecord(project!!.company_id!!,floor!!)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val recordInfo = payload.payload
                            if (null != recordInfo){
                                val button = recordInfo.button
                                setApproveButtonStatus(button)

                                val flow = recordInfo.flow
                                if (null != flow && flow.size > 0){
                                    approveDatas.clear()
                                    approveDatas.addAll(flow)

                                    approveAdapter.dataList.clear()
                                    approveAdapter.dataList.addAll(flow)
                                    approve_list.adapter = approveAdapter
                                    approveAdapter.notifyDataSetChanged()
                                    val approveFlow = flow[flow.size - 1]
                                    setApproveEditStatus(approveFlow)
                                    view_file.visibility = View.VISIBLE
                                }else{
                                    view_file.visibility = View.GONE
                                }

                            }
                        }else{
                            showErrorToast(payload.message)
                        }
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("获取数据失败")
                        view_file.visibility = View.GONE
                    }
                }
    }

    private fun setApproveEditStatus(approveFlow: ApproveRecordInfo.ApproveFlow) {
        if (null != approveFlow){
            if (null != approveFlow.status && enableEdit){
                when(approveFlow.status){
                    1 -> {
                        tv_edit.visibility = View.GONE
                    }
                    2 -> {
                        tv_edit.visibility = View.GONE
                    }
                    else -> {
                        tv_edit.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun initRefreshConfig(): RefreshConfig? {
        val config = RefreshConfig()
        config.loadMoreEnable = false
        config.autoLoadMoreEnable = true
        config.scrollContentWhenLoaded = true
        config.disableContentWhenRefresh = true
        return config
    }

    override fun doRefresh() {
        if (null != project){
            getApprevoRecordInfo()
            if (null != presenter){
                presenter!!.getProApproveInfo(project!!.company_id!!,floor!!)
            }
        }
    }

    override fun doLoadMore() {

    }

    val realButtons = ArrayList<ApproveRecordInfo.ApproveButton>()
    private fun setApproveButtonStatus(button: List<ApproveRecordInfo.ApproveButton>?) {
        realButtons.clear()
        val checkButtons =ArrayList<ApproveRecordInfo.ApproveButton>()
        if (null != button && button.size > 0){
            ps_bottom.visibility = View.VISIBLE
            for (btn in button){
                if (btn.type != 3){
                    realButtons.add(btn)
                }
                if (btn.type != 3 && btn.type != 4){
                    checkButtons.add(btn)
                }
            }
            setRealButtonStatus(checkButtons)
        }else{
            ps_bottom.visibility = View.GONE
        }
    }

    private fun setRealButtonStatus(realButtons: ArrayList<ApproveRecordInfo.ApproveButton>) {
        when(realButtons.size){
            1 -> {
                tv_agree1.visibility = View.VISIBLE

                tv_agree2.visibility = View.GONE
                tv_refuse2.visibility = View.GONE
                view1.visibility = View.GONE
                tv_refuse.visibility = View.GONE
                tv_agree_lxh.visibility = View.GONE
                tv_agree.visibility = View.GONE
                view_line1.visibility = View.GONE
                view_line2.visibility = View.GONE
                val type = realButtons[0].type
                val name = realButtons[0].name
                clickJumpStatus1(type,name)
            }
            2 -> {
                tv_agree2.visibility = View.VISIBLE
                tv_refuse2.visibility = View.VISIBLE
                view1.visibility = View.VISIBLE

                tv_agree1.visibility = View.GONE
                tv_refuse.visibility = View.GONE
                tv_agree_lxh.visibility = View.GONE
                tv_agree.visibility = View.GONE
                view_line1.visibility = View.GONE
                view_line2.visibility = View.GONE
                val type1 = realButtons[0].type
                val type2 = realButtons[1].type
                val name1 = realButtons[0].name
                val name2 = realButtons[1].name
                clickJumpStatus2(type1,type2,name1,name2)
            }
            3 -> {
                tv_agree1.visibility = View.GONE
                tv_agree2.visibility = View.GONE
                tv_refuse2.visibility = View.GONE
                view1.visibility = View.GONE

                tv_refuse.visibility = View.VISIBLE
                tv_agree_lxh.visibility = View.VISIBLE
                tv_agree.visibility = View.VISIBLE
                view_line1.visibility = View.VISIBLE
                view_line2.visibility = View.VISIBLE
                val type1 = realButtons[0].type
                val type2 = realButtons[1].type
                val type3 = realButtons[2].type
                val name1 = realButtons[0].name
                val name2 = realButtons[1].name
                val name3 = realButtons[2].name
                clickJumpStatus3(type1,type2,type3,name1,name2,name3)
            }
        }
    }

    private fun clickJumpStatus3(type1: Int, type2: Int, type3: Int, name1: String, name2: String, name3: String) {
        tv_refuse.text = name1
        tv_agree.text = name2
        tv_agree_lxh.text = name3

        tv_refuse.setOnClickListener {
            when(type1){
                -1 -> {
                    //退回修改
                    showConfirmDialog()
                }
                1 -> {
                    //同意通过
                    showAgreeDialog()
                }
                2 -> {
                    //同意上立项会
                    showAgreeLxhDialog()
                }
            }
        }
        tv_agree.setOnClickListener {
            when(type2){
                -1 -> {
                    //退回修改
                    showConfirmDialog()
                }
                1 -> {
                    //同意通过
                    showAgreeDialog()
                }
                2 -> {
                    //同意上立项会
                    showAgreeLxhDialog()
                }
            }
        }
        tv_agree_lxh.setOnClickListener {
            when(type3){
                -1 -> {
                    //退回修改
                    showConfirmDialog()
                }
                1 -> {
                    //同意通过
                    showAgreeDialog()
                }
                2 -> {
                    //同意上立项会
                    showAgreeLxhDialog()
                }
            }
        }
    }

    private fun clickJumpStatus2(type1: Int, type2: Int, name1: String, name2: String) {
        tv_agree2.text = name1
        tv_refuse2.text = name2

        tv_agree2.setOnClickListener {
            when(type1){
                -1 -> {
                    //退回修改
                    showConfirmDialog()
                }
                1 -> {
                    //同意通过
                    showAgreeDialog()
                }
                2 -> {
                    //同意上立项会
                    showAgreeLxhDialog()
                }
            }
        }

        tv_refuse2.setOnClickListener {
            when(type2){
                -1 -> {
                    //退回修改
                    showConfirmDialog()
                }
                1 -> {
                    //同意通过
                    showAgreeDialog()
                }
                2 -> {
                    //同意上立项会
                    showAgreeLxhDialog()
                }
            }
        }
    }

    private fun showConfirmDialog() {
        if (null != dialog){
            dialog!!.showRejectBuildProDialog(this,project)
        }else{
            BuildProjectDialog().showRejectBuildProDialog(this,project)
        }
    }

    private fun showAgreeDialog(){
        if (null != dialog){
            dialog!!.showAgreeBuildProDialog(this,project)
        }else{
            BuildProjectDialog().showAgreeBuildProDialog(this,project)
        }
    }

    private fun showAgreeLxhDialog(){
        if (null != dialog){
            dialog!!.showAgreeBuildLxh(this,project)
        }else{
            BuildProjectDialog().showAgreeBuildLxh(this,project)
        }
    }

    private fun clickJumpStatus1(type: Int, name: String) {
        tv_agree1.text = name
        tv_agree1.setOnClickListener {
            when(type){
                -1 -> {
                    //退回修改
                    showConfirmDialog()
                }
                1 -> {
                    //同意通过
                    showAgreeDialog()
                }
                2 -> {
                    //同意上立项会
                    showAgreeLxhDialog()
                }
            }
        }
    }

    private fun setLoadding(){
        view_recover.visibility = View.VISIBLE
    }

    override fun goneLoading() {
        view_recover.visibility = View.INVISIBLE
        if (this::refresh.isLateinit && refresh.isRefreshing) {
            refresh.finishRefresh()
        }
    }

    private fun bindListener() {
        postAdapter.onItemClick = {v,position ->
            //预览
            val dataList = postAdapter.dataList
            if (null != dataList && dataList.size > 0 && null != dataList[position]){
                OnlinePreviewActivity.start(this,dataList[position].originUrl,dataList[position].file_name)
            }
        }

        tv_edit.clickWithTrigger {
            //编辑
            startActivity<ProjectApprovalActivity>(Extras.DATA to project,Extras.FLAG to floor)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
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
        val tv_meel_image = itemView.findViewById<TextView>(R.id.tv_meel_image)
        val view_bg = itemView.find<View>(R.id.view_bg) //会议背景
        val tv_meel_plan = itemView.find<TextView>(R.id.tv_meel_plan) //会议安排
        val view_space = itemView.find<View>(R.id.view_space) //间隔
        val ll_bottom = itemView.find<LinearLayout>(R.id.ll_bottom)
        val rl_suggest = itemView.find<RelativeLayout>(R.id.rl_suggest)
        val tv_suggest = itemView.find<TextView>(R.id.tv_suggest)
        val tv_file = itemView.find<TextView>(R.id.tv_file)
        val ll_files = itemView.find<LinearLayout>(R.id.ll_files)
        val fl_assign_approve = itemView.find<FrameLayout>(R.id.fl_assign_approve)
        val ll_assign = itemView.find<LinearLayout>(R.id.ll_assign)
        val tv_assign_person = itemView.find<TextView>(R.id.tv_assign_person)
        val view_line2 = itemView.find<View>(R.id.view_line2)
        override fun setData(view: View, data: ApproveRecordInfo.ApproveFlow, position: Int) {
            if (null == data) return
            Glide.with(this@ProjectApprovalShowActivity).load(data.url).apply(RequestOptions.circleCropTransform()
                    .placeholder(R.mipmap.ic_launch_pe_round)
                    .error(R.mipmap.ic_launch_pe_round)).into(iv_image)
            tv_name.text = data.name
            tv_time.text = data.approve_time
            if (!data.fenpei.isNullOrEmpty()){
                ll_assign.visibility = View.VISIBLE
                tv_assign_person.text = data.fenpei
            }else{
                ll_assign.visibility = View.GONE
            }
            if (approveDatas.size > 0){
                if (approveDatas.size == 1){
                    view_line2.visibility = View.GONE
                }else{
                    if (position == approveDatas.size - 1){
                        view_line2.visibility = View.GONE
                    }else{
                        view_line2.visibility = View.VISIBLE
                    }
                }
            }
            when(data.status){
                -1 -> {
                    //否决
                    tv_agree.text = "已否决"
                    tv_agree.setTextColor(Color.parseColor("#FF5858"))
                    tv_agree_pro.visibility = View.GONE
                    tv_meel_time.visibility = View.GONE
                    tv_meel_person.visibility = View.GONE
                    tv_meel_image.visibility = View.GONE
                    view_bg.visibility = View.GONE
                    tv_meel_plan.visibility = View.GONE
                    if (!data.content.isNullOrEmpty()){
                        val span = SpannableStringBuilder("意见意${data.content}")
                        span.setSpan(ForegroundColorSpan(Color.TRANSPARENT),0,3, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                        tv_suggest.text = span
                        rl_suggest.visibility = View.VISIBLE
                    }else{
                        rl_suggest.visibility = View.GONE
                    }
                    if (null != data.file && data.file!!.size > 0){
                        //有文件
                        ll_files.removeAllViews()
                        for (file in data.file!!){
                            val item = View.inflate(context,R.layout.file_item,null)
                            val iv_image = item.find<ImageView>(R.id.iv_image)
                            val tv_name = item.find<TextView>(R.id.tv_name)
                            iv_image.imageResource = FileTypeUtils.getFileType(file.file_name).icon
                            tv_name.text = file.file_name
                            item.setOnClickListener {
                                //预览页面
                                OnlinePreviewActivity.start(this@ProjectApprovalShowActivity,file.url,file.file_name)
                            }
                            ll_files.addView(item)
                        }
                    }else{
                        //无文件
                        tv_file.visibility = View.GONE
                        ll_files.visibility = View.GONE
                    }
                }
                0 -> {
                    //待审批
                    tv_agree.text = "待审批"
                    tv_agree.setTextColor(Color.parseColor("#FFA715"))
                    tv_agree_pro.visibility = View.GONE
                    tv_meel_time.visibility = View.GONE
                    tv_meel_person.visibility = View.GONE
                    tv_meel_image.visibility = View.GONE
                    view_bg.visibility = View.GONE
                    tv_meel_plan.visibility = View.GONE
                    ll_bottom.visibility = View.GONE
                    if (null != approveAdapter.dataList && approveAdapter.dataList.size > 0){
                        if (position == approveAdapter.dataList.size - 1){
                            if (realButtons.size > 0){
                                for (btn in realButtons){
                                    if (btn.type == 4){
                                        fl_assign_approve.visibility = View.VISIBLE
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    //重新发起
                    tv_agree.text = "重新发起"
                    tv_agree.setTextColor(Color.parseColor("#808080"))
                    tv_agree_pro.visibility = View.GONE
                    tv_meel_time.visibility = View.GONE
                    tv_meel_person.visibility = View.GONE
                    tv_meel_image.visibility = View.GONE
                    view_bg.visibility = View.GONE
                    tv_meel_plan.visibility = View.GONE
                    ll_bottom.visibility = View.GONE
                }
                1 -> {
                    //同意通过
                    tv_agree.text = "同意通过"
                    tv_agree.setTextColor(Color.parseColor("#50D59D"))
                    tv_agree_pro.visibility = View.GONE
                    tv_meel_time.visibility = View.GONE
                    tv_meel_person.visibility = View.GONE
                    tv_meel_image.visibility = View.GONE
                    view_bg.visibility = View.GONE
                    tv_meel_plan.visibility = View.GONE
                    if (!data.content.isNullOrEmpty()){
                        val span = SpannableStringBuilder("意见意${data.content}")
                        span.setSpan(ForegroundColorSpan(Color.TRANSPARENT),0,3, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                        tv_suggest.text = span
                        rl_suggest.visibility = View.VISIBLE
                    }else{
                        rl_suggest.visibility = View.GONE
                    }
                    if (null != data.file && data.file!!.size > 0){
                        //有文件
                        ll_files.removeAllViews()
                        for (file in data.file!!){
                            val item = View.inflate(context,R.layout.file_item,null)
                            val iv_image = item.find<ImageView>(R.id.iv_image)
                            val tv_name = item.find<TextView>(R.id.tv_name)
                            iv_image.imageResource = FileTypeUtils.getFileType(file.file_name).icon
                            tv_name.text = file.file_name
                            item.setOnClickListener {
                                //预览页面
                                OnlinePreviewActivity.start(this@ProjectApprovalShowActivity,file.url,file.file_name)
                            }
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
                    tv_agree.text = "同意通过"
                    tv_agree.setTextColor(Color.parseColor("#50D59D"))
                    tv_agree_pro.visibility = View.VISIBLE
                    tv_agree_pro.text = "同意上预审会"
                    if (null != data.meet && !"".equals(data.meet!!.meeting_time)
                    && null != data.meet!!.meeter && data.meet!!.meeter!!.size > 0){
                        tv_meel_plan.visibility = View.VISIBLE
                        view_bg.visibility = View.VISIBLE
                        tv_meel_time.visibility = View.VISIBLE
                        tv_meel_person.visibility = View.VISIBLE
                        tv_meel_image.visibility = View.VISIBLE
                    }else{
                        tv_meel_time.visibility = View.GONE
                        tv_meel_person.visibility = View.GONE
                        tv_meel_image.visibility = View.GONE
                        view_bg.visibility = View.GONE
                        tv_meel_plan.visibility = View.GONE
                    }

                    if (!data.content.isNullOrEmpty()){
                        val span = SpannableStringBuilder("意见意${data.content}")
                        span.setSpan(ForegroundColorSpan(Color.TRANSPARENT),0,3, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                        tv_suggest.text = span
                        rl_suggest.visibility = View.VISIBLE
                    }else{
                        rl_suggest.visibility = View.GONE
                    }
                    if (null != data.file && data.file!!.size > 0){
                        //有文件
                        ll_files.removeAllViews()
                        for (file in data.file!!){
                            val item = View.inflate(context,R.layout.file_item,null)
                            val iv_image = item.find<ImageView>(R.id.iv_image)
                            val tv_name = item.find<TextView>(R.id.tv_name)
                            iv_image.imageResource = FileTypeUtils.getFileType(file.file_name).icon
                            tv_name.text = file.file_name
                            item.setOnClickListener {
                                //预览页面
                                OnlinePreviewActivity.start(this@ProjectApprovalShowActivity,file.url,file.file_name)
                            }
                            ll_files.addView(item)
                        }
                    }else{
                        //无文件
                        tv_file.visibility = View.GONE
                        ll_files.visibility = View.GONE
                    }
                }
            }

            if (null != data.meet){
                if (!"".equals(data.meet!!.meeting_time)){
                    tv_meel_time.text = data.meet!!.meeting_time
                }
                if (null != data.meet!!.meeter && data.meet!!.meeter!!.size > 0){
                    var names = ""
                    for (meeter in data.meet!!.meeter!!){
                        names += "${meeter.name}、"
                    }
                    tv_meel_person.text = names.substring(0,names.length-1)
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
        val REQ_REJECT_FILE = 0x1004
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
                REQ_REJECT_FILE -> {
                    val paths = data?.getStringArrayListExtra(Extras.LIST)
                    paths?.forEach {
                        val info = ProjectApproveInfo.ApproveFile()
                        val file = File(it)
                        info.file = file
                        info.file_name = file.name
                        if (null != dialog){
                            dialog!!.addFileData(info,file,2)
                        }else{
                            BuildProjectDialog().addFileData(info,file,2)
                        }
                    }
                }

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