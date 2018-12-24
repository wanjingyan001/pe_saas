package com.sogukj.pe.module.approve.baseView.controlView

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import com.amap.api.mapcore.util.it
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.module.approve.baseView.BaseControl
import com.sogukj.pe.module.approve.baseView.viewBean.ApproveValueBean
import com.sogukj.pe.service.ApproveService
import com.sogukj.service.SoguApi
import kotlinx.android.synthetic.main.layout_control_date_range.view.*
import org.jetbrains.anko.find
import kotlin.properties.Delegates

/**
 * 日期区间控件
 * Created by admin on 2018/10/10.
 */
class DateRangeControl @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {
    private var start: Long = 0
    private var end: Long = 0
    private var dateRange by Delegates.observable(0L to 0L) { _, _, newValue ->
        (newValue.first > 0L && newValue.second > 0L && controlBean.is_scal!!).yes {
            countDuration()
        }
    }
    var needAssociate: Int = -1
    var holiday: ApproveValueBean? = null

    var selectionType: ApproveValueBean? by Delegates.observable(null as ApproveValueBean?) { _, _, newValue ->
        newValue?.let {
            it.format?.isNotEmpty()?.yes {
                formatStr = it.format
            }
        }
    }

    var block: ((days: Double, unit: String?) -> Unit)? = null
    val timeFormat = booleanArrayOf(true, true, true, true, true, true)
    var formatStr: String by Delegates.observable("") { _, _, newValue ->
        timeFormat[0] = newValue.contains("yyyy")
        timeFormat[1] = newValue.contains("MM")
        timeFormat[2] = newValue.contains("dd")
        timeFormat[3] = (newValue.contains("HH") || newValue.contains("hh"))
        timeFormat[4] = newValue.contains("mm")
        timeFormat[5] = newValue.contains("ss")
    }

    override fun getContentResId(): Int = R.layout.layout_control_date_range

