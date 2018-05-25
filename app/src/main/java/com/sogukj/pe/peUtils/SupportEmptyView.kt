package com.sogukj.pe.peUtils

import android.view.View
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.base.BaseActivity
import com.sogukj.pe.baselibrary.base.BaseFragment
import com.sogukj.pe.baselibrary.widgets.RecyclerAdapter
import org.jetbrains.anko.find

/**
 * Created by admin on 2018/5/22.
 */
interface SupportEmptyView {
    companion object {
        fun checkEmpty(fragment: BaseFragment, adapter: RecyclerAdapter<*>) {
            val view = fragment.view
            val refreshLayout: TwinklingRefreshLayout? = view?.find(R.id.refresh)
            val emptyView: View? = view?.find(R.id.iv_empty)
            emptyView?.visibility = if (adapter.dataList.isEmpty()) View.VISIBLE else View.GONE
            emptyView?.setOnClickListener {
                refreshLayout?.startRefresh()
            }
        }

        fun checkEmpty(activity: BaseActivity, adapter: RecyclerAdapter<*>) {
            val refreshLayout: TwinklingRefreshLayout? = activity.find(R.id.refresh)
            val emptyView: View? = activity.find(R.id.iv_empty)
            emptyView?.visibility = if (adapter.dataList.isEmpty()) View.VISIBLE else View.GONE
            emptyView?.setOnClickListener {
                refreshLayout?.startRefresh()
            }
        }
    }
}