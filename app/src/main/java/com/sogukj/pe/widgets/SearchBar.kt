package com.sogukj.pe.widgets

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.utils.Utils

class SearchBar : LinearLayout {
    internal lateinit var et_search: EditText
    lateinit var tv_cancel: View
    var onSearch: ((String) -> Unit)? = null
    var onTextChange: ((String) -> Unit)? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }


    fun setCancel(bool: Boolean, l: (View) -> Unit) {
        tv_cancel.visibility = if (bool) View.VISIBLE else View.GONE
        tv_cancel.setOnClickListener { v -> l(v) }
    }

    fun setOnEditorActionListener(l: TextView.OnEditorActionListener) {
        et_search.setOnEditorActionListener(l)
    }

    fun setOnEditTouchListener(listener: View.OnTouchListener) {
        et_search.setOnTouchListener(listener)
    }

    fun addTextChangedListener(watcher: TextWatcher) {
        et_search.addTextChangedListener(watcher)
    }

    private fun init(context: Context, attrs: AttributeSet) {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.search_bar, null)

        tv_cancel = view.findViewById(R.id.tv_cancel)
        et_search = view.findViewById(R.id.et_search)
        et_search.isFocusable = true
        et_search.isFocusableInTouchMode = true
        et_search.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val editable = et_search.textStr
                val str = Utils.stringFilter(editable)
                if (null != onSearch && !TextUtils.isEmpty(str.trim { it <= ' ' })) {
                    onSearch!!(str)
                    true
                }
            }
            false
        }


        et_search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {

                val editable = et_search.text.toString()
                val str = Utils.stringFilter(editable.toString())
                if (editable != str) {
                    et_search.setText(str)
                    et_search.setSelection(str.length)
                }
                if (null != onTextChange)
                    onTextChange!!(str)
            }
        })

        et_search.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus)
                Utils.showInput(context, et_search)
            else
                Utils.closeInput(context, et_search)
        }
        addView(view, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT))
    }

    var search: String
        get() = et_search.text.toString()
        set(value) {
            et_search.setText(value)
        }

    var hint: String
        get() = et_search.hint.toString()
        set(value) {
            et_search.hint = value
        }

    fun setSelection(selection: Int) {
        et_search.setSelection(selection)
    }

    interface OnSearchListener {
        fun onSearch(text: String)
    }

    interface OnTextChangeListener {
        fun onTextChange(text: String)
    }
}