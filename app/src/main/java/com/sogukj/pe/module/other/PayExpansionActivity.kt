package com.sogukj.pe.module.other

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
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
import org.jetbrains.anko.ctx
import org.jetbrains.anko.dip
import org.jetbrains.anko.info
import kotlin.properties.Delegates

@SuppressLint("SetTextI18n")
class PayExpansionActivity : BaseActivity() {
    private lateinit var pjAdapter: RecyclerAdapter<PackageChild>
    private lateinit var calenderAdapter: RecyclerAdapter<PackageChild>
    private var product: ProductInfo by Delegates.observable(ProductInfo(), { property, oldValue, newValue ->
        savingAmount.setVisible(newValue.discountPrice != newValue.OriginalPrice)
        savingAmount.text = "立省￥${(oldValue.OriginalPrice - newValue.discountPrice).toMoney()}"
        paymentPrice.text = "￥${(newValue.discountPrice + newValue.calenderPrice).toMoney()}"
    })
    private val payReqChild = PayReqChid()


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

    }

    override fun onDestroy() {
        super.onDestroy()
        RxBus.getIntanceBus().unSubscribe("RxBus")
    }


    private var disInfo: List<Discount>? = null

    private fun getPayPackageInfo() {
        SoguApi.getService(application, OtherService::class.java).getPayType()
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
