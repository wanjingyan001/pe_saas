package com.sogukj.pe.module.receipt

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.sogukj.pe.App
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.PdfBook
import com.sogukj.pe.peUtils.ToastUtil
import com.sogukj.service.SoguApi
import org.jetbrains.anko.find
import java.util.*

/**
 * Created by CH-ZH on 2018/10/24.
 */
class PayDialog {
    companion object {
        //征信和购买账户套餐
        private var isClickPer = false
        private var isClickBus = false
        private var context : Context ? = null
        fun showPayCreditDialog(context:Context,type:Int,callBack: AllPayCallBack){
            this.context = context
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
            val tv_bus_title = dialog.find<TextView>(R.id.tv_bus_title)
            val tv_per_title = dialog.find<TextView>(R.id.tv_per_title)
            var coin = 9.9
            var isCheckPer = false
            var isCheckBus = false
            var isCheckWx = false
            var isCheckZfb = true
            var pay_type = 3 //1 :个人 2：企业 3：支付宝 4 ：微信
            var selectCombo = 1 // 默认套餐一
            var count = 100
            var realPrice = 0
            if (type == 1){
                ll_buy.visibility = View.VISIBLE
                ll_credit.visibility = View.INVISIBLE
                tv_title1.text = "征信已达上限"
                tv_title2.text = "请购买征信套餐"
                tv_coin.text="￥999"
                realPrice = 999
            }else{
                ll_buy.visibility = View.INVISIBLE
                ll_credit.visibility = View.VISIBLE
                tv_title1.text = "需要购买账号套餐才"
                tv_title2.text = "能使用相应权限"
                tv_coin.text="￥99"
                realPrice = 99
            }

            iv_close.clickWithTrigger {
                if (dialog.isShowing){
                    dialog.dismiss()
                }
            }

            ll_combo1.clickWithTrigger {
                //套餐1
                if (selectCombo != 1){
                    ll_combo1.setBackgroundResource(R.drawable.bg_credit)
                    ll_combo2.setBackgroundResource(R.drawable.bg_credit_normal)
                    ll_combo3.setBackgroundResource(R.drawable.bg_credit_normal)
                    ll_combo4.setBackgroundResource(R.drawable.bg_credit_normal)
                    selectCombo = 1
                    tv_coin.text="￥999"
                    count = 100
                    realPrice = 999
                }
            }

            ll_combo2.clickWithTrigger {
                //套餐2
                if (selectCombo != 2){
                    selectCombo = 2
                    ll_combo1.setBackgroundResource(R.drawable.bg_credit_normal)
                    ll_combo2.setBackgroundResource(R.drawable.bg_credit)
                    ll_combo3.setBackgroundResource(R.drawable.bg_credit_normal)
                    ll_combo4.setBackgroundResource(R.drawable.bg_credit_normal)
                    tv_coin.text="￥8800"
                    count = 1000
                    realPrice = 8800
                }
            }

            ll_combo3.clickWithTrigger {
                //套餐3
                if (selectCombo != 3){
                    selectCombo = 3
                    ll_combo1.setBackgroundResource(R.drawable.bg_credit_normal)
                    ll_combo2.setBackgroundResource(R.drawable.bg_credit_normal)
                    ll_combo3.setBackgroundResource(R.drawable.bg_credit)
                    ll_combo4.setBackgroundResource(R.drawable.bg_credit_normal)
                    tv_coin.text="￥48000"
                    count = 7000
                    realPrice = 48000
                }
            }

            ll_combo4.clickWithTrigger {
                //套餐4
                if (selectCombo != 4){
                    selectCombo = 4
                    ll_combo1.setBackgroundResource(R.drawable.bg_credit_normal)
                    ll_combo2.setBackgroundResource(R.drawable.bg_credit_normal)
                    ll_combo3.setBackgroundResource(R.drawable.bg_credit_normal)
                    ll_combo4.setBackgroundResource(R.drawable.bg_credit)
                    tv_coin.text="￥98000"
                    count = 15000
                    realPrice = 98000
                }
            }
            getPerAccountInfo(tv_per_balance,iv_pre_select,tv_per_title,false,realPrice.toString())
            getBusAccountInfo(tv_bus_balance,iv_bus_select,tv_bus_title,false,realPrice.toString())
            rl_bus.clickWithTrigger {
                //企业账户
                if (isClickBus){
                    if (!isCheckBus){
                        isCheckBus = !isCheckBus
                        iv_bus_select.setImageResource(R.mipmap.ic_unselect_receipt)
                        if(isClickPer){
                            iv_pre_select.setImageResource(R.mipmap.ic_select_receipt)
                            isCheckPer = false
                        }
                        iv_wx_select.setImageResource(R.mipmap.ic_select_receipt)
                        iv_zfb_select.setImageResource(R.mipmap.ic_select_receipt)
                        isCheckWx = false
                        isCheckZfb = false

                        pay_type = 2
                    }
                }
            }

            rl_pre.clickWithTrigger {
                //个人账户
                if (isClickPer){
                    if (!isCheckPer){
                        isCheckPer = !isCheckPer
                        iv_pre_select.setImageResource(R.mipmap.ic_unselect_receipt)
                        if(isClickBus){
                            iv_bus_select.setImageResource(R.mipmap.ic_select_receipt)
                            isCheckBus = false
                        }
                        iv_wx_select.setImageResource(R.mipmap.ic_select_receipt)
                        iv_zfb_select.setImageResource(R.mipmap.ic_select_receipt)
                        isCheckWx = false
                        isCheckZfb = false

                        pay_type = 1
                    }
                }
            }

            rl_wx.clickWithTrigger {
                //微信
                if (!isCheckWx){
                    isCheckWx = !isCheckWx
                    iv_wx_select.setImageResource(R.mipmap.ic_unselect_receipt)
                    iv_zfb_select.setImageResource(R.mipmap.ic_select_receipt)
                    if (isClickBus){
                        iv_bus_select.setImageResource(R.mipmap.ic_select_receipt)
                        isCheckBus = false
                    }
                    if (isCheckPer){
                        iv_pre_select.setImageResource(R.mipmap.ic_select_receipt)
                        isCheckPer = false
                    }
                    isCheckZfb = false
                    pay_type = 4
                }
            }

            rl_zfb.clickWithTrigger {
                //支付宝
                if (!isCheckZfb){
                    isCheckZfb = !isCheckZfb
                    iv_wx_select.setImageResource(R.mipmap.ic_select_receipt)
                    iv_zfb_select.setImageResource(R.mipmap.ic_unselect_receipt)
                    if (isClickBus){
                        iv_bus_select.setImageResource(R.mipmap.ic_select_receipt)
                        isCheckBus = false
                    }
                    if (isCheckPer){
                        iv_pre_select.setImageResource(R.mipmap.ic_select_receipt)
                        isCheckPer = false
                    }
                    isCheckWx = false

                    pay_type = 3
                }
            }

            tv_pay.clickWithTrigger {
                //去支付
                if (null != callBack){
                    if (type == 1){
                        //购买征信
                        callBack.pay(3,count,pay_type,realPrice.toString(),tv_per_balance,
                                iv_pre_select,tv_bus_balance,iv_bus_select,tv_per_title,tv_bus_title,dialog)
                    }
                }
            }


        }

        fun getBusAccountInfo(tv_bus_balance: TextView, iv_bus_select: ImageView, tv_bus_title:TextView,isRefresh: Boolean,realPrice:String) {
            SoguApi.getStaticHttp(App.INSTANCE)
                    .getBussAccountInfo()
                    .execute {
                        onNext { payload ->
                            if (payload.isOk){
                                val recordBean = payload.payload
                                if (null != recordBean){
                                    tv_bus_balance.text = "账户余额：${recordBean.balance}"
                                    if (realPrice.toFloat() > recordBean.balance.toFloat()){
                                        iv_bus_select.setImageResource(R.mipmap.ic_gray_receipt)
                                        tv_bus_title.setTextColor(context!!.resources.getColor(R.color.gray_a0))
                                        tv_bus_balance.setTextColor(context!!.resources.getColor(R.color.gray_a0))
                                        isClickBus = false
                                    }else{
                                        if (recordBean.balance.equals("0") || recordBean.balance.equals("")){
                                            iv_bus_select.setImageResource(R.mipmap.ic_gray_receipt)
                                            tv_bus_title.setTextColor(context!!.resources.getColor(R.color.gray_a0))
                                            tv_bus_balance.setTextColor(context!!.resources.getColor(R.color.gray_a0))
                                            isClickBus = false
                                        }else{
                                            if (!isRefresh){
                                                iv_bus_select.setImageResource(R.mipmap.ic_select_receipt)
                                            }
                                            tv_bus_title.setTextColor(context!!.resources.getColor(R.color.black_28))
                                            tv_bus_balance.setTextColor(context!!.resources.getColor(R.color.gray_80))
                                            isClickBus = true
                                        }
                                    }
                                }
                            }else{
                                ToastUtil.showCustomToast(R.drawable.icon_toast_fail, payload.message, context!!)
                            }
                        }

                        onError {
                            it.printStackTrace()
                            ToastUtil.showCustomToast(R.drawable.icon_toast_fail, "获取个人账号信息失败", context!!)
                        }
                    }
        }

        fun getPerAccountInfo(tv_per_balance: TextView, iv_pre_select: ImageView, tv_per_title:TextView,isRefresh: Boolean,
                              realPrice:String) {
            SoguApi.getStaticHttp(App.INSTANCE)
                    .getPersonAccountInfo()
                    .execute {
                        onNext { payload ->
                            if (payload.isOk){
                                val recordBean = payload.payload
                                if (null != recordBean){
                                    tv_per_balance.text = "账户余额：${recordBean.balance}"
                                    if (realPrice.toFloat() > recordBean.balance.toFloat()){
                                        iv_pre_select.setImageResource(R.mipmap.ic_gray_receipt)
                                        tv_per_title.setTextColor(context!!.resources.getColor(R.color.gray_a0))
                                        tv_per_balance.setTextColor(context!!.resources.getColor(R.color.gray_a0))
                                        isClickPer = false
                                    }else{
                                        if (recordBean.balance.equals("0") || recordBean.balance.equals("")){
                                            iv_pre_select.setImageResource(R.mipmap.ic_gray_receipt)
                                            tv_per_title.setTextColor(context!!.resources.getColor(R.color.gray_a0))
                                            tv_per_balance.setTextColor(context!!.resources.getColor(R.color.gray_a0))
                                            isClickPer = false
                                        }else{
                                            if (!isRefresh){
                                                iv_pre_select.setImageResource(R.mipmap.ic_select_receipt)
                                            }
                                            tv_per_title.setTextColor(context!!.resources.getColor(R.color.black_28))
                                            tv_per_balance.setTextColor(context!!.resources.getColor(R.color.gray_80))
                                            isClickPer = true
                                        }
                                    }
                                }
                            }else{
                                ToastUtil.showCustomToast(R.drawable.icon_toast_fail, payload.message, context!!)
                            }
                        }

                        onError {
                            it.printStackTrace()
                            ToastUtil.showCustomToast(R.drawable.icon_toast_fail, "获取个人账号信息失败", context!!)
                        }
                    }
        }

        //智能文书和账户管理
        fun showPayBookDialog(context:Context, type:Int, callBack: AllPayCallBack, title:String, price:String, count:Int, id:String, book: PdfBook?){
            this.context = context
            val dialog = MaterialDialog.Builder(context)
                    .theme(Theme.DARK)
                    .customView(R.layout.layout_pay_book, false)
                    .canceledOnTouchOutside(false)
                    .build()
            dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
            val iv_close = dialog.find<ImageView>(R.id.iv_close)
            val tv_title1 = dialog.find<TextView>(R.id.tv_title1)
            val tv_time = dialog.find<TextView>(R.id.tv_time)
            val iv_pay = dialog.find<ImageView>(R.id.iv_pay)
            val tv_fee = dialog.find<TextView>(R.id.tv_fee)
            val tv_name = dialog.find<TextView>(R.id.tv_name)
            val tv_count = dialog.find<TextView>(R.id.tv_count)

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
            val tv_bus_title = dialog.find<TextView>(R.id.tv_bus_title)
            val tv_per_title = dialog.find<TextView>(R.id.tv_per_title)
            var isCheckPer = false
            var isCheckBus = false
            var isCheckWx = false
            var isCheckZfb = true
            var pay_type = 3 //1 :个人 2：企业 3：支付宝 4 ：微信

            tv_time.text = Utils.getTime(Date(),"yyyy-MM-dd HH:mm:ss")
            tv_name.text = title
            tv_fee.text = "￥${price}"
            tv_count.text = "X${count}"
            if (type == 1){
                tv_title1.text = "智能文书"
                iv_pay.setImageResource(R.mipmap.ic_book_head)
            }else{
                tv_title1.text = "付费账号"
                iv_pay.setImageResource(R.mipmap.ic_account_head)
            }
            var realPrice = price
            if (Utils.isInteger(price)){
                val iPrice = price.toInt()
                val amount = iPrice * count
                tv_coin.text = "￥${amount}"
                realPrice = amount.toString()
            }else{
                tv_coin.text = "￥${price}"
                realPrice = price
            }
            getPerAccountInfo(tv_per_balance,iv_pre_select,tv_per_title,false,realPrice)
            getBusAccountInfo(tv_bus_balance,iv_bus_select,tv_bus_title,false,realPrice)
            iv_close.clickWithTrigger {
                if (dialog.isShowing){
                    dialog.dismiss()
                }
            }
            rl_bus.clickWithTrigger {
                //企业账户
                if (isClickBus){
                    if (!isCheckBus){
                        isCheckBus = !isCheckBus
                        iv_bus_select.setImageResource(R.mipmap.ic_unselect_receipt)
                        if(isClickPer){
                            iv_pre_select.setImageResource(R.mipmap.ic_select_receipt)
                            isCheckPer = false
                        }
                        iv_wx_select.setImageResource(R.mipmap.ic_select_receipt)
                        iv_zfb_select.setImageResource(R.mipmap.ic_select_receipt)
                        isCheckWx = false
                        isCheckZfb = false

                        pay_type = 2
                    }
                }
            }

            rl_pre.clickWithTrigger {
                //个人账户
                if (isClickPer){
                    if (!isCheckPer){
                        isCheckPer = !isCheckPer
                        iv_pre_select.setImageResource(R.mipmap.ic_unselect_receipt)
                        if(isClickBus){
                            iv_bus_select.setImageResource(R.mipmap.ic_select_receipt)
                            isCheckBus = false
                        }
                        iv_wx_select.setImageResource(R.mipmap.ic_select_receipt)
                        iv_zfb_select.setImageResource(R.mipmap.ic_select_receipt)
                        isCheckWx = false
                        isCheckZfb = false

                        pay_type = 1
                    }
                }
            }

            rl_wx.clickWithTrigger {
                //微信
                if (!isCheckWx){
                    isCheckWx = !isCheckWx
                    iv_wx_select.setImageResource(R.mipmap.ic_unselect_receipt)
                    iv_zfb_select.setImageResource(R.mipmap.ic_select_receipt)
                    if (isClickBus){
                        iv_bus_select.setImageResource(R.mipmap.ic_select_receipt)
                        isCheckBus = false
                    }
                    if (isCheckPer){
                        iv_pre_select.setImageResource(R.mipmap.ic_select_receipt)
                        isCheckPer = false
                    }
                    isCheckZfb = false
                    pay_type = 4
                }
            }

            rl_zfb.clickWithTrigger {
                //支付宝
                if (!isCheckZfb){
                    isCheckZfb = !isCheckZfb
                    iv_wx_select.setImageResource(R.mipmap.ic_select_receipt)
                    iv_zfb_select.setImageResource(R.mipmap.ic_unselect_receipt)
                    if (isClickBus){
                        iv_bus_select.setImageResource(R.mipmap.ic_select_receipt)
                        isCheckBus = false
                    }
                    if (isCheckPer){
                        iv_pre_select.setImageResource(R.mipmap.ic_select_receipt)
                        isCheckPer = false
                    }
                    isCheckWx = false

                    pay_type = 3
                }
            }

            tv_pay.clickWithTrigger {
                //去支付
                if (null != callBack){
                    if (type == 1){
                        callBack.payForOther(id,2,count,pay_type,price,tv_per_balance,iv_pre_select,
                                tv_bus_balance,iv_bus_select,tv_per_title,tv_bus_title,dialog,book!!)
                    }else{
                        callBack.payForOther(id,1,count,pay_type,price,tv_per_balance,iv_pre_select,
                                tv_bus_balance,iv_bus_select,tv_per_title,tv_bus_title,dialog,book!!)
                    }
                }
            }
        }

    }
}