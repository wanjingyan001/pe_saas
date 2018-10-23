package com.sogukj.pe.module.approve.baseView.controlView

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.google.gson.internal.LinkedTreeMap
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.module.approve.MyHolidayActivity
import com.sogukj.pe.module.approve.baseView.BaseControl
import com.sogukj.pe.module.approve.baseView.ControlFactory
import com.sogukj.pe.module.approve.baseView.viewBean.ApproveValueBean
import com.sogukj.pe.module.approve.baseView.viewBean.MyLeaveBean
import com.sogukj.pe.service.ApproveService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.layout_control_leave.view.*
import org.jetbrains.anko.dip

/**
 * 请假套件
 * Created by admin on 2018/10/10.
 */
class LeaveControl @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {
    private lateinit var holiday: MyLeaveBean

    override fun getContentResId(): Int = R.layout.layout_control_leave

    private lateinit var dateRange: DateRangeControl

    override fun bindContentView() {
        hasInit.yes {
            inflate.star.setVisible(isMust)
            /**
             * 基础控件初始化
             */
            controlBean.children?.let {
                inflate.leaveTitle.text = it[0].name
                inflate.leaveTypeTitle.text = it[1].name
                it[1].value?.let { values ->
                    values.isNotEmpty().yes {
                        val beans = mutableListOf<ApproveValueBean>()
                        values.forEach { map ->
                            val treeMap = map as LinkedTreeMap<String, Any>
                            beans.add(ApproveValueBean(name = treeMap["name"] as String))
                        }
                        it[1].value?.clear()
                        it[1].value?.addAll(beans)
                        inflate.leaveTypeTv.text = beans[0].name
                        inflate.leaveTypeTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    }.otherWise {
                        inflate.leaveTypeTv.hint = it[1].placeholder
                        inflate.leaveTypeTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right, 0)
                    }
                }
                inflate.leaveTypeTv.clickWithTrigger {
                    getHolidaysList()
                }
                initJumpLink(inflate.myLeave, it[2].name ?: "", it[2].linkText ?: "")
                inflate.leaveTip.text = it[4].name
                /**
                 * 时间选择初始化
                 */
                dateRange = ControlFactory(activity).createControl(DateRangeControl::class.java, it[3]) as DateRangeControl
                dateRange.needAssociate = 1
                val view = View(activity)
                val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dip(10))
                view.layoutParams = lp
                inflate.timeSelection.addView(view)
                inflate.timeSelection.addView(dateRange)
            }
        }
    }



    private fun getHolidaysList() {
        SoguApi.getService(activity.application, ApproveService::class.java)
                .holidaysList()
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let {
                                MaterialDialog.Builder(activity)
                                        .theme(Theme.LIGHT)
                                        .items(it.map {
                                            "${it.name}(" + when (it.status) {
                                                0 -> "已申请"
                                                else -> "剩余"
                                            } + "${it.hours}小时)"
                                        })
                                        .itemsCallbackSingleChoice(it.map { it.name }.indexOf(inflate.leaveTypeTv.text)) { dialog, _, which, _ ->
                                            holiday = it[which]
                                            dateRange.holiday = holiday
                                            inflate.leaveTypeTv.text = it.map { it.name }[which]
                                            controlBean.children!![1].value?.clear()
                                            controlBean.children!![1].value?.add(ApproveValueBean(name = it[which].name,id = it[which].id.toString()))
                                            dialog.dismiss()
                                            true
                                        }
                                        .build().show()

                            }
                        }.otherWise {
                            showErrorToast(payload.message)
                        }
                    }
                }
    }

    private fun initJumpLink(tv: TextView, name: String, linkText: String) {
        val spa = SpannableString(name + linkText)
        spa.setSpan(ClickSpann(activity), name.length, name.length + linkText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tv.text = spa
        tv.movementMethod = LinkMovementMethod.getInstance()
    }

    inner class ClickSpann(val context: Context) : ClickableSpan() {
        override fun onClick(widget: View) {
            MyHolidayActivity.start(activity)
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = context.resources.getColor(R.color.colorPrimary)
            ds.isUnderlineText = false
        }
    }
}