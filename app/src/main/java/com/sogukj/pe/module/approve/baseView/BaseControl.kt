package com.sogukj.pe.module.approve.baseView

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v4.app.FragmentActivity
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.base.AvoidOnResult
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.module.approve.baseView.viewBean.ControlBean
import com.sogukj.pe.module.main.MainActivity
import kotlinx.android.synthetic.main.layout_control_fund_seal.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.dip
import org.jetbrains.anko.find

/**
 * 审批控件基类
 * Created by admin on 2018/9/26.
 */
abstract class BaseControl @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), AnkoLogger {

    lateinit var controlBean: ControlBean
    lateinit var activity: FragmentActivity
    protected abstract fun getContentResId(): Int
    protected abstract fun bindContentView()
    protected lateinit var inflate: View
    protected val hasInit: Boolean by lazy { ::controlBean.isLateinit }
    private val isMust: Boolean by lazy { hasInit.yes { controlBean.is_must ?: false }.otherWise { false } }

    fun init() {
        inflate = LayoutInflater.from(context).inflate(getContentResId(), null)
        bindContentView()
        necessity()
        addView(inflate, ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT))
    }

    private fun necessity() {
        if (findViewById<ImageView>(R.id.star) != null) {
            find<ImageView>(R.id.star).setVisible(isMust)
        }
    }


    /**
     * 图片选择需要该回调
     */
    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

    }

    protected fun showErrorToast(text: CharSequence?) {
        (activity as BaseActivity).showErrorToast(text)
    }

    protected fun showSuccessToast(text: CharSequence?) {
        (activity as BaseActivity).showSuccessToast(text)
    }

    protected fun showCommonToast(text: CharSequence?) {
        (activity as BaseActivity).showCommonToast(text)
    }


    protected fun resetValues(controlBean: ControlBean): ControlBean {
        controlBean.value?.clear()
        controlBean.children?.let {
            it.forEach { bean ->
                resetValues(bean)
            }
        }
        return controlBean
    }


    protected fun getDividerView(height: Int = 10): View {
        val view = View(activity)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dip(height))
        view.backgroundColorResource = R.color.divider2
        view.layoutParams = lp
        return view
    }
}