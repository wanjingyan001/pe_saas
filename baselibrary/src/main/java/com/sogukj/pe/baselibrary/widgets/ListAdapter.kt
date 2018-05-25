package com.sogukj.pe.baselibrary.widgets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import java.util.*
/**
 * Created by qinfei on 17/7/18.
 */
interface ListHolder<T> {
    fun createView(inflater: LayoutInflater): View
    fun showData(convertView: View, position: Int, itemData: T?)
}

class ListAdapter<T>(val creator: () -> ListHolder<T>) : BaseAdapter() {
    var dataList: ArrayList<T> = ArrayList()


    private fun createViewHolder(): ListHolder<T> {
        return creator()
    }

    override fun getCount(): Int {
        return if (dataList == null) 0 else dataList.size
    }

    override fun getItem(position: Int): T? {
        return if (dataList == null || dataList.size <= position) null else dataList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        var holder: ListHolder<T>? = null
        val itemData = getItem(position) as T?

        if (convertView == null) {
            val inflater = LayoutInflater.from(parent.context)

            holder = createViewHolder()
            convertView = holder.createView(inflater)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ListHolder<T>
        }

        if (holder != null) {
            holder.showData(convertView, position, itemData)
        }

        return convertView
    }

    //局部刷新
    fun updateSingleRow(listView: ListView?, id: Long) {
        if (listView != null) {
            val start = listView.firstVisiblePosition
            var i = start
            val j = listView.lastVisiblePosition
            while (i <= j) {
                if (id == listView.getItemIdAtPosition(i)) {
                    val view = listView.getChildAt(i - start)
                    getView(i, view, listView)
                    break
                }
                i++
            }
        }
    }
}
