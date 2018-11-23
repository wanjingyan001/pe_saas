package com.sogukj.pe.module.receipt

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.fromJson
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.MechanismBasicInfo
import com.sogukj.pe.bean.UserBean
import com.sogukj.pe.peUtils.Store
import kotlinx.android.synthetic.main.activity_recorder_detail.*
import org.jetbrains.anko.imageResource

/**
 * Created by CH-ZH on 2018/10/24.
 * 订单详情
 */
class RecordDetailActivity : ToolbarActivity() {
    private var time = ""
    private var describe = ""
    private var count = 1
    private var fee = "" //单价
    private var price = "" //总价
    private var type = 1
    private var user : UserBean ? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recorder_detail)
        Utils.setWindowStatusBarColor(this, R.color.white)
        initView()
        initData()
    }

    private fun initData() {
        val company = sp.getString(Extras.SAAS_BASIC_DATA, "")
        val detail = Gson().fromJson<MechanismBasicInfo?>(company)
        tv_company.text = detail?.mechanism_name ?: "XPE"
        if (null != user){
            tv_name.text = "购买人：${user!!.name}"
            tv_account.text = "购买账号：${user!!.phone}"
        }

        tv_time.text = time
        when(type){
            4 -> iv_pay.imageResource = R.mipmap.ic_sentiment_head
            3 -> iv_pay.imageResource = R.mipmap.ic_credit_head
            1 -> iv_pay.imageResource = R.mipmap.ic_account_head
            2 -> iv_pay.imageResource = R.mipmap.ic_book_head
        }

        tv_title.text = describe
        tv_fee.text = "￥${fee}"
        tv_count.text = "X${count}"
        tv_amount.text = "￥${price}"
    }

    private fun initView() {
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        setBack(true)
        setTitle("订单详情")

        time = intent.getStringExtra("time")
        describe = intent.getStringExtra("describe")
        count = intent.getIntExtra("count",1)
        fee = intent.getStringExtra("fee")
        price = intent.getStringExtra("price")
        type = intent.getIntExtra("type",1)
        user = Store.store.getUser(this)
    }

    companion object {
        fun invoke(context: Context,time:String,describe:String,count:Int,fee:String,price:String,type:Int){
            val intent = Intent(context,RecordDetailActivity::class.java)
            intent.putExtra("time",time)
            intent.putExtra("describe",describe)
            intent.putExtra("count",count)
            intent.putExtra("fee",fee)
            intent.putExtra("type",type)
            intent.putExtra("price",price)
            if (context !is Activity){
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}