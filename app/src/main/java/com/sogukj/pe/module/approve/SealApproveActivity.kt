package com.sogukj.pe.module.approve

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
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
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.*
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.peUtils.PdfUtil
import com.sogukj.pe.service.ApproveService
import com.sogukj.pe.widgets.CircleImageView
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_seal_approve.*
import kotlinx.android.synthetic.main.seal_approve_part1.*
import kotlinx.android.synthetic.main.seal_approve_part2.*
import kotlinx.android.synthetic.main.seal_approve_part3.*
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import java.text.SimpleDateFormat

/**
 * Created by qinfei on 17/10/18.
 */
class SealApproveActivity : ToolbarActivity() {

    lateinit var inflater: LayoutInflater
    lateinit var paramTitle: String
    var paramId: Int? = null
    var paramType: Int? = null
    var is_mine = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflater = LayoutInflater.from(this)
        val paramObj = intent.getSerializableExtra(Extras.DATA)
        is_mine = intent.getIntExtra("is_mine", 2)
        if (paramObj == null) {
            paramId = intent.getIntExtra(Extras.ID, -1)
            paramTitle = intent.getStringExtra(Extras.TITLE)
            paramType = intent.getIntExtra(Extras.TYPE, 2)
        } else {
            if (paramObj is ApprovalBean) {
                paramTitle = paramObj.kind!!
                paramId = paramObj.approval_id!!
                paramType = paramObj.type
            } else if (paramObj is MessageBean) {
                paramTitle = paramObj.type_name!!
                paramId = paramObj.approval_id!!
                paramType = paramObj.type
            } else if (paramObj is SpGroupItemBean) {
                paramTitle = paramObj.name!!
                paramId = paramObj.id!!
                paramType = paramObj.type
            } else {
                finish()
                return
            }
        }
        setContentView(R.layout.activity_seal_approve)
        setBack(true)
        title = paramTitle

        toolbar_menu.text = ""
        toolbar_menu.setOnClickListener {
            if (!toolbar_menu.text.isNullOrEmpty()) {
                ApproveFillActivity.start(context, true, paramType!!, paramId!!, paramTitle)
            }
        }

