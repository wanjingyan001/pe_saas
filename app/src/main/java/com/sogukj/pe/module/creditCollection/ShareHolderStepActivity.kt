package com.sogukj.pe.module.creditCollection

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.Gravity
import android.view.View
import android.widget.EditText
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.base.ActivityHelper
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.RxBus
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.CustomSealBean
import com.sogukj.pe.bean.MessageEvent
import com.sogukj.pe.bean.ProjectBean
import com.sogukj.pe.module.approve.ListSelectorActivity
import com.sogukj.pe.peExtended.hasCreditListActivity
import com.sogukj.pe.peExtended.removeStep1
import com.sogukj.pe.service.CreditService
import com.sogukj.pe.widgets.IOSPopwindow
import com.sogukj.pe.widgets.PayView
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_share_holder_step.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.find

class ShareHolderStepActivity : ToolbarActivity(), View.OnClickListener {

    companion object {

        //step=1，后面两个参数不需要
        fun start(ctx: Context?, step: Int, company_id: Int, company_name: String) {
            val intent = Intent(ctx, ShareHolderStepActivity::class.java)
            intent.putExtra(Extras.DATA, step)
            intent.putExtra(Extras.ID, company_id)
            intent.putExtra(Extras.NAME, company_name)
            ctx?.startActivity(intent)
        }
    }

    private var selectType = 0
    private lateinit var popwin: IOSPopwindow
    var step = 0
    var selectId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_holder_step)
        step = intent.getIntExtra(Extras.DATA, 0)
        setBack(true)
        if (step == 1) {
            title = "关联公司"
            step_icon.backgroundResource = R.drawable.step1
            step_title.text = "选择关联公司"
            step_subtitle.text = "可将征信记录自动归类至项目中"

            enter.text = "下一步"
            step_layout_1.visibility = View.VISIBLE
            step_layout_2.visibility = View.GONE
        } else if (step == 2) {
            title = "添加人员"
            step_icon.backgroundResource = R.drawable.step2
            step_title.text = "填写查询基本信息"
            step_subtitle.text = "选填信息可增加查询信息准确度"

            enter.text = "开始查询"
            step_layout_1.visibility = View.GONE
            step_layout_2.visibility = View.VISIBLE
        }

        popwin = IOSPopwindow(this)
        typeSelect.setOnClickListener(this)
        companySelect.setOnClickListener(this)
        enter.setOnClickListener(this)
        phoneEdt.setOnFocusChangeListener { v, hasFocus ->
            val editText = v as EditText
            if (!hasFocus && editText.text.isNotEmpty() && !Utils.isMobileExact(editText.text)) {
                editText.setText("")
                showCustomToast(R.drawable.icon_toast_common, "请输入正确的手机号")
            }
        }
        IDCardEdt.setOnFocusChangeListener { v, hasFocus ->
            val editText = v as EditText
            if (!hasFocus && editText.text.isNotEmpty() && !Utils.isIDCard18(editText.text)) {
                editText.setText("")
                showCustomToast(R.drawable.icon_toast_common, "请输入正确的身份证号")
            }
        }
        popwin.setOnItemClickListener { v, select ->
            if (select == 1) {
                typeSelectTv.text = "董监高"
            } else {
                typeSelectTv.text = "股东"
            }
            selectType = select
        }
    }

    var params = HashMap<String, Any>()

    private fun prepare(): Boolean {
        if (nameEdt.text.isEmpty()) {
            showCustomToast(R.drawable.icon_toast_common, "请填写名字")
            return false
        }
        if (IDCardEdt.text.toString().isEmpty()) {
            showCustomToast(R.drawable.icon_toast_common, "请填写身份证")
            return false
        }

        var tmp = HashMap<String, Any>()
        tmp.put("phone", phoneEdt.text.toString())
        tmp.put("name", nameEdt.text.toString())
        tmp.put("idCard", IDCardEdt.text.toString())
        tmp.put("company_id", intent.getIntExtra(Extras.ID, 0))
        tmp.put("company", intent.getStringExtra(Extras.NAME))
        tmp.put("type", selectType)

        params.put("ae", tmp)
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0x001 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 权限被用户同意，可以去放肆了。
                    try {
                        val intent = Intent(Intent.ACTION_CALL)
                        val data = Uri.parse("tel:" + telephone)
                        intent.data = data
                        startActivity(intent)
                    } catch (e: SecurityException) {
                    }
                } else {
                    // 权限被用户拒绝了，洗洗睡吧。
                }
                return
            }
        }
    }

    var telephone = ""

    @TargetApi(Build.VERSION_CODES.KITKAT)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.enter -> {
                if (step == 1) {
                    ShareHolderStepActivity.start(context, 2, selectId, companyName.text.toString())
                    //ActivityHelper已添加
                } else if (step == 2) {
                    if (!prepare()) {
                        return
                    }
                    enter.isEnabled = false
                    SoguApi.getService(application, CreditService::class.java)
                            .queryCreditInfo(params)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ payload ->
                                enter.isEnabled = true
                                if (payload.isOk) {
                                    var item = payload.payload

                                    var project = ProjectBean()
                                    project.name = intent.getStringExtra(Extras.NAME)
                                    project.company_id = intent.getIntExtra(Extras.ID, 0)
                                    //是否有ShareholderCreditActivity。有就发送，没有就删除step1，然后创建
                                    ActivityHelper.removeStep1()
                                    if (ActivityHelper.hasCreditListActivity()) {
                                        RxBus.getIntanceBus().post(MessageEvent(item))
                                    } else {
                                        ShareholderCreditActivity.start(this@ShareHolderStepActivity, project)
                                    }
                                    finish()
                                } else {
                                    if (payload.message == "9528") {
                                        //提示支付
                                        val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                                        if (permission != PackageManager.PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), 0x001)
                                        } else {
                                            SoguApi.getStaticHttp(application).getPayType()
                                                    .execute {
                                                        onNext {payload ->
                                                            if (payload.isOk) {
                                                                payload.payload?.let {
                                                                    val bean = it.find { it.mealName == "征信套餐" }
                                                                    val pay = PayView(context,bean)
                                                                    pay.show(1, bean?.tel)
                                                                }
                                                            }else{
                                                                showErrorToast(payload.message)
                                                            }
                                                        }
                                                    }
                                        }
                                    }else{
                                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                                    }
                                }
                            }, { e ->
                                enter.isEnabled = true
                                Trace.e(e)
                                showCustomToast(R.drawable.icon_toast_fail, "查询失败")
                            })
                }
            }
            R.id.companySelect -> {
                var map = CustomSealBean.ValueBean()
                map.type = 2
                var bean = CustomSealBean()
                bean.name = "公司名称"
                bean.value_map = map
                ListSelectorActivity.start(this, bean)
            }
            R.id.typeSelect -> {
                Utils.closeInput(this, IDCardEdt)
                popwin.showAtLocation(find(R.id.enter), Gravity.BOTTOM, 0, 0)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ListSelectorActivity.REQ_LIST_SELECTOR && resultCode === Activity.RESULT_OK) {
            val valueBean = data!!.getSerializableExtra(Extras.DATA2) as CustomSealBean.ValueBean
            companyName.text = valueBean.name
            selectId = valueBean.id!!
        }
    }
}
