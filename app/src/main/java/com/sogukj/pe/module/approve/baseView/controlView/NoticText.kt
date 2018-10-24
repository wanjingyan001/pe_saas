package com.sogukj.pe.module.approve.baseView.controlView

import android.content.Context
import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.core.net.toUri
import com.sogukj.pe.Extras
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.ifNotNull
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.module.approve.baseView.BaseControl
import com.sogukj.pe.module.approve.baseView.SkipType
import com.sogukj.pe.module.register.InfoSupplementActivity
import kotlinx.android.synthetic.main.layout_control_notice_text.view.*
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity

/**
 * 说明文字
 * Created by admin on 2018/9/27.
 */
class NoticText @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseControl(context, attrs, defStyleAttr) {
    val urlRegex = "[a-zA-z]+://[^\\s]*".toRegex()
    override fun getContentResId(): Int = R.layout.layout_control_notice_text

    override fun bindContentView() {
        hasInit.yes {
            initJumpLink(inflate.noticeTv, controlBean.name ?: "", controlBean.linkText ?: "")
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
            controlBean.skip?.let {
                if (it[0].skip_type == SkipType.SKIP_LINK) {
                    info { it[0].skip_site }
                    urlRegex.matches(it[0].skip_site).yes {
                        activity.startActivity(Intent(Intent.ACTION_VIEW, it[0].skip_site.toUri()))
                    }.otherWise {
                        showErrorToast("跳转链接错误")
                    }
                }
            }
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = context.resources.getColor(R.color.colorPrimary)
            ds.isUnderlineText = false
        }
    }
}