package com.sogukj.pe.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sogukj.pe.R
import com.sogukj.pe.bean.UserBean
import org.jetbrains.anko.find
import org.jetbrains.anko.layoutInflater

/**
 * Created by admin on 2018/3/9.
 */
class WorkArrangePersonView(context: Context?) : ViewGroup(context) {
    lateinit var inflate: View
    lateinit var personNumTv: TextView
    private val personViews = ArrayList<CircleImageView>()

    init {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet) : this(context){
        init()
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

    }

    fun init() {
        inflate = context.layoutInflater.inflate(R.layout.layout_work_attend_person, null)
        personViews.add(inflate.find(R.id.person1))
        personViews.add(inflate.find(R.id.person2))
        personViews.add(inflate.find(R.id.person3))
        personNumTv = inflate.find(R.id.personNum)
    }

    @SuppressLint("SetTextI18n")
    fun setPersons(persons: ArrayList<UserBean>) {
        val data: List<UserBean> = if (persons.size > 3) {
            persons.dropLast(persons.size - 3)
        } else {
            persons
        }
        personNumTv.text = "${persons.size}人"
        personViews.forEachIndexed { index, imageView ->
            if (index < data.size) {
                imageView.visibility = View.VISIBLE
                if(data[index].url.isNullOrEmpty()){
                    imageView.setChar(data[index].name.first())
                } else {
                    Glide.with(context)
                            .load(data[index].url)
                            .apply(RequestOptions().placeholder(R.drawable.default_head))
                            .into(imageView)
                }
            }else{
                imageView.visibility = View.GONE
            }
        }
        invalidate()
    }

}