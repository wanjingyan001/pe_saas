package com.sogukj.pe.baselibrary.widgets

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.sogukj.pe.baselibrary.Extended.*
import com.sogukj.pe.baselibrary.R
import com.sogukj.pe.baselibrary.utils.Utils
import org.jetbrains.anko.*
import kotlin.properties.Delegates


/**
 * Created by admin on 2018/5/30.
 */
class SingleEditLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), TextWatcher, AnkoLogger, View.OnClickListener {
    private var count: Int = 4
    private var singleWidth: Int = 56
    private var currentSelector = 0
    private val inputs = ArrayList<TextView>()
    private val verCodeStr = ArrayList<String>()
    private val screenWidth by lazy { context.resources.displayMetrics.widthPixels }
    private var listener: InputFinish? = null

    init {
        attrs?.let {
            val attributes = context.obtainStyledAttributes(it, R.styleable.SingleEditLayout)
            count = attributes.getInteger(R.styleable.SingleEditLayout_editCount, 4)
            singleWidth = attributes.getInteger(R.styleable.SingleEditLayout_singleWidth, 56)
            attributes.recycle()
        }
        orientation = LinearLayout.HORIZONTAL
        (0 until count).forEachIndexed { index, i ->
            val tv = TextView(context)
            tv.textSize = 16f
            tv.background = resources.getDrawable(R.drawable.bg_single_edt)
            tv.maxEms = 1
            tv.gravity = Gravity.CENTER
            tv.maxWidth = dip(56)
            val params = LinearLayout.LayoutParams(0, dip(40))
            params.weight = 1f
            params.leftMargin = (screenWidth - dip(80) - Utils.dpToPx(context, singleWidth) * count).div(8)
            params.rightMargin = (screenWidth - dip(80) - Utils.dpToPx(context, singleWidth) * count).div(8)
            params.gravity = Gravity.CENTER
            inputs.add(tv)
            verCodeStr.add("")
            addView(tv,params)
        }
        val edt = EditText(context)
        edt.id = R.id.inputEdt
        edt.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(count))
        edt.inputType = InputType.TYPE_CLASS_NUMBER
        val params = LinearLayout.LayoutParams(0, 0)
        edt.addTextChangedListener(this)
        addView(edt, params)
        obtainFocus()
        setOnClickListener(this)
    }


    fun setFinishListener(listener: InputFinish) {
        this.listener = listener
    }

    fun obtainFocus() {
        if (count > 0) {
            getChildAt(0).isSelected = true
        }
        val editText = findViewById<EditText>(R.id.inputEdt)
        editText.isFocusableInTouchMode = true
        editText.requestFocus()
    }

    override fun onClick(v: View) {
        val editText = findViewById<View>(R.id.inputEdt)
        editText.isFocusableInTouchMode = true
        editText.requestFocus()
        Utils.toggleSoftInput(context, editText)
    }


    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        info { "s:${s}==start:${start} == before:${before} == count:${count}" }

        if (before == 0 && currentSelector < inputs.size) {
            inputs[currentSelector].text = s?.get(start - before).toString()
            verCodeStr[currentSelector] = s?.get(start - before).toString()
            currentSelector++
        }

        if (before > 0 && currentSelector > 0) {
            currentSelector--
            inputs[currentSelector].text = ""
            verCodeStr[currentSelector] = ""
        }


        if (listener != null) {
            val verCode = verCodeStr.joinToString(separator = "")
            val isFinish = currentSelector == inputs.size
            listener?.finish(isFinish, verCode)
        }

        inputs.forEachIndexed { index, textView ->
            textView.isSelected = currentSelector == index
        }
    }

    fun getCompleteInput() = verCodeStr.joinToString(separator = "")

    interface InputFinish {
        fun finish(isFinish: Boolean, verCode: String)
    }
}