package com.sogukj.pe.module.approve.baseView.controlView

import android.content.Context
import android.util.AttributeSet
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.module.approve.baseView.BaseControl
import kotlinx.android.synthetic.main.layout_control_sms_notification.view.*
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange

/**
 * 是否短信通知审批人
 * Created by admin on 2018/10/9.
 */
class SMSNotification @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {
    var optionChange: (() -> Unit)? = null
    override fun getContentResId(): Int = R.layout.layout_control_sms_notification

    override fun bindContentView() {
        hasInit.yes {
            inflate.optionGroup.onCheckedChange { group, checkedId ->
                optionChange?.invoke()
            }
        }
    }
}