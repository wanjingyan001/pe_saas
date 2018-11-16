package com.sogukj.pe.module.other

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import com.alipay.sdk.app.PayTask
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.RxBus
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import com.sogukj.pe.baselibrary.widgets.RecyclerHolder
import com.sogukj.pe.baselibrary.widgets.SpaceItemDecoration
import com.sogukj.pe.bean.*
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_pay_expansion.*
import kotlinx.android.synthetic.main.item_pay_discount.view.*
import kotlinx.android.synthetic.main.item_pay_expansion_list.view.*
import kotlinx.android.synthetic.main.layout_pay_slient.*
import kotlinx.android.synthetic.main.layout_pay_way.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.dip
import org.jetbrains.anko.info
import org.jetbrains.anko.sdk25.coroutines.textChangedListener
import kotlin.properties.Delegates

@SuppressLint("SetTextI18n")
class PayExpansionActivity : BaseActivity() {
    private lateinit var pjAdapter: RecyclerAdapter<PackageChild>
    private lateinit var calenderAdapter: RecyclerAdapter<PackageChild>
    private var product: ProductInfo by Delegates.observable(ProductInfo(), { property, oldValue, newValue ->
        savingAmount.setVisible(newValue.discountPrice != newValue.OriginalPrice)
        savingAmount.text = "立省￥${(oldValue.OriginalPrice - newValue.discountPrice).toMoney()}"
//        paymentPrice.text = "￥${(newValue.discountPrice + newValue.calenderPrice).toMoney()}"
    })
    private val payReqChild = PayReqChid()

