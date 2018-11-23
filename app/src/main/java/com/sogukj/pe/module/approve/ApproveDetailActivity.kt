package com.sogukj.pe.module.approve

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.amap.api.mapcore.util.it
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.github.gcacace.signaturepad.views.SignaturePad
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.AvoidOnResult
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.module.approve.baseView.viewBean.*
import com.sogukj.pe.module.fileSelector.FileMainActivity
import com.sogukj.pe.peUtils.FileUtil
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.peUtils.PdfUtil
import com.sogukj.pe.service.ApproveService
import com.sogukj.pe.widgets.CircleImageView
import com.sogukj.pe.widgets.UserRequestListener
import com.sogukj.service.SoguApi
import io.reactivex.Observable
import io.reactivex.internal.util.HalfSerializer.onNext
import kotlinx.android.synthetic.main.activity_approve_detail.*
import kotlinx.android.synthetic.main.item_approve_seal_approver.view.*
import kotlinx.android.synthetic.main.item_control_attach_selection.view.*
import kotlinx.android.synthetic.main.item_new_approve_copy_list.view.*
import kotlinx.android.synthetic.main.item_rate.view.*
import kotlinx.android.synthetic.main.seal_approve_part1.*
import kotlinx.android.synthetic.main.seal_approve_part1.view.*
import kotlinx.android.synthetic.main.seal_approve_part2.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.*
import java.io.FileOutputStream
import kotlin.properties.Delegates

