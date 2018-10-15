package com.sogukj.pe.module.approve.baseView.controlView

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import com.bigkoo.pickerview.TimePickerView
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.module.approve.baseView.BaseControl
import kotlinx.android.synthetic.main.layout_control_data_selection.view.*
import java.util.*

/**
 * 时间选择
 * Created by admin on 2018/9/27.
 */
class DateSelection @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {
    override fun getContentResId(): Int = R.layout.layout_control_data_selection

    override fun bindContentView() {
        hasInit.yes {
            inflate.dateSelectionTitle.text = controlBean.name
            controlBean.value?.let {
                it.isNotEmpty().yes {
                    inflate.dateTv.text = controlBean.value!![0] as String
                    inflate.dateTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }.otherWise {
                    inflate.dateTv.hint = controlBean.placeholder
                    inflate.dateTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right, 0)
                }
            }
            inflate.dateTv.clickWithTrigger {
                val timeFormat = booleanArrayOf(true, true, true, true, true, true)
                controlBean.format?.let {
                    timeFormat[0] = it.contains("yyyy")
                    timeFormat[1] = it.contains("MM")
                    timeFormat[2] = it.contains("dd")
                    timeFormat[3] = (it.contains("HH") || it.contains("hh"))
                    timeFormat[4] = it.contains("mm")
                    timeFormat[5] = it.contains("ss")
                    val startDate = Calendar.getInstance()
                    startDate.set(1949, 1, 1)
                    val endDate = Calendar.getInstance()
                    endDate.set(2049, 1, 1)
                    TimePickerView.Builder(activity) { date, v ->
                        inflate.dateTv.text = Utils.getTime(date, it)
                        controlBean.value?.clear()
                        controlBean.value?.add(Utils.getTime(date, it))
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

            }
        }
    }
}