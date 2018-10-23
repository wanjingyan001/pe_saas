package com.sogukj.pe.module.approve

import android.content.Intent
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.amap.api.mapcore.util.it
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.AvoidOnResult
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.module.approve.baseView.BaseControl
import com.sogukj.pe.module.approve.baseView.ControlFactory
import com.sogukj.pe.module.approve.baseView.controlView.*
import com.sogukj.pe.module.approve.baseView.viewBean.Approvers
import com.sogukj.pe.module.approve.baseView.viewBean.ControlBean
import com.sogukj.pe.module.approve.baseView.viewBean.User
import com.sogukj.pe.module.main.ContactsActivity
import com.sogukj.pe.service.ApproveService
import com.sogukj.pe.widgets.ApproverItemDecoration
import com.sogukj.pe.widgets.CircleImageView
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_approve_initiate.*
import kotlinx.android.synthetic.main.item_new_approver_list.view.*
import kotlinx.android.synthetic.main.layout_new_approve_user_list.view.*
import org.jetbrains.anko.*

class ApproveInitiateActivity : ToolbarActivity() {
    private val tid: Int by extraDelegate(Extras.ID, 0)//模板id
    private val aid: Int? by extraDelegate(Extras.ID2)//修改的审批id
    private val titleName: String by extraDelegate(Extras.NAME, "审批")//标题
    /**
     * 审批人
     */
    private lateinit var approverAdapter: RecyclerAdapter<User>
    /**
     * 抄送人
     */
    private val copyPeos = mutableListOf<User>()
    private lateinit var copyAdapter: CopyPeoAdapter
    private lateinit var userLayout: View
    private lateinit var phoneSelection: PhoneSelection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_approve_initiate)
        title = titleName
        setBack(true)
        approverAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            ApproverHolder(_adapter.getView(R.layout.item_new_approver_list, parent))
        }
        userLayout = layoutInflater.inflate(R.layout.layout_new_approve_user_list, null)
        userLayout.approverList.apply {
            layoutManager = GridLayoutManager(ctx, 6)
            adapter = approverAdapter
        }

        copyAdapter = CopyPeoAdapter(copyPeos)
        userLayout.copyPeoList.apply {
            layoutManager = GridLayoutManager(ctx, 6)
            adapter = copyAdapter
        }
        copyAdapter.onItemClick = { v, position ->
            val users = copyPeos.map {
                val bean = UserBean()
                bean.uid = it.id.toInt()
                bean.name = it.name
                bean.url = it.url
                return@map bean
            } as ArrayList

            var defaultIDs = ArrayList<Int>()
            val def = approvers.cs.def
            def.isNotEmpty().yes {
                defaultIDs = def.split(",").map { it.toInt() } as ArrayList
            }
            ContactsActivity.startWithDefault(this, users, false, false, defaultIDs, Extras.REQUESTCODE)
        }
        showTemplate()
        commitApprove.clickWithTrigger {
            submitApproval()
        }
    }

    /**
     * 展示模板
     */
    private fun showTemplate() {
        SoguApi.getService(application, ApproveService::class.java)
                .showTemplate(tid, aid)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                it.forEach {
                                    info { it.jsonStr }
                                    val factory = ControlFactory(this@ApproveInitiateActivity)
                                    val control = when (it.control) {
                                        1 -> factory.createControl(SingLineInput::class.java, it)
                                        2 -> factory.createControl(MultiLineInput::class.java, it)
                                        3 -> factory.createControl(NumberInput::class.java, it)
                                        4 -> factory.createControl(SingSelection::class.java, it)
                                        5 -> factory.createControl(MultiSelection::class.java, it)
                                        6 -> factory.createControl(DateSelection::class.java, it)
                                        7 -> {
                                            phoneSelection = factory.createControl(PhoneSelection::class.java, it) as PhoneSelection
                                            phoneSelection
                                        }
                                        8 -> factory.createControl(NoticText::class.java, it)
                                        9 -> factory.createControl(MoneyInput::class.java, it)
                                        10 -> factory.createControl(AttachmentSelection::class.java, it)
                                        11 -> factory.createControl(ContactSelection::class.java, it)
                                        12 -> factory.createControl(DepartmentControl::class.java, it)
                                    //13当前地点暂时不做
                                        14 -> factory.createControl(DocumentAssociate::class.java, it)
                                        15 -> factory.createControl(SealSelection::class.java, it)
                                        16 -> factory.createControl(RadioControl::class.java, it)
                                        17 -> factory.createControl(CheckBoxControl::class.java, it)
                                        18 -> {
                                            val project = factory.createControl(ProjectSelection::class.java, it)
                                            (project as ProjectSelection).block = { value ->
                                                getApprovers(projectId = value.id)
                                            }
                                            project
                                        }
                                        19 -> {
                                            val fund = factory.createControl(FundSelection::class.java, it)
                                            (fund as FundSelection).block = {
                                                getApprovers(fundId = it.id)
                                            }
                                            fund
                                        }
                                        20 -> factory.createControl(ForeignControl::class.java, it)
                                        21 -> {
                                            val fap = factory.createControl(FAPControl::class.java, it)
                                            (fap as FAPControl).block = { v1, v2 ->
                                                getApprovers(fundId = v1, projectId = v2)
                                            }
                                            fap
                                        }
                                        22 -> factory.createControl(DateRangeControl::class.java, it)
                                        24 -> factory.createControl(SMSNotification::class.java, it)
                                        25 -> factory.createControl(CitySelection::class.java, it)
                                        -1 -> factory.createControl(LeaveControl::class.java, it)
                                        -2 -> factory.createControl(TravelControl::class.java, it)
                                        -3 -> factory.createControl(GoOutControl::class.java, it)
                                        -21 -> {
                                            val fs = factory.createControl(FundSealControl::class.java, it)
                                            (fs as FundSealControl).block = { v1, v2 ->
                                                getApprovers(fundId = v1, projectId = v2)
                                            }
                                            fs
                                        }
                                        -22 -> factory.createControl(CompanySealControl::class.java, it)
                                        -23 -> factory.createControl(ForeignSealControl::class.java, it)
                                        -41 -> factory.createControl(ReimburseControl::class.java, it)
                                        else -> return@forEach
                                    }
                                    val view = View(ctx)
                                    val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dip(10))
                                    view.layoutParams = lp
                                    controlLayout.addView(view)
                                    controlLayout.addView(control)
                                }
                                (controlLayout.childCount > 0).yes {
                                    controlLayout.removeViewAt(0)
                                }
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                    onComplete {
                        getApprovers()
                    }
                }
    }

    private lateinit var approvers: Approvers
    /**
     * 获取审批人
     */
    private fun getApprovers(fundId: String? = null, projectId: String? = null) {
        SoguApi.getService(application, ApproveService::class.java)
                .getApprovers(tid, fund_id = fundId, project_id = projectId)
                .execute {
                    onSubscribe {
                        val view = View(this@ApproveInitiateActivity)
                        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dip(10))
                        view.backgroundColorResource = R.color.bg_record
                        view.layoutParams = lp
                        contentLayout.removeView(view)
                        contentLayout.removeView(userLayout)
                        contentLayout.addView(view)
                        contentLayout.addView(userLayout)
                    }
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                approvers = it
                                val sizes = it.sp.map { it.users.size }
                                val users = it.sp.flatMap { it.users }
                                approverAdapter.refreshData(users)
                                sizes.isNotEmpty().yes {
                                    userLayout.approverList.addItemDecoration(ApproverItemDecoration(ctx, sizes))
                                }

                                copyAdapter.defaultCS = it.cs.def.split(",")
                                copyPeos.clear()
                                copyPeos.addAll(it.cs.users)
                                copyAdapter.notifyDataSetChanged()
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }

    /**
     * 提交审批
     */
    private fun submitApproval() {
        val value = mutableListOf<ControlBean>()
        (0 until controlLayout.childCount).forEach {
            val view = controlLayout.getChildAt(it)
            if (view is BaseControl) {
                info {
                    view.controlBean.jsonStr
                }
                getBean(view).yes {
                    value.add(view.controlBean)
                }
            }
        }
        if (approvers.sp.isEmpty()){
            showCommonToast("审批人不能为空")
            return
        }
        (value.isNotEmpty() && ::approvers.isLateinit).yes {
            SoguApi.getService(application, ApproveService::class.java)
                    .submitNewApprove(tid, value.jsonStr, approvers.sp.jsonStr, approvers.cs.jsonStr, approvers.jr.jsonStr)
                    .execute {
                        onNext { payload ->
                            payload.isOk.yes {
                                showSuccessToast("审批提交成功")
                                if (aid != null) {
                                    finish()
                                } else {
                                    setResult(Extras.RESULTCODE)
                                    finish()
                                }
                            }.otherWise {
                                showErrorToast(payload.message)
                            }
                        }
                    }
        }
    }


    private fun getBean(view: BaseControl): Boolean {
        (view.controlBean.control > 0).yes {
            view.controlBean.is_must?.yes {
                view.controlBean.value?.let {
                    it.isEmpty().yes {
                        showErrorToast(when (view) {
                            is SingLineInput,
                            is MultiLineInput,
                            is NumberInput,
                            is MoneyInput -> "${view.controlBean.name}为必填项"
                            else -> "${view.controlBean.name}为必选项"
                        })
                        return false
                    }
                }
            }
        }.otherWise {
            return view.getBean()
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        (0 until controlLayout.childCount).forEach {
            if (controlLayout.getChildAt(it) is PhoneSelection) {
                (controlLayout.getChildAt(it) as PhoneSelection).onActivityResult(requestCode, resultCode, data)
            }
            if (controlLayout.getChildAt(it) is FundSealControl) {
                (controlLayout.getChildAt(it) as FundSealControl).onActivityResult(requestCode, resultCode, data)
            }
        }

        if (requestCode == Extras.REQUESTCODE && resultCode == Extras.RESULTCODE && data != null) {
            val users = data.getSerializableExtra(Extras.DATA) as ArrayList<UserBean>
            val newCopy = users.map { User(name = it.name, id = it.uid.toString(), url = it.url ?: "") }
            copyPeos.clear()
            copyPeos.addAll(newCopy)
            approvers.cs.users.clear()
            approvers.cs.users.addAll(newCopy)
            copyAdapter.notifyDataSetChanged()
        }
    }


    override fun onBackPressed() {
        val valueNotEmpty = mutableListOf<ControlBean>()//有值的控件集合
        val views = mutableListOf<ControlBean>()//所有控件集合
        (0 until controlLayout.childCount).forEach {
            val view = controlLayout.getChildAt(it)
            if (view is BaseControl) {
                views.add(view.controlBean)
                view.controlBean.value.isNullOrEmpty().no {
                    valueNotEmpty.add(view.controlBean)
                }
                view.controlBean.children?.forEach {
                    it.value.isNullOrEmpty().no {
                        valueNotEmpty.add(it)
                    }
                }
            }
        }
        valueNotEmpty.isNotEmpty().yes {
            initDialog {
                views.forEach {
                    info { it.jsonStr }
                }
                saveApproveDraft(views.jsonStr)
            }.show()
        }.otherWise {
            super.onBackPressed()
        }
    }


    private fun initDialog(block: () -> Unit): MaterialDialog {
        val mDialog = MaterialDialog.Builder(this)
                .theme(Theme.LIGHT)
                .canceledOnTouchOutside(true)
                .customView(R.layout.dialog_yongyin, false)
                .build()
        val content = mDialog.find<TextView>(R.id.content)
        val cancel = mDialog.find<Button>(R.id.cancel)
        val yes = mDialog.find<Button>(R.id.yes)
        content.text = "是否需要保存草稿"
        cancel.text = "否"
        yes.text = "是"
        cancel.setOnClickListener {
            mDialog.dismiss()
            super.onBackPressed()
        }
        yes.setOnClickListener {
            block.invoke()
        }
        return mDialog
    }


    private fun saveApproveDraft(data: String) {
        SoguApi.getService(application, ApproveService::class.java)
                .saveApproveDraft(tid, data)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            showSuccessToast("草稿保存成功")
                            finish()
                        }.otherWise {
                            showErrorToast("草稿保存失败")
                        }
                    }
                    onError {
                        showErrorToast("草稿保存失败")
                    }
                }
    }


    inner class ApproverHolder(itemView: View) : RecyclerHolder<User>(itemView) {
        override fun setData(view: View, data: User, position: Int) {
            Glide.with(this@ApproveInitiateActivity)
                    .load(data.url)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            val ch = data.name.first()
                            view.approverHeader.setChar(ch)
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            return false
                        }
                    }).into(view.approverHeader)
            view.approverName.text = data.name
        }
    }

    inner class CopyPeoAdapter(val users: MutableList<User>) : RecyclerView.Adapter<CopyPeoAdapter.CopyHolder>() {
        var onItemClick: ((v: View, position: Int) -> Unit)? = null
        var defaultCS: List<String>? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CopyHolder =
                CopyHolder(layoutInflater.inflate(R.layout.item_new_approve_copy_list, parent, false))

        override fun onBindViewHolder(holder: CopyHolder, position: Int) {
            if (position == users.size) {
                holder.header.setImageResource(R.drawable.invalid_name3)
                holder.name.text = "添加"
                holder.itemView.setOnClickListener { v ->
                    if (null != onItemClick) {
                        onItemClick!!(v, position)
                    }
                }
            } else {
                val user = users[position]
                holder.name.text = user.name
                if (user.url.isEmpty()) {
                    val ch = user.name.first()
                    holder.header.setChar(ch)
                } else {
                    Glide.with(ctx)
                            .load(user.url)
                            .listener(object : RequestListener<Drawable> {
                                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                    return false
                                }

                                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                    holder.header.setChar(user.name.first())
                                    return false
                                }

                            })
                            .into(holder.header)
                }
                defaultCS?.let {
                    holder.default.setVisible(it.contains(user.id))
                    holder.remove.setVisible(!it.contains(user.id))
                }
                if (holder.remove.visibility == View.VISIBLE) {
                    holder.itemView.setOnClickListener {
                        approvers.cs.users.removeAt(position)
                        copyPeos.removeAt(position)
                        notifyItemRemoved(position)
                    }
                }
            }
        }

        override fun getItemCount(): Int = users.size + 1


        inner class CopyHolder(item: View) : RecyclerView.ViewHolder(item) {
            val header = item.find<CircleImageView>(R.id.approverHeader)
            val name = item.find<TextView>(R.id.approverName)
            val default = item.find<ImageView>(R.id.cs_default)
            val remove = item.find<ImageView>(R.id.cs_remove)
        }
    }
}
