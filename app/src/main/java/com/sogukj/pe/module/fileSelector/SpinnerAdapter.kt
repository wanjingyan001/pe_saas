package com.sogukj.pe.module.fileSelector

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.sogukj.pe.R

/**
 * Created by admin on 2018/3/1.
 */
class SpinnerAdapter(val content: Context, val dirs: Array<String>) : BaseAdapter() {

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view: View? = convertView
//        view = LayoutInflater.from(content).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)
        view = LayoutInflater.from(content).inflate(R.layout.file_spinner_dropdown_item, parent, false)
        if (view != null) {
            val textView = view.findViewById<TextView>(R.id.text1) as TextView
            textView.text = dirs[position]
        }
        return view!!
    }

    override fun getItem(position: Int): Any = dirs[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = dirs.size
}