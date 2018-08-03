package com.sogukj.pe.module.creditCollection

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.CreditInfo
import com.sogukj.pe.bean.CustomSealBean
import com.sogukj.pe.module.approve.ListSelectorActivity
import com.sogukj.pe.service.CreditService
import com.sogukj.pe.widgets.IOSPopwindow
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_add_credit.*
import org.jetbrains.anko.find


class AddCreditActivity : BaseActivity(), View.OnClickListener {
    private lateinit var popwin: IOSPopwindow
    private var selectType = 0
    var data: CreditInfo.Item? = null
    var type = ""
    var selectId = 0

    companion object {
        fun start(ctx: Activity?, type: String, data: CreditInfo.Item? = null, code: Int) {
            val intent = Intent(ctx, AddCreditActivity::class.java)
            intent.putExtra(Extras.TYPE, type)
            intent.putExtra(Extras.DATA, data)
            ctx?.startActivityForResult(intent, code)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_credit)
        type = intent.getStringExtra(Extras.TYPE)
        data = intent.getSerializableExtra(Extras.DATA) as CreditInfo.Item
        if (type == "ADD") {
            toolbar_title.text = "添加人员"
            data?.apply {
                companyName.text = company
                selectId = company_id
            }
        } else if (type == "EDIT") {
            data?.apply {
                nameEdt.setText(name)
                IDCardEdt.setText(idCard)
                phoneEdt.setText(phone)
                companyName.text = company
                selectId = company_id
                typeSelectTv.text =
                        when (type) {
                            1 -> "董监高"
                            2 -> "股东"
                            else -> ""
                        }
                selectType = type

                if (name.isEmpty()) {
                    nameEdt.setSelection(0)
                } else {
                    nameEdt.setSelection(name.length)
                }
            }
            toolbar_title.text = "编辑信息"
            toolbar_menu.visibility = View.VISIBLE
            toolbar_menu.setOnClickListener(this)
        }

        popwin = IOSPopwindow(this)
        toolbar_back.visibility = View.VISIBLE
        toolbar_back.setOnClickListener(this)
        typeSelect.setOnClickListener(this)
        companySelect.setOnClickListener(this)
        save.setOnClickListener(this)
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
        if (type.equals("ADD")) {
            tmp.put("phone", phoneEdt.text.toString())
            tmp.put("name", nameEdt.text.toString())
            tmp.put("idCard", IDCardEdt.text.toString())
            tmp.put("company_id", selectId)
            tmp.put("company", companyName.text.toString())
            tmp.put("type", selectType)
        } else if (type.equals("EDIT")) {
            tmp.put("id", data!!.id)
            tmp.put("phone", phoneEdt.text.toString())
            tmp.put("name", nameEdt.text.toString())
            tmp.put("idCard", IDCardEdt.text.toString())
            tmp.put("company_id", selectId)
            tmp.put("company", companyName.text.toString())
            tmp.put("type", selectType)
        }

        params.put("ae", tmp)
        return true
    }

    private fun doInquire() {
        if (!prepare()) {
            return
        }
        save.isEnabled = false
        SoguApi.getService(application,CreditService::class.java)
                .queryCreditInfo(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    save.isEnabled = true
                    if (payload.isOk) {
                        val intent = Intent()
                        intent.putExtra(Extras.DATA, payload.payload)
                        setResult(Extras.RESULTCODE, intent)
                        finish()
                    } else {
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    }
                }, { e ->
                    save.isEnabled = true
                    Trace.e(e)
                    showCustomToast(R.drawable.icon_toast_fail, "查询失败")
                })
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.toolbar_back -> finish()
            R.id.save -> {
                doInquire()
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
                popwin.showAtLocation(find(R.id.add_layout), Gravity.BOTTOM, 0, 0)
            }
            R.id.toolbar_menu -> {
                var mDialog = MaterialDialog.Builder(this@AddCreditActivity)
                        .theme(Theme.LIGHT)
                        .canceledOnTouchOutside(true)
                        .customView(R.layout.dialog_yongyin, false).build()
                mDialog.show()
                val content = mDialog.find<TextView>(R.id.content)
                val cancel = mDialog.find<Button>(R.id.cancel)
                val yes = mDialog.find<Button>(R.id.yes)
                content.text = "是否需要删除该征信记录"
                cancel.text = "否"
                yes.text = "是"
                cancel.setOnClickListener {
                    mDialog.dismiss()
                }
                yes.setOnClickListener {
                    SoguApi.getService(application,CreditService::class.java)
                            .deleteCredit(data!!.id)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ payload ->
                                if (payload.isOk) {
                                    showCustomToast(R.drawable.icon_toast_success, "删除成功")
                                    mDialog.dismiss()
                                    val intent = Intent()
                                    intent.putExtra(Extras.DATA, data)
                                    intent.putExtra(Extras.TYPE, "DELETE")
                                    setResult(Extras.RESULTCODE, intent)
                                    finish()
                                } else {
                                    showCustomToast(R.drawable.icon_toast_fail, payload.message)
                                }
                            }, { e ->
                                showCustomToast(R.drawable.icon_toast_fail, "删除失败")
                            })
                }
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
