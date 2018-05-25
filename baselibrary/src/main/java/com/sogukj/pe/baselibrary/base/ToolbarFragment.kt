package com.sogukj.pe.baselibrary.base
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.TextView
import com.sogukj.pe.baselibrary.R

/**
 * Created by qinfei on 17/7/18.
 */
abstract class ToolbarFragment : BaseFragment() {
    val menuId: Int
        get() = 0

    //设置返回按钮
    var displayHomeAsUpEnabled: Boolean
        get() = false
        set(showHomeAsUp) {
            val actionBar = baseActivity!!.supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(showHomeAsUp)
        }

    var toolbar: Toolbar? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (menuId != 0) {
            setHasOptionsMenu(true)
        } else {
            setHasOptionsMenu(false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        initToolbar(view)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if (isAdded && isVisible && isResumed && !isRemoving && !isDetached) {
            if (menuId != 0) {
                inflater!!.inflate(menuId, menu)
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onStart() {
        super.onStart()

        setTitle(titleId)
        displayHomeAsUpEnabled = displayHomeAsUpEnabled
    }

    protected fun setTitle(resId: Int) {
        val toolbar = toolbar
        if (toolbar != null) {
            val title = toolbar.findViewById<TextView>(R.id.toolbar_title)

            if (title != null && resId != 0) {
                title.setText(resId)
            }
        }
    }

    protected fun setTitle(resStr: CharSequence?) {
        val toolbar = toolbar
        if (toolbar != null) {
            val title = toolbar.findViewById<TextView>(R.id.toolbar_title)

            if (title != null && resStr != null) {
                title.text = resStr
            }
        }
    }

    //初始化toolbar
    fun initToolbar(v: View?) {
        if (v?.findViewById<Toolbar>(R.id.toolbar) == null) {
            return
        }

        toolbar = v.findViewById(R.id.toolbar)
        if (toolbar != null) {
            val title = toolbar!!.findViewById<TextView>(R.id.toolbar_title) as TextView
            if (titleId != 0) title.setText(titleId)
            baseActivity!!.setSupportActionBar(toolbar)
            baseActivity!!.supportActionBar!!.setDisplayShowTitleEnabled(false)
            toolbar!!.background.alpha = 255
            toolbar!!.setBackgroundColor(resources.getColor(R.color.colorPrimary))
        }
    }

}
