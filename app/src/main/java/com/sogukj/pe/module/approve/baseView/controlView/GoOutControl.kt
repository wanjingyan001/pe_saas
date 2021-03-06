package com.sogukj.pe.module.approve.baseView.controlView

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.google.gson.internal.LinkedTreeMap
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.module.approve.baseView.BaseControl
import com.sogukj.pe.module.approve.baseView.ControlFactory
import com.sogukj.pe.module.approve.baseView.viewBean.ApproveValueBean
import kotlinx.android.synthetic.main.layout_control_go_out.view.*
import org.jetbrains.anko.dip
import kotlin.properties.Delegates

/**
 * 外出套件
 * Created by admin on 2018/10/11.
 */
class GoOutControl @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {
    private var selectionType: ApproveValueBean by Delegates.observable(ApproveValueBean(name = "", scal_unit = "",format = "yyyy-MM-dd HH:mm:ss"))
    { _, _, newValue ->
        newValue.name.isNotEmpty().yes {
            controlBean.children!![0].value?.let {
                it.clear()
                it.add(selectionType)
                dateRange.selectionType = selectionType
            }
            controlBean.children!![1].let {
                it.format = selectionType.format
                it.scal_unit = selectionType.scal_unit
            }
        }
    }
    private lateinit var dateRange: DateRangeControl

    override fun getContentResId(): Int = R.layout.layout_control_go_out

    override fun bindContentView() {
        hasInit.yes {
            inflate.star.setVisible(isMust)
            controlBean.children?.let {
                /**
                 * 时间选择初始化
                 */
                dateRange = ControlFactory(activity).createControl(DateRangeControl::class.java, it[1]) as DateRangeControl
                dateRange.needAssociate = 2
                val view = View(activity)
                val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dip(10))
                view.layoutParams = lp
                inflate.dateRangeLayout.addView(view)
                inflate.dateRangeLayout.addView(dateRange)

                inflate.goOutTitle.text = it[0].name
                it[0].value?.let { values ->
                    values.isNotEmpty().yes {
                        val beans = mutableListOf<ApproveValueBean>()
                        values.forEach { map ->
                            val treeMap = map as LinkedTreeMap<*, *>
                            beans.add(ApproveValueBean(name = treeMap["name"] as String,
                                    scal_unit = treeMap["scal_unit"] as String,
                                    format = treeMap["format"] as? String
                            ))
                        }
                        it[0].value?.clear()
                        it[0].value?.addAll(beans)
                        dateRange.selectionType = beans[0]
                        inflate.goOutTv.text = beans[0].name
                        inflate.goOutTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    }.otherWise {
                        inflate.goOutTv.hint = it[0].placeholder
                        inflate.goOutTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right, 0)
                    }
                }
                inflate.goOutTv.clickWithTrigger { _ ->
                    it[0].options?.let { opt ->
                        MaterialDialog.Builder(activity)
                                .theme(Theme.LIGHT)
                                .items(opt.map { it.name })
                                .itemsCallbackSingleChoice(opt.map { it.name }.indexOf(inflate.goOutTv.text))
                                { dialog, itemView, which, text ->
                                    selectionType = ApproveValueBean(name = opt[which].name,
                                            scal_unit = opt[which].scal_unit,
                                            format = opt[which].format)
                                    inflate.goOutTv.text = opt[which].name
                                    true
                                }
                                .build()
                                .show()
                    }
                }
                inflate.leaveTip.text = it[2].name
            }
        }
    }
}