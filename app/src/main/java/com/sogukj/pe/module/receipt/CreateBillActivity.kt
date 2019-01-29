package com.sogukj.pe.module.receipt

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_create_bill.*

/**
 * Created by CH-ZH on 2018/10/18.
 * 开发票
 */
class CreateBillActivity : ToolbarActivity() {
    private var fragments = ArrayList<Fragment>()
    private var money = "0.00"
    private var orders: MutableList<String>? = null
    companion object {
        var currentType = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_bill)
        Utils.setWindowStatusBarColor(this, R.color.white)
        toolbar?.setBackgroundColor(resources.getColor(R.color.white))
        initView()
        initData()
        bindListener()

    }

    private fun initView() {
        money = intent.getStringExtra(Extras.DATA)
        orders = intent.getStringArrayListExtra(Extras.LIST)
        setBack(true)
        setTitle("开具纸质发票")
        fragments.add(ElectronBillFragment.newInstance(1, money, ll_submit, tv_submit))
        fragments.add(ElectronBillFragment.newInstance(2, money, ll_submit, tv_submit))
        radio_group.check(R.id.rb_one)
        view_electron.clickWithTrigger {
            showCommonToast("电子发票暂未开通")
        }
    }

    open fun getOrders(): MutableList<String>? {
        return orders
    }

    private fun initData() {
        getInvoiceMini()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fl_container, fragments[0]).add(R.id.fl_container, fragments[1]).commit()

        checkFragment(R.id.rb_one)
    }

    private fun bindListener() {
        radio_group.setOnCheckedChangeListener { group, checkedId ->
            checkFragment(checkedId)
        }

    }

    private fun getInvoiceMini() {
        SoguApi.getStaticHttp(application)
                .getInvoiceMini()
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                val mini = it["invoiceMini"]?.toDouble() ?: 0.0
                                if (mini < money.toDouble()) {
                                    tipTv.setVisible(false)
                                    rb_two.isEnabled = true
                                } else {
                                    tipTv.setVisible(true)
                                    tipTv.text = "发票金额不足${mini}元，不能开纸质发票"
                                    rb_two.isEnabled = false
                                }
                            }
                        }
                    }
                }
    }


    private fun checkFragment(checkedId: Int) {
        val transaction = supportFragmentManager.beginTransaction()
        fragments.forEach {
            transaction.hide(it)
        }
        when (checkedId) {
            R.id.rb_one -> {
                currentType = 1
                transaction.show(fragments[0])
                title = "开具电子发票"
            }
            R.id.rb_two -> {
                currentType = 2
                transaction.show(fragments[1])
                title = "开具纸质发票"
            }
        }
        transaction.commit()
    }

}