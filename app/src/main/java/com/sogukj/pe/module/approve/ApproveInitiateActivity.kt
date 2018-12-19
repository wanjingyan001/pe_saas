package com.sogukj.pe.module.approve

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.bean.LastApproveBean
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.module.approve.baseView.BaseControl
import com.sogukj.pe.module.approve.baseView.ControlFactory
import com.sogukj.pe.module.approve.baseView.controlView.*
import com.sogukj.pe.module.approve.baseView.viewBean.Approvers
import com.sogukj.pe.module.approve.baseView.viewBean.ControlBean
import com.sogukj.pe.module.approve.baseView.viewBean.User
import com.sogukj.pe.module.main.ContactsActivity
import com.sogukj.pe.peUtils.Store
import com.sogukj.pe.service.ApproveService
import com.sogukj.pe.service.Payload
import com.sogukj.pe.widgets.ApproverItemDecoration
import com.sogukj.pe.widgets.CircleImageView
import com.sogukj.pe.widgets.UserRequestListener
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_approve_initiate.*
import kotlinx.android.synthetic.main.item_new_approver_list.view.*
import kotlinx.android.synthetic.main.layout_new_approve_user_list.view.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

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
    private var isHidden: Int = 0
    override val menuId: Int
        get() = R.menu.menu_new_approve_copy

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

        commitApprove.text = if (aid != null) "修改" else "提交"
        commitApprove.clickWithTrigger {
            submitApproval()
        }

        isHidden = Store.store.getApproveConfig(this)
    }

    /**
     * 展示模板
     */
    private fun showTemplate() {
        SoguApi.getService(application, ApproveService::class.java)
                .showTemplate(tid, aid)
                .execute {
                    onNext { payload ->
                        initApproveControl(payload)
                    }
                    onComplete {
                        getApprovers()
                    }
                }
    }

    private fun initApproveControl(payload: Payload<List<ControlBean>>) {
        payload.isOk.yes {
            payload.payload?.let {
                controlLayout.removeAllViews()
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
                        7 -> factory.createControl(PhotoSelection::class.java, it)
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
                                getApprovers(projectId = value.id.toString())
                            }
                            project
                        }
                        19 -> {
                            val fund = factory.createControl(FundSelection::class.java, it)
                            (fund as FundSelection).block = {
                                getApprovers(fundId = it.id.toString())
                            }
                            fund
                        }
                        20 -> factory.createControl(ForeignControl::class.java, it)
                        21 -> {
                            val fap = factory.createControl(FAPControl::class.java, it)
                            (fap as FAPControl).block = { v1, v2 ->
                                getApprovers(fundId = v1.toString(), projectId = v2.toString())
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
                                getApprovers(fundId = v1.toString(), projectId = v2.toString())
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

    private lateinit var approvers: Approvers
    private lateinit var decoration: ApproverItemDecoration

    /**
     * 获取审批人
     */
    private fun getApprovers(fundId: String? = null, projectId: String? = null) {
        SoguApi.getService(application, ApproveService::class.java)
                .getApprovers(tid, aid, fund_id = fundId, project_id = projectId)
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
                        approverAdapter.selectedItems.clear()
                    }
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                approvers = it
                                val sizes = it.sp.map { it.users.size }
                                val users = it.sp.flatMap { it.users }
                                (userLayout.approverList.itemDecorationCount == 0 && sizes.isNotEmpty()).yes {
                                    decoration = ApproverItemDecoration(ctx, sizes)
                                    userLayout.approverList.addItemDecoration(decoration)
                                }
                                approverAdapter.refreshData(users)
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
        val checkValue = mutableListOf<Boolean>()
        (0 until controlLayout.childCount).forEach {
            val view = controlLayout.getChildAt(it)
            if (view is BaseControl) {
                view.controlBean.children.isNullOrEmpty().yes {
                    info {
                        view.controlBean.jsonStr
                    }
                }.otherWise {
                    info {
                        view.controlBean.children?.forEach {
                            info {
                                it.jsonStr
                            }
                        }
                    }
                }

                val checks = getBean(view)
                checks.any { !it }.no {
                    value.add(view.controlBean)
                }
                checkValue.addAll(checks)
            }
        }

        if (approvers.sp.isEmpty()) {
            showCommonToast("审批人不能为空")
            return
        }
        (checkValue.none { !it } && value.isNotEmpty() && ::approvers.isLateinit).yes {
            val service = SoguApi.getService(application, ApproveService::class.java)
            val observable = if (aid != null) {
                service.modifyApprove(aid!!, data = value.jsonStr, sp = approvers.sp.jsonStr, cs = approvers.cs.jsonStr, jr = approvers.jr.jsonOrNull)
            } else {
                service.submitNewApprove(tid, data = value.jsonStr, sp = approvers.sp.jsonStr, cs = approvers.cs.jsonStr, jr = approvers.jr.jsonOrNull)
            }
            observable.execute {
                onNext { payload ->
                    payload.isOk.yes {
                        if (aid == null) {
                            showSuccessToast("审批提交成功")
                            finish()
                        } else {
                            showSuccessToast("审批修改成功")
                            setResult(Extras.RESULTCODE)
                            finish()
                        }
                    }.otherWise {
                        showErrorToast(payload.message)
                    }
                }
                onError {
                    if (aid == null) {
                        showErrorToast("审批提交失败")
                    } else {
                        showErrorToast("审批修改失败")
                    }
                }
            }
        }
    }


    private fun getBean(view: BaseControl): List<Boolean> {
        val checkValue = mutableListOf<Boolean>()
        (view.controlBean.control > 0).yes {
            //控件的判断
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
                        checkValue.add(false)
                    }.otherWise {
                        checkValue.add(true)
                    }
                }
            }
        }.otherWise {
            //套件的判断
            checkValue.addAll(view.getBean(view.controlBean))
        }
        return checkValue
    }

    private var dialog: MaterialDialog? = null
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.copyApprove -> {
                mMenu.findItem(R.id.copyApprove).isCheckable = false
                SoguApi.getService(application, ApproveService::class.java)
                        .getLastApproveID(tid)
                        .execute {
                            onNext { payload ->
                                payload.isOk.yes {
                                    payload.payload?.let {
                                        if (dialog == null) {
                                            dialog = showCopyDialog(it) {
                                                it.sid?.let {
                                                    getLastApproveDetail(it)
                                                }
                                            }
                                            dialog?.show()
                                        }
                                    }
                                }.otherWise {
                                    showErrorToast(payload.message)
                                }
                            }
                        }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        (0 until controlLayout.childCount).forEach {
            if (controlLayout.getChildAt(it) is PhotoSelection) {
                (controlLayout.getChildAt(it) as PhotoSelection).onActivityResult(requestCode, resultCode, data)
            }
            if (controlLayout.getChildAt(it) is FundSealControl) {
                (controlLayout.getChildAt(it) as FundSealControl).onActivityResult(requestCode, resultCode, data)
            }
        }

        if (requestCode == Extras.REQUESTCODE && resultCode == Extras.RESULTCODE && data != null) {
            val users = data.getSerializableExtra(Extras.DATA) as ArrayList<UserBean>
            val newCopy = users.map {
                User(name = it.name, id = it.uid.toString(), url = it.url ?: "")
            }.toMutableList()
            val def = approvers.cs.def.split(",")
            def.forEachIndexed { index, defId ->
                val find = newCopy.find { it.id == defId }
                find?.let {
                    newCopy.remove(it)
                    newCopy.add(index, it)
                }
            }
            copyPeos.clear()
            copyPeos.addAll(newCopy)
            approvers.cs.users.clear()
            approvers.cs.users.addAll(newCopy)
            copyAdapter.users.clear()
            copyAdapter.users.addAll(newCopy)
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
                getChildValue(view.controlBean.children, valueNotEmpty)
            }
        }
        if (aid != null) {
            super.onBackPressed()
        } else {
            valueNotEmpty.isNotEmpty().yes {
                //尚融不保存草稿
                if (isHidden != 1) {
                    initDialog {
                        views.forEach {
                            info { it.jsonStr }
                        }
                        saveApproveDraft(views.jsonStr, it.yes { 1 }.otherWise { 0 })
                    }.show()
                }
            }.otherWise {
                super.onBackPressed()
            }
        }
    }

    private fun getChildValue(childs: List<ControlBean>?, valueNotEmpty: MutableList<ControlBean>) {
        childs?.forEach {
            it.value.isNullOrEmpty().no {
                valueNotEmpty.add(it)
            }.otherWise {
                getChildValue(it.children, valueNotEmpty)
            }
        }
    }

    private fun initDialog(block: (flag: Boolean) -> Unit): MaterialDialog {
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
            block.invoke(false)
            mDialog.dismiss()
//            super.onBackPressed()
        }
        yes.setOnClickListener {
            mDialog.dismiss()
            block.invoke(true)
        }
        return mDialog
    }


    private fun saveApproveDraft(data: String, save: Int) {
        val str = when (save) {
            0 -> "清除"
            else -> "保存"
        }
        SoguApi.getService(application, ApproveService::class.java)
                .saveApproveDraft(tid, data, save)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            showSuccessToast("草稿${str}成功")
                            finish()
                        }.otherWise {
                            showErrorToast("草稿${str}失败")
                        }
                    }
                    onError {
                        showErrorToast("草稿${str}失败")
                    }
                }
    }


    private fun showCopyDialog(lastBean: LastApproveBean, block: () -> Unit): MaterialDialog {
        val mDialog = MaterialDialog.Builder(this)
                .theme(Theme.LIGHT)
                .canceledOnTouchOutside(true)
                .dismissListener {
                    dialog = null
                }
                .customView(R.layout.dialog_yongyin, false).build()
        val content = mDialog.find<TextView>(R.id.content)
        val cancel = mDialog.find<Button>(R.id.cancel)
        val yes = mDialog.find<Button>(R.id.yes)
        val spannable1 = SpannableString("是否将上次${lastBean.name}填写内容一键复制填写")
        lastBean.name?.let {
            spannable1.setSpan(ForegroundColorSpan(Color.parseColor("#808080")), 5, 5 + it.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }
        content.text = spannable1
        cancel.onClick {
            mDialog.dismiss()
        }
        yes.onClick {
            mDialog.dismiss()
            block.invoke()
        }
        return mDialog
    }

    private fun getLastApproveDetail(sid: Int) {
        SoguApi.getService(application, ApproveService::class.java)
                .getLastApproveDetail(sid)
                .execute {
                    onNext { payload ->
                        initApproveControl(payload)
                    }
                    onComplete {
                        getApprovers()
                    }
                }
    }


    inner class ApproverHolder(itemView: View) : RecyclerHolder<User>(itemView) {
        override fun setData(view: View, data: User, position: Int) {
//            view.itemDec.setVisible(approverAdapter.selectedItems.contains(position))
            Glide.with(this@ApproveInitiateActivity)
                    .load(data.url)
                    .listener(UserRequestListener(view.approverHeader, data.name)).into(view.approverHeader)
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
                holder.header.invalidate()
                holder.name.text = "添加"
                holder.remove.setVisible(false)
                holder.itemView.setOnClickListener { v ->
                    if (null != onItemClick) {
                        onItemClick!!(v, position)
                    }
                }
            } else if (position < users.size) {
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
                                    holder.header.setImageDrawable(resource)
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
                        (approvers.cs.users.size > position).yes {
                            approvers.cs.users.removeAt(position)
                            copyPeos.removeAt(position)
                            notifyItemRemoved(position)
                            notifyDataSetChanged()
                        }
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
