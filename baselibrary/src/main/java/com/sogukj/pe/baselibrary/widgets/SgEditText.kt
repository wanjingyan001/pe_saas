package com.sogukj.pe.baselibrary.widgets

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.R
import kotlinx.android.synthetic.main.layout_sg_edittext.view.*
import android.text.TextUtils
import android.text.InputFilter
import android.text.InputType.TYPE_CLASS_TEXT
import android.view.Gravity
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import com.sogukj.pe.baselibrary.Extended.yes
import com.sogukj.pe.baselibrary.R.id.clearInput
import com.sogukj.pe.baselibrary.R.id.sgEdt
import org.jetbrains.anko.*


/**
 * Created by admin on 2018/5/30.
 */
class SgEditText @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), AnkoLogger {
    /**
     * 空格类型
     */
    enum class SpaceType(val value: Int, val maxEms: Int) {
        /** 默认类型  */
        DefaultType(0, 50),
        /** 银行卡类型  */
        BankCardNumberType(1, 48),
        /** 手机号类型  */
        MobilePhoneNumberType(2, 13),
        /** 身份证类型  */
        IDCardNumberType(3, 21)
    }

    private var hint: String = ""
    private var inputType: Int = InputType.TYPE_NULL
    private var needDelete = true
    private var formatType: Int = 0
    private var showHint: Boolean = true
    private var inputLayout: TextInputLayout
    private var sgEdt: EditText
    private var clearInput: ImageView
    private var textWatcher: AddSpaceTextWatcher
    lateinit var block:()->Unit

