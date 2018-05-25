package com.sogukj.pe.module.project.businessBg

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.ToolbarActivity
import com.sogukj.pe.baselibrary.utils.Trace
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.baselibrary.widgets.ListAdapter
import com.sogukj.pe.baselibrary.widgets.ListHolder
import com.sogukj.pe.bean.*
import com.sogukj.pe.service.InfoService
import com.sogukj.service.SoguApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_project_annual_report.*
import org.jetbrains.anko.find
/**
 * Created by qinfei on 17/8/11.
 */
open class QiYeLianBaoActivity : ToolbarActivity() {

    val adapterSelector = ListAdapter {
        object : ListHolder<AnnualReportBean> {
            lateinit var text: TextView
            override fun createView(inflater: LayoutInflater): View {
                text = inflater.inflate(R.layout.item_textview_selector, null) as TextView
                return text
            }

            override fun showData(convertView: View, position: Int, data: AnnualReportBean?) {
                text.text = data?.reportYear
                if (position != selectedIndex)
                    text.setTextColor(resources.getColor(R.color.colorBlue))
                else
                    text.setTextColor(resources.getColor(R.color.text_2))
            }

        }
    }

    val adapterChangeRecord = ListAdapter {
        object : ListHolder<ChangeRecordBean> {
            lateinit var tvName: TextView
            lateinit var tvTime: TextView
            lateinit var tvBefore: TextView
            lateinit var tvAfter: TextView

            override fun createView(inflater: LayoutInflater): View {
                val convertView = inflater.inflate(R.layout.item_project_change_record, null)
                tvName = convertView.findViewById<TextView>(R.id.tv_name) as TextView
                tvTime = convertView.findViewById<TextView>(R.id.tv_time) as TextView
                tvBefore = convertView.findViewById<TextView>(R.id.tv_before) as TextView
                tvAfter = convertView.findViewById<TextView>(R.id.tv_after) as TextView
                return convertView
            }

            override fun showData(convertView: View, position: Int, data: ChangeRecordBean?) {
                tvName.text = data?.changeItem
                tvTime.text = data?.changeTime
                tvBefore.text = data?.contentBefore
                tvAfter.text = data?.contentAfter
            }

        }
    }
    val adapterInvestment = ListAdapter {
        object : ListHolder<InvestmentBean> {
            lateinit var tvName: TextView
            lateinit var tvRegNum: TextView
            lateinit var tvCreditCode: TextView

            override fun createView(inflater: LayoutInflater): View {
                val convertView = inflater.inflate(R.layout.item_project_report_investment, null)

                tvName = convertView.findViewById<TextView>(R.id.tv_name) as TextView
                tvRegNum = convertView.findViewById<TextView>(R.id.tv_regNum) as TextView
                tvCreditCode = convertView.findViewById<TextView>(R.id.tv_creditCode) as TextView

                return convertView
            }

            override fun showData(convertView: View, position: Int, data: InvestmentBean?) {
                tvName.text = data?.outcompanyName
                tvRegNum.text = data?.regNum
                tvCreditCode.text = data?.creditCode
            }

        }
    }
    val adapterGuarentee = ListAdapter {
        object : ListHolder<ChangeRecordBean> {

            override fun createView(inflater: LayoutInflater): View {
                return inflater.inflate(R.layout.item_project_change_record, null)
            }

            override fun showData(convertView: View, position: Int, data: ChangeRecordBean?) {
            }

        }
    }
    val adapterShareHolders = ListAdapter {
        object : ListHolder<ShareHolderBean> {
            lateinit var tvName: TextView
            lateinit var tvSubscribeAmount: TextView
            lateinit var tvSubscribeTime: TextView
            lateinit var tvSubscribeType: TextView
            lateinit var tvPaidAmount: TextView
            lateinit var tvPaidTime: TextView
            lateinit var tvPaidType: TextView

            override fun createView(inflater: LayoutInflater): View {
                val convertView = inflater.inflate(R.layout.item_project_report_shareholder, null)
                tvName = convertView.findViewById<TextView>(R.id.tv_name) as TextView
                tvSubscribeAmount = convertView.findViewById<TextView>(R.id.tv_subscribeAmount) as TextView
                tvSubscribeTime = convertView.findViewById<TextView>(R.id.tv_subscribeTime) as TextView
                tvSubscribeType = convertView.findViewById<TextView>(R.id.tv_subscribeType) as TextView
                tvPaidAmount = convertView.findViewById<TextView>(R.id.tv_paidAmount) as TextView
                tvPaidTime = convertView.findViewById<TextView>(R.id.tv_paidTime) as TextView
                tvPaidType = convertView.findViewById<TextView>(R.id.tv_paidType) as TextView

                return convertView
            }

            override fun showData(convertView: View, position: Int, data: ShareHolderBean?) {
                tvName.text = data?.investorName
                tvSubscribeAmount.text = data?.subscribeAmount
                tvSubscribeTime.text = data?.subscribeTime
                tvSubscribeType.text = data?.subscribeType
                tvPaidAmount.text = data?.paidAmount
                tvPaidTime.text = data?.paidTime
                tvPaidType.text = data?.paidType
            }

        }
    }

