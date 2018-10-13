package com.sogukj.pe.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.sogukj.pe.R
import com.sogukj.pe.bean.ProjectApproveInfo
import java.util.*

/**
 * Created by CH-ZH on 2018/9/19.
 */
class ProjectIndexView : LinearLayout {
    private var mContext : Context ? = null
    private var mRootView : View? = null
    private var sortFrames = ArrayList<ProjectApproveInfo.ApproveInfo>()
    private var tv_value1 : TextView? = null
    private var tv_value2 : TextView? = null
    private var tv_value3 : TextView? = null
    private var tv_value4 : TextView? = null
    private var tv1 : TextView ? = null
    private var ll_2 : LinearLayout ? = null
    private var tv2 : TextView ? = null
    private var tv3 : TextView ? = null
    private var ll_3 : LinearLayout ? = null
    private var tv4 : TextView ? = null
    private var tv5 : TextView ? = null
    private var tv6 : TextView ? = null
    private var project_chart : ProjectLineChartView ? = null
    private var amounts = ArrayList<Float>()
    constructor(context: Context) : super(context){
        mContext = context
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mContext = context
        initView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mContext = context
        initView()
    }

    private fun initView() {
        mRootView = View.inflate(mContext, R.layout.layout_project_index,this)
        tv_value1 = mRootView!!.findViewById(R.id.tv_value1)
        tv_value2 = mRootView!!.findViewById(R.id.tv_value2)
        tv_value3 = mRootView!!.findViewById(R.id.tv_value3)
        tv_value4 = mRootView!!.findViewById(R.id.tv_value4)
        tv1 = mRootView!!.findViewById(R.id.tv1)
        tv2 = mRootView!!.findViewById(R.id.tv2)
        tv3 = mRootView!!.findViewById(R.id.tv3)
        tv4 = mRootView!!.findViewById(R.id.tv4)
        tv5 = mRootView!!.findViewById(R.id.tv5)
        tv6 = mRootView!!.findViewById(R.id.tv6)
        ll_2 = mRootView!!.findViewById(R.id.ll_2)
        ll_3 = mRootView!!.findViewById(R.id.ll_3)
        project_chart = mRootView!!.findViewById(R.id.project_chart)
    }

    open fun setAmountData(frames: List<ProjectApproveInfo.ApproveInfo>) {
        sortFrames.clear()
        amounts.clear()
        if (null != frames && frames.size > 0){
            for (frame in frames) {
                if (!frame.asset.isNullOrEmpty()){
                    amounts.add(frame.asset.toFloat())
                }
                if (!frame.income.isNullOrEmpty()){
                    amounts.add(frame.income.toFloat())
                }
                if (!frame.profit.isNullOrEmpty()){
                    amounts.add(frame.profit.toFloat())
                }
            }
        }
        if (amounts.size <= 0) return
        val max = Collections.max(amounts)

        val space = max / 3
        val value1 = max
        val value4 = 0
        val value3 = 0 + space
        val value2 = max - space
        tv_value1!!.text = value1.toString()
        tv_value2!!.text = value2.toString()
        tv_value3!!.text = value3.toString()
        tv_value4!!.text = value4.toString()

        if (null != frames && frames.size == 3){
            val year1 = frames[0].year
            val year2 = frames[1].year
            val year3 = frames[2].year
            var realYear1 = ""
            var realYear2 = ""
            var realYear3 = ""
            val years = ArrayList<Int>()
            if (null != year1 && year1.length >= 4 && !year1.startsWith("__年")){
                realYear1 = year1.substring(0,4)
            }else{
                realYear1 = ""
            }

            if (null != year2 && year2.length >= 4 && !year1.startsWith("__年")){
                realYear2 = year2.substring(0,4)
            }else{
                realYear2 = ""
            }

            if (null != year3 && year3.length >= 4 && !year1.startsWith("__年")){
                realYear3 = year3.substring(0,4)
            }else{
                realYear3 = ""
            }

            if (!"".equals(realYear1)){
                years.add(realYear1.toInt())
            }
            if (!"".equals(realYear2)){
                years.add(realYear2.toInt())
            }
            if (!"".equals(realYear3)){
                years.add(realYear3.toInt())
            }

            val maxYear = Collections.max(years)
            val minYear = Collections.min(years)
            if (years.size == 1){

                tv1!!.text = maxYear.toString()
                tv1!!.visibility = View.VISIBLE
                ll_2!!.visibility = View.INVISIBLE
                ll_3!!.visibility = View.INVISIBLE

            }else if (years.size == 2){

                tv2!!.text = minYear.toString()
                tv3!!.text = maxYear.toString()
                tv1!!.visibility = View.INVISIBLE
                ll_2!!.visibility = View.VISIBLE
                ll_3!!.visibility = View.INVISIBLE

            }else if (years.size == 3){

                tv4!!.text = minYear.toString()
                tv6!!.text = maxYear.toString()

                tv1!!.visibility = View.INVISIBLE
                ll_2!!.visibility = View.INVISIBLE
                ll_3!!.visibility = View.VISIBLE

                for (year in years){
                    if (year != maxYear && year != minYear){
                        tv5!!.text = year.toString()
                    }
                }
            }

            project_chart!!.fitDataAndInvalite(frames,max,maxYear,minYear,years.size,realYear1,realYear2,realYear3)
        }

    }
}