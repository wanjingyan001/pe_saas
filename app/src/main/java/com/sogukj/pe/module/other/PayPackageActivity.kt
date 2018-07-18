package com.sogukj.pe.module.other

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.widget.TextView
import com.alipay.sdk.app.PayTask
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.StatusBarUtil
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.PackageBean
import com.sogukj.pe.bean.PartyTabBean
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.activity_pay_package.*
import org.jetbrains.anko.find

class PayPackageActivity : BaseActivity() {
    lateinit var mAdapter: PayPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_package)
        Utils.setWindowStatusBarColor(this,R.color.approve_blue)
        getData()
    }


    private fun getData() {
        SoguApi.getService(application, OtherService::class.java).getPayType()
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            payload.payload?.let {
                                initPager(it)
                            }
                        } else {
                            showErrorToast(payload.message)
                        }
                    }
                }

    }

    private fun initPager(data: List<PackageBean>) {
        mAdapter = PayPagerAdapter(supportFragmentManager, data)
        payContentPager.adapter = mAdapter
        tabLayout.setViewPager(payContentPager)
        tabLayout.setTabViewFactory { parent, _ ->
            parent.removeAllViews()
            for (i in 0 until data.size) {
                val view = LayoutInflater.from(this).inflate(R.layout.item_pay_indicator, parent, false)
                view.find<TextView>(R.id.indicatorTv).text = data[i].mealName
                parent.addView(view)
            }
        }
    }

    inner class PayPagerAdapter(fm: FragmentManager?, val data: List<PackageBean>) : FragmentPagerAdapter(fm) {
        override fun getCount(): Int = data.size

        override fun getItem(position: Int): Fragment {
            val bean = data[position]
            return PackageFragment.newInstance(bean)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return data[position].mealName
        }

    }

}
