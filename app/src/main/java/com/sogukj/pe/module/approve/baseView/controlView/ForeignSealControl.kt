package com.sogukj.pe.module.approve.baseView.controlView

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.module.approve.baseView.BaseControl
import com.sogukj.pe.module.approve.baseView.ControlFactory
import kotlinx.android.synthetic.main.layout_control_foreign_seal.view.*
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.dip

/**
 * 外资用印
 * Created by admin on 2018/10/12.
 */
class ForeignSealControl @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {
    override fun getContentResId(): Int = R.layout.layout_control_foreign_seal

    override fun bindContentView() {
        hasInit.yes {
            controlBean.children?.let {
                val factory = ControlFactory(activity)
                it.forEach {
                    val control = when (it.control) {
                        20 -> factory.createControl(ForeignControl::class.java, it)
                        18 -> factory.createControl(ProjectSelection::class.java, it)
                        10 -> factory.createControl(AttachmentSelection::class.java, it)
                        24 -> factory.createControl(SMSNotification::class.java, it)
                        else -> throw  Exception("类型错误")
                    }
                    inflate.controlLayout.addView(getDividerView())
                    inflate.controlLayout.addView(control)
                }
            }
        }
    }
}