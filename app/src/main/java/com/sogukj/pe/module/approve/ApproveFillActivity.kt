package com.sogukj.pe.module.approve

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bigkoo.pickerview.OptionsPickerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.huantansheng.easyphotos.EasyPhotos
import com.netease.nim.uikit.support.glide.GlideEngine
import com.sogukj.pe.BuildConfig
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.setOnClickFastListener
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.RxBus
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.utils.XmlDb
import com.sogukj.pe.baselibrary.widgets.FlowLayout
import com.sogukj.pe.bean.*
import com.sogukj.pe.module.fileSelector.FileMainActivity
import com.sogukj.pe.module.main.ContactsActivity
import com.sogukj.pe.peUtils.MyGlideUrl
import com.sogukj.pe.service.ApproveService
import com.sogukj.pe.widgets.CalendarDingDing
import com.sogukj.pe.widgets.CircleImageView
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_approve_fill.*
import kotlinx.android.synthetic.main.toolbar.*
import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.find
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ApproveFillActivity : ToolbarActivity() {

    lateinit var inflater: LayoutInflater
    var paramId: Int? = null
    var paramTitle: String? = null
    var paramType: Int? = null
    var flagEdit = false
    var isOneKey = false
    var gson = Gson()
    var name = ""
    companion object {
        fun start(ctx: Activity?, itemBean: SpGroupItemBean) {
            val intent = Intent(ctx, ApproveFillActivity::class.java)
            intent.putExtra(Extras.DATA, itemBean)
            ctx?.startActivity(intent)
        }

        fun start(ctx: Activity?, bean: ApprovalBean) {
            val intent = Intent(ctx, ApproveFillActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            ctx?.startActivity(intent)
        }

        fun start(ctx: Activity?, bean: MessageBean) {
            val intent = Intent(ctx, ApproveFillActivity::class.java)
            intent.putExtra(Extras.DATA, bean)
            ctx?.startActivity(intent)
        }

        fun start(ctx: Activity, edit: Boolean, paramType: Int, id: Int, title: String, restart: Int? = null) {
            var intent = Intent(ctx, ApproveFillActivity::class.java)
            intent.putExtra(Extras.FLAG, edit)
            intent.putExtra(Extras.ID, id)
            intent.putExtra(Extras.TYPE, paramType)
            intent.putExtra(Extras.TITLE, title)
            if (restart == null) {
                intent.putExtra(Extras.RESTART, 0)
            } else {
                intent.putExtra(Extras.RESTART, 1)
            }
            ctx.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_approve_fill)
        inflater = LayoutInflater.from(context)
        val paramObj = intent.getSerializableExtra(Extras.DATA)
        flagEdit = intent.getBooleanExtra(Extras.FLAG, false)
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
            paramId = intent.getIntExtra(Extras.ID, -1)
            paramType = intent.getIntExtra(Extras.TYPE, -1)
            paramTitle = intent.getStringExtra(Extras.TITLE)
        }
        if (paramId == -1 || paramType == -1) {
            showCustomToast(R.drawable.icon_toast_fail, "参数错误")
            finish()
            return
        }
        setBack(true)

        title = paramTitle
        //title下面是出差请假，上面是其他两种
        if (paramId == 10) {
            title = "出差"
        } else if (paramId == 11) {
            title = "请假"
        }
        if (flagEdit == true) {
            var paramsTitle = intent.getStringExtra(Extras.TITLE)
            if (paramsTitle.equals("出差")) {
                title = "出差"
            } else if (paramsTitle.equals("请假")) {
                title = "请假"
            }
        }

        load()
        btn_commit.setOnClickListener {
            var flag = true
            for (chk in checkList) {
                flag = flag.and(chk())
                if (!flag) break
            }
            if (flag) {
                doConfirm()
            } else {
                showCustomToast(R.drawable.icon_toast_common, "请填写完整后再提交")
            }
        }
        toolbar_menu.setImageResource(R.drawable.copy)
        toolbar_menu.visibility = View.VISIBLE
        XmlDb.open(context).set(Extras.ID, "${paramId}")
        var tmpId = XmlDb.open(context).get(Extras.ID, "")
        SoguApi.getService(application, ApproveService::class.java)
                .getLastApprove(tmpId.toInt())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (payload.payload?.sid == null) {
                            toolbar_menu.visibility = View.INVISIBLE
                        }
                        //toolbar_menu 可能要隐藏，比如重新发起审批就不需要这个
                        toolbar_menu.setOnClickListener {

                            paramId = payload.payload?.sid
                            flagEdit = true
                            isOneKey = true
                            var name = payload.payload?.name

                            var mDialog = MaterialDialog.Builder(this@ApproveFillActivity)
                                    .theme(Theme.LIGHT)
                                    .canceledOnTouchOutside(true)
                                    .customView(R.layout.dialog_yongyin, false).build()
                            mDialog.show()
                            val content = mDialog.find<TextView>(R.id.content)
                            val cancel = mDialog.find<Button>(R.id.cancel)
                            val yes = mDialog.find<Button>(R.id.yes)

                            name = "“${name}”"
                            val spannable1 = SpannableString("是否将上次${name}填写内容一键复制填写")
                            spannable1.setSpan(ForegroundColorSpan(Color.parseColor("#808080")), 5, 5 + name.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                            content.text = spannable1

                            cancel.setOnClickListener {
                                mDialog.dismiss()
                            }
                            yes.setOnClickListener {
                                load()
                                mDialog.dismiss()
                            }
                        }
                    }
                }, { e ->
                    Trace.e(e)
                })
    }

    override fun onPause() {
        super.onPause()
        hideProgress()
    }

    override fun onBackPressed() {
        if (flagEdit && !isOneKey) {
            super.onBackPressed()
            return
        }
        for (list in checkList) {
            list()
        }

        var tmpMap = HashMap<String, Any?>()
        for ((k, v) in paramMap) {
            if (v == null) {

            } else {//先把value为null的去掉，然后去掉默认的value（有的为空，有的为0，有的为1）
                if (k == "reasons" ||//签字事由  或者是出差请假的理由，默认为""
                        k == "info" || //备注说明  默认为""
                        k == "end_city" ||
                        k == "time_range" ||
                        k == "total_hours" ||
                        k == "copier" ||
                        k == "lawyer_opinion" ||
                        k == "manager_opinion") {//投资经理意见  默认为""
                    if ((v as String) != "") {
                        tmpMap.put(k, v)
                    }
                } else if (k == "sealFile" || //签字文件  默认size=0
                        k == "lawyerFile") {//律师意见文件 默认size=0
                    if ((v as ArrayList<CustomSealBean.ValueBean>).size != 0) {
                        tmpMap.put(k, v)
                    }
                } else if (k == "sms" || //是否发短信提醒审批人  默认为0
                        k == "is_lawyer") {//是否需要律师意见  默认为0
                    if ((v as Int) != 0) {
                        tmpMap.put(k, v)
                    }
                } else if (k == "seal") {
                    if (!judgeSealEmpty(v as ArrayList<CustomSealBean.ValueBean>)) {
                        tmpMap.put(k, v)
                    }
                } else {
                    tmpMap.put(k, v)
                }
            }
        }
        paramMap.clear()
        paramMap.putAll(tmpMap)

        if (paramMap.size == 0 || (paramMap.size == 1 && paramMap.containsKey("copier"))) {
            super.onBackPressed()
            return
        }
        //签字申请
        //project_id          项目名称                2
        //reasons               签字事由                4
        //sealFile              签字文件                9
        //info                  备注说明                4
        //sms              是否发短信提醒审批人      5
//        if ((paramMap.get("reasons") == null || (paramMap.get("reasons") as String?).isNullOrEmpty()) &&
//                (paramMap.get("sealFile") == null || (paramMap.get("sealFile") as ArrayList<CustomSealBean.ValueBean>).size == 0) &&
//                (paramMap.get("info") == null || (paramMap.get("info") as String?).isNullOrEmpty()) &&
//                (paramMap.get("sms") == null || (paramMap.get("sms") as Int) == 0) &&
//                (paramMap.get("project_id") == null)) {//project_id 选填
//            super.onBackPressed()
//            return
//        }
        //用印申请
//        if ((paramMap.get("seal") == null || judgeSealEmpty(paramMap.get("seal") as ArrayList<CustomSealBean.ValueBean>)) &&
//                (paramMap.get("fund_id") == null) &&
//                (paramMap.get("reasons") == null || (paramMap.get("reasons") as String?).isNullOrEmpty()) &&
//                (paramMap.get("info") == null || (paramMap.get("info") as String?).isNullOrEmpty()) &&
//                (paramMap.get("manager_opinion") == null || (paramMap.get("manager_opinion") as String?).isNullOrEmpty()) &&
//                (paramMap.get("lawyerFile") == null || (paramMap.get("lawyerFile") as ArrayList<CustomSealBean.ValueBean>).size == 0) &&
//                (paramMap.get("is_lawyer") == null || (paramMap.get("is_lawyer") as Int) == 0) &&
//                (paramMap.get("sms") == null || (paramMap.get("sms") as Int) == 0) &&
//                (paramMap.get("sealFile") == null || (paramMap.get("sealFile") as ArrayList<CustomSealBean.ValueBean>).size == 0) &&
//                (paramMap.get("project_name") == null) &&
//                (paramMap.get("foreign_id") == null)) {
//            super.onBackPressed()
//            return
//        }
        //出差请假
//        if ((paramMap.get("end_city") == null || (paramMap.get("end_city") as String?).isNullOrEmpty()) &&
//                (paramMap.get("reasons") == null || (paramMap.get("reasons") as String?).isNullOrEmpty()) &&
//                (paramMap.get("time_range") == null || (paramMap.get("time_range") as String?).isNullOrEmpty()) &&
//                (paramMap.get("total_hours") == null || (paramMap.get("total_hours") as String?).isNullOrEmpty()) &&
        //(paramMap.get("copier") == null || (paramMap.get("copier") as String?).isNullOrEmpty()) &&//抄送人不处理
//                (paramMap.get("leave_type") == null)) {
//            super.onBackPressed()
//            return
//        }
        var mDialog = MaterialDialog.Builder(this@ApproveFillActivity)
                .theme(Theme.LIGHT)
                .canceledOnTouchOutside(true)
                .customView(R.layout.dialog_yongyin, false).build()
        mDialog.show()
        val content = mDialog.find<TextView>(R.id.content)
        val cancel = mDialog.find<Button>(R.id.cancel)
        val yes = mDialog.find<Button>(R.id.yes)
        content.text = "是否需要保存草稿"
        cancel.text = "否"
        yes.text = "是"
        cancel.setOnClickListener {
            val builder = HashMap<String, Any>()
            paramId = XmlDb.open(context).get(Extras.ID, "").toInt()
            builder.put("template_id", paramId!!)
            builder.put("data", HashMap<String, Any?>())
            SoguApi.getService(application, ApproveService::class.java)
                    .saveDraft(builder)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            super.onBackPressed()
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
                    })
        }
        yes.setOnClickListener {
            val builder = HashMap<String, Any>()
            paramId = XmlDb.open(context).get(Extras.ID, "").toInt()
            builder.put("template_id", paramId!!)
            builder.put("data", paramMap)
            SoguApi.getService(application, ApproveService::class.java)
                    .saveDraft(builder)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            showCustomToast(R.drawable.icon_toast_success, "草稿保存成功")
                            super.onBackPressed()
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
                        showCustomToast(R.drawable.icon_toast_fail, "草稿保存失败")
                    })
        }
    }

    private fun judgeIsLeaveBusiness(): Boolean {
        var isLeaveBusiness = false
        if (flagEdit) {
            if (isOneKey) {
                var tmpId = XmlDb.open(context).get(Extras.ID, "").toInt()
                if (tmpId == 10 || tmpId == 11 || tmpId == 14) {
                    isLeaveBusiness = true
                }
            } else {
                var paramsTitle = intent.getStringExtra(Extras.TITLE)
                if (paramsTitle.equals("出差") || paramsTitle.equals("请假") || paramsTitle.equals("外出")) {
                    isLeaveBusiness = true
                }
            }
        } else {
            if (paramId == 10 || paramId == 11 || paramId == 14) {
                isLeaveBusiness = true
            }
        }
        if("用印事由".equals(name)){
            isLeaveBusiness = true
        }
        return isLeaveBusiness
    }

    private fun load() {
        checkList.clear()
        ll_up.removeAllViews()
        SoguApi.getService(application, ApproveService::class.java)
                .approveInfo(template_id = if (flagEdit) null else paramId!!,
                        sid = if (!flagEdit) null else paramId!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (!payload.isOk) {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        return@subscribe
                    }
                    var isNeedRequest = true
                    payload.payload?.forEach { bean ->
                        addRow(bean)
                        if (bean.control == 2 && bean.is_fresh == 1) {
                            isNeedRequest = false
                        }
                    }
                    //用印申请
                    hideFields.forEach { field ->
                        val view = fieldMap.get(field)
                        view?.visibility = View.GONE
                    }
                    //律师意见默认为否，渲染的时候view未生成，所以生成不了。等生成的时候并没有隐藏
                    payload.payload?.forEach { bean ->
                        if (bean.control == 5) {
                            if (bean.value_map?.is_select == 1) {
                                bean.value_map?.hide?.split(",")?.forEach { field ->
                                    if (!TextUtils.isEmpty(field)) {
                                        val view = fieldMap[field]
                                        view?.visibility = View.VISIBLE
                                    }
                                }
                            } else {
                                bean.value_map?.hide?.split(",")?.forEach { field ->
                                    if (!TextUtils.isEmpty(field)) {
                                        val view = fieldMap[field]
                                        view?.visibility = View.GONE
                                    }
                                }
                            }
                        }
                    }

                    if (isNeedRequest) {
                        requestLeaveInfo()
                    }
                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_common, "暂无可用数据")
                })
    }

    private fun requestLeaveInfo() {
        ll_approver.removeAllViews()
        SoguApi.getService(application, ApproveService::class.java)
                .leaveInfo(template_id = if (flagEdit) null else paramId!!,
                        sid = if (!flagEdit) null else paramId!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (!payload.isOk) {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        return@subscribe
                    }
                    payload.payload?.apply {
                        addSP(sp!!)

                        mDefaultID.clear()
                        if (!default.isNullOrEmpty()) {
                            default!!.split(",").forEach {
                                mDefaultID.add(it.toInt())
                            }
                        }

                        if (cs == null) {
                            addCS(arrayListOf(UserBean()))
                        } else {
                            cs!!.add(UserBean())
                            addCS(cs!!)
                        }
                    }
                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_common, "暂无可用数据")
                })
    }

