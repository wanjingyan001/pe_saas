package com.sogukj.pe.module.project

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.team.TeamService
import com.netease.nimlib.sdk.team.model.Team
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.ifNotNull
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.*
import com.sogukj.pe.bean.*
import com.sogukj.pe.module.approve.ApproveListActivity
import com.sogukj.pe.module.creditCollection.ShareHolderDescActivity
import com.sogukj.pe.module.creditCollection.ShareholderCreditActivity
import com.sogukj.pe.module.fund.BookListActivity
import com.sogukj.pe.module.main.ContactsActivity
import com.sogukj.pe.module.project.archives.EquityListActivity
import com.sogukj.pe.module.project.archives.FinanceListActivity
import com.sogukj.pe.module.project.archives.ManagerActivity
import com.sogukj.pe.module.project.archives.RecordTraceActivity
import com.sogukj.pe.module.project.businessBg.*
import com.sogukj.pe.module.project.businessDev.*
import com.sogukj.pe.module.project.intellectualProperty.CopyrightListActivity
import com.sogukj.pe.module.project.intellectualProperty.ICPListActivity
import com.sogukj.pe.module.project.intellectualProperty.PatentListActivity
import com.sogukj.pe.module.project.listingInfo.*
import com.sogukj.pe.module.project.operate.*
import com.sogukj.pe.peExtended.needIm
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.NewService
import com.sogukj.pe.service.OtherService
import com.sogukj.pe.widgets.DividerGridItemDecoration
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_project_detail.*
import kotlinx.android.synthetic.main.layout_project_header.view.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.ctx
import org.jetbrains.anko.find
import org.jetbrains.anko.textColor

class ProjectDetailActivity : ToolbarActivity(), BaseQuickAdapter.OnItemClickListener {
    lateinit var project: ProjectBean
    var position = 0
    var type = 0
    private lateinit var detailAdapter: ProjectDetailAdapter
    private val detailModules = mutableListOf<ProjectModules>()
    private lateinit var headView: View
    private var projectDetail: ProjectDetailBean? = null
    private val user by lazy { Store.store.getUser(this) }

    private var is_business: Int? = null//非空(1=>有价值 ,2=>无价值)
    private var is_ability: Int? = null//非空(1=>有能力,2=>无能力)
    private var NAVIGATION_GESTURE: String = when {
        Rom.isEmui() -> "navigationbar_is_min"
        Rom.isMiui() -> "force_fsg_nav_bar"
        else -> "navigation_gesture_on"
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, ProjectDetailActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }

        fun start(ctx: Context, project: ProjectBean, type: Int, position: Int) {
            val intent = Intent(ctx, ProjectDetailActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.CODE, position)
            if (ctx is Activity) {
                ctx.startActivityForResult(intent, 0x001)
            } else if (ctx is Fragment) {
                ctx.startActivityForResult(intent, 0x001)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_detail)
        StatusBarUtil.setTranslucentForCoordinatorLayout(this, 0)
        setBack(true)
        toolbar?.setBackgroundColor(resources.getColor(R.color.transparent))
        initDataWithIntent()
        deviceHasNavigationBar()
        initView()
        initDetailsList()
        getProjectDetail(project.company_id!!)
    }

    private fun initDataWithIntent() {
        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        position = intent.getIntExtra(Extras.CODE, 0)
        type = intent.getIntExtra(Extras.TYPE, 0)
    }

