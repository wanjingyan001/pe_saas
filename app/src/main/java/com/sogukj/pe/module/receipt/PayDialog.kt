package com.sogukj.pe.module.receipt

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.setVisible
import org.jetbrains.anko.find

/**
 * Created by CH-ZH on 2018/10/24.
 */
class PayDialog {
    companion object {
        //征信和购买账户套餐
        fun showPayCreditDialog(context:Context,type:Int){
            val dialog = MaterialDialog.Builder(context)
                    .theme(Theme.DARK)
                    .customView(R.layout.layout_pay_credit, false)
                    .canceledOnTouchOutside(false)
                    .build()
            dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
            val iv_close = dialog.find<ImageView>(R.id.iv_close)
            val tv_title1 = dialog.find<TextView>(R.id.tv_title1)
            val tv_title2 = dialog.find<TextView>(R.id.tv_title2)
            val ll_buy = dialog.find<LinearLayout>(R.id.ll_buy)
            val ll_combo1 = dialog.find<LinearLayout>(R.id.ll_combo1)
            val ll_combo2 = dialog.find<LinearLayout>(R.id.ll_combo2)
            val ll_combo3 = dialog.find<LinearLayout>(R.id.ll_combo3)
            val ll_combo4 = dialog.find<LinearLayout>(R.id.ll_combo4)

            val ll_credit = dialog.find<LinearLayout>(R.id.ll_credit)

            val tv_coin = dialog.find<TextView>(R.id.tv_coin)
            val rl_bus = dialog.find<RelativeLayout>(R.id.rl_bus)
            val tv_bus_balance = dialog.find<TextView>(R.id.tv_bus_balance)
            val iv_bus_select = dialog.find<ImageView>(R.id.iv_bus_select)
            val rl_pre = dialog.find<RelativeLayout>(R.id.rl_pre)
            val tv_per_balance = dialog.find<TextView>(R.id.tv_per_balance)
            val iv_pre_select = dialog.find<ImageView>(R.id.iv_pre_select)
            val rl_wx = dialog.find<RelativeLayout>(R.id.rl_wx)
            val iv_wx_select = dialog.find<ImageView>(R.id.iv_wx_select)
            val rl_zfb = dialog.find<RelativeLayout>(R.id.rl_zfb)
            val iv_zfb_select = dialog.find<ImageView>(R.id.iv_zfb_select)
            val tv_pay = dialog.find<TextView>(R.id.tv_pay)
            var coin = 9.9
            var isCheckPer = false
            var isCheckBus = false
            var isCheckWx = false
            var isCheckZfb = true
            var pay_type = 1 //1 :支付宝 2：微信 3：个人 4 ：企业

            if (type == 1){
                ll_buy.visibility = View.VISIBLE
                ll_credit.visibility = View.INVISIBLE
                tv_title1.text = "征信已达上限"
                tv_title2.text = "请购买征信套餐"
                tv_coin.text="￥999"
            }else{
                ll_buy.visibility = View.INVISIBLE
                ll_credit.visibility = View.VISIBLE
                tv_title1.text = "需要购买账号套餐才"
                tv_title2.text = "能使用相应权限"
                tv_coin.text="￥99"
            }

            ll_combo1.clickWithTrigger {
                //套餐1
            }

            ll_combo2.clickWithTrigger {
                //套餐2
            }

            ll_combo3.clickWithTrigger {
                //套餐3
            }

            ll_combo4.clickWithTrigger {
                //套餐4
            }

        }
        //智能文书和账户管理
        fun showPayBookDialog(context:Context,type:Int){
            val dialog = MaterialDialog.Builder(context)
                    .theme(Theme.DARK)
                    .customView(R.layout.layout_show_pay, false)
                    .canceledOnTouchOutside(false)
                    .build()
            dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
            val iv_close = dialog.find<ImageView>(R.id.iv_close)
            val tv_subtract = dialog.find<TextView>(R.id.tv_subtract)
            val et_count = dialog.find<EditText>(R.id.et_count)
            val tv_add = dialog.find<TextView>(R.id.tv_add)
            val tv_coin = dialog.find<TextView>(R.id.tv_coin)
            val rl_bus = dialog.find<RelativeLayout>(R.id.rl_bus)
            val tv_bus_balance = dialog.find<TextView>(R.id.tv_bus_balance)
            val iv_bus_select = dialog.find<ImageView>(R.id.iv_bus_select)
            val rl_pre = dialog.find<RelativeLayout>(R.id.rl_pre)
            val tv_per_balance = dialog.find<TextView>(R.id.tv_per_balance)
            val iv_pre_select = dialog.find<ImageView>(R.id.iv_pre_select)
            val rl_wx = dialog.find<RelativeLayout>(R.id.rl_wx)
            val iv_wx_select = dialog.find<ImageView>(R.id.iv_wx_select)
            val rl_zfb = dialog.find<RelativeLayout>(R.id.rl_zfb)
            val iv_zfb_select = dialog.find<ImageView>(R.id.iv_zfb_select)
            val tv_pay = dialog.find<TextView>(R.id.tv_pay)
            var count = 1 //订单数量
            var coin = 9.9
            var isCheckPer = false
            var isCheckBus = false
            var isCheckWx = false
            var isCheckZfb = true
            var pay_type = 1 //1 :支付宝 2：微信 3：个人 4 ：企业
        }
    }
}