package com.sogukj.pe.module.receipt

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.sogukj.pe.App
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.peUtils.ToastUtil
import com.sogukj.service.SoguApi
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity

/**
 * Created by CH-ZH on 2018/10/19.
 */
class CreateBillDialog {
    companion object {
        fun showBillDialog(context: Activity, map: HashMap<String, Any>) {
            val dialog = Dialog(context, R.style.AppTheme_Dialog)
            dialog.setContentView(R.layout.dialog_create_bill)
            val lay = dialog.window!!.attributes
            lay.height = WindowManager.LayoutParams.WRAP_CONTENT
            lay.width = WindowManager.LayoutParams.MATCH_PARENT
            lay.gravity = Gravity.BOTTOM
            dialog.window!!.attributes = lay
            dialog.show()
            val tv_title = dialog.find<TextView>(R.id.tv_title)
            val tv_type = dialog.find<TextView>(R.id.tv_type)
            val tv_header = dialog.find<TextView>(R.id.tv_header)
            val tv_duty = dialog.find<TextView>(R.id.tv_duty)
            val tv_accept = dialog.find<TextView>(R.id.tv_accept)
            val tv_phone = dialog.find<TextView>(R.id.tv_phone)
            val tv_address = dialog.find<TextView>(R.id.tv_address)
            val tv_detail = dialog.find<TextView>(R.id.tv_detail)
            val tv_tips = dialog.find<TextView>(R.id.tv_tips)
            val ll_duty = dialog.find<LinearLayout>(R.id.ll_duty)
            val view_duty = dialog.find<View>(R.id.view_duty)
            val iv_close = dialog.find<ImageView>(R.id.iv_close)
            val ll_submit = dialog.find<LinearLayout>(R.id.ll_submit)
            var title_type = 1 // 1 企业发票 2 个人发票
            if (null != map) {
                tv_header.text = map.get("title") as String
                Utils.setSpaceText(tv_duty,map.get("tax_no") as String)
                tv_accept.text = map.get("receiver") as String
                tv_phone.text = map.get("phone") as String
                tv_address.text = map.get("province") as String + " " + map.get("city") as String + " " + map.get("county") as String
                tv_detail.text = map.get("address") as String
            }
            if (title_type == 2){
                ll_duty.setVisible(false)
                view_duty.setVisible(false)
            }else{
                ll_duty.setVisible(true)
                view_duty.setVisible(true)
            }

            iv_close.clickWithTrigger {
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }
            ll_submit.clickWithTrigger {
                SoguApi.getStaticHttp(App.INSTANCE)
                        .submitBillDetail(map)
                        .execute {
                            onNext { payload ->
                                if (payload.isOk) {
                                    context.startActivity<SubmitBillSucActivity>(Extras.TITLE to "开具纸质发票")
                                    LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(MineReceiptActivity.REFRESH_ACTION))
                                    context.finish()
                                }
                            }

                            onError {
                                it.printStackTrace()
                                ToastUtil.showCustomToast(R.drawable.icon_toast_fail, "提交失败", context)
                            }
                        }
            }
        }

        private var isClick = false

