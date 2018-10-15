package com.sogukj.pe.module.approve.baseView.controlView

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import com.amap.api.mapcore.util.it
import com.bigkoo.pickerview.TimePickerView
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.module.approve.baseView.BaseControl
import com.sogukj.pe.module.approve.baseView.viewBean.MyLeaveBean
import com.sogukj.pe.module.approve.baseView.viewBean.OptionBean
import com.sogukj.pe.service.ApproveService
import com.sogukj.service.SoguApi
import io.reactivex.internal.util.HalfSerializer.onNext
import kotlinx.android.synthetic.main.layout_control_date_range.view.*
import java.util.*
import kotlin.properties.Delegates

/**
 * 日期区间控件
 * Created by admin on 2018/10/10.
 */
class DateRangeControl @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {
    private var start: Long = 0
    private var end by Delegates.observable(0L, { property, oldValue, newValue ->
        (newValue > 0L && controlBean.is_scal!!).yes {
            countDuration()
        }
    })
    var needAssociate: Int = -1
    var holiday: MyLeaveBean? = null
    var selectionType: OptionBean? = null

    override fun getContentResId(): Int = R.layout.layout_control_date_range

    override fun bindContentView() {
        hasInit.yes {
            inflate.startTimeTitle.text = controlBean.name1
            inflate.endTimeTitle.text = controlBean.name2
            inflate.durationTitle.text = controlBean.name3
            controlBean.value?.let {
                it as MutableList<String>
                it.isNotEmpty().yes {
                    it[0].isNotEmpty().yes {
                        inflate.startTimeTv.text = it[0]
                        inflate.startTimeTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    }.otherWise {
                        inflate.startTimeTv.hint = controlBean.placeholder
                        inflate.startTimeTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right, 0)
                    }
                    it[1].isNotEmpty().yes {
                        inflate.endTimeTv.text = it[1]
                        inflate.endTimeTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    }.otherWise {
                        inflate.endTimeTv.hint = controlBean.placeholder
                        inflate.endTimeTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right, 0)
                    }
                    it[2].isNotEmpty().yes {
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

            val timeFormat = booleanArrayOf(true, true, true, true, true, true)
            var formatStr = "yyyy-MM-dd HH:mm:ss"
            controlBean.format?.let {
                timeFormat[0] = it.contains("yyyy")
                timeFormat[1] = it.contains("MM")
                timeFormat[2] = it.contains("dd")
                timeFormat[3] = (it.contains("HH") || it.contains("hh"))
                timeFormat[4] = it.contains("mm")
                timeFormat[5] = it.contains("ss")
                formatStr = it
            }
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
                            (holiday!!.hours.toDouble() <= 0.0).yes {
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
                TimePickerView.Builder(activity) { date, v ->
                    inflate.startTimeTv.text = Utils.getTime(date, formatStr)
                    controlBean.value?.clear()
                    controlBean.value?.add(Utils.getTime(date, formatStr))
                    start = date.time
                } //年月日时分秒 的显示与否，不设置则默认全部显示
                        .setType(timeFormat)
                        .setDividerColor(Color.DKGRAY)
                        .setContentSize(21)
                        .setDate(Calendar.getInstance())
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
                TimePickerView.Builder(activity) { date, v ->
                    inflate.endTimeTv.text = Utils.getTime(date, formatStr)
                    if (controlBean.value!!.size > 1)
                        controlBean.value?.removeAt(1)
                    controlBean.value?.add(Utils.getTime(date, formatStr))
                    end = date.time
                } //年月日时分秒 的显示与否，不设置则默认全部显示
                        .setType(timeFormat)
                        .setDividerColor(Color.DKGRAY)
                        .setContentSize(21)
                        .setDate(Calendar.getInstance())
                        .setCancelColor(resources.getColor(R.color.shareholder_text_gray))
                        .setRangDate(startDate, endDate)
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
                            payload.payload?.let {
                                val unit = when (controlBean.scal_unit!!) {
                                    "year" -> "年"
                                    "month" -> "月"
                                    "day" -> "天"
                                    "hour" -> "小时"
                                    "min" -> "分钟"
                                    "sec" -> "秒"
                                    "work" -> "工作时长"
                                    else -> ""
                                }
                                controlBean.value?.add(it)
                                inflate.durationTv.setText(it + unit)
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