    /**
     * 判断设备是否存在NavigationBar
     *
     * @return true 存在, false 不存在
     */
    @SuppressLint("PrivateApi")
    private fun deviceHasNavigationBar() {
        var haveNav = false
        try {
            //1.通过WindowManagerGlobal获取windowManagerService
            // 反射方法：IWindowManager windowManagerService = WindowManagerGlobal.getWindowManagerService();
            val windowManagerGlobalClass = Class.forName("android.view.WindowManagerGlobal")
            val getWmServiceMethod = windowManagerGlobalClass.getDeclaredMethod("getWindowManagerService")
            getWmServiceMethod.isAccessible = true
            //getWindowManagerService是静态方法，所以invoke null
            val iWindowManager = getWmServiceMethod.invoke(null)

            //2.获取windowMangerService的hasNavigationBar方法返回值
            // 反射方法：haveNav = windowManagerService.hasNavigationBar();
            val iWindowManagerClass = iWindowManager.javaClass
            val hasNavBarMethod = iWindowManagerClass.getDeclaredMethod("hasNavigationBar")
            hasNavBarMethod.isAccessible = true
            haveNav = hasNavBarMethod.invoke(iWindowManager) as Boolean
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (haveNav && !navigationGestureEnabled()) {
            val param1 = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            param1.bottomMargin = Utils.dpToPx(ctx, 50)
            //var params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            layoutRoot.layoutParams = param1
        }
    }

    private fun navigationGestureEnabled(): Boolean {
        return Settings.Global.getInt(contentResolver, NAVIGATION_GESTURE, 0) != 0
    }


    private fun initView() {
        Glide.with(context)
                .load(project.logo)
                .apply(RequestOptions().placeholder(R.drawable.default_icon).error(R.drawable.default_icon))
                .into(imgIcon)
        companyTitle.text = project.name
        if (sp.getString(Extras.HTTPURL, "").contains("sr")) {
            proj_stage.visibility = View.INVISIBLE
        } else {
            proj_stage.visibility = View.VISIBLE
        }
        when (type) {
            ProjectListFragment.TYPE_DY -> {
                proj_stage.text = "储 备"
                history.visibility = View.GONE
            }
            ProjectListFragment.TYPE_CB -> {
                proj_stage.text = "立 项"
                history.visibility = View.GONE
            }
            ProjectListFragment.TYPE_LX -> {
                proj_stage.text = "投 决"
                edit.visibility = View.GONE
                history.visibility = View.GONE
            }
            ProjectListFragment.TYPE_YT -> {
                proj_stage.text = "退 出"
                edit.visibility = View.GONE
                history.visibility = View.GONE
                delete.visibility = View.GONE
                if (project.quit == 1) {
                    history.visibility = View.VISIBLE
                }
            }
            ProjectListFragment.TYPE_TC -> {
                proj_stage.visibility = View.GONE
                edit.visibility = View.GONE
                delete.visibility = View.GONE
            }
        }

        proj_stage.clickWithTrigger {
            when (type) {
                ProjectListFragment.TYPE_YT -> ProjectTCActivity.start(context, false, project)  //进入新增的退出模块
                else -> doAdd()
            }
        }
        delete.clickWithTrigger {
            doDel()
        }
        edit.clickWithTrigger {
            if (type == ProjectListFragment.TYPE_CB) {
                StoreProjectAddActivity.startEdit(context, project)
            } else if (type == ProjectListFragment.TYPE_DY) {
                ProjectAddActivity.startEdit(context, project)
            }
        }
        history.clickWithTrigger {
            ProjectTcHistoryActivity.start(context, project)
        }
        im.clickWithTrigger {
            projectDetail?.let {
                when (it.type) {
                    0 -> {
                        //群组存在就申请加群
                        showProgress("已发送入群申请")
                        NIMClient.getService(TeamService::class.java).applyJoinTeam(it.group_id, "")
                                .setCallback(object : RequestCallback<Team> {
                                    override fun onFailed(code: Int) {
                                        hideProgress()
                                        if (code == 809) {
                                            NimUIKit.startTeamSession(this@ProjectDetailActivity, it.group_id.toString())
                                        } else {
                                            showCustomToast(R.drawable.icon_toast_common, "申请已发出,等待群主同意")
                                        }
                                    }

                                    override fun onSuccess(param: Team) {
                                        hideProgress()
                                        NimUIKit.startTeamSession(this@ProjectDetailActivity, param.id)
                                    }

                                    override fun onException(exception: Throwable) {
                                        hideProgress()
                                        showCustomToast(R.drawable.icon_toast_fail, exception.message)
                                    }
                                })
                    }
                    1 -> {
                        //可以建群就去建群
                        val alreadySelect = ArrayList<UserBean>()
                        alreadySelect.add(Store.store.getUser(this)!!)
                        ContactsActivity.start(this, alreadySelect, true, true, project = project)
                    }
                    2 -> {
                        //不可以建群就弹提示
                        showCustomToast(R.drawable.icon_toast_fail, "项目群需要该项目负责人创建")
                    }
                }
            }
        }
//        divide2.visibility = View.VISIBLE
        AppBarLayout.addOnOffsetChangedListener { _, verticalOffset ->
            val alpha = Math.abs(verticalOffset) * 1.0 / Utils.dpToPx(context, 60)
            down.alpha = 1 - alpha.toFloat()

            if (down.alpha < 0.05) {
                toolbar_title.text = if (project.shortName.isNullOrEmpty()) project.name else project.shortName
            } else {
                toolbar_title.text = ""
            }
        }
    }

    private fun initDetailsList() {
        initDetailHeadView()
        detailAdapter = ProjectDetailAdapter(detailModules)
        detailAdapter.addHeaderView(headView)
        detailList.apply {
            layoutManager = GridLayoutManager(this@ProjectDetailActivity, 4)
            addItemDecoration(DividerGridItemDecoration(this@ProjectDetailActivity))
            adapter = detailAdapter

        }
        detailAdapter.onItemClickListener = this
    }


    private fun initDetailHeadView() {
        headView = layoutInflater.inflate(R.layout.layout_project_header, null)
        is_business = project.is_business
        is_ability = project.is_ability
        when (is_business) {
            1 -> {
                headView.btn_yes.setBackgroundResource(R.drawable.bg_rectangle_blue)
                headView.btn_yes.textColor = Color.parseColor("#ffffff")

                headView.btn_no.setBackgroundResource(R.drawable.bg_rectangle_white)
                headView.btn_no.textColor = Color.parseColor("#282828")
            }
            2 -> {
                headView.btn_no.setBackgroundResource(R.drawable.bg_rectangle_blue)
                headView.btn_no.textColor = Color.parseColor("#ffffff")

                headView.btn_yes.setBackgroundResource(R.drawable.bg_rectangle_white)
                headView.btn_yes.textColor = Color.parseColor("#282828")
            }
            null -> {
                headView.btn_yes.setBackgroundResource(R.drawable.bg_rectangle_white)
                headView.btn_yes.textColor = Color.parseColor("#282828")

                headView.btn_no.setBackgroundResource(R.drawable.bg_rectangle_white)
                headView.btn_no.textColor = Color.parseColor("#282828")
            }
        }

        when (is_ability) {
            1 -> {
                headView.btn_you.setBackgroundResource(R.drawable.bg_rectangle_blue)
                headView.btn_you.textColor = Color.parseColor("#ffffff")

                headView.btn_wu.setBackgroundResource(R.drawable.bg_rectangle_white)
                headView.btn_wu.textColor = Color.parseColor("#282828")
            }
            2 -> {
                headView.btn_wu.setBackgroundResource(R.drawable.bg_rectangle_blue)
                headView.btn_wu.textColor = Color.parseColor("#ffffff")

                headView.btn_you.setBackgroundResource(R.drawable.bg_rectangle_white)
                headView.btn_you.textColor = Color.parseColor("#282828")
            }
            null -> {
                headView.btn_you.setBackgroundResource(R.drawable.bg_rectangle_white)
                headView.btn_you.textColor = Color.parseColor("#282828")

                headView.btn_wu.setBackgroundResource(R.drawable.bg_rectangle_white)
                headView.btn_wu.textColor = Color.parseColor("#282828")
            }
        }
        headView.btn_yes.setOnClickListener {
            if (is_business == 1) {
                return@setOnClickListener
            }
            headView.btn_yes.setBackgroundResource(R.drawable.bg_rectangle_blue)
            headView.btn_yes.textColor = Color.parseColor("#ffffff")

            headView.btn_no.setBackgroundResource(R.drawable.bg_rectangle_white)
            headView.btn_no.textColor = Color.parseColor("#282828")

            is_business = 1

            managerAssess()
        }

        headView.btn_no.setOnClickListener {
            if (is_business == 2) {
                return@setOnClickListener
            }
            headView.btn_no.setBackgroundResource(R.drawable.bg_rectangle_blue)
            headView.btn_no.textColor = Color.parseColor("#ffffff")

            headView.btn_yes.setBackgroundResource(R.drawable.bg_rectangle_white)
            headView.btn_yes.textColor = Color.parseColor("#282828")

            is_business = 2

            managerAssess()
        }

        headView.btn_you.setOnClickListener {
            if (is_ability == 1) {
                return@setOnClickListener
            }
            headView.btn_you.setBackgroundResource(R.drawable.bg_rectangle_blue)
            headView.btn_you.textColor = Color.parseColor("#ffffff")

            headView.btn_wu.setBackgroundResource(R.drawable.bg_rectangle_white)
            headView.btn_wu.textColor = Color.parseColor("#282828")

            is_ability = 1

            managerAssess()
        }

        headView.btn_wu.setOnClickListener {
            if (is_ability == 2) {
                return@setOnClickListener
            }
            headView.btn_wu.setBackgroundResource(R.drawable.bg_rectangle_blue)
            headView.btn_wu.textColor = Color.parseColor("#ffffff")

            headView.btn_you.setBackgroundResource(R.drawable.bg_rectangle_white)
            headView.btn_you.textColor = Color.parseColor("#282828")

            is_ability = 2

            managerAssess()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initIntelligence(yu: Int, fu: Int) {
        headView.neg_num.setVisible(true)
        headView.yq_num.setVisible(true)
        if (fu == 0) {
            headView.neg_num.text = "运营良好"
            headView.neg_num.textColor = Color.parseColor("#ff27d2ab")
            headView.neg_num.backgroundResource = R.drawable.neg_yq_bg2
            headView.neg.setOnClickListener(null)
        } else {
            headView.neg_num.text = "${fu}条"
            headView.neg_num.textColor = Color.parseColor("#ffff5858")
            headView.neg_num.backgroundResource = R.drawable.neg_yq_bg
            headView.neg.setOnClickListener {
                ProjectNewsActivity.start(context, "负面讯息", 1, project.company_id!!)
            }
        }
        headView.yq_num.text = "${yu}条"
        if (yu == 0) {
            headView.yuqing.setOnClickListener(null)
        } else {
            headView.yuqing.setOnClickListener {
                ProjectNewsActivity.start(context, "企业舆情", 2, project.company_id!!)
            }
        }
    }


    private fun getProjectDetail(companyId: Int) {
        SoguApi.getService(application, NewService::class.java)
                .projectPage(companyId)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            payload.payload?.let {
                                //如果没有消息，也就不需要im
                                im.setVisible(it.type != 2 && !user?.accid.isNullOrEmpty() && needIm())
                                projectDetail = it
                                it.counts?.forEach {
                                    detailModules.add(ProjectModules(true, it.title!!, it.state!!))
                                    it.value?.forEach {
                                        detailModules.add(ProjectModules(it))
                                    }
                                }
                                initIntelligence(it.yu!!, it.fu!!)
                            }
                        } else {
                            payload.message
                        }
                    }
//                    onSubscribe {
//                        Glide.with(this@ProjectDetailActivity)
//                                .asGif()
//                                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
//                                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
//                                .into(iv_loading)
//                        iv_loading?.visibility = View.VISIBLE
//                    }
//                    onComplete {
//                        iv_loading?.visibility = View.GONE
//                    }
//                    onError { e ->
//                        iv_loading?.visibility = View.GONE
//                        Trace.e(e)
//                    }
                }
    }