        fun showMoreDialog(context: Context, callBack: ShowMoreCallBack,explain:String,phoneaddress:String,bank:String) {
            val dialog = Dialog(context, R.style.AppTheme_Dialog)
            dialog.setContentView(R.layout.dialog_show_more)
            val lay = dialog.window!!.attributes
            lay.height = WindowManager.LayoutParams.WRAP_CONTENT
            lay.width = WindowManager.LayoutParams.MATCH_PARENT
            lay.gravity = Gravity.BOTTOM
            dialog.window!!.attributes = lay
            dialog.show()

            val ll_submit = dialog.findViewById<LinearLayout>(R.id.ll_submit)
            val tv_submit = dialog.findViewById<TextView>(R.id.tv_submit)
            val iv_close = dialog.findViewById<ImageView>(R.id.iv_close)
            val et_explain = dialog.findViewById<EditText>(R.id.et_explain)
            val et_phoneaddress = dialog.findViewById<EditText>(R.id.et_phoneaddress)
            val et_bank = dialog.findViewById<EditText>(R.id.et_bank)
            if (!explain.isNullOrEmpty()){
                et_explain.setText(explain)
                et_explain.setSelection(explain.length)
            }
            if (!phoneaddress.isNullOrEmpty()){
                et_phoneaddress.setText(phoneaddress)
            }
            if (!bank.isNullOrEmpty()){
                et_bank.setText(bank)
            }
            et_explain.addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(s: Editable?) {
                    if (et_explain.textStr.isNullOrEmpty() && et_phoneaddress.textStr.isNullOrEmpty()
                        && et_bank.textStr.isNullOrEmpty()){
                        tv_submit.setBackgroundResource(R.drawable.bg_create_pro_gray)
                        isClick = false
                    }else{
                        tv_submit.setBackgroundResource(R.drawable.bg_create_pro)
                        isClick = true
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

            })
            et_phoneaddress.addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(s: Editable?) {
                    if (et_explain.textStr.isNullOrEmpty() && et_phoneaddress.textStr.isNullOrEmpty()
                            && et_bank.textStr.isNullOrEmpty()){
                        tv_submit.setBackgroundResource(R.drawable.bg_create_pro_gray)
                        isClick = false
                    }else{
                        tv_submit.setBackgroundResource(R.drawable.bg_create_pro)
                        isClick = true
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

            })
            et_bank.addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(s: Editable?) {
                    if (et_explain.textStr.isNullOrEmpty() && et_phoneaddress.textStr.isNullOrEmpty()
                            && et_bank.textStr.isNullOrEmpty()){
                        tv_submit.setBackgroundResource(R.drawable.bg_create_pro_gray)
                        isClick = false
                    }else{
                        tv_submit.setBackgroundResource(R.drawable.bg_create_pro)
                        isClick = true
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

            })
            iv_close.clickWithTrigger {
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }
            ll_submit.clickWithTrigger {
                //提交
                if (isClick){
                    if (et_explain.textStr.isNullOrEmpty() && et_phoneaddress.textStr.isNullOrEmpty() && et_bank.textStr.isNullOrEmpty()) {

                    } else {
                        if (!et_explain.textStr.isNullOrEmpty() && !et_phoneaddress.textStr.isNullOrEmpty() && !et_bank.textStr.isNullOrEmpty()) {
                            if (null != callBack) {
                                callBack.showMoreDetail(3, et_explain.textStr, et_phoneaddress.textStr, et_bank.textStr)
                            }
                        }
                        if (!et_explain.textStr.isNullOrEmpty() && et_phoneaddress.textStr.isNullOrEmpty() && et_bank.textStr.isNullOrEmpty()
                                || !et_phoneaddress.textStr.isNullOrEmpty() && et_explain.textStr.isNullOrEmpty() && et_bank.textStr.isNullOrEmpty()
                                || !et_bank.textStr.isNullOrEmpty() && et_explain.textStr.isNullOrEmpty() && et_phoneaddress.textStr.isNullOrEmpty()) {
                            if (null != callBack) {
                                callBack.showMoreDetail(1, et_explain.textStr, et_phoneaddress.textStr, et_bank.textStr)
                            }
                        }
                        if (et_explain.textStr.isNullOrEmpty() && !et_phoneaddress.textStr.isNullOrEmpty() && !et_bank.textStr.isNullOrEmpty()
                                || et_phoneaddress.textStr.isNullOrEmpty() && !et_explain.textStr.isNullOrEmpty() && !et_bank.textStr.isNullOrEmpty()
                                || et_bank.textStr.isNullOrEmpty() && !et_explain.textStr.isNullOrEmpty() && !et_phoneaddress.textStr.isNullOrEmpty()) {
                            if (null != callBack) {
                                callBack.showMoreDetail(2, et_explain.textStr, et_phoneaddress.textStr, et_bank.textStr)
                            }
                        }
                    }

                    if (dialog.isShowing){
                        dialog.dismiss()
                    }
                }
            }
        }

    }
}