class ApproveDetailActivity : ToolbarActivity() {
//    private val kind by extraDelegate(Extras.TYPE, 4)
    private val approveId by extraDelegate(Extras.ID, 0)
    private val isMine by extraDelegate(Extras.FLAG, 0)
    private var tid by Delegates.notNull<Int>()//审批模板id
    private lateinit var tName: String//审批模板名
    private lateinit var btns: List<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_approve_detail)
        setBack(true)
    }

    override fun onResume() {
        super.onResume()
        getDetail()
    }

    private fun getDetail() {
        SoguApi.getService(application, ApproveService::class.java)
                .getApproveDetail(approveId, isMine)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                iv_state_agreed.visibility = View.GONE
                                initBasic(it.fixation)
                                initInfo(it.relax)
                                initFiles(it.files)
                                initApprovers(it.flow)
                                initManager(it.handle)
                                initCopyPeo(it.copier)
                                initButtons(it.click)
                                it.button?.let {
                                    btns = it
                                }
                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }

    /**
     * 头像,申请人,审批编号
     */
    private fun initBasic(fixation: Fixation) {
        tid = fixation.tid
        tName = fixation.tName
        Glide.with(this)
                .load(fixation.url)
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .thumbnail(0.1f)
                .listener(UserRequestListener(applicantHeader, fixation.name))
                .into(applicantHeader)
        applicantName.text = fixation.name
        approveNum.text = "审批编号:${fixation.number}"
        title = when (fixation.control) {
            -1 -> "请假"
            -2 -> "出差"
            -3 -> "外出"
            -21 -> "基金用印"
            -22 -> "管理公司用印"
            -23 -> "外资用印"
            -41 -> "报销"
            else -> fixation.title
        }

        val status = fixation.mainStatus
        if (status != null && status > 1) {
            iv_state_agreed.visibility = View.VISIBLE
            if (status == 5) {
                iv_state_agreed.imageResource = R.drawable.ic_flow_state_chexiao
            }
        }
    }

    /**
     * 审批模板中的控件和填写的内容的信息
     */
    private fun initInfo(relax: List<Relax>?) {
        relax?.let {
            val buff = StringBuffer()
            it.forEach {
                val str = if (it.value.isNullOrEmpty()) "无" else it.value
                buff.append("${it.key}: <font color='#666666'>$str</font><br/>")
            }
            approveInfo.text = Html.fromHtml(buff.toString())
        }
    }

    /**
     * 用印文件清单
     */
    private fun initFiles(files: List<File>?) {
        files.isNullOrEmpty().yes {
            part1.setVisible(false)
            return
        }.otherWise {
            files?.let {
                part1.setVisible(true)
                part1.ll_files.removeAllViews()
                it.forEachIndexed { index, file ->
                    val fileTv = layoutInflater.inflate(R.layout.item_file_single, null) as TextView
                    fileTv.text = "${index + 1}、${file.name}"
                    fileTv.clickWithTrigger {
                        PdfUtil.loadPdf(this, file.url, file.name)
                    }
                    ll_files.addView(fileTv)
                }
            }
        }
    }

    /**
     * 审批人
     */
    private fun initApprovers(flow: List<Flow>?) {
        flow.isNullOrEmpty().yes {
            part2.setVisible(false)
            return
        }.otherWise {
            flow?.let {
                it.isNotEmpty().yes {
                    part2.setVisible(true)
                    part2.ll_approvers.removeAllViews()
                    it.forEachIndexed { index, flow ->
                        val convertView = layoutInflater.inflate(R.layout.item_approve_seal_approver, null)
                        convertView.tv_position.text = flow.position
                        Glide.with(this).load(flow.url)
                                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                                .thumbnail(0.1f)
                                .listener(UserRequestListener(convertView.iv_user, flow.name))
                                .into(convertView.iv_user)
                        convertView.tv_name.text = flow.name
                        convertView.tv_status.text = flow.getStatusStr
                        convertView.tv_time.setVisible(flow.approval_time.isNotEmpty())
                        convertView.tv_time.text = flow.approval_time
                        convertView.singLayout.setVisible(flow.sign_img.isNotEmpty())
                        convertView.infoLayout.setVisible(flow.sign_img.isEmpty())
                        if (flow.status == 3 || flow.status == 5) {
                            when (flow.is_edit_file) {
                                1 -> {
                                    convertView.tv_edit.setVisible(true)
                                    convertView.tv_edit.text = "文件已修改"
                                    convertView.tv_edit.backgroundResource = R.drawable.bg_tag_edit_file_1
                                }
                                0 -> {
                                    convertView.tv_edit.setVisible(true)
                                    convertView.tv_edit.text = "文件未修改"
                                    convertView.tv_edit.backgroundResource = R.drawable.bg_tag_edit_file_0
                                }
                                else -> {
                                    convertView.tv_edit.setVisible(false)
                                }
                            }
                        } else {
                            convertView.tv_edit.setVisible(false)
                        }
                        convertView.tv_content.setVisible(!flow.content.isNullOrEmpty())
                        convertView.tv_content.text = Html.fromHtml("意见: <font color='#666666'>${flow.content ?: ""}</font><br/>")
                        convertView.ll_comments.setVisible(!flow.comment.isNullOrEmpty())
                        flow.sign_img.isNotEmpty().yes {
                            Glide.with(this)
                                    .load(flow.sign_img)
                                    .into(convertView.iv_sign)
                        }
                        flow.comment?.let { com ->
                            convertView.ll_comments.removeAllViews()
                            com.forEach {
                                addComment(convertView.ll_comments, flow.id, it)
                            }
                        }
                        convertView.tv_content.setOnClickListener {
                            initInputDialog("评论", "请输入评论文字...") {
                                reply(flow.id, content = it)
                            }.show()
                        }
                        part2.ll_approvers.addView(convertView)
                    }
                }.otherWise {
                    part2.setVisible(false)
                }
            }
        }
    }

    /**
     * (审批流程)经办人
     */
    private fun initManager(handle: List<Handle>?) {
        handle.isNullOrEmpty().yes {
            part3.setVisible(false)
            return
        }.otherWise {
            part3.setVisible(true)
            ll_segments.removeAllViews()
            handle?.let {
                it.forEach { handler ->
                    val segmentItem = layoutInflater.inflate(R.layout.item_approve_seal_segment, null)
                    ll_segments.addView(segmentItem)
                    segmentItem.tv_position.text = "经办人"
                    segmentItem.tv_name.text = handler.name
                    segmentItem.tv_status.text = handler.status_str
                    segmentItem.tv_time.setVisible(!handler.approval_time.isNullOrEmpty())
                    segmentItem.tv_time.text = handler.approval_time
                    Glide.with(this)
                            .load(handler.url)
                            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                            .thumbnail(0.1f)
                            .listener(UserRequestListener(segmentItem.iv_user, handler.name))
                            .into(segmentItem.iv_user)
                }
            }
        }
    }

    /**
     * 抄送人
     */
    private fun initCopyPeo(copier: List<Copier>?) {
        copier.isNullOrEmpty().yes {
            cs_layout.setVisible(false)
            return
        }.otherWise {
            cs_layout.setVisible(true)
            copier?.let {
                val copyAdapter = RecyclerAdapter<Copier>(this) { _adapter, parent, _ ->
                    object : RecyclerHolder<Copier>(_adapter.getView(R.layout.item_new_approve_copy_list, parent)) {
                        override fun setData(view: View, data: Copier, position: Int) {
                            Glide.with(this@ApproveDetailActivity)
                                    .load(data.url)
                                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                                    .thumbnail(0.1f)
                                    .listener(UserRequestListener(view.approverHeader, data.name))
                                    .into(view.approverHeader)
                            view.approverName.text = data.name
                        }
                    }
                }
                copyAdapter.dataList.addAll(it)
                copyPeoList.apply {
                    layoutManager = GridLayoutManager(this@ApproveDetailActivity, 6)
                    adapter = copyAdapter
                }
                copyAdapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * 除待我审批之外的3中状态初始化下方操作按钮
     */
    private fun initButtons(click: List<Int>?) {
        click.isNullOrEmpty().yes {
            btnLayout.setVisible(false)
            return
        }.otherWise {
            btnLayout.setVisible(true)
            operateLayout.removeAllViews()
            click?.let {
                it.forEach {
                    //添加按钮
                    val operateBtn = TextView(this)
                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
                    operateBtn.setPadding(0, dip(12), 0, dip(12))
                    operateBtn.layoutParams = params
                    operateBtn.background = null
                    operateBtn.text = ""
                    operateBtn.textColor = resources.getColor(R.color.colorPrimary)
                    operateBtn.textSize = 14f
                    operateBtn.gravity = Gravity.CENTER
                    if (click.size == 1) {
                        operateBtn.setBackgroundResource(R.drawable.bg_btn_blue1)
                        operateBtn.textColor = resources.getColor(R.color.white)
                    }
                    operateLayout.addView(operateBtn)
                    //添加分割线
                    val divider = View(this)
                    val p = LinearLayout.LayoutParams(Utils.dpToPx(context, 1), LinearLayout.LayoutParams.MATCH_PARENT)
                    p.setMargins(0, Utils.dpToPx(context, 10), 0, Utils.dpToPx(context, 10))
                    divider.layoutParams = p
                    divider.backgroundColor = resources.getColor(R.color.text_3)
                    divider.alpha = 0.5f
                    operateLayout.addView(divider)
                    when (it) {
                        1 -> {
                            operateBtn.text = "申请加急"
                            operateBtn.clickWithTrigger {
                                expedited()
                            }
                        }
                        2 -> {
                            operateBtn.text = "修改"
                            operateBtn.clickWithTrigger {
                                startActivityForResult<ApproveInitiateActivity>(Extras.REQUESTCODE,
                                        Extras.ID to tid,
                                        Extras.ID2 to approveId,
                                        Extras.NAME to tName)
                            }

                        }
                        3 -> {
                            operateBtn.text = "重新发起"
                        }
                        4 -> {
                            operateBtn.text = "撤销"
                            operateBtn.clickWithTrigger {
                                initInputDialog("撤销", "请输入撤销理由") {
                                    cancelApprove(it)
                                }.show()
                            }
                        }
                        5 -> {
                            operateBtn.text = "审批"
                            operateBtn.clickWithTrigger {
                                initApproveDialog(btns)
                            }
                        }
                        6 -> {
                            operateBtn.text = "导出审批单"
                            operateBtn.clickWithTrigger {
                                deriveFile()
                            }
                        }
                        7 -> {
                            //原用印完成
                            operateBtn.text = "审批完成"
                            operateBtn.clickWithTrigger {
                                approveOver()
                            }
                        }
                        8 -> {
                            operateBtn.text = "确认意见并签字"
                            operateBtn.clickWithTrigger { _ ->
                                info { btns.jsonStr }
                                initApproveDialog(btns,true)

                            }
                        }
                        9 -> {
                            operateBtn.text = "文件签发"
                        }
                        10 -> {
                            operateBtn.text = "评论"
                            operateBtn.clickWithTrigger {
                                initInputDialog("评论", "请输入评论文字...") {
                                    commentSubmit(it)
                                }.show()
                            }
                        }
                        else -> {
                            operateLayout.removeAllViews()
                        }
                    }
                }
                if (operateLayout.childCount >= 1) {
                    operateLayout.removeViewAt(operateLayout.childCount - 1)
                }
            }
        }
    }


    /**
     * 将留言列表添加到界面上
     */
    private fun addComment(commentLayout: LinearLayout, hid: Int, data: Comment) {
        val convertView = layoutInflater.inflate(R.layout.item_approve_comment, null)
        commentLayout.addView(convertView)
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
        Glide.with(this)
                .load(MyGlideUrl(data.url))
                .thumbnail(0.1f)
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .listener(UserRequestListener(ivUser, data.name))
                .into(ivUser)
        convertView.setOnClickListener {
            initInputDialog("评论", "请输入评论文字...") {
                reply(hid, data.comment_id, it)
            }.show()
        }
    }


    /**
     * 提交评论
     */
    private fun commentSubmit(content: String) {
        SoguApi.getService(application, ApproveService::class.java)
                .submitComment(approveId, content)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            showSuccessToast("提交成功")
                            getDetail()
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }

    /**
     * 回复
     */
    private fun reply(hid: Int, comment_id: Int = 0, content: String) {
        SoguApi.getService(application, ApproveService::class.java)
                .reply(hid, comment_id, content)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            getDetail()
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }

    /**
     * 输入dialog
     */
    private fun initInputDialog(titleStr: String, hint: String, block: (content: String) -> Unit): MaterialDialog {
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
        title.text = titleStr
        commentInput.filters = Utils.getFilter(this)
        commentInput.hint = hint
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
                block.invoke(commentInput.textStr)
            }
        }
        return dialog
    }

    /**
     * 签字dialog
     */
    private fun initSignatureDialog(block: (signature: java.io.File) -> Unit): MaterialDialog {
        val dialog = MaterialDialog.Builder(this)
                .theme(Theme.LIGHT)
                .customView(R.layout.dialog_approve_sgin, false)
                .cancelable(true)
                .canceledOnTouchOutside(true)
                .build()
        val pad = dialog.findViewById(R.id.signature_pad) as SignaturePad
        val btnLeft = dialog.findViewById(R.id.btn_left)
        val btnRight = dialog.findViewById(R.id.btn_right)
        btnLeft.setOnClickListener {
            pad.clear()
        }
        btnRight.setOnClickListener {
            dialog.dismiss()
            val file = java.io.File(cacheDir, "${System.currentTimeMillis()}.jpg")
            FileUtil.createNewFile(file)
            val fos = FileOutputStream(file)
            pad.signatureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            block.invoke(file)
        }
        return dialog
    }

    /**
     * 审批dialog(按钮由接口返回,可能是"同意","不同意"等等)
     */
    private fun initApproveDialog(button: List<Button>,isSignature:Boolean = false) {
        val inflate = LayoutInflater.from(this).inflate(R.layout.layout_new_approve_dialog, null)
        val dialog = MaterialDialog.Builder(this)
                .theme(Theme.LIGHT)
                .customView(inflate, false)
                .cancelable(true)
                .build()
        val operateBtns = inflate.find<LinearLayout>(R.id.operateLayout)
        val contentEdt = inflate.find<EditText>(R.id.approval_comments_input)
        val commentAttachment = inflate.find<TextView>(R.id.commentAttachment)
        val fileName = inflate.find<TextView>(R.id.fileName)
        val deleteFile = inflate.find<ImageView>(R.id.deleteFile)
        val addAttachment = inflate.find<TextView>(R.id.addAttachment)
        var filePath: String? = null
        button.let {
            it.isNotEmpty().yes {
                it.forEach {
                    if (it.value.contains("上传")) {
                        //上传附件
                        commentAttachment.setVisible(true)
                        addAttachment.setVisible(true)
                        commentAttachment.text = it.value
                        addAttachment.clickWithTrigger {
                            showProgress("正在读取内存文件")
                            AvoidOnResult(this)
                                    .startForResult<FileMainActivity>(Extras.REQUESTCODE,
                                            Extras.TYPE to true,
                                            Extras.FLAG to true,
                                            Extras.DATA to 1)
                                    .filter { it.resultCode == Activity.RESULT_OK }
                                    .flatMap {
                                        val path = it.data.getStringExtra(Extras.DATA)
                                        Observable.just(path)
                                    }.subscribe { path ->
                                hideProgress()
                                fileName.setVisible(true)
                                deleteFile.setVisible(true)
                                filePath = path
                                fileName.text = java.io.File(path).name
                            }
                        }
                        deleteFile.clickWithTrigger {
                            filePath = null
                            fileName.setVisible(false)
                            deleteFile.setVisible(false)
                        }
                    } else {
                        //同意,不同意
                        val operateBtn = TextView(this)
                        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
                        params.rightMargin = dip(10)
                        operateBtn.setPadding(0, dip(12), 0, dip(12))
                        operateBtn.layoutParams = params
                        if (it.value == "同意") {
                            operateBtn.setBackgroundResource(R.drawable.bg_btn_blue1)
                            operateBtn.textColor = resources.getColor(R.color.white)
                        } else {
                            operateBtn.setBackgroundResource(R.drawable.bg_rect_blue)
                            operateBtn.textColor = resources.getColor(R.color.colorPrimary)
                        }
                        operateBtn.text = it.value
                        operateBtn.textSize = 16f
                        operateBtn.gravity = Gravity.CENTER
                        operateBtns.addView(operateBtn)
                        operateBtn.clickWithTrigger { _ ->
                            dialog.dismiss()
                            if (isSignature){
                                initSignatureDialog { file ->
                                    doApprove(type = it.key,content = contentEdt.textStr, file = file)
                                }.show()
                            }else{
                                val file = if (filePath == null) null else java.io.File(filePath)
                                showConfirmDialog(it, contentEdt.textStr, file = file)
                            }
                        }
                    }
                }
            }
        }
        dialog.show()
    }


    private fun showConfirmDialog(btn: Button, content: String? = null, file: java.io.File? = null) {
        val title = "是否确认${btn.value}审批？"
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
            doApprove(btn.key, content, file)
        }
        build.show()
    }

    /**
     * 提交审批
     */
    private fun doApprove(type: Int, content: String? = null, file: java.io.File? = null) {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("aid", approveId.toString())
                .addFormDataPart("type", type.toString())
        content?.let {
            builder.addFormDataPart("content", it)
        }
        file?.let {
            builder.addFormDataPart("file", it.name, RequestBody.create(MediaType.parse(""), it))
        }
        SoguApi.getService(application, ApproveService::class.java)
                .doApprove(builder.build())
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            showSuccessToast("审批完成")
                            finish()
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }

    /**
     * 撤销审批
     */
    private fun cancelApprove(content: String? = null) {
        SoguApi.getService(application, ApproveService::class.java)
                .approveCancel(approveId, content)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            showSuccessToast("撤销完成")
                            finish()
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }

    /**
     * 申请加急
     */
    private fun expedited() {
        SoguApi.getService(application, ApproveService::class.java)
                .expedited(approveId)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            showSuccessToast("加急成功")
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }

    /**
     * 完成用印|签字完成
     */
    private fun approveOver() {
        SoguApi.getService(application, ApproveService::class.java)
                .approveOver(approveId)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            showSuccessToast("审批完成")
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }

    /**
     * 上传文件
     */
    private fun uploadFiles(file: java.io.File, block: (bean: ApproveValueBean) -> Unit) {
        SoguApi.getService(application, ApproveService::class.java)
                .uploadFiles(MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("file", file.name, RequestBody.create(MediaType.parse("*/*"), file))
                        .build())
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            showSuccessToast("上传成功")
                            payload.payload?.let {
                                block.invoke(ApproveValueBean(name = it.name, url = it.url, size = it.size))
                            }
                        }.otherWise {
                            showErrorToast("上传失败")
                        }
                    }
                    onError {
                        showErrorToast("上传失败")
                    }
                }
    }

    private fun deriveFile() {
        SoguApi.getService(application, ApproveService::class.java)
                .deriveWps(approveId)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            val bean = payload.payload
                            bean?.let {
                                PdfUtil.loadPdf(this@ApproveDetailActivity, it.url, it.name)
                            }
                        } else {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Extras.REQUESTCODE && resultCode == Extras.RESULTCODE) {
            getDetail()
        }
    }
}
