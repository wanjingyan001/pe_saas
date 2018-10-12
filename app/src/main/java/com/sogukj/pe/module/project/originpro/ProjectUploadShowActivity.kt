package com.sogukj.pe.module.project.originpro

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
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
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.sogukj.pe.App
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.*
import com.sogukj.pe.module.project.originpro.adapter.ExpandableItemAdapter
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.peUtils.ToastUtil
import com.sogukj.pe.service.OtherService
import com.sogukj.pe.widgets.BuildProjectDialog
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_upload_show.*
import kotlinx.android.synthetic.main.commom_blue_title.*
import kotlinx.android.synthetic.main.project_show_bottom.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity
import java.io.File
import java.util.*

/**
 * Created by CH-ZH on 2018/9/20.
 * 预审会
 */
class ProjectUploadShowActivity : ToolbarActivity(){
    private  var adapter : ExpandableItemAdapter ?  = null
    private var project : ProjectBean ? = null
    private var floor : Int ?= null
    private var user : UserBean ? = null
    private var approveDatas = ArrayList<ProjectApproveInfo>()
    private var linkFundDatas = ArrayList<LinkFundBean>()
    private lateinit var approveAdapter:RecyclerAdapter<ApproveRecordInfo.ApproveFlow>
    private var dialog : BuildProjectDialog ? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_show)
        initView()
        initData()
        bindListener()
    }

    private fun initView() {
        setBack(true)
        setTitle("预审会")

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean?
        floor = intent.getIntExtra(Extras.FLAG,-1)
        user = Store.store.getUser(this)
        Glide.with(this)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading)
        dialog = BuildProjectDialog()
        ll_fund.visibility = View.GONE

        rv_file.layoutManager = LinearLayoutManager(this)
    }

    private fun initData() {
        approveAdapter = RecyclerAdapter(this){_adapter,parent,_ ->
            ProjectApproveHolder(_adapter.getView(R.layout.item_approve_list, parent))
        }

        approve_list.layoutManager = LinearLayoutManager(this)
        approve_list.adapter = approveAdapter

        if (null != project){
            setLoadding()
            getProjectComBase()
            getApproveShowData()
            getApprevoRecordInfo()
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
                                    approveAdapter.dataList.clear()
                                    approveAdapter.dataList.addAll(flow)
                                    approveAdapter.notifyDataSetChanged()
                                    val approveFlow = flow[flow.size - 1]
                                    setApproveEditStatus(approveFlow)
                                }
                            }
                        }else{
                            showErrorToast(payload.message)
                        }
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("获取审批记录失败")
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
                    if (null != dialog){
                        dialog!!.showAgreeBuildProDialog(this,project)
                    }else{
                        BuildProjectDialog().showAgreeBuildProDialog(this,project)
                    }
                }
                2 -> {
                    //同意上立项会
                    if (null != dialog){
                        dialog!!.showAgreeBuildLxh(this,project)
                    }else{
                        BuildProjectDialog().showAgreeBuildLxh(this,project)
                    }
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
                    if (null != dialog){
                        dialog!!.showAgreeBuildProDialog(this,project)
                    }else{
                        BuildProjectDialog().showAgreeBuildProDialog(this,project)
                    }
                }
                2 -> {
                    //同意上立项会
                    if (null != dialog){
                        dialog!!.showAgreeBuildLxh(this,project)
                    }else{
                        BuildProjectDialog().showAgreeBuildLxh(this,project)
                    }
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
                    if (null != dialog){
                        dialog!!.showAgreeBuildProDialog(this,project)
                    }else{
                        BuildProjectDialog().showAgreeBuildProDialog(this,project)
                    }
                }
                2 -> {
                    //同意上立项会
                    if (null != dialog){
                        dialog!!.showAgreeBuildLxh(this,project)
                    }else{
                        BuildProjectDialog().showAgreeBuildLxh(this,project)
                    }
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
                    if (null != dialog){
                        dialog!!.showAgreeBuildProDialog(this,project)
                    }else{
                        BuildProjectDialog().showAgreeBuildProDialog(this,project)
                    }
                }
                2 -> {
                    //同意上立项会
                    if (null != dialog){
                        dialog!!.showAgreeBuildLxh(this,project)
                    }else{
                        BuildProjectDialog().showAgreeBuildLxh(this,project)
                    }
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
                    if (null != dialog){
                        dialog!!.showAgreeBuildProDialog(this,project)
                    }else{
                        BuildProjectDialog().showAgreeBuildProDialog(this,project)
                    }
                }
                2 -> {
                    //同意上立项会
                    if (null != dialog){
                        dialog!!.showAgreeBuildLxh(this,project)
                    }else{
                        BuildProjectDialog().showAgreeBuildLxh(this,project)
                    }
                }
            }
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
                    if (null != dialog){
                        dialog!!.showAgreeBuildProDialog(this,project)
                    }else{
                        BuildProjectDialog().showAgreeBuildProDialog(this,project)
                    }
                }
                2 -> {
                    //同意上立项会
                    if (null != dialog){
                        dialog!!.showAgreeBuildLxh(this,project)
                    }else{
                        BuildProjectDialog().showAgreeBuildLxh(this,project)
                    }
                }
            }
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
            refuseApprove()
        }
        build.show()
    }

    private fun refuseApprove() {
        if (null == project) return
        val map = HashMap<String,Any>()
        map.put("company_id",project!!.company_id!!)
        map.put("floor",project!!.floor!!)
        map.put("type",-1) //-1=>’否决’,1=>’同意通过’,2=> 同意上储备,3=>同意退出
        map.put("current",0)

        SoguApi.getService(App.INSTANCE,OtherService::class.java)
                .commitApprove(map)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            getApprevoRecordInfo()
                        }else{
                            showErrorToast(payload.message)
                        }
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("否决失败")
                    }
                }

    }

    private fun getApproveShowData() {
        SoguApi.getService(App.INSTANCE,OtherService::class.java)
                .getProApproveInfo(project!!.company_id!!,floor!!)
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val datas = payload.payload
                            if (null != datas && datas.size > 0){
                                val list = datas[0]
                                if (null != list && list.size > 0){
                                    approveDatas = list as ArrayList<ProjectApproveInfo>
                                    setMultiItemData(list)
                                }

                            }
                        }else{
                            showErrorToast(payload.message)
                        }
                        goneLoadding()
                    }

                    onError {
                        it.printStackTrace()
                        showErrorToast("获取数据失败")
                        goneLoadding()
                    }
                }
    }

    private fun setMultiItemData(datas: List<ProjectApproveInfo>) {
        val res = ArrayList<MultiItemEntity>()
        for (data in datas){
            val level0Item = Level0Item(data.name)
            if (null != data.son && data.son!!.size > 0){
                for (son in data.son!!){
                    val level1Item = Level1Item(son.name)
                    if (null != son.files && son.files!!.size > 0){
                        for (file in son.files!!){
                            val level2Item = Level2Item()
                            level2Item.type = 0
                            level2Item.file = file
                            level1Item.addSubItem(level2Item)
                        }
                    }else{
                        val level2Item = Level2Item()
                        level2Item.type = -1
                        level1Item.addSubItem(level2Item)
                    }

                    level0Item.addSubItem(level1Item)
                }
            }else{
                if (null != data.files && data.files!!.size > 0){
                    //有文件
                    for (file in data.files!!){
                        val level2Item = Level2Item()
                        level2Item.type = 0
                        level2Item.file = file
                        level0Item.addSubItem(level2Item)
                    }
                }else{
                    //无文件
                    val level2Item = Level2Item()
                    level2Item.type = -1
                    level0Item.addSubItem(level2Item)
                }

            }
            res.add(level0Item)
        }
        adapter = ExpandableItemAdapter(res,1,this)
        rv_file.adapter = adapter
        adapter!!.expandAll()
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
                        }else{
                            ToastUtil.showCustomToast(R.drawable.icon_toast_fail, payload.message, ctx!!)
                        }
                    }
                    onComplete {

                    }

                    onError {
                        it.printStackTrace()
                        ToastUtil.showCustomToast(R.drawable.icon_toast_fail, "获取权限失败", ctx!!)
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

    private fun bindListener() {
        if (null != adapter){
            adapter!!.setOnItemChildClickListener { adapter, view, position ->
//                adapter.remove(position)
            }
        }

        tv_edit.setOnClickListener {
            //编辑
            startActivity<ProjectUploadActivity>(Extras.DATA to approveDatas,Extras.FUND to linkFundDatas
                    ,Extras.PROJECT to project,Extras.FLAG to floor,Extras.TITLE to "预审会")
            finish()
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
        override fun setData(view: View, data: ApproveRecordInfo.ApproveFlow, position: Int) {
            if (null == data) return
            Glide.with(this@ProjectUploadShowActivity).load(data.url).apply(RequestOptions.circleCropTransform()
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
                    view_space.visibility = View.GONE
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
                    view_space.visibility = View.VISIBLE
                    ll_bottom.visibility = View.GONE

                    if (null != approveAdapter.dataList && approveAdapter.dataList.size > 0){
                        if (position == approveAdapter.dataList.size - 1){
                            if (realButtons.size > 0){
                                for (btn in realButtons){
                                    if (btn.type == 4){
                                        ll_bottom.visibility = View.VISIBLE
                                        rl_suggest.visibility = View.GONE
                                        tv_file.visibility = View.GONE
                                        ll_files.visibility = View.GONE
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
                    view_bg.visibility = View.GONE
                    tv_meel_plan.visibility = View.GONE
                    view_space.visibility = View.GONE
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
                    view_space.visibility = View.GONE
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
                    tv_agree.text = "同意通过"
                    tv_agree.setTextColor(Color.parseColor("#50D59D"))
                    tv_agree_pro.visibility = View.VISIBLE
                    if (null != data.meet && !"".equals(data.meet!!.meeting_time)
                            && null != data.meet!!.meeter && data.meet!!.meeter!!.size > 0){
                        tv_meel_plan.visibility = View.VISIBLE
                        view_bg.visibility = View.VISIBLE
                        tv_meel_time.visibility = View.VISIBLE
                        tv_meel_person.visibility = View.VISIBLE
                    }else{
                        tv_meel_time.visibility = View.GONE
                        tv_meel_person.visibility = View.GONE
                        view_bg.visibility = View.GONE
                        tv_meel_plan.visibility = View.GONE
                    }

                    if (!data.content.isNullOrEmpty()){
                        val span = SpannableStringBuilder("意见意${data.content}")
                        span.setSpan(ForegroundColorSpan(Color.TRANSPARENT),0,3, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                        tv_suggest.text = span
                        rl_suggest.visibility = View.VISIBLE
                        view_space.visibility = View.VISIBLE
                    }else{
                        rl_suggest.visibility = View.GONE
                        view_space.visibility = View.GONE
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
                    dialog!!.allocationApprove(this@ProjectUploadShowActivity,project)
                }else{
                    BuildProjectDialog().allocationApprove(this@ProjectUploadShowActivity,project)
                }
            }
        }

    }

    private fun setLoadding(){
        view_recover.visibility = View.VISIBLE
    }

    private fun goneLoadding(){
        view_recover.visibility = View.INVISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (null != data){
            when(requestCode){
                ProjectApprovalShowActivity.REQ_SELECT_FILE -> {
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

                ProjectApprovalShowActivity.REQ_LXH_FILE -> {
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

                ProjectApprovalShowActivity.REW_SELECT_TIME -> {
                    val date = data.getSerializableExtra(Extras.DATA)
                    if (null != dialog){
                        dialog!!.setTime(date as Date?)
                    }else{
                        BuildProjectDialog().setTime(date as Date?)
                    }
                }

                ProjectApprovalShowActivity.REQ_ADD_MEMBER -> {
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

                ProjectApprovalShowActivity.REQ_ADD_ALLOCATION -> {
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