    init {
        attrs?.let {
            val array = context.obtainStyledAttributes(it, R.styleable.SgEditText)
            hint = array.getString(R.styleable.SgEditText_sgEdt_hint)
            inputType = array.getIndex(R.styleable.SgEditText_sgEdt_inputType)
            needDelete = array.getBoolean(R.styleable.SgEditText_sgEdt_needDelete, true)
            formatType = array.getInteger(R.styleable.SgEditText_sgEdt_formatType, SpaceType.DefaultType.value)
            showHint = array.getBoolean(R.styleable.SgEditText_sgEdt_showHint, true)
            array.recycle()
        }
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        inputLayout = TextInputLayout(context)
        sgEdt = EditText(context)
        sgEdt.background = null
        sgEdt.gravity = Gravity.CENTER_VERTICAL
        sgEdt.textColorResource = R.color.text_1
        sgEdt.hintTextColor = resources.getColor(R.color.text_3)
        sgEdt.textSize = 16f
//        sgEdt.setPadding(0, 0, 0, 0)
        if (!showHint) {
            sgEdt.hint = hint
        }
        when (formatType) {
            1 -> {
                sgEdt.inputType = InputType.TYPE_CLASS_NUMBER
                textWatcher = AddSpaceTextWatcher(sgEdt, SpaceType.BankCardNumberType.maxEms)
                textWatcher.setSpaceType(SpaceType.BankCardNumberType)
            }
            2 -> {
                sgEdt.inputType = InputType.TYPE_CLASS_NUMBER
                textWatcher = AddSpaceTextWatcher(sgEdt, SpaceType.MobilePhoneNumberType.maxEms)
                textWatcher.setSpaceType(SpaceType.MobilePhoneNumberType)
            }
            3 -> {
                sgEdt.inputType = InputType.TYPE_CLASS_TEXT
                textWatcher = AddSpaceTextWatcher(sgEdt, SpaceType.IDCardNumberType.maxEms)
                textWatcher.setSpaceType(SpaceType.IDCardNumberType)
            }
            else -> {
                sgEdt.inputType = InputType.TYPE_CLASS_TEXT
                textWatcher = AddSpaceTextWatcher(sgEdt, SpaceType.DefaultType.maxEms)
                textWatcher.setSpaceType(SpaceType.DefaultType)
            }
        }
        val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.weight = 1f
        params.marginEnd = dip(12)
        inputLayout.addView(sgEdt,LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        if (showHint) {
            inputLayout.hint = hint
        }
        inputLayout.isHintEnabled = showHint
        inputLayout.setHintTextAppearance(R.style.TextInputStyle)

        addView(inputLayout, params)

        clearInput = ImageView(context)
        clearInput.imageResource = R.mipmap.icon_edt_delete
        clearInput.setVisible(false)
        clearInput.clickWithTrigger {
            sgEdt.setText("")
            this::block.isLateinit.yes {
                block.invoke()
            }
        }
        val cParams =  LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        addView(clearInput,cParams)
    }


    fun setHintText(hint: CharSequence) {
        this.hint = hint.toString()
        sgEdt.hint = hint
    }

    fun setInputType(type: Int) {
        this.inputType = type
        sgEdt.inputType = type
    }

    fun setFormatType(type: SpaceType) {
        this.formatType = type.value
        textWatcher.setSpaceType(type)
    }

    fun getEditText() = sgEdt

    fun getClearIcon() = clearInput

    fun getInput() = sgEdt.textStr

    fun getFormatType() = textWatcher.getSpaceType()

    fun setText(text:CharSequence?) = sgEdt.setText(text)

    fun setEnable(enable:Boolean){
        sgEdt.isEnabled = enable
        clearInput.isEnabled = enable
        clearInput.setVisible(enable)
    }

    inner class AddSpaceTextWatcher(private val editText: EditText,
                                    /** text最大长度限制  */
                                    private val maxLength: Int) : TextWatcher {
        /** text改变之前的长度  */
        private var beforeTextLength = 0
        private var onTextLength = 0
        private var isChanged = false
        private val buffer = StringBuffer()
        /** 改变之前text空格数量  */
        /**
         * 得到空格数量
         *
         * @return
         * @see [类、类.方法、类.成员]
         */
        var spaceCount = 0
            internal set
        private var spaceType: SpaceType? = null
        /** 记录光标的位置  */
        private var location = 0
        /** 是否是主动设置text  */
        private var isSetText = false

        /**
         * 得到输入的字符串去空格后的字符串
         *
         * @return
         * @see [类、类.方法、类.成员]
         */
        private val textNotSpace: String?
            get() = delSpace(editText.text.toString())

        /**
         * 得到输入的字符串去空格后的长度
         *
         * @return
         * @see [类、类.方法、类.成员]
         */
        val lenghtNotSpace: Int
            get() {
                return textNotSpace!!.length
            }

        init {
            spaceType = SpaceType.DefaultType
            editText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))
            editText.addTextChangedListener(this)
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                       after: Int) {
            beforeTextLength = s.length
            if (buffer.isNotEmpty()) {
                buffer.delete(0, buffer.length)
            }
            spaceCount = (0 until s.length).count { s[it] == ' ' }
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            onTextLength = s.length
            if (sgEdt.textStr.isNotEmpty() && needDelete) {
                clearInput.setVisible(true)
            } else {
                clearInput.setVisible(false)
            }
            buffer.append(s.toString())
            if (onTextLength == beforeTextLength || onTextLength > maxLength
                    || isChanged) {
                isChanged = false
                return
            }
            isChanged = true
        }

        override fun afterTextChanged(s: Editable) {
            if (isChanged) {
                location = editText.selectionEnd
                var index = 0
                while (index < buffer.length) { // 删掉所有空格
                    if (buffer[index] == ' ') {
                        buffer.deleteCharAt(index)
                    } else {
                        index++
                    }
                }

                index = 0
                var spaceNumberB = 0
                while (index < buffer.length) { // 插入所有空格
                    spaceNumberB = insertSpace(index, spaceNumberB)
                    index++
                }

                val str = buffer.toString()

                // 下面是计算光位置的
                if (spaceNumberB > spaceCount) {
                    location += spaceNumberB - spaceCount
                    spaceCount = spaceNumberB
                }
                if (isSetText) {
                    location = str.length
                    isSetText = false
                } else if (location > str.length) {
                    location = str.length
                } else if (location < 0) {
                    location = 0
                }
                updateContext(s, str)
                isChanged = false
            }
        }

