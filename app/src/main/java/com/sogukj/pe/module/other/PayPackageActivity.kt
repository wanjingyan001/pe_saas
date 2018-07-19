package com.sogukj.pe.module.other

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.alipay.sdk.app.PayTask
import com.google.gson.Gson
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.execute
import com.sogukj.pe.baselibrary.Extended.fromJson
import com.sogukj.pe.baselibrary.Extended.jsonStr
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.utils.StatusBarUtil
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.PackageBean
import com.sogukj.pe.bean.PartyTabBean
import com.sogukj.pe.bean.PayResult
import com.sogukj.pe.service.OtherService
import com.sogukj.service.SoguApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_pay_package.*
import org.jetbrains.anko.find
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.onPageChangeListener
import kotlin.properties.Delegates

class PayPackageActivity : BaseActivity() {
    private lateinit var mAdapter: PayPagerAdapter
    var sId: Int by Delegates.observable(0, { _, _, newValue ->
        aliPayBtn.isEnabled = newValue != 0
    })
    private lateinit var packageBean: PackageBean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_package)
        Utils.setWindowStatusBarColor(this, R.color.main_bottom_bar_color)
        getData()
        aliPayBtn.clickWithTrigger {
            getPayInfo(sId)
        }
        toolbar_back.clickWithTrigger {
            finish()
        }
    }


    private fun getData() {
        SoguApi.getService(application, OtherService::class.java).getPayType()
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            payload.payload?.let {
                                initPager(it)
                                val bean = it.find { it.mealName == "项目套餐" }
                                bean?.let {
                                    packageBean = it
                                    payPackageName.text = "项目·扩容套餐"
                                    if (it.max == 0) {
                                        currentTotal.text = "无限个项目"
                                    } else{
                                        currentTotal.text = "${it.max}个项目"
                                    }
                                    currentlyUsed.text = "已使用${it.used}个"
                                }
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
        payContentPager.onPageChangeListener {
            onPageSelected { i ->
                supportFragmentManager.fragments.forEach {
                    val fragment = it as PackageFragment
                    fragment.payAdapter.selectedPosition = -1

                    packageBean = mAdapter.data[i]
                    if (packageBean.mealName == "项目套餐") {
                        payPackageName.text = "项目·扩容套餐"
                        if (packageBean.max == 0) {
                            currentTotal.text = "无限个项目"
                        } else{
                            currentTotal.text = "${packageBean.max}个项目"
                        }
                        currentlyUsed.text = "已使用${packageBean.used}个"
                    } else if (packageBean.mealName == "征信套餐") {
                        payPackageName.text = "征信·扩容套餐"
                        currentTotal.text = "${packageBean.max}次征信"
                        currentlyUsed.text = "已使用${packageBean.used}次"
                    }
                }
            }
        }
    }


    private fun getPayInfo(id: Int) {
        SoguApi.getService(application, OtherService::class.java).getPayInfo(Extras.PAY_PUBLIC_KEY, id)
                .execute {
                    onNext { payload ->
                        if (payload.isOk) {
                            info { payload.payload }
                            payload.payload?.let {
                                aliPay(it)
                            }
                        } else {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }


    private fun aliPay(commodityInfo: String) {
        Observable.create<Map<String, String>> { e ->
            val payTask = PayTask(this)
            val result = payTask.payV2(commodityInfo, true)
            e.onNext(result)
        }.execute {
            onNext { result ->
                info { result.jsonStr }
                val payResult = Gson().fromJson<PayResult?>(result.jsonStr)
                if (payResult != null) {
                    when (payResult.resultStatus) {
                        "9000" -> {
                            showSuccessToast("支付成功")

                        }
                        else -> {
                            showErrorToast(payResult.memo)

                            val typeAdapter = mAdapter.currentFragment.payAdapter
                            startActivity<PayResultActivity>(Extras.DATA to packageBean,
                                    Extras.BEAN to typeAdapter.dataList[typeAdapter.selectedPosition])
                            finish()
                        }
                    }
                } else {
                    showErrorToast("请求出错")
                }
            }
        }
    }

    inner class PayPagerAdapter(fm: FragmentManager?, val data: List<PackageBean>) : FragmentPagerAdapter(fm) {
        lateinit var currentFragment: PackageFragment
        override fun getCount(): Int = data.size

        override fun getItem(position: Int): Fragment {
            val bean = data[position]
            return PackageFragment.newInstance(bean)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return data[position].mealName
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
            currentFragment = obj as PackageFragment
            super.setPrimaryItem(container, position, obj)
        }
    }
}