//    private fun requestApprove(fund_id: Int? = null) {
//        SoguApi.getService(application,ApproveService::class.java)
//                .approver(template_id = paramId!!
//                        , type = paramType, fund_id = fund_id)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe({ payload ->
//                    if (!payload.isOk) {
//                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
//                        return@subscribe
//                    }
//                    payload.payload?.forEach { bean ->
//                        addApprover(bean)
//                    }
//                }, { e ->
//                    Trace.e(e)
//                    //showToast("暂无可用数据")
//                    showCustomToast(R.drawable.icon_toast_common, "暂无可用数据")
//                })
//    }

//    private fun addApprover(bean: ApproverBean) {
//        val convertView = inflater.inflate(R.layout.cs_row_approver, null) as LinearLayout
//        ll_approver.addView(convertView)
//        val tvLabel = convertView.findViewById(R.id.tv_label) as TextView
//        val etValue = convertView.findViewById(R.id.et_value) as TextView
//        tvLabel.text = bean.position
//        etValue.text = bean.approver
//    }

    private fun doConfirm() {
        if (isOneKey) {
            flagEdit = false
            paramId = XmlDb.open(context).get(Extras.ID, "").toInt()
        }
        val builder = FormBody.Builder()
        if (flagEdit) {
            builder.add("approval_id", "${paramId}")
            for ((k, v) in paramMap) {
                if (v == null) {

                } else if (v is String) {
                    builder.add(k, v)
                } else
                    builder.add(k, gson.toJson(v))
            }
            var restart = intent.getIntExtra(Extras.RESTART, 0)
            if (restart == 1) {
                SoguApi.getService(application, ApproveService::class.java)
                        .updateApprove(builder.build())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ payload ->
                            if (payload.isOk) {
                                showCustomToast(R.drawable.icon_toast_success, "提交成功")
                                finish()
                            } else {
                                showCustomToast(R.drawable.icon_toast_fail, payload.message)
                            }
                        }, { e ->
                            Trace.e(e)
                            showCustomToast(R.drawable.icon_toast_fail, "提交失败")
                        })
            } else {
                if (judgeIsLeaveBusiness()) {
                    SoguApi.getService(application, ApproveService::class.java)
                            .editLeave(builder.build())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ payload ->
                                if (payload.isOk) {
                                    showCustomToast(R.drawable.icon_toast_success, "提交成功")
                                    finish()
                                } else {
                                    showCustomToast(R.drawable.icon_toast_fail, payload.message)
                                }
                            }, { e ->
                                Trace.e(e)
                                showCustomToast(R.drawable.icon_toast_fail, "提交失败")
                            })
                } else {
                    SoguApi.getService(application, ApproveService::class.java)
                            .editApprove(builder.build())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ payload ->
                                if (payload.isOk) {
                                    showCustomToast(R.drawable.icon_toast_success, "提交成功")
                                    finish()
                                } else {
                                    showCustomToast(R.drawable.icon_toast_fail, payload.message)
                                }
                            }, { e ->
                                Trace.e(e)
                                showCustomToast(R.drawable.icon_toast_fail, "提交失败")
                            })
                }
            }
        } else {
            builder.add("template_id", "${paramId}")
            for ((k, v) in paramMap) {
                if (v == null) {

                } else if (v is String) {
                    builder.add(k, v)
                } else
                    builder.add(k, gson.toJson(v))
            }
            SoguApi.getService(application, ApproveService::class.java)
                    .submitApprove(builder.build())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk) {
                            showCustomToast(R.drawable.icon_toast_success, "提交成功")
                            handler.postDelayed({
                                finish()
                            }, 2000)
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
                        showCustomToast(R.drawable.icon_toast_fail, "提交失败")
                    })
        }
    }

    private fun judgeSealEmpty(list: ArrayList<CustomSealBean.ValueBean>): Boolean {
        if (list.size == 0) {
            return true
        } else {
            var flag = true
            for (item in list) {
                if (item.count > 0) {
                    flag = false
                    break
                }
            }
            return flag
        }
    }

    val checkList = ArrayList<() -> Boolean>()
    val paramMap = HashMap<String, Any?>()
    val fieldMap = HashMap<String, View?>()
    val viewMap = HashMap<String, TextView>()
    val hideFields = ArrayList<String>()
    val REQ_SELECT_FILE = 0xf0
    var SEND = 0x007
    var filesBean: CustomSealBean? = null
    var filesView: LinearLayout? = null
    var dstCity = ArrayList<CityArea.City>()
    var startDate: Date? = null
    var endDate: Date? = null
    var date_type: Int? = null
    lateinit var ding_start: CalendarDingDing
    lateinit var ding_end: CalendarDingDing

    private fun addRow(bean: CustomSealBean) {
        when (bean.control) {
            1 -> add1(bean)
            2 -> add2(bean)
            3 -> add3(bean)
            4 -> add4(bean)
            5 -> add5(bean)
            6 -> add6(bean)
            8 -> add8(bean)
            9 -> add9(bean)
            10 -> add10(bean)
            11 -> add11(bean)
            12 -> add12(bean)
            13 -> add13(bean)
            14 -> add14(bean)
        }
    }

    private fun add1(bean: CustomSealBean) {
        val convertView = inflater.inflate(R.layout.cs_row_10, null)
        ll_up.addView(convertView)
        fieldMap.put(bean.fields, convertView)

        val tvLabel = convertView.findViewById<TextView>(R.id.tv_label) as TextView
        val etValue = convertView.findViewById<TextView>(R.id.et_value) as TextView
        val iv_star = convertView.findViewById<ImageView>(R.id.star)
        if (bean.is_must == 1) {
            iv_star.visibility = View.VISIBLE
        } else {
            iv_star.visibility = View.INVISIBLE
        }

        tvLabel.text = bean.name
        if (!bean.value_map?.pla.isNullOrEmpty()) {
            etValue.hint = bean.value_map?.pla
        }
        if (!bean.value_map?.name.isNullOrEmpty()) {
            etValue.text = bean.value_map?.name
        }

        val items = ArrayList<String?>()
        val map = HashMap<String, CustomSealBean.ValueBean>()
        bean.value_list?.forEach { v ->
            if (v.name != null && v.name!!.isNotEmpty()) {
                items.add(v.name)
                map.put(v.name!!, v)
            }
            if (v.is_select == 1) {
                etValue.text = v.name
                paramMap.put(bean.fields, v.id)
            }
        }
        if (map.isNotEmpty()) {
            convertView.setOnClickListener {
                var pvOptions = OptionsPickerView.Builder(this, OptionsPickerView.OnOptionsSelectListener { options1, option2, options3, v ->
                    val tx = items.get(options1)
                    etValue.text = tx
                    val valBean = map[tx]
                    paramMap.put(bean.fields, valBean?.id)
                }).build()
                pvOptions.setPicker(items, null, null)
                pvOptions.show()
            }
        }
        checkList.add {
            val str = etValue.text?.toString()
            !(bean.is_must == 1 && str.isNullOrEmpty())
        }
    }

    private fun add2(bean: CustomSealBean) {
        val convertView = inflater.inflate(R.layout.cs_row_pop_list, null)
        ll_up.addView(convertView)
        fieldMap.put(bean.fields, convertView)

        val tvLabel = convertView.findViewById<TextView>(R.id.tv_label) as TextView
        val etValue = convertView.findViewById<TextView>(R.id.et_value) as TextView
        tvLabel.text = bean.name
        val iv_star = convertView.findViewById<ImageView>(R.id.star)
        if (bean.is_must == 1) {
            iv_star.visibility = View.VISIBLE
        } else {
            iv_star.visibility = View.INVISIBLE
        }
        etValue.text = bean.value_map?.name
        viewMap.put(bean.fields, etValue)
        paramMap.put(bean.fields, bean.value_map?.id)
        checkList.add {
            val str = etValue.text?.toString()
            !(bean.is_must == 1 && str.isNullOrEmpty())
        }
        etValue.setOnClickListener {
            ListSelectorActivity.start(this, bean)
        }
        //----------------------------------------------------------------------------------------------------------------------------------------------------
        if (bean.is_fresh == 1) {
            if (!isInit) {
                isInit = true
                reloadSPCS(bean, bean.value_map?.id)
            }
        }
    }

    var pro_id: Int? = null
    var fund_id: Int? = null
    var isInit = false

    fun reloadSPCS(bean: CustomSealBean, id: Int? = null) {
        ll_approver.removeAllViews()

        pro_id = if (paramMap.get("project_id") == null) null else paramMap.get("project_id") as Int
        fund_id = if (paramMap.get("fund_id") == null) null else paramMap.get("fund_id") as Int

        SoguApi.getService(application, ApproveService::class.java)
                .leaveInfo(template_id = if (flagEdit) null else paramId!!,
                        project_id = pro_id, fund_id = fund_id,
                        sid = if (!flagEdit) null else paramId!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (!payload.isOk) {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        return@subscribe
                    }
                    payload.payload?.apply {
                        addSP(sp!!)

                        mDefaultID.clear()
                        if (!default.isNullOrEmpty()) {
                            default!!.split(",").forEach {
                                mDefaultID.add(it.toInt())
                            }
                        }

                        if (cs == null) {
                            addCS(arrayListOf(UserBean()))
                        } else {
                            cs!!.add(UserBean())
                            addCS(cs!!)
                        }
                    }
                }, { e ->
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_common, "暂无可用数据")
                })
    }

    private fun add3(bean: CustomSealBean) {
        val convertView = inflater.inflate(R.layout.cs_row_edit_text, null)
        ll_up.addView(convertView)
        fieldMap.put(bean.fields, convertView)

        val tvLabel = convertView.findViewById<TextView>(R.id.tv_label) as TextView
        val etValue = convertView.findViewById<EditText>(R.id.et_value) as EditText
        tvLabel.text = bean.name
        val iv_star = convertView.findViewById<ImageView>(R.id.star)
        if (bean.is_must == 1) {
            iv_star.visibility = View.VISIBLE
        } else {
            iv_star.visibility = View.INVISIBLE
        }
        etValue.filters = Utils.getFilter(this)

        checkList.add {
            val str = etValue.text?.toString()
            paramMap.put(bean.fields, str)
            !(bean.is_must == 1 && str.isNullOrEmpty())
        }

    }

    private fun add4(bean: CustomSealBean) {
        name = bean.name!!
        var convertView: View
        if (judgeIsLeaveBusiness()) {
            convertView = inflater.inflate(R.layout.cs_row_reason, null)
        } else {
            convertView = inflater.inflate(R.layout.cs_row_edit_box, null)
        }
        ll_up.addView(convertView)
        fieldMap.put(bean.fields, convertView)

        val tvLabel = convertView.findViewById<TextView>(R.id.tv_label) as TextView
        val etValue = convertView.findViewById<EditText>(R.id.et_value) as EditText
        tvLabel.text = bean.name
        val iv_star = convertView.findViewById<ImageView>(R.id.star)
        if (bean.is_must == 1) {
            iv_star.visibility = View.VISIBLE
        } else {
            iv_star.visibility = View.INVISIBLE
        }
        etValue.setText(bean.value)
        if (bean.value.isNullOrEmpty()) {
            etValue.setSelection(0)
        } else {
            etValue.setSelection(bean.value!!.length)
        }

        paramMap.put(bean.fields, bean.value)// TODO
        checkList.add {
            val str = etValue.text?.toString()
            paramMap.put(bean.fields, str)
            !(bean.is_must == 1 && str.isNullOrEmpty())
        }
    }

    private fun add5(bean: CustomSealBean) {
        val convertView = inflater.inflate(R.layout.cs_row_radio, null)
        ll_up.addView(convertView)
        fieldMap.put(bean.fields, convertView)
        val tvLabel = convertView.findViewById<TextView>(R.id.tv_label) as TextView
        val rgCheck = convertView.findViewById<RadioGroup>(R.id.rg_check) as RadioGroup
        val rbYes = convertView.findViewById<RadioButton>(R.id.rb_yes) as RadioButton
        val rbNo = convertView.findViewById<RadioButton>(R.id.rb_no) as RadioButton
        tvLabel.text = bean.name
        val iv_star = convertView.findViewById<ImageView>(R.id.star)
        if (bean.is_must == 1) {
            iv_star.visibility = View.VISIBLE
        } else {
            iv_star.visibility = View.INVISIBLE
        }
        paramMap.put(bean.fields, bean.value_map?.is_select)
        if (bean.value_map?.is_select == 1) {
            rbNo.isChecked = false
            rbYes.isChecked = true
        } else {
            rbNo.isChecked = true
            rbYes.isChecked = false
        }
        bean.value_map?.hide?.split(",")?.forEach { field ->
            if (bean.value_map?.is_select == 1)
                hideFields.add(field)
        }
        rbNo.setOnClickListener {
            paramMap.put(bean.fields, 0)
            if (bean.fields.equals("sms")) {
                return@setOnClickListener
            }
            bean.value_map?.hide?.split(",")?.forEach { field ->
                if (!TextUtils.isEmpty(field)) {
                    val view = fieldMap[field]
                    view?.visibility = View.GONE
                }
            }
        }
        rbYes.setOnClickListener {
            paramMap.put(bean.fields, 1)
            if (bean.fields.equals("sms")) {
                return@setOnClickListener
            }
            bean.value_map?.hide?.split(",")?.forEach { field ->
                if (!TextUtils.isEmpty(field)) {
                    val view = fieldMap[field]
                    view?.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun add6(bean: CustomSealBean) {
        val convertView = inflater.inflate(R.layout.cs_row_check_box, null)
        ll_up.addView(convertView)
        fieldMap.put(bean.fields, convertView)

        val tvLabel = convertView.findViewById<TextView>(R.id.tv_label) as TextView
        tvLabel.text = bean.name
        val iv_star = convertView.findViewById<ImageView>(R.id.star)
        if (bean.is_must == 1) {
            iv_star.visibility = View.VISIBLE
        } else {
            iv_star.visibility = View.INVISIBLE
        }

        val ll_check = convertView.findViewById<LinearLayout>(R.id.ll_check) as LinearLayout
        ll_check.removeAllViews()
        bean.value_list?.forEach { v ->
            if (v.name != null && v.name!!.isNotEmpty()) {
                val convertView = inflater.inflate(R.layout.cs_item_check, null)
                ll_check.addView(convertView)
                val cbCheck = convertView.findViewById<CheckBox>(R.id.cb_check) as CheckBox
                val tvMinus = convertView.findViewById<TextView>(R.id.tv_minus) as TextView
                val etNum = convertView.findViewById<TextView>(R.id.et_num) as TextView
                val tvPlus = convertView.findViewById<TextView>(R.id.tv_plus) as TextView
                cbCheck.text = v.name
                cbCheck.isChecked = v.is_select == 1 || v.count > 0
                etNum.text = "${v.count}"
                if (cbCheck.isChecked) {
                    v.is_select = 1
                    paramMap.put(bean.fields, bean.value_list)
                }
                cbCheck.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        v.is_select = 1
                        v.count = 1
                    } else {
                        v.is_select = 0
                        v.count = 0
                    }
                    etNum.text = v.count.toString()
                    paramMap.put(bean.fields, bean.value_list)
                }
                tvMinus.setOnClickListener {
                    var num = 0
                    etNum.text.toString().toIntOrNull()?.apply {
                        num = this
                    }
                    --num
                    if (num <= 0) {
                        num = 0
                        cbCheck.isChecked = false
                    }
                    etNum.text = "${num}"
                    v.count = num
                    paramMap.put(bean.fields, bean.value_list)
                }
                tvPlus.setOnClickListener {
                    var num = 0
                    etNum.text.toString().toIntOrNull()?.apply {
                        num = this
                    }
                    ++num
                    cbCheck.isChecked = true
                    etNum.text = "${num}"
                    v.count = num
                    paramMap.put(bean.fields, bean.value_list)
                }

            }
        }

        checkList.add {
            paramMap.put(bean.fields, bean.value_list)
            var flag = false
            if (bean.is_must == 1) {
                bean.value_list?.forEach { v ->
                    flag = flag.or(v.is_select == 1 && v.count > 0)
                }
            } else {
                flag = true
            }
            flag
        }
    }

    private fun add8(bean: CustomSealBean) {
        val convertView = inflater.inflate(R.layout.cs_row_images, null)
        ll_up.addView(convertView)
        fieldMap.put(bean.fields, convertView)
        val tvLabel = convertView.findViewById<TextView>(R.id.tv_label) as TextView
        tvLabel.text = bean.name
        val iv_star = convertView.findViewById<ImageView>(R.id.star)
        if (bean.is_must == 1) {
            iv_star.visibility = View.VISIBLE
        } else {
            iv_star.visibility = View.INVISIBLE
        }

        val ll_images = convertView.findViewById<FlowLayout>(R.id.ll_images) as FlowLayout
        ll_images.removeAllViews()
        refreshImages(bean, ll_images)
    }

    private fun add9(bean: CustomSealBean) {
        val convertView = inflater.inflate(R.layout.cs_row_files, null)
        ll_up.addView(convertView)
        fieldMap.put(bean.fields, convertView)
        val tvLabel = convertView.findViewById<TextView>(R.id.tv_label) as TextView
        val tvFile = convertView.findViewById<TextView>(R.id.tv_file) as TextView
        tvLabel.text = bean.name
        val iv_star = convertView.findViewById<ImageView>(R.id.star)
        if (bean.is_must == 1) {
            iv_star.visibility = View.VISIBLE
        } else {
            iv_star.visibility = View.INVISIBLE
        }

        val ll_files = convertView.findViewById<LinearLayout>(R.id.ll_files) as LinearLayout
        ll_files.removeAllViews()
        filesBean = bean
        filesView = ll_files
        refreshFiles(filesBean!!, filesView!!)

        tvFile.setOnClickFastListener {
            showProgress("正在读取内存文件")
            FileMainActivity.start(context, requestCode = REQ_SELECT_FILE)
        }
    }

    private fun add10(bean: CustomSealBean) {
        val convertView = inflater.inflate(R.layout.cs_row_10, null) as LinearLayout
        ll_up.addView(convertView)
        convertView.tag = "time"

        val tvLabel = convertView.findViewById<TextView>(R.id.tv_label) as TextView
        val etValue = convertView.findViewById<TextView>(R.id.et_value) as TextView

        val iv_star = convertView.findViewById<ImageView>(R.id.star)
        if (bean.is_must == 1) {
            iv_star.visibility = View.VISIBLE
        } else {
            iv_star.visibility = View.INVISIBLE
        }

        tvLabel.text = bean.name
        //etValue.hint = bean.value_map?.pla
        if (!bean.value_map?.name.isNullOrEmpty()) {
            etValue.text = bean.value_map?.name
        }
        var list = bean.value_map?.value as ArrayList<LinkedTreeMap<String, Any>>
        if (list == null || list.size == 0) {

        } else {
            var changeList = ArrayList<CityArea.City>()
            for (city in list) {
                var item = CityArea.City()

                item.id = city.get("id").toString().toDouble().toInt()
                item.pid = city.get("pid").toString().toDouble().toInt()
                item.name = city.get("name").toString()

                changeList.add(item)
            }
            initDstCity(changeList)
        }

        //end_city
        convertView.setOnClickListener {
            var input_id = XmlDb.open(context).get(Extras.ID, "").toInt()
            if (flagEdit) {
                if (isOneKey) {
                    input_id = XmlDb.open(context).get(Extras.ID, "").toInt()
                } else {
                    var paramsTitle = intent.getStringExtra(Extras.TITLE)
                    if (paramsTitle.equals("出差")) {
                        input_id = 10
                    } else if (paramsTitle.equals("请假")) {
                        input_id = 11
                    }
                }
            } else {
                input_id = paramId!!
            }
            DstCityActivity.start(context, input_id, dstCity)
            Log.e("TAG","start ---" + System.currentTimeMillis())
        }

        checkList.add {
            //end_city
            var cities = ""
            for (city in dstCity) {
                cities = "${cities}${city.id},"
            }
            cities = cities.removeSuffix(",")
            paramMap.put(bean.fields, cities)
            if (bean.is_must == 1) {
                dstCity.size != 0
            } else {
                true
            }
        }
    }

    private fun add11(bean: CustomSealBean) {
        //time_range
        var nameList = bean.name?.split(",") as ArrayList<String>
        for (index in nameList.indices) {
            var name = nameList[index]
            val convertView = inflater.inflate(R.layout.cs_row_11, null) as LinearLayout
            ll_up.addView(convertView)

            val tvLabel = convertView.findViewById<TextView>(R.id.tv_label) as TextView
            val etValue = convertView.findViewById<TextView>(R.id.et_value) as TextView
            if (index == 0) {
                etValue.tag = "index0"
            } else {
                etValue.tag = "index1"
            }

            val iv_star = convertView.findViewById<ImageView>(R.id.star)
            if (bean.is_must == 1) {
                iv_star.visibility = View.VISIBLE
            } else {
                iv_star.visibility = View.GONE
            }

            tvLabel.text = name
            etValue.hint = bean.value_map?.pla
            var str = bean.value_map?.value as String?
            paramMap.put(bean.fields, str)

            date_type = bean.value_map?.type// 1天数   2时分

            if (!str.isNullOrEmpty()) {
                var dates = str!!.split(",")
                if (!dates[0].isNullOrEmpty()) {
                    if (date_type == 1) {
                        val format = SimpleDateFormat("yyyy-MM-dd")
                        etValue.text = format.format(dates[index].toLong() * 1000)
                        startDate = format.parse(format.format(dates[0].toLong() * 1000))
                        endDate = format.parse(format.format(dates[1].toLong() * 1000))
                    } else if (date_type == 2) {
                        val format = SimpleDateFormat("yyyy-MM-dd HH:mm")
                        etValue.text = format.format(dates[index].toLong() * 1000)
                        startDate = format.parse(format.format(dates[0].toLong() * 1000))
                        endDate = format.parse(format.format(dates[1].toLong() * 1000))
                    }
                }
            }

            if (index == 0) {
                ding_start = CalendarDingDing(context)
            } else if (index == 1) {
                ding_end = CalendarDingDing(context)
            }
            convertView.setOnClickListener {
                if (index == 0) {
                    if (startDate == null) {
                        startDate = Date()
                    }
                    ding_start.show(date_type!!, startDate, CalendarDingDing.onTimeClick { date ->
                        if (date == null) {
                            var etValue = ll_up.findViewWithTag<TextView>("index0") as TextView
                            if (etValue.text.trim().equals("")) {
                                startDate = null
                            }
                            return@onTimeClick
                        } else {
                            startDate = date
                            setTime(etValue, date, date_type!!)
                            if (endDate == null) {
                                return@onTimeClick
                            }
                        }

                        if (date_type == 1) {
                            if (startDate!!.time / 1000 > endDate!!.time / 1000) {
                                showCustomToast(R.drawable.icon_toast_common, "开始时间不能大于结束时间")
                                return@onTimeClick
                            }
                        } else if (date_type == 2) {
                            if (startDate!!.time / 1000 >= endDate!!.time / 1000) {
                                showCustomToast(R.drawable.icon_toast_common, "开始时间不能大于等于结束时间")
                                return@onTimeClick
                            }
                        }
                        setTime(etValue, date, date_type!!)
                        calculateTime(date_type!!)
                    })
                } else if (index == 1) {
                    if (startDate == null) {
                        showCustomToast(R.drawable.icon_toast_common, "请先选择开始时间")
                        return@setOnClickListener
                    }
                    if (endDate == null) {
                        endDate = Date()
                    }
                    ding_end.show(date_type!!, endDate, CalendarDingDing.onTimeClick { date ->
                        if (date == null) {
                            var etValue = ll_up.findViewWithTag<TextView>("index1") as TextView
                            if (etValue.text.trim().equals("")) {
                                endDate = null
                            }
                            return@onTimeClick
                        } else {
                            endDate = date
                        }

                        if (date_type == 1) {
                            if (startDate!!.time / 1000 > endDate!!.time / 1000) {
                                showCustomToast(R.drawable.icon_toast_common, "开始时间不能大于结束时间")
                                return@onTimeClick
                            }
                        } else if (date_type == 2) {
                            if (startDate!!.time / 1000 >= endDate!!.time / 1000) {
                                showCustomToast(R.drawable.icon_toast_common, "开始时间不能大于等于结束时间")
                                return@onTimeClick
                            }
                        }
                        setTime(etValue, date, date_type!!)
                        calculateTime(date_type!!)
                    })
                }
            }
        }
        checkList.add {
            if (bean.is_must == 1) {
                if (startDate != null && endDate != null) {
                    paramMap.put(bean.fields, "${(startDate!!.time) / 1000},${(endDate!!.time) / 1000}")
                    true
                } else {
                    false
                }
            } else {
                true
            }
        }
    }

    private fun add12(bean: CustomSealBean) {
        //fields为空
        val convertView = inflater.inflate(R.layout.cs_row_13, null) as LinearLayout
        ll_up.addView(convertView)
        val tvLabel = convertView.findViewById<TextView>(R.id.tv_label) as TextView
        val etValue = convertView.findViewById<TextView>(R.id.et_value) as TextView
        tvLabel.text = bean.name
        etValue.text = bean.value_map?.value as String?
        convertView.setOnClickListener {
            if (paramId == 11 || (flagEdit && paramTitle.equals("请假")) || isOneKey) {
                MyHolidayActivity.start(context)
            }
        }
    }

    private fun add13(bean: CustomSealBean) {
        //total_hours
        val convertView = inflater.inflate(R.layout.cs_row_approver, null) as LinearLayout
        ll_up.addView(convertView)
        val tvLabel = convertView.findViewById<TextView>(R.id.tv_label) as TextView
        val etValue = convertView.findViewById<TextView>(R.id.et_value) as TextView
        val icon = convertView.findViewById<ImageView>(R.id.starIcon) as ImageView
        tvLabel.text = bean.name
        etValue.hint = bean.value_map?.pla
        etValue.tag = bean.fields
        //etValue.text = bean.value_map?.value as String?
        if (startDate != null && endDate != null) {
            calculateTime(date_type!!)
        }
        if (flagEdit) {
            var str = bean.value_map?.value as String?
            if (isOneKey) {
                var tmpId = XmlDb.open(context).get(Extras.ID, "").toInt()
                if (tmpId == 10) {
                    etValue.text = "${str}天"
                } else if (tmpId == 11) {
                    etValue.text = "${str}小时"
                }
            } else {
                var paramsTitle = intent.getStringExtra(Extras.TITLE)
                if (paramsTitle.equals("出差")) {
                    etValue.text = "${str}天"
                } else if (paramsTitle.equals("请假")) {
                    etValue.text = "${str}小时"
                }
            }
        }
        if (bean.is_must == 1) {
            icon.visibility = View.VISIBLE
        } else {
            icon.visibility = View.GONE
        }
        checkList.add {
            var txt = etValue.text.toString()
            if (bean.is_must == 1) {
                !txt.isNullOrEmpty()
            } else {
                true
            }
        }
    }

    private fun add14(bean: CustomSealBean) {
        //fields为空
        add12(bean)
    }

    private fun refreshImages(bean: CustomSealBean, imagesView: FlowLayout) {
        paramMap.put(bean.fields, bean.value_list)
        imagesView.removeAllViews()
        val inflater = LayoutInflater.from(this)

        bean.value_list?.forEach { v ->
            if (v.url != null && v.url!!.isNotEmpty()) {
                val convertView = inflater.inflate(R.layout.cs_item_img, null)
                imagesView.addView(convertView)

                val img = convertView.findViewById<ImageView>(R.id.img) as ImageView
                val ivDel = convertView.findViewById<ImageView>(R.id.iv_del) as ImageView
                val ivAdd = convertView.findViewById<ImageView>(R.id.iv_add) as ImageView
                ivAdd.visibility = View.GONE
                Glide.with(this)
                        .load(MyGlideUrl(v.url))
                        .into(img)
                img.setOnClickListener {
                    bean.value_list?.remove(v)
                    refreshImages(bean, imagesView)
                }
            }
        }

        run {
            val convertView = inflater.inflate(R.layout.cs_item_img, null)
            imagesView.addView(convertView)
            val img = convertView.findViewById<ImageView>(R.id.img) as ImageView
            val ivDel = convertView.findViewById<ImageView>(R.id.iv_del) as ImageView
            val ivAdd = convertView.findViewById<ImageView>(R.id.iv_add) as ImageView
            ivAdd.setOnClickListener {
                EasyPhotos.createAlbum(this, true, GlideEngine.getInstance())
                        .setFileProviderAuthority(BuildConfig.FILEPROVIDER)
                        .setPuzzleMenu(false)
                        .start(Extras.REQUESTCODE)
            }
            val subscribe = RxBus.getIntanceBus().getObservable(String::class.java).subscribe({ path ->
                if (!TextUtils.isEmpty(path))
                    uploadImage(path!!, bean, imagesView)
            })
            RxBus.getIntanceBus().addSubscription(String, subscribe)
        }
    }

    private fun uploadImage(filePath: String?, imagesBean: CustomSealBean, imagesView: FlowLayout) {
        if (null != imagesBean && null != imagesView && null != filePath) {
            val file = File(filePath)
            SoguApi.getService(application, ApproveService::class.java)
                    .uploadApprove(MultipartBody.Builder().setType(MultipartBody.FORM)
                            .addFormDataPart("file", file.name, RequestBody.create(MediaType.parse("*/*"), file))
                            .addFormDataPart("control", 8.toString())
                            .addFormDataPart("template_id", "${paramId}")
                            .build())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk && payload.payload != null) {
                            showCustomToast(R.drawable.icon_toast_success, "上传成功")
                            imagesBean.value_list?.add(payload.payload!!)
                            refreshImages(imagesBean, imagesView)
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
                        showCustomToast(R.drawable.icon_toast_fail, "上传失败")
                    })
        }
    }

    private fun refreshFiles(bean: CustomSealBean, filesView: LinearLayout) {
        paramMap.put(bean.fields, bean.value_list)
        filesView.removeAllViews()
        val inflater = LayoutInflater.from(this)
        bean.value_list?.forEach { v ->
            if (v.url != null && v.url!!.isNotEmpty()) {
                val convertView = inflater.inflate(R.layout.cs_item_file, null)
                filesView.addView(convertView)

                val ivFile = convertView.findViewById<ImageView>(R.id.iv_file) as ImageView
                val tvName = convertView.findViewById<TextView>(R.id.tv_name) as TextView
                val tvSize = convertView.findViewById<TextView>(R.id.tv_size) as TextView
                val ivDel = convertView.findViewById<ImageView>(R.id.iv_del) as ImageView

                tvName.text = v.file_name
                tvSize.text = v.size
                ivDel.setOnClickListener {
                    bean.value_list?.remove(v)
                    refreshFiles(bean, filesView)
                }
            }
        }
    }

    private fun initDstCity(list: ArrayList<CityArea.City>) {
        dstCity.clear()
        dstCity.addAll(list)

        var cities = ""
        for (city in dstCity) {
            cities = "${cities}${city.id},"
        }
        cities = cities.removeSuffix(",")
        paramMap.put("end_city", cities)

        var view = ll_up.findViewWithTag<LinearLayout>("time") as LinearLayout
        var citys = view.findViewById<LinearLayout>(R.id.cities) as LinearLayout
        citys.visibility = View.VISIBLE

        var etValue = view.findViewById<TextView>(R.id.et_value) as TextView
        etValue.text = "${list.size}个"

        var cityValue1 = view.findViewById<TextView>(R.id.city1) as TextView
        var cityValue2 = view.findViewById<TextView>(R.id.city2) as TextView
        var cityValue3 = view.findViewById<TextView>(R.id.city3) as TextView
        cityValue1.visibility = View.VISIBLE
        cityValue2.visibility = View.VISIBLE
        cityValue3.visibility = View.VISIBLE
        if (list.size == 1) {
            cityValue1.visibility = View.GONE
            cityValue2.visibility = View.GONE
            cityValue3.text = list[0].name
        } else if (list.size == 2) {
            cityValue1.visibility = View.GONE
            cityValue2.text = list[0].name
            cityValue3.text = list[1].name
        } else if (list.size >= 3) {
            cityValue1.text = list[0].name
            cityValue2.text = list[1].name
            cityValue3.text = list[2].name
        }
    }

    private fun setTime(etValue: TextView, date: Date, type: Int) {
        if (type == 1) {
            etValue.text = Utils.getTime(date, "yyyy-MM-dd")
        } else if (type == 2) {
            etValue.text = Utils.getTime(date, "yyyy-MM-dd HH:mm")
        }
    }

    private fun calculateTime(type: Int) {
        var total = ll_up.findViewWithTag<ImageView>("total_hours") as TextView
        SoguApi.getService(application, ApproveService::class.java)
                .calcTotalTime(start_time = (startDate!!.time / 1000).toString(), end_time = (endDate!!.time / 1000).toString(), type = type)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    if (payload.isOk) {
                        if (type == 1) {
                            total.text = payload.payload + "天"
                        } else if (type == 2) {
                            total.text = payload.payload + "小时"
                        }
                        paramMap.put("total_hours", payload.payload)//total_hours
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    Trace.e(e)
                })
    }

    private fun uploadFile(filePath: String?) {
        if (null != filesBean && null != filesView && null != filePath) {
            val file = File(filePath)
            SoguApi.getService(application, ApproveService::class.java)
                    .uploadApprove(MultipartBody.Builder().setType(MultipartBody.FORM)
                            .addFormDataPart("file", file.name, RequestBody.create(MediaType.parse("*/*"), file))
                            .addFormDataPart("control", 9.toString())
                            .addFormDataPart("template_id", "${paramId}")
                            .build())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ payload ->
                        if (payload.isOk && payload.payload != null) {
                            showCustomToast(R.drawable.icon_toast_success, "上传成功")
                            filesBean?.value_list?.add(payload.payload!!)
                            refreshFiles(filesBean!!, filesView!!)
                        } else {
                            showCustomToast(R.drawable.icon_toast_fail, payload.message)
                        }
                    }, { e ->
                        Trace.e(e)
                        handler.postDelayed({
                            hideProgress()
                            showCustomToast(R.drawable.icon_toast_fail, "上传失败")
                        }, 500)
                    }, {
                        handler.postDelayed({
                            hideProgress()
                        }, 500)
                    }, {
                        showProgress("正在上传")
                    })
        }
    }

    private fun refreshListSelector(bean: CustomSealBean, data: CustomSealBean.ValueBean) {
        val view = viewMap.get(bean.fields)
        if (null != view) {
            view.text = data.name
            paramMap.put(bean.fields, data.id)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_SELECT_FILE && resultCode === Activity.RESULT_OK) {
            val paths = data?.getStringArrayListExtra(Extras.LIST)
            paths?.forEach {
                uploadFile(it)
            }
        } else if (requestCode == ListSelectorActivity.REQ_LIST_SELECTOR && resultCode === Activity.RESULT_OK) {
            val bean = data?.getSerializableExtra(Extras.DATA) as CustomSealBean
            val data = data.getSerializableExtra(Extras.DATA2) as CustomSealBean.ValueBean
            paramMap.put(bean.fields, data.id)
            refreshListSelector(bean, data)
            if (bean.is_fresh == 1) {
                reloadSPCS(bean, data.id)
            }
            //refreshListSelector(bean, data)
        } else if (requestCode == SEND && resultCode == Extras.RESULTCODE) {
            var grid_to = ll_approver.findViewWithTag<GridView>("CS") as GridView
            if (grid_to != null) {
                var adapter = grid_to.adapter as MyCSAdapter
                adapter.list.clear()
                adapter.list.addAll(data!!.getSerializableExtra(Extras.DATA) as ArrayList<UserBean>)
                adapter.list.add(UserBean())
                adapter.notifyDataSetChanged()
            }
        } else if (requestCode == Extras.REQUESTCODE && resultCode == Extras.RESULTCODE) {
            var list = data!!.getSerializableExtra(Extras.DATA) as ArrayList<CityArea.City>
            initDstCity(list)
        } else if (requestCode == Extras.REQUESTCODE && resultCode == Activity.RESULT_OK && data != null) {
            RxBus.getIntanceBus().post(data.getStringArrayListExtra(EasyPhotos.RESULT_PATHS)[0])
        }
    }

    var mDefaultID = ArrayList<Int>()

    fun addSP(list: ArrayList<ArrayList<UserBean>>) {
        var datalist = ArrayList<UserBean>()
        for (inner in list) {
            datalist.add(UserBean())
            datalist.addAll(inner)
        }
        datalist.removeAt(0)

        val convertView = inflater.inflate(R.layout.cs_row_sendto, null) as LinearLayout
        ll_approver.addView(convertView)
        var grid_to = convertView.findViewById<GridView>(R.id.grid_chaosong_to) as GridView
        var adapter = MySPAdapter(context, datalist)
        //grid_to.adapter = adapter

        val tvLabel = convertView.findViewById<TextView>(R.id.tv_label) as TextView
        val icon = convertView.findViewById<ImageView>(R.id.starIcon) as ImageView

        grid_to.tag = "SP"
        icon.visibility = View.VISIBLE
        tvLabel.text = "审批人"

        grid_to.visibility = View.GONE
        var content = convertView.findViewById<LinearLayout>(R.id.llll) as LinearLayout
        content.visibility = View.VISIBLE
        for (index in 0 until datalist.size) {
            var item = datalist[index]
            if (item.name.equals("")) {
                var view = inflater.inflate(R.layout.send_item11, null) as LinearLayout

                var params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.leftMargin = Utils.dpToPx(context, 7)
                params.rightMargin = Utils.dpToPx(context, 7)
                view.layoutParams = params

                content.addView(view)
            } else {
                var view = inflater.inflate(R.layout.send_item, null) as LinearLayout
                var icon = view.findViewById<CircleImageView>(R.id.icon) as CircleImageView
                var name = view.findViewById<TextView>(R.id.name) as TextView
                if (item.url.isNullOrEmpty()) {
                    val ch = item.name.first()
                    icon.setChar(ch)
                } else {
                    Glide.with(context)
                            .load(MyGlideUrl(item.url))
                            .listener(object : RequestListener<Drawable> {
                                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                    icon.setImageDrawable(resource)
                                    return false
                                }

                                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                    val ch = item.name.first()
                                    icon.setChar(ch)
                                    return false
                                }
                            })
                            .into(icon)
                }
                name.text = item.name

                var params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                if (index == 0) {
                    //params.leftMargin = Utils.dpToPx(context, 15)
                } else {
                    if (datalist[index - 1].name.equals("")) {
                        params.leftMargin = Utils.dpToPx(context, 0)
                    } else {
                        params.leftMargin = Utils.dpToPx(context, 15)
                    }
                }
                view.layoutParams = params

                content.addView(view)
            }
        }
