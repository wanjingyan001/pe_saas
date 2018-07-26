package com.sogukj.pe.baselibrary.widgets

import android.content.Context
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sogukj.pe.baselibrary.utils.DiffCallBack
import java.util.*
import kotlin.properties.Delegates

/**
 * Created by qinfei on 16/10/19.
 */

abstract class RecyclerHolder<T>(val convertView: View) : RecyclerView.ViewHolder(convertView) {
    abstract fun setData(view: View, data: T, position: Int)
}

open class RecyclerAdapter<T>(val context: Context, val creator: (RecyclerAdapter<T>, ViewGroup, Int) -> RecyclerHolder<T>)
    : RecyclerView.Adapter<RecyclerHolder<T>>() {

    var dataList = mutableListOf<T>()
    val inflater: LayoutInflater = LayoutInflater.from(context)
    var comparator: Comparator<T>? = null
    var onItemClick: ((v: View, position: Int) -> Unit)? = null
    var onItemLongClick: ((v: View, position: Int) -> Boolean)? = null
    var selectedItems = ArrayList<Int>()
    var mode: Int = 0
    var selectChange: ((oldValue: Int, newValue: Int) -> Unit)? = null
    var selectedPosition: Int by Delegates.observable(-1, { _, oldValue, newValue ->
        notifyItemChanged(oldValue)
        notifyItemChanged(newValue)
        if (selectChange != null) {
            selectChange!!.invoke(oldValue, newValue)
        }
    })

    init {
        this.selectedItems = ArrayList<Int>()
        this.mode = MODE_SINGLE
    }


    fun getView(layout: Int, parent: ViewGroup): View {
        return inflater.inflate(layout, parent, false)
    }

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): RecyclerHolder<T> {
        return creator(this, parent, type)
    }


    override fun onBindViewHolder(holder: RecyclerHolder<T>, position: Int) {
        val data = dataList[position]
        holder.setData(holder.convertView, data, position)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }


    override fun onBindViewHolder(holder: RecyclerHolder<T>, position: Int, payloads: MutableList<Any>) {
        holder.itemView.setOnClickListener { v ->
            selectedPosition = position
            if (null != onItemClick)
                onItemClick!!(v, position)
        }
        if (null != onItemLongClick) {
            holder.itemView.isLongClickable = true
            holder.itemView.setOnLongClickListener { v ->
                onItemLongClick!!(v, position)
            }
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(hasStableIds)
    }

    override fun onViewRecycled(holder: RecyclerHolder<T>) {
        super.onViewRecycled(holder)
    }

    override fun onFailedToRecycleView(holder: RecyclerHolder<T>): Boolean {
        return super.onFailedToRecycleView(holder)
    }


    override fun onViewAttachedToWindow(holder: RecyclerHolder<T>) {
        super.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerHolder<T>) {
        super.onViewDetachedFromWindow(holder)
    }

    override fun onAttachedToRecyclerView(view: RecyclerView) {
        super.onAttachedToRecyclerView(view)
    }

    override fun onDetachedFromRecyclerView(view: RecyclerView) {
        super.onDetachedFromRecyclerView(view)
    }

    override fun registerAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
        super.registerAdapterDataObserver(observer)
    }

    override fun unregisterAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
        super.unregisterAdapterDataObserver(observer)
    }


    fun getItem(position: Int): T {
        return dataList[position]
    }


    fun isSelected(position: Int): Boolean {
        return selectedItems.contains(Integer.valueOf(position))
    }

    /**
     * 使用DiffUtil刷新数据
     */
    fun refreshData(newData: List<T>) {
        val result = DiffUtil.calculateDiff(DiffCallBack(dataList, newData))
        result.dispatchUpdatesTo(this)
        dataList.clear()
        dataList.addAll(newData)
        notifyDataSetChanged()
    }


    val selectedItemCount: Int
        get() = selectedItems.size


    companion object {
        val TAG = RecyclerAdapter::class.java.simpleName


        val MODE_SINGLE = 1
        val MODE_MULTI = 2
    }
}
