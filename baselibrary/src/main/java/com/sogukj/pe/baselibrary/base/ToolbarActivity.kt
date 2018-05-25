package com.sogukj.pe.baselibrary.base
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.sogukj.pe.baselibrary.R

/**
 * Created by qinfei on 17/7/18.
 */
abstract class ToolbarActivity : BaseActivity() {

    open val titleId: Int
        get() = 0

    open val menuId: Int
        get() = 0

    val displayHomeAsUpEnabled: Boolean
        get() = false

    var toolbar: Toolbar? = null
        private set

    lateinit var mMenu:Menu

    fun setBack(visible: Boolean) {
        toolbar?.apply {
            val back = this.findViewById<ImageView>(R.id.toolbar_back)
            back?.visibility = if (visible) View.VISIBLE else View.INVISIBLE
            back?.setOnClickListener {
                onBackPressed()
            }
        }
    }

    fun initToolbar() {
        if (findViewById<Toolbar>(R.id.toolbar) == null) {
            return
        }

        toolbar = findViewById(R.id.toolbar)
        toolbar?.apply {
            val title = this.findViewById<TextView>(R.id.toolbar_title)
            if (title != null && titleId != 0)
                title.setText(titleId)
            setSupportActionBar(this)
            supportActionBar?.setDisplayShowTitleEnabled(false)
            this.background.alpha = 255
            this.setBackgroundColor(resources.getColor(R.color.colorPrimary))
        }
    }

    val actionBarSize: Int
        get() {
            val typedValue = TypedValue()
            val textSizeAttr = intArrayOf(R.attr.actionBarSize)
            val indexOfAttrTextSize = 0
            val a = obtainStyledAttributes(typedValue.data, textSizeAttr)
            val actionBarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1)
            a.recycle()
            return actionBarSize
        }

    override fun setTitle(resId: Int) {
        if (null != toolbar) {
            val title = toolbar!!.findViewById<TextView>(R.id.toolbar_title)
            if (title != null) {
                if (resId != 0) {
                    title.setText(resId)
                }
            }
        }
    }

    override fun setTitle(titleRes: CharSequence?) {
        if (null != toolbar) {
            val title = toolbar!!.findViewById<TextView>(R.id.toolbar_title)
            if (title != null) {
                if (titleRes != null) {
                    title.text = titleRes
                }
            }
        }
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        initToolbar()
    }

    override fun setContentView(view: View) {
        super.setContentView(view)
        initToolbar()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        mMenu = menu
        if (menuId != 0) {
            menuInflater.inflate(menuId, menu)
            return true
        }

        return super.onCreateOptionsMenu(menu)
    }
}