//        checkList.add {
////            var sp = ""
////            for (inner in list) {
////                for(item in inner){
////                    sp = "${sp}${item.uid},"
////                }
////            }
////            sp = sp.removeSuffix(",")
//            paramMap.put("sp", gson.toJson(list))
//            true
//        }
    }

    fun addCS(list: ArrayList<UserBean>) {
        val convertView = inflater.inflate(R.layout.cs_row_sendto, null) as LinearLayout
        ll_approver.addView(convertView)
        var grid_to = convertView.findViewById<GridView>(R.id.grid_chaosong_to) as GridView
        var adapter = MyCSAdapter(context, list, mDefaultID)
        grid_to.adapter = adapter

        val tvLabel = convertView.findViewById<TextView>(R.id.tv_label) as TextView
        val icon = convertView.findViewById<ImageView>(R.id.starIcon) as ImageView

        icon.visibility = View.INVISIBLE
        tvLabel.text = "抄送人"
        grid_to.tag = "CS"
        grid_to.setOnItemClickListener { parent, view, position, id ->
            if (position == adapter.list.size - 1) {
                var list = ArrayList<UserBean>()
                for (index in 0 until adapter.list.size - 1) {//不包含
                    list.add(adapter.list[index])
                }
//                TeamSelectActivity.startForResult(this, true, list, false, false, true, SEND, default)
                ContactsActivity.startWithDefault(this, list, false, false, mDefaultID, SEND)
            } else {
                var item = adapter.list.get(position)
                if (mDefaultID.contains(item.uid)) {

                } else {
                    adapter.list.removeAt(position)
                    adapter.notifyDataSetChanged()
                }
            }
        }
        checkList.add {
            var copier = ""
            for (index in 0 until list.size - 1) {
                copier = "${copier}${list[index].uid},"
            }
            copier = copier.removeSuffix(",")
            paramMap.put("copier", copier)
            true
        }
//        checkList.add {
//            paramMap.put("copier", gson.toJson(list))
//
//            var defaultStr = ""
//            for (index in 0 until mDefaultID.size) {
//                defaultStr = "${defaultStr}${mDefaultID[index]},"
//            }
//            defaultStr = defaultStr.removeSuffix(",")
//            paramMap.put("default", defaultStr)
//            true
//        }
    }

    class MySPAdapter(var context: Context, val list: ArrayList<UserBean>) : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var conView = convertView

            if (getItemViewType(position) == 0) {
                conView = LayoutInflater.from(context).inflate(R.layout.send_item11, null) as LinearLayout
            } else {
                conView = LayoutInflater.from(context).inflate(R.layout.send_item, null) as LinearLayout
                var icon = conView.findViewById<CircleImageView>(R.id.icon) as CircleImageView
                var name = conView.findViewById<TextView>(R.id.name) as TextView
                if (list[position].url.isNullOrEmpty()) {
                    val ch = list[position].name.first()
                    icon.setChar(ch)
                } else {
                    Glide.with(context)
                            .load(MyGlideUrl(list[position].url))
                            .listener(object : RequestListener<Drawable> {
                                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                    icon.setImageDrawable(resource)
                                    return false
                                }

                                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                    val ch = list[position].name.first()
                                    icon.setChar(ch)
                                    return false
                                }
                            })
                            .into(icon)
                }
                name.text = list[position].name
            }
            return conView
        }

        override fun getItem(position: Int): Any {
            return list.get(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItemViewType(position: Int): Int {
            if (list[position].name.equals("")) {
                return 0
            } else {
                return 1
            }
        }

        override fun getViewTypeCount(): Int {
            return 2
        }

        override fun getCount(): Int {
            return list.size
        }
    }

    class MyCSAdapter(var context: Context, val list: ArrayList<UserBean>, val default: ArrayList<Int>) : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var viewHolder: ViewHolder
            var conView = convertView
            if (conView == null) {
                viewHolder = ViewHolder()
                conView = LayoutInflater.from(context).inflate(R.layout.send_item, null) as LinearLayout
                viewHolder.icon = conView.findViewById<CircleImageView>(R.id.icon) as CircleImageView
                viewHolder.name = conView.findViewById<TextView>(R.id.name) as TextView
                viewHolder.cs_add = conView.findViewById<ImageView>(R.id.cs_add) as ImageView
                viewHolder.cs_default = conView.findViewById<ImageView>(R.id.cs_default) as ImageView
                conView.setTag(viewHolder)
            } else {
                viewHolder = conView.tag as ViewHolder
            }
            if (list[position].uid == null) {
                Glide.with(context)
                        .load("android.resource://" + context.packageName + "/drawable/" + R.drawable.send_add)
                        .apply(RequestOptions().error(R.drawable.nim_avatar_default).fallback(R.drawable.nim_avatar_default))
                        .into(viewHolder.icon!!)
                viewHolder.name?.text = "添加"
                viewHolder.cs_add?.visibility = View.GONE
                viewHolder.cs_default?.visibility = View.GONE
            } else {
                if (list[position].url.isNullOrEmpty()) {
                    viewHolder.icon?.setChar(list[position].name.first())
                } else {
                    Glide.with(context)
                            .load(MyGlideUrl(list[position].url))
                            .listener(object : RequestListener<Drawable> {
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
                if (default.contains(list[position].uid)) {
                    viewHolder.cs_default?.visibility = View.VISIBLE
                    viewHolder.cs_add?.visibility = View.GONE
                } else {
                    viewHolder.cs_default?.visibility = View.GONE
                    viewHolder.cs_add?.visibility = View.VISIBLE
                }
            }
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
            var cs_add: ImageView? = null
            var cs_default: ImageView? = null
        }
    }
}