    private fun managerAssess() {
        ifNotNull(is_business, is_ability, project.company_id, { is_business, is_ability, id ->
            SoguApi.getService(application, OtherService::class.java)
                    .assess(id, is_business, is_ability)
                    .execute {
                        onNext { payload ->
                            if (payload.isOk) {
                                Log.e("success", "success")
                                project.is_ability = is_ability
                                project.is_business = is_business
                            } else
                                showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }
        })
    }

    private fun doAdd() {
        var titleStr = ""
        when (type) {
            ProjectListFragment.TYPE_DY -> titleStr = "是否添加到储备"
            ProjectListFragment.TYPE_CB -> titleStr = "是否添加到立项"
            ProjectListFragment.TYPE_LX -> titleStr = "是否添加到已投"
            ProjectListFragment.TYPE_YT -> titleStr = "是否添加到退出"
        }
        val inflate = LayoutInflater.from(this).inflate(R.layout.layout_input_dialog1, null)
        val dialog = MaterialDialog.Builder(this)
                .customView(inflate, false)
                .cancelable(true)
                .build()
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val veto = inflate.find<TextView>(R.id.veto_comment)
        val confirm = inflate.find<TextView>(R.id.confirm_comment)
        val title = inflate.find<TextView>(R.id.approval_comments_title)
        title.text = titleStr
        veto.text = "取消"
        confirm.text = "确定"
        veto.setOnClickListener {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
        confirm.setOnClickListener {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
            val status = when (type) {
                ProjectListFragment.TYPE_DY -> 1
                ProjectListFragment.TYPE_CB -> 2
                ProjectListFragment.TYPE_LX -> 3
                else -> return@setOnClickListener
            }
            SoguApi.getService(application, NewService::class.java)
                    .changeStatus(project.company_id!!, status)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            when (type) {
                                ProjectListFragment.TYPE_DY -> showCustomToast(R.drawable.icon_toast_success, "成功添加到储备")
                                ProjectListFragment.TYPE_CB -> showCustomToast(R.drawable.icon_toast_success, "成功添加到立项")
                                ProjectListFragment.TYPE_LX -> showCustomToast(R.drawable.icon_toast_success, "成功添加到已投")
                                ProjectListFragment.TYPE_YT -> showCustomToast(R.drawable.icon_toast_success, "成功添加到退出")
                            }
                            setResult(Activity.RESULT_OK)
                            finish()
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
                        when (type) {
                            ProjectListFragment.TYPE_DY -> showCustomToast(R.drawable.icon_toast_fail, "添加到储备失败")
                            ProjectListFragment.TYPE_CB -> showCustomToast(R.drawable.icon_toast_fail, "添加到立项失败")
                            ProjectListFragment.TYPE_LX -> showCustomToast(R.drawable.icon_toast_fail, "添加到已投失败")
                            ProjectListFragment.TYPE_YT -> showCustomToast(R.drawable.icon_toast_fail, "添加到退出失败")
                        }
                    })
        }
        dialog.show()
    }