    lateinit var project: ProjectBean
    var selectedIndex = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_annual_report)

        project = intent.getSerializableExtra(Extras.DATA) as ProjectBean
        setBack(true)
        title = "企业年报"
        tv_name.text = project.name
        lv_change_record.adapter = adapterChangeRecord
        lv_investment.adapter = adapterInvestment
        lv_guarantee_info.adapter = adapterGuarentee
        lv_shareholders.adapter = adapterShareHolders


        tv_previous.setOnClickListener {
            setGroup(selectedIndex - 1)
        }
        tv_next.setOnClickListener {
            setGroup(selectedIndex + 1)
        }
        tv_select.setOnClickListener { v ->
            showDropdown()
        }
        handler.postDelayed({
            doRequest()
        }, 100)
    }

    internal var popSelector: PopupWindow? = null
    fun showDropdown() {
        if (null == popSelector) {
            val popupView = View.inflate(this, R.layout.drop_down_selector, null)
            val lv_dropdown = popupView.find<ListView>(R.id.lv_dropdown)
            popSelector = PopupWindow(popupView, WindowManager.LayoutParams.MATCH_PARENT, Utils.dpToPx(this,160), true)
            popSelector?.isTouchable = true
            popSelector?.isFocusable = true
            popSelector?.isOutsideTouchable = true
            popSelector?.setBackgroundDrawable(BitmapDrawable(resources, null as Bitmap?))

            lv_dropdown.adapter = adapterSelector
            lv_dropdown.setOnItemClickListener { parent, view, position, id ->
                val group = adapterSelector.dataList[position]
                group.apply { setGroup(position) }
                tv_select.isChecked = false
                popSelector?.dismiss()
            }
        }
        val location = IntArray(2)
        fl_content.getLocationInWindow(location)
        val x = location[0]
        val y = location[1]
        popSelector?.showAtLocation(fl_content, Gravity.NO_GRAVITY, location[0], location[1])
    }

    fun setGroup(index: Int = 0) {
        tv_previous.isEnabled = false
        tv_next.isEnabled = false
        val size = adapterSelector.dataList.size
        if (size <= 0) return
        if (index < 0) return
        val group = adapterSelector.dataList[index]
        selectedIndex = index
        if (size > 0 && selectedIndex < size - 1)
            tv_next.isEnabled = true
        if (selectedIndex > 0)
            tv_previous.isEnabled = true
        adapterSelector.notifyDataSetChanged()
        group.apply {
            tv_select.text = reportYear
            tv_regNumber.text = regNumber
            tv_manageState.text = manageState
            tv_employeeNum.text = employeeNum
            tv_phoneNumber.text = phoneNumber
            tv_email.text = email
            tv_postcode.text = postcode
            tv_postalAddress.text = postalAddress
            tv_totalAssets.text = totalAssets
            tv_totalEquity.text = totalEquity
            tv_totalSales.text = totalSales
            tv_totalProfit.text = totalProfit
            tv_primeBusProfit.text = primeBusProfit
            tv_totalTax.text = totalTax
            tv_totalLiability.text = totalLiability
            tv_have_onlineStore.text = have_onlineStore
            tv_have_boundInvest.text = have_boundInvest

            webInfoList?.firstOrNull()?.apply {
                tv_website_name.text = name
                tv_webType.text = webType
                tv_website.text = website
            }

            changeRecordList?.apply {
                adapterChangeRecord.dataList.clear()
                adapterChangeRecord.dataList.addAll(this)
                adapterChangeRecord.notifyDataSetChanged()
            }

            shareholderList?.apply {
                adapterShareHolders.dataList.clear()
                adapterShareHolders.dataList.addAll(this)
                adapterShareHolders.notifyDataSetChanged()
            }

            outboundInvestmentList?.apply {
                adapterInvestment.dataList.clear()
                adapterInvestment.dataList.addAll(this)
                adapterInvestment.notifyDataSetChanged()
            }

        }
    }

    fun doRequest() {
        SoguApi.getService(application,InfoService::class.java)
                .listAnnualReport(project.company_id!!, page = 1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ payload ->
                    adapterSelector.dataList.clear()
                    if (payload.isOk) {
                        payload.payload?.apply {
                            adapterSelector.dataList.clear()
                            adapterSelector.dataList.addAll(this)
                        }
                    } else
                        showCustomToast(R.drawable.icon_toast_fail, payload.message)
                    setGroup(0)
                    adapterSelector.notifyDataSetChanged()
                }, { e ->
                    Trace.e(e)
                })
    }

    companion object {
        fun start(ctx: Activity?, project: ProjectBean) {
            val intent = Intent(ctx, QiYeLianBaoActivity::class.java)
            intent.putExtra(Extras.DATA, project)
            ctx?.startActivity(intent)
        }
    }
}
