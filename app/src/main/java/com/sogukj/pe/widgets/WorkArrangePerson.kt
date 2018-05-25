package com.sogukj.pe.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sogukj.pe.R
import com.sogukj.pe.bean.UserBean
import org.jetbrains.anko.find

/**
 * Created by admin on 2018/3/9.
 */
class WorkArrangePerson : LinearLayout {
    lateinit var inflate: View
    lateinit var personNumTv: TextView
    private val personViews = ArrayList<ImageView>()


    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }


    fun init() {
        inflate = LayoutInflater.from(context).inflate(R.layout.layout_work_attend_person, this, true)
        personViews.add(inflate.find(R.id.person1))
        personViews.add(inflate.find(R.id.person2))
        personViews.add(inflate.find(R.id.person3))
        personNumTv = inflate.find(R.id.personNum)
        setPersons(ArrayList())
    }

    @SuppressLint("SetTextI18n")
    fun setPersons(persons: ArrayList<UserBean>) {
        if (persons.isEmpty()) {
            personViews.forEach {
                it.visibility = View.GONE
                personNumTv.visibility = View.GONE
            }
        } else {
            personNumTv.visibility = View.VISIBLE
            val data: List<UserBean> = if (persons.size > 3) {
                persons.dropLast(persons.size - 3)
            } else {
                persons
            }
            personNumTv.text = "${persons.size}人"
            personViews.forEachIndexed { index, imageView ->
                if (index < data.size) {
                    imageView.visibility = View.VISIBLE
                    Glide.with(context)
                            .load(data[index].url)
                            .apply(RequestOptions().placeholder(R.drawable.default_head))
                            .into(imageView)
                } else {
                    imageView.visibility = View.GONE
                }
            }
        }

    }

}