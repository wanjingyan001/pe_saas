package com.sogukj.pe.baselibrary.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import com.sogukj.pe.baselibrary.Extended.clickWithTrigger
import com.sogukj.pe.baselibrary.Extended.setVisible
import com.sogukj.pe.baselibrary.Extended.textStr
import com.sogukj.pe.baselibrary.R
import com.sogukj.pe.baselibrary.utils.Utils
import kotlinx.android.synthetic.main.layout_sg_search_view.view.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk25.coroutines.onEditorAction
import org.jetbrains.anko.sdk25.coroutines.onFocusChange
import org.jetbrains.anko.sdk25.coroutines.textChangedListener
import kotlin.properties.Delegates

/**
 * Created by admin on 2018/6/12.
 */
class SGSearchView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private lateinit var backDrawable: Drawable
    private var sgHint = ""
    private var edtGravity: Boolean by Delegates.notNull()
    var doSearch: ((String) -> Unit)? = null
    var onTextChange: ((String) -> Unit)? = null

    init {
        val inflate = LayoutInflater.from(context).inflate(R.layout.layout_sg_search_view, null)
        attrs?.let {
            val attributes = context.obtainStyledAttributes(it, R.styleable.SGSearchView)
            backDrawable = attributes.getDrawable(R.styleable.SGSearchView_background)
            sgHint = attributes.getString(R.styleable.SGSearchView_sg_hint)
            edtGravity = attributes.getBoolean(R.styleable.SGSearchView_gravity_center, false)
            attributes.recycle()
        }
        inflate.background = backDrawable
        val searchEdt =  inflate.find<EditText>(R.id.searchEdt)
        val clearInput =  inflate.find<ImageView>(R.id.clearInput)
        searchEdt.apply {
            hint = sgHint
            gravity = if (edtGravity) Gravity.CENTER else Gravity.LEFT or Gravity.CENTER_VERTICAL
            filters = Utils.getFilter(context)
            onFocusChange { v, hasFocus ->
                if (hasFocus) {
                    Utils.showInput(context, searchEdt)
                } else {
                    Utils.closeInput(context, searchEdt)
                    hint = sgHint
                }
            }
            onEditorAction { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val searchStr = searchEdt.textStr
                    if (doSearch != null && searchStr.isNotEmpty()) {
                        doSearch?.invoke(searchStr)
                    }
                }
            }
            textChangedListener {
                onTextChanged { _, _, _, _ ->
                    clearInput.setVisible(isFocused and textStr.isNotEmpty() )
                    if (onTextChange != null) {
                        onTextChange?.invoke(searchEdt.textStr)
                    }
                }
            }
        }
        clearInput.clickWithTrigger {
            searchEdt.setText("")
        }
        val params = ConstraintLayout.LayoutParams(0, dip(36))
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        addView(inflate, params)
    }
}