        refresh()
    }

    fun refresh() {
        SoguApi.getService(application, ApproveService::class.java)
                .showApprove(approval_id = paramId!!, classify = paramType, is_mine = is_mine)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        initUser(payload.payload?.fixation)
                        initInfo(payload.payload?.fixation, payload.payload?.relax)
                        initFiles(payload.payload?.file_list)
                        initApprovers(payload.payload?.approve)
                        initSegments(payload.payload?.segment)
                        payload.payload?.copier?.apply {
                            initCS(this)
                        }
                        initButtons(payload.payload?.click)

                        iv_state_agreed.visibility = View.GONE
                        iv_state_signed.visibility = View.GONE

                        val status = payload?.payload?.mainStatus
                        if (status != null && status > 1) {
                            iv_state_agreed.visibility = View.VISIBLE
                            if (status == 5) {
                                iv_state_agreed.imageResource = R.drawable.ic_flow_state_chexiao
                            }
                        }

                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_common, "暂无可用数据")
                })
    }

    fun initCS(list: ArrayList<UserBean>) {
        var adapter = MyAdapter(context, list)
        grid_chaosong_to.adapter = adapter
        if(list.size == 0){
            cs_layout.visibility = View.GONE
        }
    }

    fun initButtons(click: Int?) {
        btn_single.visibility = View.GONE
        ll_twins.visibility = View.GONE
        when (click) {
            0 -> {
                btn_single.visibility = View.GONE
            }
            1 -> {
                ll_twins.visibility = View.VISIBLE
                btn_left.text = "申请加急"
                btn_left.setOnClickListener {
                    SoguApi.getService(application,ApproveService::class.java)
                            .approveUrgent(paramId!!)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ payload ->
                                if (payload.isOk) {
                                    showCustomToast(R.drawable.icon_toast_success, "提交成功")
                                } else {
                                    showCustomToast(R.drawable.icon_toast_fail, payload.message)
                                }
                            }, { e ->
                                Trace.e(e)
                                showCustomToast(R.drawable.icon_toast_fail, "请求失败")
                            })
                }
                btn_right.text = "撤销"
                btn_right.setOnClickListener {
                    val inflate = LayoutInflater.from(this).inflate(R.layout.layout_input_dialog, null)
                    val dialog = MaterialDialog.Builder(this)
                            .customView(inflate, false)
                            .cancelable(true)
                            .build()
                    dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    val title = inflate.find<TextView>(R.id.approval_comments_title)
                    val commentInput = inflate.find<EditText>(R.id.approval_comments_input)
                    val veto = inflate.find<TextView>(R.id.veto_comment)
                    val confirm = inflate.find<TextView>(R.id.confirm_comment)
                    commentInput.filters = Utils.getFilter(this)
                    title.text = "请输入撤销理由"
                    title.textSize = 16.toFloat()
                    commentInput.hint = ""
                    veto.text = "取消"
                    confirm.text = "提交"
                    veto.setOnClickListener {
                        if (dialog.isShowing) {
                            dialog.dismiss()
                        }
                    }
                    confirm.setOnClickListener {
                        if (dialog.isShowing) {
                            dialog.dismiss()
                        }
                        SoguApi.getService(application,ApproveService::class.java)
                                .cancelApprove(paramId!!)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe({ payload ->
                                    if (payload.isOk) {
                                        showCustomToast(R.drawable.icon_toast_success, "提交成功")
                                        refresh()
                                    } else {
                                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                                    }
                                }, { e ->
                                    Trace.e(e)
                                    showCustomToast(R.drawable.icon_toast_fail, "撤销失败")
                                })
                    }
                    dialog.show()
                }
                toolbar_menu.text = ""
            }
            2 -> {
                btn_single.visibility = View.VISIBLE
                btn_single.text = "申请加急"
                btn_single.setOnClickListener {
                    SoguApi.getService(application,ApproveService::class.java)
                            .approveUrgent(paramId!!)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ payload ->
                                if (payload.isOk) {
                                    showCustomToast(R.drawable.icon_toast_success, "提交成功")
                                } else {
                                    showCustomToast(R.drawable.icon_toast_fail, payload.message)
                                }
                            }, { e ->
                                Trace.e(e)
                                showCustomToast(R.drawable.icon_toast_fail, "请求失败")
                            })
                }
                toolbar_menu.text = ""
            }
            3 -> {
                btn_single.visibility = View.VISIBLE
                btn_single.text = "重新发起审批"
                btn_single.setOnClickListener {
                    //BuildSealActivity.start(this, paramId!!, paramType, paramTitle, true)
                    ApproveFillActivity.start(context, true, paramType!!, paramId!!, paramTitle, 1)
                    finish()
                }
                toolbar_menu.text = ""
            }
            4 -> {
                ll_twins.visibility = View.VISIBLE
                btn_left.setOnClickListener {
                    SoguApi.getService(application,ApproveService::class.java)
                            .exportPdf(paramId!!)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ payload ->
                                if (payload.isOk) {
                                    val bean = payload.payload
                                    bean?.let {
                                        //                                        if (!TextUtils.isEmpty(it.url)) {
//                                            val intent = Intent(Intent.ACTION_VIEW)
//                                            intent.data = Uri.parse(it.url)
//                                            startActivity(intent)
//                                        }
                                        PdfUtil.loadPdf(this, it.url, it.name)
                                    }
                                } else {
                                    showCustomToast(R.drawable.icon_toast_fail, payload.message)
                                }
                            }, { e ->
                                Trace.e(e)
//                                showToast("请求失败")
                                showCustomToast(R.drawable.icon_toast_fail, "请求失败")
                            })
                }
                btn_right.setOnClickListener {
                    SoguApi.getService(application,ApproveService::class.java)
                            .finishApprove(paramId!!)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ payload ->
                                if (payload.isOk) {
                                    refresh()
                                } else {
                                    showCustomToast(R.drawable.icon_toast_fail, payload.message)
                                }
                            }, { e ->
                                Trace.e(e)
//                                showToast("请求失败")
                                showCustomToast(R.drawable.icon_toast_fail, "请求失败")
                            })
                }
            }
            5 -> {
                btn_single.visibility = View.VISIBLE
                btn_single.text = "审批"
                btn_single.setOnClickListener {
                    val inflate = LayoutInflater.from(this).inflate(R.layout.layout_input_dialog, null)
                    val dialog = MaterialDialog.Builder(this)
                            .customView(inflate, false)
                            .cancelable(true)
                            .build()
                    dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    val commentInput = inflate.find<EditText>(R.id.approval_comments_input)
                    val veto = inflate.find<TextView>(R.id.veto_comment)
                    val confirm = inflate.find<TextView>(R.id.confirm_comment)
                    commentInput.filters = Utils.getFilter(this)
                    veto.setOnClickListener {
                        if (dialog.isShowing) {
                            dialog.dismiss()
                        }
                        showConfirmDialog(-1, commentInput.text.toString())
                    }
                    confirm.setOnClickListener {
                        if (dialog.isShowing) {
                            dialog.dismiss()
                        }
                        showConfirmDialog(1, commentInput.text.toString())
                    }
                    dialog.show()
                }
            }
            6 -> {
                btn_single.visibility = View.VISIBLE
                btn_single.text = "导出审批单"
                btn_single.setOnClickListener {
                    SoguApi.getService(application,ApproveService::class.java)
                            .exportPdf(paramId!!)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ payload ->
                                if (payload.isOk) {
                                    val bean = payload.payload
//                                    if (!TextUtils.isEmpty(url)) {
//                                        val intent = Intent(Intent.ACTION_VIEW)
//                                        intent.data = Uri.parse(url)
//                                        startActivity(intent)
//                                    }
                                    bean?.let {
                                        PdfUtil.loadPdf(this, it.url, it.name)
                                    }
                                } else {
                                    showCustomToast(R.drawable.icon_toast_fail, payload.message)
                                }
                            }, { e ->
                                Trace.e(e)
//                                showToast("请求失败")
                                showCustomToast(R.drawable.icon_toast_fail, "请求失败")
                            })
                }


            }
        }
    }

    private fun showConfirmDialog(type: Int, text: String = "") {
        val title = if (type == 1) "是否确认通过审批？" else "是否确认否决审批？"
        val build = MaterialDialog.Builder(this)
                .theme(Theme.LIGHT)
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
            examineApprove(type, text)
        }
        build.show()
    }

    fun examineApprove(type: Int, text: String = "") {
        SoguApi.getService(application,ApproveService::class.java)
                .examineApprove(paramId!!, type, text)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
//                        showToast("提交成功")
                        showCustomToast(R.drawable.icon_toast_success, "提交成功")
                        refresh()
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
//                    showToast("提交失败")
                    showCustomToast(R.drawable.icon_toast_fail, "提交失败")
                })
    }


    fun initSegments(segments: List<ApproverBean>?) {
        ll_segments.removeAllViews()
        if (null == segments || segments.isEmpty()) {
            part3.visibility = View.GONE
            return
        }
        part3.visibility = View.VISIBLE
        val inflater = LayoutInflater.from(this)
        segments.forEach { v ->
            val convertView = inflater.inflate(R.layout.item_approve_seal_segment, null)
            ll_segments.addView(convertView)

            val tvPosition = convertView.findViewById<TextView>(R.id.tv_position) as TextView
            val ivUser = convertView.findViewById<CircleImageView>(R.id.iv_user) as CircleImageView
            val tvName = convertView.findViewById<TextView>(R.id.tv_name) as TextView
            val tvStatus = convertView.findViewById<TextView>(R.id.tv_status) as TextView
            val tvTime = convertView.findViewById<TextView>(R.id.tv_time) as TextView
            tvPosition.text = v.position
            tvName.text = v.name
            tvStatus.text = v.status_str
            tvTime.text = v.approval_time
            if (null != v.approval_time || !TextUtils.isEmpty(v.approval_time)) {
                tvTime.visibility = View.VISIBLE
            } else {
                tvTime.visibility = View.GONE
            }
            if(v.url.isNullOrEmpty()){
                val ch = v.name?.first()
                ivUser.setChar(ch)
            } else {
                Glide.with(this)
                        .load(MyGlideUrl(v.url))
                        .listener(object :RequestListener<Drawable> {
                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                ivUser.setImageDrawable(resource)
                                return false
                            }

                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                val ch = v.name?.first()
                                ivUser.setChar(ch)
                                return false
                            }
                        })
                        .into(ivUser)
            }
        }

    }

    private fun initApprovers(approveList: List<ApproverBean>?) {
        ll_approvers.removeAllViews()
        if (null == approveList || approveList.isEmpty()) {
            part2.visibility = View.GONE
            return
        }
        part2.visibility = View.VISIBLE
        val inflater = LayoutInflater.from(this)
        approveList.forEach { v ->
            val convertView = inflater.inflate(R.layout.item_approve_seal_approver, null)
            ll_approvers.addView(convertView)

            val tvPosition = convertView.findViewById<TextView>(R.id.tv_position) as TextView
            val ivUser = convertView.findViewById<CircleImageView>(R.id.iv_user) as CircleImageView
            val tvName = convertView.findViewById<TextView>(R.id.tv_name) as TextView
            val tvStatus = convertView.findViewById<TextView>(R.id.tv_status) as TextView
            val tvTime = convertView.findViewById<TextView>(R.id.tv_time) as TextView
            val tvEdit = convertView.findViewById<TextView>(R.id.tv_edit) as TextView
            val tvContent = convertView.findViewById<TextView>(R.id.tv_content) as TextView
            val llComments = convertView.findViewById<LinearLayout>(R.id.ll_comments) as LinearLayout

            if (v.status == 3) {
                tvEdit.visibility = View.VISIBLE
                if (v.is_edit_file == 1) {
                    tvEdit.text = "文件已修改"
                    tvEdit.setBackgroundResource(R.drawable.bg_tag_edit_file_1)
                } else {
                    tvEdit.text = "文件未修改"
                    tvEdit.setBackgroundResource(R.drawable.bg_tag_edit_file_0)
                }
            } else
                tvEdit.visibility = View.GONE
            tvPosition.text = v.position
            tvName.text = v.name
            tvTime.text = v.approval_time
            if (null != v.approval_time || !TextUtils.isEmpty(v.approval_time)) {
                tvTime.visibility = View.VISIBLE
            } else {
                tvTime.visibility = View.GONE
            }
            if(v.url.isNullOrEmpty()){
                val ch = v.name?.first()
                ivUser.setChar(ch)
            } else {
                Glide.with(this)
                        .load(MyGlideUrl(v.url))
                        .listener(object :RequestListener<Drawable> {
                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                ivUser.setImageDrawable(resource)
                                return false
                            }

                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                val ch = v.name?.first()
                                ivUser.setChar(ch)
                                return false
                            }
                        })
                        .into(ivUser)
            }

            tvStatus.text = v.status_str
            val buff = StringBuffer()
            if (null != v.content) {
                appendLine(buff, "意见", v.content)
            }
            tvContent.text = Html.fromHtml(buff.toString())
            tvContent.visibility = View.GONE
            llComments.visibility = View.GONE
            if (null != v.content && !TextUtils.isEmpty(v.content)) {
                tvContent.visibility = View.VISIBLE
                tvContent.setOnClickListener {
                    doComment(llComments, v.hid!!)
                }

                if (null != v.comment && v.comment!!.isNotEmpty()) {
                    llComments.visibility = View.VISIBLE
                    llComments.removeAllViews()
                    v.comment?.forEach { data ->
                        addComment(llComments, v.hid!!, data)
                    }
                }
            }
        }
    }

    fun doComment(llComments: LinearLayout, hid: Int, commentId: Int = 0) {
        val inflate = LayoutInflater.from(this).inflate(R.layout.layout_input_dialog, null)
        val dialog = MaterialDialog.Builder(this)
                .theme(Theme.LIGHT)
                .customView(inflate, false)
                .cancelable(true)
                .build()
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val title = inflate.find<TextView>(R.id.approval_comments_title)
        val commentInput = inflate.find<EditText>(R.id.approval_comments_input)
        val veto = inflate.find<TextView>(R.id.veto_comment)
        val confirm = inflate.find<TextView>(R.id.confirm_comment)
        title.text = "评论"
        commentInput.filters = Utils.getFilter(this)
        commentInput.hint = "请输入评论文字..."
        veto.text = resources.getString(R.string.cancel)
        confirm.text = resources.getString(R.string.confirm)
        veto.setOnClickListener {
            Utils.closeInput(this, commentInput)
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
        confirm.setOnClickListener {
            Utils.closeInput(this, commentInput)
            if (dialog.isShowing) {
                dialog.dismiss()
            }
            val text = commentInput.text.toString()
            if (!TextUtils.isEmpty(text))
                SoguApi.getService(application,ApproveService::class.java)
                        .submitComment(hid, comment_id = commentId, content = text)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ payload ->
                            if (payload.isOk) {
//                                showToast("提交成功")
                                showCustomToast(R.drawable.icon_toast_success, "提交成功")
                                refresh()
                            } else {
                                showCustomToast(R.drawable.icon_toast_fail, payload.message)
                            }
                        }, { e ->
                            Trace.e(e)
//                            showToast("提交失败")
                            showCustomToast(R.drawable.icon_toast_fail, "提交失败")
                        })
        }
        dialog.show()
    }

    val fmt_time = SimpleDateFormat("")
    fun addComment(llComments: LinearLayout, hid: Int, data: ApproveViewBean.CommentBean) {
        val convertView = inflater.inflate(R.layout.item_approve_comment, null)
        llComments.addView(convertView)
        val ivUser = convertView.findViewById<CircleImageView>(R.id.iv_user) as CircleImageView
        val tvName = convertView.findViewById<TextView>(R.id.tv_name) as TextView
        val tvTime = convertView.findViewById<TextView>(R.id.tv_time) as TextView
        val tvComment = convertView.findViewById<TextView>(R.id.tv_comment) as TextView

        tvName.text = data.name
        tvTime.text = data.add_time
        val buff = StringBuffer()
        if (!TextUtils.isEmpty(data.reply))
            buff.append("回复<font color='#608cf8'>${data.reply}</font>")
        buff.append(data.content)
        tvComment.text = Html.fromHtml(buff.toString())
        if(data.url.isNullOrEmpty()){
            val ch = data.name?.first()
            ivUser.setChar(ch)
        } else {
            Glide.with(this)
                    .load(MyGlideUrl(data.url))
                    .listener(object :RequestListener<Drawable> {
                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            ivUser.setImageDrawable(resource)
                            return false
                        }

                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            val ch = data.name?.first()
                            ivUser.setChar(ch)
                            return false
                        }
                    })
                    .into(ivUser)
        }

        convertView.setOnClickListener {
            doComment(llComments, hid, data.comment_id!!)
        }
    }

    private fun initFiles(file_list: List<ApproveViewBean.FileBean>?) {
        if (file_list == null || file_list.isEmpty()) {
            part1.visibility = View.GONE
            return
        }
        part1.visibility = View.VISIBLE
        ll_files.removeAllViews()
        val inflater = LayoutInflater.from(this)
        file_list.forEachWithIndex { i, v ->
            val view = inflater.inflate(R.layout.item_file_single, null) as TextView
            view.text = "${i + 1}、${v.file_name}"
            ll_files.addView(view)
            if (!TextUtils.isEmpty(v.url))
                view.setOnClickListener {
                    //                    val intent = Intent(Intent.ACTION_VIEW)
//                    intent.data = Uri.parse(v.url)
//                    startActivity(intent)
                    PdfUtil.loadPdf(this, v.url, v.file_name)
                }
        }
    }

    private fun initInfo(from: ApproveViewBean.FromBean?, relax: List<ApproveViewBean.ValueBean>?) {
        val buff = StringBuffer()
        if (null != from) {
            appendLine(buff, "用印类别", from.sp_type)
            appendLine(buff, "提交时间", from.add_time)
        }
        relax?.forEach { v ->
            appendLine(buff, v.name, v.value)
        }
        tv_info.text = Html.fromHtml(buff.toString())
    }

    private fun initUser(fixation: ApproveViewBean.FromBean?) {
        if (null == fixation) return
        if(fixation.url.isNullOrEmpty()){
            val ch = fixation.name?.first()
            iv_user.setChar(ch)
        } else {
            Glide.with(this)
                    .load(fixation.url)
                    .listener(object :RequestListener<Drawable> {
                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            iv_user.setImageDrawable(resource)
                            return false
                        }

                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            val ch = fixation.name?.first()
                            iv_user.setChar(ch)
                            return false
                        }
                    })
                    .into(iv_user)
        }
        tv_name.text = fixation.name
        tv_num.text = "审批编号:${fixation.number}"
    }

    fun appendLine(buff: StringBuffer, k: String?, v: String?) {
        if (null == k) return
        val sval = if (TextUtils.isEmpty(v)) "" else v
        buff.append("$k: <font color='#666666'>$sval</font><br/>")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Extras.REQ_EDIT && resultCode === Activity.RESULT_OK) {
            val id = data?.getIntExtra(Extras.ID, paramId!!)
            if (null != id) {
                paramId = id
                refresh()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        fun start(ctx: Activity?, bean: ApprovalBean, is_mine: Int) {
            val intent = Intent(ctx, SealApproveActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            intent.putExtra("is_mine", is_mine)
            ctx?.startActivity(intent)
        }

        fun start(ctx: Activity?, bean: MessageBean, is_mine: Int) {
            val intent = Intent(ctx, SealApproveActivity::class.java)
            intent.putExtra("is_mine", is_mine)
            intent.putExtra(Extras.DATA, bean)
            ctx?.startActivity(intent)
        }

        fun start(ctx: Activity?, data_id: Int, title: String) {
            val intent = Intent(ctx, SealApproveActivity::class.java)
            intent.putExtra("is_mine", 2)
            intent.putExtra(Extras.ID, data_id)
            intent.putExtra(Extras.TITLE, title)
            ctx?.startActivity(intent)
        }

        fun start(ctx: Context, approval_id: Int, is_mine: Int, title: String, type: Int? = 2) {
            val intent = Intent(ctx, SealApproveActivity::class.java)
            intent.putExtra(Extras.ID, approval_id)
            intent.putExtra("is_mine", is_mine)
            intent.putExtra(Extras.TITLE, title)
            intent.putExtra(Extras.TYPE, type)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(intent)
        }
    }

    class MyAdapter(var context: Context, val list: ArrayList<UserBean>) : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var viewHolder: ViewHolder
            var conView = convertView
            if (conView == null) {
                viewHolder = ViewHolder()
                conView = LayoutInflater.from(context).inflate(R.layout.send_item, null) as LinearLayout
                viewHolder.icon = conView.findViewById<CircleImageView>(R.id.icon) as CircleImageView
                viewHolder.name = conView.findViewById<TextView>(R.id.name) as TextView
                conView.setTag(viewHolder)
            } else {
                viewHolder = conView.tag as ViewHolder
            }
            if (list[position].url.isNullOrEmpty()) {
                viewHolder.icon?.setChar(list[position].name.first())
            } else {
                Glide.with(context)
                        .load(MyGlideUrl(list[position].url))
                        .listener(object :RequestListener<Drawable> {
                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                viewHolder.icon?.setImageDrawable(resource)
                                return false
                            }

                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                val ch = list[position].name.first()
                                viewHolder.icon?.setChar(ch)
                                return false
                            }
                        })
                        .into(viewHolder.icon!!)
            }
            viewHolder.name?.text = list[position].name
            return conView
        }

        override fun getItem(position: Int): Any {
            return list.get(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return list.size
        }

        class ViewHolder {
            var icon: CircleImageView? = null
            var name: TextView? = null
        }
    }
}