    override fun bindContentView() {
        hasInit.yes {
            inflate.star.setVisible(isMust)
            inflate.startTimeTitle.text = controlBean.name1
            inflate.endTimeTitle.text = controlBean.name2
            inflate.durationTitle.text = controlBean.name3
            formatStr = controlBean.format ?: "yyyy-MM-dd HH:mm:ss"
            controlBean.value?.let {
                it as MutableList<String>
                it.isNotEmpty().yes {
                    it[0].isNotEmpty().yes {
                        inflate.startTimeTv.text = it[0]
                        start = Utils.getTime(it[0], formatStr)
                        inflate.startTimeTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    }.otherWise {
                        inflate.startTimeTv.hint = controlBean.placeholder
                        inflate.startTimeTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right, 0)
                    }
                    (it.size > 1 && it[1].isNotEmpty()).yes {
                        inflate.endTimeTv.text = it[1]
                        end = Utils.getTime(it[1], formatStr)
                        inflate.endTimeTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    }.otherWise {
                        inflate.endTimeTv.hint = controlBean.placeholder
                        inflate.endTimeTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right, 0)
                    }
                    (it.size > 2 && it[2].isNotEmpty()).yes {
                        inflate.durationTv.setText(it[2])
                    }
                }.otherWise {
                    inflate.startTimeTv.hint = controlBean.placeholder
                    inflate.startTimeTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right, 0)
                    inflate.endTimeTv.hint = controlBean.placeholder
                    inflate.endTimeTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right, 0)
                }
            }
            inflate.durationTv.isClickable = !controlBean.is_scal!!

            val startDate = Calendar.getInstance()
            startDate.set(1949, 1, 1)
            val endDate = Calendar.getInstance()
            endDate.set(2049, 1, 1)

            inflate.startTimeTv.clickWithTrigger {
                when (needAssociate) {
                    1 -> {
                        (holiday == null).yes {
                            showCommonToast("请先选择假期类型")
                            return@clickWithTrigger
                        }.otherWise {
                            (holiday!!.status?.toInt() == 1 && holiday!!.hours!!.toDouble() <= 0.0).yes {
                                showCommonToast("无可用时长")
                                return@clickWithTrigger
                            }
                        }
                    }
                    2 -> {
                        (selectionType == null).yes {
                            showCommonToast("请先选择外出类型")
                            return@clickWithTrigger
                        }
                    }
                }
                TimePickerBuilder(activity) { date, _ ->
                    inflate.startTimeTv.text = Utils.getTime(date, formatStr)
                    if ((controlBean.value!!.size > 1)) {
                        controlBean.value?.removeAt(0)
                    }
                    controlBean.value?.add(0, Utils.getTime(date, formatStr))
                    start = date.time
                    dateRange = start to end
                } //年月日时分秒 的显示与否，不设置则默认全部显示
                        .setType(timeFormat)
                        .setDividerColor(Color.DKGRAY)
                        .setContentTextSize(18)
                        .setDate(Calendar.getInstance())
                        .setDecorView(activity.window.decorView.find(android.R.id.content))
                        .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
                        .setRangDate(startDate, endDate)
                        .build()
                        .show()

            }
            inflate.endTimeTv.clickWithTrigger {
                (start == 0L).yes {
                    showCommonToast("请先选择开始时间")
                    return@clickWithTrigger
                }
                val instance = Calendar.getInstance()
                instance.time = Date(start)
                startDate.set(instance.get(Calendar.YEAR), instance.get(Calendar.MONTH), 1)
                TimePickerBuilder(activity) { date, _ ->
                    if (date.time < start) {
                        showErrorToast("结束时间必须大于开始时间")
                        return@TimePickerBuilder
                    }
                    inflate.endTimeTv.text = Utils.getTime(date, formatStr)
                    if (controlBean.value!!.size > 1)
                        controlBean.value?.removeAt(1)
                    controlBean.value?.add(1, Utils.getTime(date, formatStr))
                    inflate.endTimeTv.postDelayed({
                        end = date.time
                        dateRange = start to end
                    }, 500)

                } //年月日时分秒 的显示与否，不设置则默认全部显示
                        .setType(timeFormat)
                        .setDividerColor(Color.DKGRAY)
                        .setContentTextSize(18)
                        .setDate(Calendar.getInstance())
                        .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
                        .setRangDate(startDate, endDate)
                        .setDecorView(activity.window.decorView.find(android.R.id.content))
                        .build()
                        .show()
            }
            inflate.durationTv.clickWithTrigger {
                if (controlBean.is_scal!!) {
                    countDuration()
                }
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun countDuration() {
        val s_unit = when (needAssociate) {
            2 -> selectionType!!.scal_unit!!
            else -> controlBean.scal_unit!!
        }
        SoguApi.getService(activity.application, ApproveService::class.java)
                .countDuration(inflate.startTimeTv.text.toString(), inflate.endTimeTv.text.toString(), s_unit)
                .execute {
                    onNext { payload ->
                        payload.isOk.yes {
                            payload.payload?.let { days ->
                                val unit = when (s_unit) {
                                    "year" -> "年"
                                    "month" -> "月"
                                    "day" -> "天"
                                    "hour", "min", "sec", "work" -> "小时"
                                    else -> ""
                                }
                                if (controlBean.value!!.size > 2)
                                    controlBean.value?.removeAt(2)
                                controlBean.value?.add(days)
                                inflate.durationTv.setText(days + unit)
                                block?.invoke(days.toDouble(), unit)
                                inflate.durationTv.isClickable = false
                            }
                        }.otherWise {
                            inflate.durationTv.setText("点击重新计算")
                            inflate.durationTv.isClickable = true
                            showErrorToast(payload.message)
                        }
                    }
                    onError {
                        inflate.durationTv.setText("点击重新计算")
                        inflate.durationTv.isClickable = true
                    }
                }
    }
}