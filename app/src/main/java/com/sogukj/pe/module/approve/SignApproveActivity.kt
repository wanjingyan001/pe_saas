package com.sogukj.pe.module.approve

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.gcacace.signaturepad.views.SignaturePad
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.bean.*
import com.sogukj.pe.peUtils.FileUtil
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.peUtils.PdfUtil
import com.sogukj.pe.service.ApproveService
import com.sogukj.pe.widgets.CircleImageView
import com.sogukj.pe.widgets.MyCSAdapter
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_sign_approve.*
import kotlinx.android.synthetic.main.sign_approve_part1.*
import kotlinx.android.synthetic.main.sign_approve_part2.*
import kotlinx.android.synthetic.main.sign_approve_part3.*
import kotlinx.android.synthetic.main.state_sign_confim.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.textColor
import java.io.File
import java.io.FileOutputStream

/**
 * Created by qinfei on 17/10/18.
 */
class SignApproveActivity : ToolbarActivity() {

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
        if (paramObj == null){
            paramId = intent.getIntExtra(Extras.ID, -1)
            paramTitle = intent.getStringExtra(Extras.TITLE)
        }else{
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
        setContentView(R.layout.activity_sign_approve)
        setBack(true)
        title = paramTitle

        toolbar_menu.text = ""
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    fun refresh() {
        SoguApi.getService(application,ApproveService::class.java)
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
                        initDiscuss(payload.payload?.discuss)
                        initButtons(payload.payload?.click)

                        iv_state_agreed.visibility = View.GONE
                        iv_state_signed.visibility = View.GONE
                        val status = payload?.payload?.mainStatus

                        if (status != null && status > 1) {
                            iv_state_signed.visibility = View.VISIBLE
                            //3=>审批完成  4=>签字通过
                            if (status == 3) {
                                iv_state_signed.imageResource = R.drawable.ic_flow_state_agree
                            }
                            if (status == 4) {
                                iv_state_signed.imageResource = R.drawable.ic_flow_state_signed
                            }
                            if (status == 5) {
                                iv_state_signed.imageResource = R.drawable.ic_flow_state_chexiao
                            }
                        }
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail,payload.message)
                    }
                }, { e ->
                    Trace.e(e)
//                    showToast("请求失败")
                    showCustomToast(R.drawable.icon_toast_fail,"请求失败")
                })
    }

    lateinit var adapter: RecyclerAdapter<ApproveViewBean.DiscussBean>

    fun initDiscuss(discussList: ArrayList<ApproveViewBean.DiscussBean>?){
        if(discussList == null || discussList.size == 0){
            discuss_layout.visibility = View.GONE
            return
        }
        discuss_layout.visibility = View.VISIBLE
        adapter = RecyclerAdapter(context, { _adapter, parent, type ->
            val convertView = _adapter.getView(R.layout.layout_discuss, parent)
            object : RecyclerHolder<ApproveViewBean.DiscussBean>(convertView) {

                val icon = convertView.find<CircleImageView>(R.id.iv_user)
                val tvName = convertView.find<TextView>(R.id.tv_name)
                val tvDiscuss = convertView.find<TextView>(R.id.tv_discuss)
                val tvTime = convertView.find<TextView>(R.id.tv_time)

                override fun setData(view: View, data: ApproveViewBean.DiscussBean, position: Int) {
                    if (data.url.isNullOrEmpty()) {
                        icon.setChar(data.name?.first())
                    } else {
                        Glide.with(context)
                                .load(MyGlideUrl(data.url))
                                .listener(object : RequestListener<Drawable> {
                                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                        icon.setImageDrawable(resource)
                                        return false
                                    }

                                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                        icon.setChar(data.name?.first())
                                        return false
                                    }
                                })
                                .into(icon)
                    }
                    tvName.text = data.name
                    tvDiscuss.text = data.message
                    if(data.time_str.isNullOrEmpty()){
                        tvTime.visibility = View.INVISIBLE
                    } else {
                        tvTime.text = data.time_str//DateUtils.timesFormat(data.time.toString(), "yyyy-MM-dd HH:mm:ss")
                    }
                }
            }
        })
        discussed.layoutManager = LinearLayoutManager(context)
        discussed.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(context, 10)))
        discussed.adapter = adapter

        adapter.dataList.addAll(discussList)
        adapter.notifyDataSetChanged()
    }

    fun initCS(list: ArrayList<UserBean>) {
        var adapter = MyCSAdapter(context, list)
        grid_chaosong_to.adapter = adapter
        adapter.filter.filter("")
        if(list.size == 0){
            cs_layout.visibility = View.GONE
        }
    }

    fun showSignDialog(type: Int = 1) {
        val dialog = MaterialDialog.Builder(this)
                .theme(Theme.LIGHT)
                .customView(R.layout.dialog_approve_sgin, false)
                .cancelable(true)
                .canceledOnTouchOutside(true)
                .build()
//                .show()
        val pad = dialog.findViewById(R.id.signature_pad) as SignaturePad
        val btnLeft = dialog.findViewById(R.id.btn_left)
        val btnRight = dialog.findViewById(R.id.btn_right)
        btnLeft.setOnClickListener {
            pad.clear()
        }
        btnRight.setOnClickListener {
            dialog.dismiss()
            try {
                val bitmap = pad.signatureBitmap
                val file = File(cacheDir, "${System.currentTimeMillis()}.jpg")
                FileUtil.createNewFile(file)
                val fos = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
                fos.close()
                SoguApi.getService(application,ApproveService::class.java)
                        .approveSign(MultipartBody.Builder().setType(MultipartBody.FORM)
                                .addFormDataPart("file", file.name, RequestBody.create(MediaType.parse("*/*"), file))
                                .addFormDataPart("approval_id", paramId.toString())
                                .addFormDataPart("type", "${type}")
                                .build())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ payload ->
                            if (payload.isOk) {
//                                showToast("保存成功")
                                showCustomToast(R.drawable.icon_toast_success,"保存成功")
                                refresh()
                            } else {
                                showCustomToast(R.drawable.icon_toast_fail,payload.message)
                            }
                        }, { e ->
                            Trace.e(e)
//                            showToast("请求失败")
                            showCustomToast(R.drawable.icon_toast_fail,"保存失败")
                        })
            } catch (e: Exception) {
//                showToast("保存失败")
                showCustomToast(R.drawable.icon_toast_fail,"保存失败")
            }
        }
        dialog.show()
    }

    private fun initButtons(click: ArrayList<Int>?) {
        if(click == null || click.size == 0){
            btnLayout.visibility = View.GONE
            return
        }
        ll_twins.removeAllViews()
        state_sign_confirm.visibility = View.GONE
        click.forEach {
            var btn = Button(context)
            var params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
            btn.layoutParams = params
            //params.setMargins(Utils.dpToPx(context, 15), 0, 0, 0)
            btn.background = null//btn.setBackgroundResource(R.drawable.bg_rect_blue)
            btn.text = ""
            btn.textColor = resources.getColor(R.color.colorBlue)
            btn.textSize = 14f
            btn.gravity = Gravity.CENTER
            ll_twins.addView(btn)
            if(click.size == 1){
                btn.setBackgroundResource(R.drawable.bg_btn_blue1)
                btn.textColor = resources.getColor(R.color.white)
            }

            var divider = View(context)
            var p = LinearLayout.LayoutParams(Utils.dpToPx(context, 1), LinearLayout.LayoutParams.MATCH_PARENT)
            p.setMargins(0, Utils.dpToPx(context, 10), 0, Utils.dpToPx(context, 10))
            divider.layoutParams = p
            divider.backgroundColor = resources.getColor(R.color.text_3)
            divider.alpha = 0.5f
            ll_twins.addView(divider)

            //0=>'不显示按钮',1=>'申请加急',2=>'修改',3=>'重新发起',4=>'撤销',5=>'审批',6=>'导出pdf|用印单',7=>'用印完成',8=>'确认意见并签字', 9=>'文件签发',10=>'评论'
            when(it){
                0 -> {
                    ll_twins.removeAllViews()
                }
                1 -> {
                    btn.text = "申请加急"
                    btn.setOnClickListener {
                        SoguApi.getService(application,ApproveService::class.java)
                                .approveUrgent(paramId!!)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe({ payload ->
                                    if (payload.isOk) {
                                        showCustomToast(R.drawable.icon_toast_success,"提交成功")
                                    } else {
                                        showCustomToast(R.drawable.icon_toast_fail,payload.message)
                                    }
                                }, { e ->
                                    Trace.e(e)
                                    showCustomToast(R.drawable.icon_toast_fail,"请求失败")
                                })
                    }
                }
                2 -> {
                    btn.text = "修改"
                    btn.setOnClickListener {
                        ApproveFillActivity.start(context, true, paramType!!, paramId!!, paramTitle)
                    }
                }
                3 -> {
                    btn.text = "重新发起"
                    btn.setOnClickListener {

                    }
                }
                4 -> {
                    btn.text = "撤销"
                    btn.setOnClickListener {
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
                                            showCustomToast(R.drawable.icon_toast_success,"提交成功")
                                            refresh()
                                        } else {
                                            showCustomToast(R.drawable.icon_toast_fail,payload.message)
                                        }
                                    }, { e ->
                                        Trace.e(e)
                                        showCustomToast(R.drawable.icon_toast_fail,"撤销失败")
                                    })
                        }
                        dialog.show()
                    }
                }
                5 -> {
                    btn.text = "审批"
                    btn.setOnClickListener {

                    }
                }
                6 -> {
                    btn.text = "导出审批单"
                    btn.setOnClickListener {
                        SoguApi.getService(application,ApproveService::class.java)
                                .exportPdf(paramId!!)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe({ payload ->
                                    if (payload.isOk) {
                                        val bean = payload.payload
                                        bean?.let {
                                            PdfUtil.loadPdf(this, it.url,it.name)
                                        }
                                    } else {
                                        showCustomToast(R.drawable.icon_toast_fail,payload.message)
                                    }
                                }, { e ->
                                    Trace.e(e)
                                    showCustomToast(R.drawable.icon_toast_fail,"请求失败")
                                })
                    }
                }
                7 -> {
                    btn.text = "用印完成"
                    btn.setOnClickListener {

                    }
                }
                8 -> {
                    btn.text = "确认意见并签字"
                    state_sign_confirm.visibility = View.VISIBLE
                    btn.setOnClickListener {
                        val type = when (rg_sign.checkedRadioButtonId) {
                            R.id.rb_item1 -> 1
                            R.id.rb_item2 -> 2
                            R.id.rb_item3 -> 3
                            else -> 1
                        }
                        showSignDialog(type)
                    }
                }
                9 -> {
                    btn.text = "文件签发"
                    btn.setOnClickListener {
                        showSignDialog()
                    }
                }
                10 -> {
                    btn.text = "评论"
                    btn.setOnClickListener {
                        val inflate = LayoutInflater.from(this).inflate(R.layout.layout_input_dialog, null)
                        val dialog = MaterialDialog.Builder(this)
                                .customView(inflate, false)
                                .cancelable(true)
                                .build()
                        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        val commentInput = inflate.find<EditText>(R.id.approval_comments_input)
                        val veto = inflate.find<TextView>(R.id.veto_comment)
                        val confirm = inflate.find<TextView>(R.id.confirm_comment)
                        val title = inflate.find<TextView>(R.id.approval_comments_title)
                        commentInput.filters = Utils.getFilter(this)
                        title.text = "评论"
                        commentInput.hint = "请输入评论（选填）"
                        veto.text = "取消"
                        confirm.text = "确认"
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
                                    .discuss(paramId!!, commentInput.text.toString())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(Schedulers.io())
                                    .subscribe({ payload ->
                                        if (payload.isOk) {
                                            showCustomToast(R.drawable.icon_toast_success, "评论成功")
                                            refresh()
                                        } else {
                                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                                        }
                                    }, { e ->
                                        Trace.e(e)
                                        showCustomToast(R.drawable.icon_toast_fail, "评论失败")
                                    })
                        }
                        dialog.show()
                    }
                }
            }
        }

        if (ll_twins.childCount >= 1) {
            ll_twins.removeViewAt(ll_twins.childCount - 1)
        }
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
            val convertView = inflater.inflate(R.layout.item_approve_sign_segment, null)
            ll_segments.addView(convertView)

            val ivUser = convertView.find<CircleImageView>(R.id.iv_user)
            val tvName = convertView.findViewById<TextView>(R.id.tv_name) as TextView
            val tvTime = convertView.findViewById<TextView>(R.id.tv_time) as TextView
            val ivSign = convertView.findViewById<ImageView>(R.id.iv_sign) as ImageView


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
                        .listener(object : RequestListener<Drawable> {
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

            Glide.with(this)
                    .load(v.sign_img)
                    .into(ivSign)
        }

    }

    fun initApprovers(approveList: List<ApproverBean>?) {
        ll_approvers.removeAllViews()
        if (null == approveList || approveList.isEmpty()) {
            part2.visibility = View.GONE
            return
        }
        part2.visibility = View.VISIBLE
        val inflater = LayoutInflater.from(this)
        approveList.forEach { v ->
            val convertView = inflater.inflate(R.layout.item_approve_sign_approver, null)
            ll_approvers.addView(convertView)

            val ivUser = convertView.findViewById<CircleImageView>(R.id.iv_user) as CircleImageView
            val tvName = convertView.findViewById<TextView>(R.id.tv_name) as TextView
            val tvTime = convertView.findViewById<TextView>(R.id.tv_time) as TextView
            val tvStatus = convertView.findViewById<TextView>(R.id.tv_status) as TextView
            val ivSign = convertView.findViewById<ImageView>(R.id.iv_sign) as ImageView
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
                        .listener(object : RequestListener<Drawable> {
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

            Glide.with(this)
                    .load(v.sign_img)
                    .into(ivSign)
        }
    }

    private fun initFiles(file_list: List<ApproveViewBean.FileBean>?) {
        if (file_list == null || file_list.isEmpty()) {
            part1.visibility = View.GONE
            return
        }
        part1.visibility = View.VISIBLE
        ll_files.removeAllViews()
        file_list.forEachWithIndex { i, v ->
            val view = inflater.inflate(R.layout.item_file_single, null) as TextView
            view.text = "${i + 1}、${v.file_name}"
            ll_files.addView(view)
            if (!TextUtils.isEmpty(v.url))
                view.setOnClickListener {
                    //                    val intent = Intent(Intent.ACTION_VIEW)
//                    intent.data = Uri.parse(v.url)
//                    startActivity(intent)
                    PdfUtil.loadPdf(this,v.url,v.file_name)
                }
        }
    }

    private fun initInfo(from: ApproveViewBean.FromBean?, relax: List<ApproveViewBean.ValueBean>?) {
        val buff = StringBuffer()
        if (null != from) {
            appendLine(buff, "签字类别", from.sp_type)
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
                    .listener(object : RequestListener<Drawable> {
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
        buff.append("$k: <font color='#666666'>$v</font><br/>")
    }

    companion object {
        fun start(ctx: Activity?, bean: ApprovalBean, is_mine: Int) {
            val intent = Intent(ctx, SignApproveActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            intent.putExtra("is_mine", is_mine)
            ctx?.startActivity(intent)
        }

        fun start(ctx: Activity?, bean: MessageBean, is_mine: Int) {
            val intent = Intent(ctx, SignApproveActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            intent.putExtra("is_mine", is_mine)
            ctx?.startActivity(intent)
        }

        fun start(ctx: Activity?, data_id: Int, title: String) {
            val intent = Intent(ctx, SignApproveActivity::class.java)
            intent.putExtra("is_mine", 2)
            intent.putExtra(Extras.ID, data_id)
            intent.putExtra(Extras.TITLE, title)
            ctx?.startActivity(intent)
        }
        fun start(ctx: Context, approval_id:Int, is_mine: Int, title: String){
            val intent = Intent(ctx, SignApproveActivity::class.java)
            intent.putExtra(Extras.ID, approval_id)
            intent.putExtra("is_mine", is_mine)
            intent.putExtra(Extras.TITLE, title)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(intent)
        }
    }
}