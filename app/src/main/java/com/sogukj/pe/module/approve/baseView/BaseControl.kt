package com.sogukj.pe.module.approve.baseView

import android.content.Context
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v4.app.FragmentActivity
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.amap.api.mapcore.util.it
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.module.approve.baseView.viewBean.ControlBean
import org.jetbrains.anko.*

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
    protected val isMust: Boolean by lazy { hasInit.yes { controlBean.is_must != false }.otherWise { false } }

    fun init() {
        inflate = LayoutInflater.from(context).inflate(getContentResId(), null)
        bindContentView()
        addView(inflate, ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT))
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


    protected fun resetValues(controlBean: ControlBean): ControlBean? {
        controlBean.children?.let {
            it.forEach { bean ->
                info { bean.name }
                bean.value?.clear()
            }
        }
        return controlBean
    }

    fun getBean(controlBean: ControlBean): List<Boolean>{
        val checkValue = mutableListOf<Boolean>()
        controlBean.children?.forEach { bean ->
            if (bean.is_must!=null){
                bean.is_must.yes {
                    bean.value?.let {
                        it.isEmpty().yes {
                            showErrorToast(when (bean.control) {
                                1, 2, 3, 9 -> "${bean.name}为必填项"
                                else -> "${bean.name}为必选项"
                            })
                            checkValue.add(false)
                        }.otherWise {
                            checkValue.add(true)
                        }
                    }
                }.otherWise {
                    checkValue.add(true)
                }
            }else{
                checkValue.addAll( getBean(bean))
            }
        }
        return checkValue
    }

    protected fun getDividerView(height: Int = 10): View {
        val view = View(activity)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dip(height))
        view.backgroundColorResource = R.color.bg_record
        view.layoutParams = lp
        return view
    }
}