    private fun doDel() {
        val inflate = LayoutInflater.from(this).inflate(R.layout.layout_input_dialog1, null)
        val dialog = MaterialDialog.Builder(this)
                .customView(inflate, false)
                .cancelable(true)
                .build()
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val veto = inflate.find<TextView>(R.id.veto_comment)
        val confirm = inflate.find<TextView>(R.id.confirm_comment)
        val title = inflate.find<TextView>(R.id.approval_comments_title)
        title.text = "是否删除该项目?"
        veto.text = "取消"
        confirm.text = "确定"
        veto.setOnClickListener {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
        confirm.setOnClickListener {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
            SoguApi.getService(application, NewService::class.java)
                    .delProject(project.company_id!!)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            showCustomToast(R.drawable.icon_toast_success, "删除成功")
                            val intent1 = Intent()
                            intent1.putExtra(Extras.FLAG, "DELETE")
                            setResult(Activity.RESULT_OK, intent1)
                            finish()
                        } else
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }, { e ->
                        Trace.e(e)
                        showCustomToast(R.drawable.icon_toast_fail, "删除失败")
                    })
        }
        dialog.show()
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        if (view.tag in 1..62) {
            SoguApi.getService(application, NewService::class.java)
                    .saveClick(view.tag as Int)
                    .execute {}
        }
        when (view.tag) {
            38 -> StockInfoActivity.start(this@ProjectDetailActivity, project)//股票行情
            39 -> CompanyInfoActivity.start(this@ProjectDetailActivity, project)//企业简介
            40 -> GaoGuanActivity.start(this@ProjectDetailActivity, project)//高管信息
            41 -> CanGuActivity.start(this@ProjectDetailActivity, project)//参股控股
            42 -> AnnouncementActivity.start(this@ProjectDetailActivity, project)//上市公告
            43 -> ShiDaGuDongActivity.start(this@ProjectDetailActivity, project)//十大股东
            44 -> ShiDaLiuTongGuDongActivity.start(this@ProjectDetailActivity, project)//十大流通
            45 -> IssueRelatedActivity.start(this@ProjectDetailActivity, project)//发行相关
            47 -> EquityChangeActivity.start(this@ProjectDetailActivity, project)//股本变动
            48 -> BonusInfoActivity.start(this@ProjectDetailActivity, project)//分红情况
            49 -> AllotmentListActivity.start(this@ProjectDetailActivity, project)//配股情况
            46 -> GuBenJieGouActivity.start(this@ProjectDetailActivity, project)//股本结构

            1 -> BizInfoActivity.start(this@ProjectDetailActivity, project)//工商信息
            3 -> ShareHolderInfoActivity.start(this@ProjectDetailActivity, project)//股东信息
            8 -> QiYeLianBaoActivity.start(this@ProjectDetailActivity, project)//企业年报
            7 -> ChangeRecordActivity.start(this@ProjectDetailActivity, project)//变更记录
            6 -> InvestmentActivity.start(this@ProjectDetailActivity, project)//对外投资
            5 -> KeyPersonalActivity.start(this@ProjectDetailActivity, project)//主要人员
            4 -> {
                val bean = EquityListBean()
                bean.hid = project.company_id
                EquityStructureActivity.start(this@ProjectDetailActivity, bean, false)
            }//股权结构
            9 -> BranchListActivity.start(this@ProjectDetailActivity, project)//分支机构
            10 -> CompanyInfo2Activity.start(this@ProjectDetailActivity, project)//公司简介

            11 -> FinanceHistoryActivity.start(this@ProjectDetailActivity, project)//融资历史
            12 -> InvestEventActivity.start(this@ProjectDetailActivity, project)//投资事件
            13 -> CoreTeamActivity.start(this@ProjectDetailActivity, project)//核心团队
            14 -> BusinessEventsActivity.start(this@ProjectDetailActivity, project)//企业业务
            15 -> ProductInfoActivity.start(this@ProjectDetailActivity, project)//竞品信息
            32 -> BrandListActivity.start(this@ProjectDetailActivity, project)//商标信息

            16 -> RecruitActivity.start(this@ProjectDetailActivity, project)//招聘信息
            17 -> BondActivity.start(this@ProjectDetailActivity, project)//债券信息
            18 -> TaxRateActivity.start(this@ProjectDetailActivity, project)//税务评级
            19 -> LandPurchaseActivity.start(this@ProjectDetailActivity, project)//购地信息
            20 -> BidsActivity.start(this@ProjectDetailActivity, project)//招投标
            21 -> QualificationListActivity.start(this@ProjectDetailActivity, project)//资质证书
            22 -> CheckListActivity.start(this@ProjectDetailActivity, project)//抽查检查
            23 -> AppListActivity.start(this@ProjectDetailActivity, project)//产品信息

            33 -> PatentListActivity.start(this@ProjectDetailActivity, project)//专利信息
            34 -> CopyrightListActivity.start(this@ProjectDetailActivity, project, 2)//软著权
            35 -> CopyrightListActivity.start(this@ProjectDetailActivity, project, 1)//著作权
            36 -> ICPListActivity.start(this@ProjectDetailActivity, project)//网站备案

            52 -> {
                val stage = when (project.type) {//（4是储备，1是立项，3是关注，5是退出，6是调研）
                    6 -> "调研"
                    4 -> "储备"
                    1 -> "立项"
                    2 -> "已投"
                    5, 7 -> "退出"
                    else -> ""
                }
                BookListActivity.start(context, project.company_id!!, 1, null, "项目文书", project.name!!, stage)
//                startActivity<NewOriginProjectActivity>()
            }
            54 -> StoreProjectAddActivity.startView(this@ProjectDetailActivity, project)//储备信息
            51 -> {
                XmlDb.open(context).set("INNER", "TRUE")
                val first = XmlDb.open(context).get("FIRST", "TRUE")
                if (first == "FALSE") {
                    ShareholderCreditActivity.start(this@ProjectDetailActivity, project)
                } else if (first == "TRUE") {
                    ShareHolderDescActivity.start(this@ProjectDetailActivity, project, "INNER")
                    XmlDb.open(context).set("FIRST", "FALSE")
                }
            }

        // 跟踪记录,尽调数据,投决数据,投后管理数据
            55 -> RecordTraceActivity.start(this@ProjectDetailActivity, project)//跟踪记录
            56 -> ManagerActivity.start(this@ProjectDetailActivity, project, 1, "尽调数据")//尽调数据
//            56 -> startActivity<ProjectUploadActivity>()
            57 -> ManagerActivity.start(this@ProjectDetailActivity, project, 8, "投决数据")//投决数据
            58 -> ManagerActivity.start(this@ProjectDetailActivity, project, 10, "投后管理")//投后管理

            59 -> ApproveListActivity.start(this@ProjectDetailActivity, 5, project.company_id)//审批历史

            60 -> EquityListActivity.start(this@ProjectDetailActivity, project)

            61 -> FinanceListActivity.start(this@ProjectDetailActivity, project)

            62 -> IncomeCurveActivity.start(this@ProjectDetailActivity, project)//收益曲线

        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra(Extras.DATA, project)
        intent.putExtra(Extras.CODE, position)
        setResult(Activity.RESULT_CANCELED, intent)
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x001 && resultCode == Activity.RESULT_OK) {
            val step = data?.getIntExtra(Extras.TYPE, 0)
            if (step == 1) {

            } else if (step == 2) {
                //全部退出才会到下一个
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }
}
