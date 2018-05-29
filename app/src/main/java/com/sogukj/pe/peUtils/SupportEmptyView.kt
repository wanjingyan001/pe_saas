package com.sogukj.pe.peUtils

import android.view.View
import com.scwang.smartrefresh.layout.SmartRefreshLayout
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
            val refreshLayout: SmartRefreshLayout? = view?.find(R.id.refresh)
           if (view?.findViewById<View>(R.id.iv_empty) != null){
               val emptyView: View? = view.find(R.id.iv_empty)
               emptyView?.visibility = if (adapter.dataList.isEmpty()) View.VISIBLE else View.GONE
               emptyView?.setOnClickListener {
                   refreshLayout?.autoRefresh()
               }
           }
        }

        fun checkEmpty(activity: BaseActivity, adapter: RecyclerAdapter<*>) {
            val refreshLayout: SmartRefreshLayout? = activity.find(R.id.refresh)
            if (activity.findViewById<View>(R.id.iv_empty) !=null) {
                val emptyView: View? = activity.find(R.id.iv_empty)
                emptyView?.visibility = if (adapter.dataList.isEmpty()) View.VISIBLE else View.GONE
                emptyView?.setOnClickListener {
                    refreshLayout?.autoRefresh()
                }
            }
        }
    }
}