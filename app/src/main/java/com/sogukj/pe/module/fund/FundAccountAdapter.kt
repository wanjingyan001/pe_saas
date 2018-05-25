package com.sogukj.pe.module.fund

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sogukj.pe.R


/**
 * Created by admin on 2017/11/24.
 */
class FundAccountAdapter(val ctx: Context?, val dataList: Map<String, String?>) : Adapter<FundAccountAdapter.FundAccountHolder>() {
    private var keyList = ArrayList<String>()
    private var valueList = ArrayList<String?>()

    init {
        val iterator = dataList.entries.iterator()
        while (iterator.hasNext()) {
            keyList.add(iterator.next().key)
            valueList.add(iterator.next().value)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FundAccountHolder {
        val inflate = LayoutInflater.from(ctx).inflate(R.layout.item_fund_account_list, parent, false)
        return FundAccountHolder(inflate)
    }

    override fun onBindViewHolder(holder: FundAccountHolder, position: Int) {
        holder.keyTv.text = keyList[position]
        holder.valueTv.text = valueList[position]
    }

    override fun getItemCount(): Int = dataList.size

    fun setData(dataList: Map<String, String?>) {
        keyList.clear()
        valueList.clear()
        val iterator = dataList.entries.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            keyList.add(next.key)
            valueList.add(next.value)
        }
        keyList.reverse()
        valueList.reverse()
        notifyDataSetChanged()
    }


    inner class FundAccountHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        var keyTv = itemView?.findViewById<TextView>(R.id.key_tv) as TextView
        var valueTv = itemView?.findViewById<TextView>(R.id.value_tv) as TextView
    }
}