        /**
         * 更新编辑框中的内容
         *
         * @param editable
         * @param values
         */
        private fun updateContext(editable: Editable, values: String) {
            if (spaceType == SpaceType.IDCardNumberType) {
                editable.replace(0, editable.length, values)
            } else {
                editText.setText(values)
                try {
                    editText.setSelection(location)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }

        /**
         * 根据类型插入空格
         *
         * @param index
         * @param spaceNumberAfter
         * @return
         * @see [类、类.方法、类.成员]
         */
        private fun insertSpace(index: Int, spaceNumberAfter: Int): Int {
            var spaceNumberAfter = spaceNumberAfter
            when (spaceType) {
                SpaceType.DefaultType// 相隔四位空格
                -> if (index > 3 && index % (4 * (spaceNumberAfter + 1)) == spaceNumberAfter) {
//                    buffer.insert(index, ' ')
                    spaceNumberAfter++
                }
                SpaceType.BankCardNumberType -> if (index > 3 && index % (4 * (spaceNumberAfter + 1)) == spaceNumberAfter) {
                    buffer.insert(index, ' ')
                    spaceNumberAfter++
                }
                SpaceType.MobilePhoneNumberType -> if (index == 3 || index > 7 && (index - 3) % (4 * spaceNumberAfter) == spaceNumberAfter) {
                    buffer.insert(index, ' ')
                    spaceNumberAfter++
                }
                SpaceType.IDCardNumberType -> if (index == 6 || index > 10 && (index - 6) % (4 * spaceNumberAfter) == spaceNumberAfter) {
                    buffer.insert(index, ' ')
                    spaceNumberAfter++
                }
                else -> if (index > 3 && index % (4 * (spaceNumberAfter + 1)) == spaceNumberAfter) {
                    buffer.insert(index, ' ')
                    spaceNumberAfter++
                }
            }
            return spaceNumberAfter
        }

        /***
         * 计算需要的空格数
         *
         * @return 返回添加空格后的字符串长度
         * @see [类、类.方法、类.成员]
         */
        private fun computeSpaceCount(charSequence: CharSequence): Int {
            buffer.delete(0, buffer.length)
            buffer.append(charSequence.toString())
            var index = 0
            var spaceNumberB = 0
            while (index < buffer.length) { // 插入所有空格
                spaceNumberB = insertSpace(index, spaceNumberB)
                index++
            }
            buffer.delete(0, buffer.length)
            return index
        }

        /**
         * 设置空格类型
         *
         * @param spaceType
         * @see [类、类.方法、类.成员]
         */
        fun setSpaceType(spaceType: SpaceType) {
            this.spaceType = spaceType
            if (this.spaceType == SpaceType.IDCardNumberType) {
                editText.inputType = InputType.TYPE_CLASS_TEXT
            }
        }

        fun getSpaceType(): SpaceType? {
            return spaceType
        }

        /**
         * 设置输入字符
         *
         * @param charSequence
         * @return 返回设置成功失败
         * @see [类、类.方法、类.成员]
         */
        fun setText(charSequence: CharSequence): Boolean {
            if (!TextUtils.isEmpty(charSequence) && computeSpaceCount(charSequence) <= maxLength) {
                isSetText = true
                editText.removeTextChangedListener(this)
                editText.setText(charSequence)
                editText.addTextChangedListener(this)
                return true
            }
            return false
        }

        /**
         * 去掉字符空格，换行符等
         *
         * @param str
         * @return
         * @see [类、类.方法、类.成员]
         */
        private fun delSpace(str: String?): String? {
            var str = str
            if (str != null) {
                str = str.replace("\r".toRegex(), "")
                str = str.replace("\n".toRegex(), "")
                str = str.replace(" ", "")
            }
            return str
        }
    }
}