    private var perBalance = "0" //个人账户余额
    private var busBalance = "0" //企业账户余额
    private var isClickPer = false
    private var isClickBus = false
    private var isCheckPer = false
    private var isCheckBus = false
    private var isCheckWx = false
    private var isCheckZfb = true
    private var count = 1 //舆情购买数量
    private var coin = 9.9 //舆情总价
    private var zxPrice = "0"
    private var allprice = "9.9"
    private var data = "2018年9月30日"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_expansion)
        Utils.setWindowStatusBarColor(this, R.color.main_bottom_bar_color)
        toolbar_title.text = "扩容套餐购买"
        toolbar_back.clickWithTrigger {
            finish()
        }
        pjAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            ExpansionHolder(_adapter.getView(R.layout.item_pay_expansion_list, parent), pjAdapter)
        }
        calenderAdapter = RecyclerAdapter(this) { _adapter, parent, _ ->
            ExpansionHolder(_adapter.getView(R.layout.item_pay_expansion_list, parent), calenderAdapter)
        }
        calenderAdapter.onItemClick = { v, position ->
            calenderAdapter.selectedPosition = position
            payReqChild.id = calenderAdapter.dataList[position].id
            product = product.copy(discountPrice = product.discountPrice,
                    OriginalPrice = product.OriginalPrice,
                    calenderPrice = calenderAdapter.dataList[position].price)
            zxPrice = calenderAdapter.dataList[position].price.toString()

            allprice = Utils.stringAdd(coin.toString(),zxPrice)
            paymentPrice.text = "￥${allprice}"
            prepareValue(allprice,perBalance,busBalance)
        }
        pjPackageList.apply {
            layoutManager = LinearLayoutManager(this@PayExpansionActivity)
            adapter = pjAdapter
            addItemDecoration(SpaceItemDecoration(dip(10)))
        }
        calenderPackageList.apply {
            layoutManager = LinearLayoutManager(this@PayExpansionActivity)
            adapter = calenderAdapter
            addItemDecoration(SpaceItemDecoration(dip(10)))
        }
        getPayPackageInfo()
        payConfirm.clickWithTrigger {
            getPayInfo()
        }
        getPerAccountInfo()
        getBusAccountInfo()
        bindListener()

        paymentPrice.text = "￥${allprice}"

        val warn = "您的套餐将于${data}到期，为了团队的正常工作，请尽快续费。"
        val spannableString = SpannableString(warn)
        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#F85959")), 6, 8 + data.length , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tv_warn.text = spannableString
    }

    private fun bindListener() {
        tv_subtract.setOnClickListener {
            //减少舆情数量
            count = et_count.textStr.toInt()
            count--
            if (count <= 1) {
                count = 1
            }
            coin = Utils.reserveTwoDecimal(9.9 * count, 2)
            et_count.setText(count.toString())
            et_count.setSelection(et_count.textStr.length)

            allprice = Utils.stringAdd(coin.toString(),zxPrice)
            paymentPrice.text = "￥${allprice}"
            prepareValue(allprice,perBalance,busBalance)
        }
        tv_add.setOnClickListener {
            //增加舆情数量
            count++
            coin = Utils.reserveTwoDecimal(9.9 * count, 2)
            et_count.setText(count.toString())
            et_count.setSelection(et_count.textStr.length)

            allprice = Utils.stringAdd(coin.toString(),zxPrice)
            paymentPrice.text = "￥${allprice}"
            prepareValue(allprice,perBalance,busBalance)
        }

        et_count.textChangedListener {
            afterTextChanged {
                if (et_count.textStr.isNullOrEmpty()) {
                    showCommonToast("购买数量不能为空")
                    et_count.setText("1")
                    return@afterTextChanged
                }
                if (et_count.textStr.startsWith("0")) {
                    showCommonToast("购买数量最少为一个")
                    et_count.setText("1")
                }
                et_count.setSelection(et_count.textStr.length)
                count = et_count.textStr.toInt()
                coin = Utils.reserveTwoDecimal(9.9 * count, 2)

                allprice = Utils.stringAdd(coin.toString(),zxPrice)
                paymentPrice.text = "￥${allprice}"
                prepareValue(allprice,perBalance,busBalance)
            }
        }

        rl_bus.clickWithTrigger {
            //企业账户
            if (isClickBus) {
                if (!isCheckBus) {
                    isCheckBus = !isCheckBus
                    iv_bus_select.setImageResource(R.mipmap.ic_unselect_receipt)
                    if (isClickPer) {
                        iv_pre_select.setImageResource(R.mipmap.ic_select_receipt)
                        isCheckPer = false
                    }
                    iv_wx_select.setImageResource(R.mipmap.ic_select_receipt)
                    iv_zfb_select.setImageResource(R.mipmap.ic_select_receipt)
                    isCheckWx = false
                    isCheckZfb = false
                }
            }
        }

        rl_pre.clickWithTrigger {
            //个人账户
            if (isClickPer) {
                if (!isCheckPer) {
                    isCheckPer = !isCheckPer
                    iv_pre_select.setImageResource(R.mipmap.ic_unselect_receipt)
                    if (isClickBus) {
                        iv_bus_select.setImageResource(R.mipmap.ic_select_receipt)
                        isCheckBus = false
                    }
                    iv_wx_select.setImageResource(R.mipmap.ic_select_receipt)
                    iv_zfb_select.setImageResource(R.mipmap.ic_select_receipt)
                    isCheckWx = false
                    isCheckZfb = false
                }
            }
        }

        rl_wx.clickWithTrigger {
            //微信
            if (!isCheckWx) {
                isCheckWx = !isCheckWx
                iv_wx_select.setImageResource(R.mipmap.ic_unselect_receipt)
                iv_zfb_select.setImageResource(R.mipmap.ic_select_receipt)
                if (isClickBus) {
                    iv_bus_select.setImageResource(R.mipmap.ic_select_receipt)
                    isCheckBus = false
                }
                if (isClickPer) {
                    iv_pre_select.setImageResource(R.mipmap.ic_select_receipt)
                    isCheckPer = false
                }
                isCheckZfb = false
            }
        }

        rl_zfb.clickWithTrigger {
            //支付宝
            if (!isCheckZfb) {
                isCheckZfb = !isCheckZfb
                iv_wx_select.setImageResource(R.mipmap.ic_select_receipt)
                iv_zfb_select.setImageResource(R.mipmap.ic_unselect_receipt)
                if (isClickBus) {
                    iv_bus_select.setImageResource(R.mipmap.ic_select_receipt)
                    isCheckBus = false
                }
                if (isClickPer) {
                    iv_pre_select.setImageResource(R.mipmap.ic_select_receipt)
                    isCheckPer = false
                }
                isCheckWx = false
            }
        }
    }

    private fun prepareValue(allprice:String,perBalance:String,busBalance:String){
        if (allprice.toDouble() > perBalance.toDouble()) {
            iv_pre_select.setImageResource(R.mipmap.ic_gray_receipt)
            tv_per_title.setTextColor(resources.getColor(R.color.gray_a0))
            tv_per_balance.setTextColor(resources.getColor(R.color.gray_a0))
            isClickPer = false
        }else{
            iv_pre_select.setImageResource(R.mipmap.ic_select_receipt)
            tv_per_title.setTextColor(resources.getColor(R.color.black_28))
            tv_per_balance.setTextColor(resources.getColor(R.color.gray_80))
            isClickPer = true
        }

        if (allprice.toDouble() > busBalance.toDouble()) {
            iv_bus_select.setImageResource(R.mipmap.ic_gray_receipt)
            tv_bus_title.setTextColor(resources.getColor(R.color.gray_a0))
            tv_bus_balance.setTextColor(resources.getColor(R.color.gray_a0))
            isClickBus = false
        }else{
            iv_bus_select.setImageResource(R.mipmap.ic_select_receipt)
            tv_bus_title.setTextColor(resources.getColor(R.color.black_28))
            tv_bus_balance.setTextColor(resources.getColor(R.color.gray_80))
            isClickBus = true
        }
    }

    private fun getPerAccountInfo(){
        SoguApi.getStaticHttp(application)
                .getPersonAccountInfo()
                .execute {
                    onNext { payload ->
                        if (payload.isOk){
                            val recordBean = payload.payload
                            recordBean?.let {
                                perBalance = recordBean.balance
                                setPerValueStatus(it)
                                prepareValue(allprice,perBalance,busBalance)
                            }
                        }else{
                            showErrorToast(payload.message)
                        }
                    }
                    onError {
                        it.printStackTrace()
                        showErrorToast("获取个人账号信息失败")
                    }
                }
    }

    private fun getBusAccountInfo() {
        SoguApi.getStaticHttp(application)
                .getBussAccountInfo()
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            val recordBean = payload.payload
                            recordBean?.let {
                                busBalance = recordBean.balance
                                setBussValueStatus(it)
                                prepareValue(allprice,perBalance,busBalance)
                            }
                        } else {
                            showErrorToast(payload.message)
                        }
                        onError {
                            it.printStackTrace()
                            showErrorToast("获取企业账号信息失败")
                        }
                    }
                }
    }

    private fun setPerValueStatus(recordBean : RechargeRecordBean){
            tv_per_balance.text = "账户余额：${Utils.reserveDecimal(recordBean.balance.toDouble())}"
        if (recordBean.balance.toFloat() <= 0 || recordBean.balance.equals("")) {
            iv_pre_select.setImageResource(R.mipmap.ic_gray_receipt)
            tv_per_title.setTextColor(resources.getColor(R.color.gray_a0))
            tv_per_balance.setTextColor(resources.getColor(R.color.gray_a0))
            isClickPer = false
        } else {
            iv_pre_select.setImageResource(R.mipmap.ic_select_receipt)
            tv_per_title.setTextColor(resources.getColor(R.color.black_28))
            tv_per_balance.setTextColor(resources.getColor(R.color.gray_80))
            isClickPer = true
        }
    }

    private fun setBussValueStatus(recordBean : RechargeRecordBean){
           tv_bus_balance.text = "账户余额：${Utils.reserveDecimal(recordBean.balance.toDouble())}"
        if (recordBean.balance.toFloat() <= 0 || recordBean.balance.equals("")) {
            iv_bus_select.setImageResource(R.mipmap.ic_gray_receipt)
            tv_bus_title.setTextColor(resources.getColor(R.color.gray_a0))
            tv_bus_balance.setTextColor(resources.getColor(R.color.gray_a0))
            isClickBus = false
        } else {
            iv_bus_select.setImageResource(R.mipmap.ic_select_receipt)
            tv_bus_title.setTextColor(resources.getColor(R.color.black_28))
            tv_bus_balance.setTextColor(resources.getColor(R.color.gray_80))
            isClickBus = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        RxBus.getIntanceBus().unSubscribe("RxBus")
    }


    private var disInfo: List<Discount>? = null

    private fun getPayPackageInfo() {
        SoguApi.getStaticHttp(application).getPayType()
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            payload.payload?.apply {
                                forEach {
                                    when (it.type) {
                                        1 -> {
                                            pjAdapter.dataList.addAll(it.list)
                                            projectTotal.text = "项目共${it.max}个"
                                            projectOver.text = "剩余${it.max - it.used}个"
                                            pjAdapter.mode = it.type
                                            disInfo = it.discountInfo
                                            pjAdapter.notifyDataSetChanged()
                                            setScribeInfos(it)
                                        }
                                        2 -> {
                                            calenderAdapter.dataList.addAll(it.list)
                                            creditTotal.text = "征信共${it.max}次"
                                            creditOver.text = "剩余${it.max - it.used}次"
                                            calenderAdapter.mode = it.type
                                            calenderAdapter.notifyDataSetChanged()
                                        }
                                    }
                                }
                            }
                        } else {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }

    private fun setScribeInfos(it: PackageBean) {
        tv_phone.text = it.tel
        if (null != it.wechat) {
            tv_wx.text = it.wechat
        }

        if (null != it.email) {
            tv_email.text = it.email
        }
    }

    private fun getPayInfo() {
        SoguApi.getService(application, OtherService::class.java).getPayInfo(PayReq(Extras.PAY_PUBLIC_KEY, payReqChild))
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            payload.payload?.let {
                                info { it }
                                aliPay(it)
                            }
                        } else {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun aliPay(commodityInfo: String) {
        Observable.create<Map<String, String>> { e ->
            val payTask = PayTask(this)
            val result = payTask.payV2(commodityInfo, true)
            e.onNext(result)
        }.execute {
            onNext { result ->
                result.forEach { t, u ->
                    info { "key:$t ==> value:$u \n" }
                }
                val payResult = Gson().fromJson<PayResult?>(result.jsonStr)
                if (payResult != null) {
                    when (payResult.resultStatus) {
                        "9000" -> {
                            showSuccessToast("支付成功")
                            getPayPackageInfo()
                        }
                        else -> {
                            showErrorToast(payResult.memo)
                        }
                    }
                } else {
                    showErrorToast("请求出错")
                }
            }
        }
    }

    inner class ExpansionHolder(itemView: View, val adapter: RecyclerAdapter<PackageChild>) : RecyclerHolder<PackageChild>(itemView) {
        @SuppressLint("SetTextI18n")
        override fun setData(view: View, data: PackageChild, position: Int) {
            val isSelected = adapter.selectedPosition == position
            view.itemLayout.isSelected = isSelected
            when (adapter.mode) {
                1 -> {
                    view.addNumberTv.text = "增加${data.quantity}个项目额度"
                    disInfo?.let {
                        view.payPrice.text = "￥${(data.price - it[0].rates).toMoney()}"
                    }
                    view.originalPrice.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG
                    view.originalPrice.setVisible(false)
                    view.offerLists.setVisible(isSelected)
                    view.offerLists.apply {
                        layoutManager = GridLayoutManager(ctx, 3)
                        val disAdapter = RecyclerAdapter<Discount>(ctx) { _adapter, parent, _ ->
                            val itemView = _adapter.getView(R.layout.item_pay_discount, parent)
                            object : RecyclerHolder<Discount>(itemView) {
                                override fun setData(view: View, discount: Discount, position: Int) {
                                    view.isSelected = _adapter.selectedPosition == position
                                    val dis = if (discount.discount != 10) "(${discount.discount}折)" else ""
                                    view.yearTv.text = "${discount.period}年$dis"
                                    disInfo?.let {
                                        view.recommend.setVisible(position == it.size - 1)
                                    }
                                }
                            }
                        }
                        disAdapter.onItemClick = { v, position ->
                            disAdapter.selectedPosition = position
                            view.originalPrice.setVisible(position != 0)
                            disInfo?.let {
                                val dis = it[position]
                                payReqChild.period = dis.period
                                val pjPrice = data.price.times(dis.period)
                                product = product.copy(discountPrice = product.discountPrice,
                                        OriginalPrice = pjPrice,
                                        calenderPrice = product.calenderPrice)
                                if (dis.discount != 10) {
                                    val actualPrice = pjPrice.times(dis.discount).div(10) - dis.rates
                                    view.payPrice.text = "￥${actualPrice.toMoney()}"
                                    view.originalPrice.text = "￥${pjPrice.toMoney()}"
                                    product.discountPrice = actualPrice
                                    product = product.copy(discountPrice = actualPrice,
                                            OriginalPrice = product.OriginalPrice,
                                            calenderPrice = product.calenderPrice)
                                } else {
                                    val actualPrice = pjPrice - dis.rates
                                    view.payPrice.text = "￥${actualPrice.toMoney()}"
                                    product.discountPrice = actualPrice
                                    product = product.copy(discountPrice = actualPrice,
                                            OriginalPrice = product.OriginalPrice,
                                            calenderPrice = product.calenderPrice)
                                }
                            }
                        }
                        disInfo?.let {
                            disAdapter.dataList.addAll(it)
                        }
                        if (isSelected) {
                            disAdapter.selectedPosition = 0
                        } else {
                            disAdapter.selectedPosition = -1
                        }
                        adapter = disAdapter
                    }
                    view.clickWithTrigger {
                        pjAdapter.selectedPosition = position
                        payReqChild.quantity = data.quantity
                        disInfo?.let {
                            val dis = it[0]
                            payReqChild.period = dis.period
                            val pjPrice = data.price.times(dis.period)
                            product = product.copy(discountPrice = product.discountPrice,
                                    OriginalPrice = pjPrice,
                                    calenderPrice = product.calenderPrice)
                            if (dis.discount != 10) {
                                val actualPrice = pjPrice.times(dis.discount).div(10) - dis.rates
                                view.payPrice.text = "￥${actualPrice.toMoney()}"
                                view.originalPrice.text = "￥${pjPrice.toMoney()}"
                                product.discountPrice = actualPrice
                                product = product.copy(discountPrice = actualPrice,
                                        OriginalPrice = product.OriginalPrice,
                                        calenderPrice = product.calenderPrice)
                            } else {
                                val actualPrice = pjPrice - dis.rates
                                view.payPrice.text = "￥${actualPrice.toMoney()}"
                                product.discountPrice = actualPrice
                                product = product.copy(discountPrice = actualPrice,
                                        OriginalPrice = product.OriginalPrice,
                                        calenderPrice = product.calenderPrice)
                            }
                        }
                    }
                }
                2 -> {
                    view.addNumberTv.text = "增加${data.quantity}次征信"
                    view.payPrice.text = "￥${data.price.toMoney()}"
                    view.offerLists.setVisible(false)
                }
            }
        }
    }
}
