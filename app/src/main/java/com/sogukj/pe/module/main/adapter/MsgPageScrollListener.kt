/**
 * Copyright (C), 2018-2018, 搜股科技有限公司
 * FileName: MsgPageScrollListener
 * Author: admin
 * Date: 2018/12/6 下午3:54
 * Description: 首页消息卡片列表滑动监听
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
package com.sogukj.pe.module.main.adapter

import android.annotation.SuppressLint
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

/**
 * @ClassName: MsgPageScrollListener
 * @Description: 首页消息卡片列表滑动监听
 * @Author: admin
 * @Date: 2018/12/6 下午3:54
 */
class MsgPageScrollListener constructor(val block: () -> Unit) : RecyclerView.OnScrollListener(), AnkoLogger {
    private var rebound = false
    override val loggerTag: String
        get() = "WJY"

    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        rebound = dx < 0
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        if (RecyclerView.SCROLL_STATE_IDLE == newState) {
            val manager = recyclerView.layoutManager
            if (manager is LinearLayoutManager) {
                val position = manager.findLastVisibleItemPosition()
                val adapter = recyclerView.adapter
                recyclerView.postDelayed({
                    if (adapter is MainMsgPageAdapter && position - 1 > -1 && !rebound) {
                        adapter.dataList.removeAt(0)
                        adapter.notifyItemRemoved(0)
                        adapter.notifyDataSetChanged()
                        recyclerView.scrollToPosition(0)
//                        recyclerView.smoothScrollToPosition(0)
                        if (adapter.itemCount == 5) {
                            block.invoke()
                        }
                    }
                }, 100)
            }
        }
    }
}