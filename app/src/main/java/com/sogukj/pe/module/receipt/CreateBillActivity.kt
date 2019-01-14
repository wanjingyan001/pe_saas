package com.sogukj.pe.module.receipt

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Utils
import kotlinx.android.synthetic.main.activity_create_bill.*

/**
 * Created by CH-ZH on 2018/10/18.
 * 开发票
 */
class CreateBillActivity : ToolbarActivity() {
    private var fragments = ArrayList<Fragment>()
    private var money = "0.00"
    private var orders : MutableList<String> ? = null
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
        fragments.add(ElectronBillFragment.newInstance(1,money,ll_submit,tv_submit))
        fragments.add(ElectronBillFragment.newInstance(2,money,ll_submit,tv_submit))
        radio_group.check(R.id.rb_two)

        view_electron.clickWithTrigger {
            showCommonToast("电子发票暂未开通")
        }
    }

    open fun getOrders(): MutableList<String>? {
        return orders
    }
    private fun initData() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fl_container,fragments[0]).add(R.id.fl_container,fragments[1]).commit()
    }

    private fun bindListener() {
        radio_group.setOnCheckedChangeListener { group, checkedId ->
//            checkFragment(checkedId)
        }

    }


    private fun checkFragment(checkedId: Int) {
        val transaction = supportFragmentManager.beginTransaction()
        when(checkedId){
            R.id.rb_one -> {
                transaction.show(fragments[0])
                        .hide(fragments[1])
                setTitle("开具电子发票")
            }
            R.id.rb_two -> {
                transaction.show(fragments[1])
                        .hide(fragments[0])
                setTitle("开具纸质发票")
            }
        }
        transaction.commit()
    }

}