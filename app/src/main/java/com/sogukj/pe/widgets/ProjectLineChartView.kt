package com.sogukj.pe.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.sogukj.pe.R
import com.sogukj.pe.baselibrary.utils.Utils
import com.sogukj.pe.bean.ApproveFrameInfo
import com.sogukj.pe.bean.ProjectApproveInfo
import java.util.*

/**
 * Created by CH-ZH on 2018/9/19.
 */
class ProjectLineChartView : View {
    private var mContext: Context? = null
    private var paintRed: Paint? = null
    private var paintBlue: Paint? = null
    private var paintGreen: Paint? = null

    private var paintCircleRed : Paint? = null
    private var paintCircleBlue : Paint? = null
    private var paintCircleGrreen : Paint? = null
    private var property1 = 0f
    private var property2 = 0f
    private var property3 = 0f

    private var realProperty1 = 0f
    private var realProperty2 = 0f
    private var realProperty3 = 0f

    private var income1 = 0f
    private var income2 = 0f
    private var income3 = 0f

    private var realIncome1 = 0f
    private var realIncome2 = 0f
    private var realIncome3 = 0f

    private var profit1 = 0f
    private var profit2 = 0f
    private var profit3 = 0f

    private var realProfit1 = 0f
    private var realProfit2 = 0f
    private var realProfit3 = 0f

    private var type = -1
    private var maxValue = 0f
    private val circleRadius = 6f
    constructor(context: Context) : super(context) {
        mContext = context
        initPaint()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mContext = context
        initPaint()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mContext = context
        initPaint()
    }

    private fun initPaint() {
        paintRed = Paint(Paint.ANTI_ALIAS_FLAG)
        paintRed!!.style = Paint.Style.STROKE
        paintRed!!.isAntiAlias = true
        paintRed!!.color = resources.getColor(R.color.red_f01)
        paintRed!!.strokeWidth = 3f

        paintBlue = Paint(Paint.ANTI_ALIAS_FLAG)
        paintBlue!!.style = Paint.Style.STROKE
        paintBlue!!.isAntiAlias = true
        paintBlue!!.color = resources.getColor(R.color.blue_17)
        paintBlue!!.strokeWidth = 3f

        paintGreen = Paint(Paint.ANTI_ALIAS_FLAG)
        paintGreen!!.style = Paint.Style.STROKE
        paintGreen!!.isAntiAlias = true
        paintGreen!!.color = resources.getColor(R.color.green_50d)
        paintGreen!!.strokeWidth = 3f

        paintCircleRed = Paint(Paint.ANTI_ALIAS_FLAG)
        paintCircleRed!!.style = Paint.Style.FILL
        paintCircleRed!!.isAntiAlias = true
        paintCircleRed!!.color = resources.getColor(R.color.red_f01)

        paintCircleBlue = Paint(Paint.ANTI_ALIAS_FLAG)
        paintCircleBlue!!.style = Paint.Style.FILL
        paintCircleBlue!!.isAntiAlias = true
        paintCircleBlue!!.color = resources.getColor(R.color.blue_17)

        paintCircleGrreen = Paint(Paint.ANTI_ALIAS_FLAG)
        paintCircleGrreen!!.style = Paint.Style.FILL
        paintCircleGrreen!!.isAntiAlias = true
        paintCircleGrreen!!.color = resources.getColor(R.color.green_50d)
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (type == 1){
            drawLineChart1(canvas!!)
        }else if (type == 2){
            drawLineChart2(canvas!!)
        }else if (type == 3){
            drawLineChart3(canvas!!)
        }

    }
    private var propertyHeight1 = 0f
    private var propertyHeight2 = 0f
    private var propertyHeight3 = 0f

    private var incomeHeight1 = 0f
    private var incomeHeight2 = 0f
    private var incomeHeight3 = 0f

    private var profitHeight1 = 0f
    private var profitHeight2 = 0f
    private var profitHeight3 = 0f

    private fun drawLineChart3(canvas: Canvas) {
        val exProperty1 = (maxValue - realProperty1).toFloat()
        val exIncome1 = (maxValue - realIncome1).toFloat()
        val exProfit1 = (maxValue - realProfit1).toFloat()

        val exProperty2 = (maxValue - realProperty2).toFloat()
        val exIncome2 = (maxValue - realIncome2).toFloat()
        val exProfit2 = (maxValue - realProfit2).toFloat()

        val exProperty3 = (maxValue - realProperty3).toFloat()
        val exIncome3 = (maxValue - realIncome3).toFloat()
        val exProfit3 = (maxValue - realProfit3).toFloat()

        propertyHeight1 = exProperty1 / maxValue.toFloat() * height.toFloat()
        incomeHeight1 = exIncome1 / maxValue.toFloat() * height.toFloat()
        profitHeight1 = exProfit1 / maxValue.toFloat() * height.toFloat()

        propertyHeight2 = exProperty2 / maxValue.toFloat() * height.toFloat()
        incomeHeight2 = exIncome2 / maxValue.toFloat() * height.toFloat()
        profitHeight2 = exProfit2 / maxValue.toFloat() * height.toFloat()

        propertyHeight3 = exProperty3 / maxValue.toFloat() * height.toFloat()
        incomeHeight3 = exIncome3 / maxValue.toFloat() * height.toFloat()
        profitHeight3 = exProfit3 / maxValue.toFloat() * height.toFloat()
        setHeightLimit()

        canvas.drawCircle(width/6.toFloat(),propertyHeight1,circleRadius,paintCircleRed)
        canvas.drawCircle(width/2.toFloat(),propertyHeight2,circleRadius,paintCircleRed)
        canvas.drawCircle(width/6*5.toFloat(),propertyHeight3,circleRadius,paintCircleRed)

        canvas.drawCircle(width/6.toFloat(),incomeHeight1,circleRadius,paintCircleBlue)
        canvas.drawCircle(width/2.toFloat(),incomeHeight2,circleRadius,paintCircleBlue)
        canvas.drawCircle(width/6*5.toFloat(),incomeHeight3,circleRadius,paintCircleBlue)

        canvas.drawCircle(width/6.toFloat(),profitHeight1,circleRadius,paintCircleGrreen)
        canvas.drawCircle(width/2.toFloat(),profitHeight2,circleRadius,paintCircleGrreen)
        canvas.drawCircle(width/6*5.toFloat(),profitHeight3,circleRadius,paintCircleGrreen)

        val pathRed = Path()
        pathRed.moveTo(width/6.toFloat(),propertyHeight1)
        pathRed.lineTo(width/2.toFloat(),propertyHeight2)
        pathRed.lineTo(width/6*5.toFloat(),propertyHeight3)
        canvas.drawPath(pathRed,paintRed)

        val pathBlue = Path()
        pathBlue.moveTo(width/6.toFloat(),incomeHeight1)
        pathBlue.lineTo(width/2.toFloat(),incomeHeight2)
        pathBlue.lineTo(width/6*5.toFloat(),incomeHeight3)
        canvas.drawPath(pathBlue,paintBlue)

        val pathGreen = Path()
        pathGreen.moveTo(width/6.toFloat(),profitHeight1)
        pathGreen.lineTo(width/2.toFloat(),profitHeight2)
        pathGreen.lineTo(width/6*5.toFloat(),profitHeight3)
        canvas.drawPath(pathGreen,paintGreen)
    }

    private fun setHeightLimit() {
        if (propertyHeight1 <= 5.0f){
            propertyHeight1 += 10.0f
        }
        if (propertyHeight1 >= height - 5.0f){
            propertyHeight1 -= 10.0f
        }

        if (propertyHeight2 <= 5.0f){
            propertyHeight2 += 10.0f
        }
        if (propertyHeight2 >= height - 5.0f){
            propertyHeight2 -= 10.0f
        }

        if (propertyHeight3 <= 5.0f){
            propertyHeight3 += 10.0f
        }
        if (propertyHeight3 >= height - 5.0f){
            propertyHeight3 -= 10.0f
        }

        if (incomeHeight1 <= 5.0f){
            incomeHeight1 += 10.0f
        }
        if (incomeHeight1 >= height - 5.0f){
            incomeHeight1 -= 10.0f
        }

        if (incomeHeight2 <= 5.0f){
            incomeHeight2 += 10.0f
        }
        if (incomeHeight2 >= height - 5.0f){
            incomeHeight2 -= 10.0f
        }

        if (incomeHeight3 <= 5.0f){
            incomeHeight3 += 10.0f
        }
        if (incomeHeight3 >= height - 5.0f){
            incomeHeight3 -= 10.0f
        }


        if (profitHeight1 <= 5.0f){
            profitHeight1 += 10.0f
        }
        if (profitHeight1 >= height - 5.0f){
            profitHeight1 -= 10.0f
        }

        if (profitHeight2 <= 5.0f){
            profitHeight2 += 10.0f
        }
        if (profitHeight2 >= height - 5.0f){
            profitHeight2 -= 10.0f
        }

        if (profitHeight3 <= 5.0f){
            profitHeight3 += 10.0f
        }
        if (profitHeight3 >= height - 5.0f){
            profitHeight3 -= 10.0f
        }

    }

    private fun drawLineChart2(canvas: Canvas) {
        val exProperty1 = (maxValue - realProperty1)
        val exIncome1 = (maxValue - realIncome1)
        val exProfit1 = (maxValue - realProfit1)

        val exProperty2 = (maxValue - realProperty2)
        val exIncome2 = (maxValue - realIncome2)
        val exProfit2 = (maxValue - realProfit2)

        var propertyHeight1 = exProperty1 / maxValue * height.toFloat()
        var incomeHeight1 = exIncome1 / maxValue * height.toFloat()
        var profitHeight1 = exProfit1 / maxValue * height.toFloat()

        var propertyHeight2 = exProperty2 / maxValue * height.toFloat()
        var incomeHeight2 = exIncome2 / maxValue * height.toFloat()
        var profitHeight2 = exProfit2 / maxValue * height.toFloat()

        if (propertyHeight1 <= 5.0f){
            propertyHeight1 += 10.0f
        }
        if (propertyHeight1 >= height - 5.0f){
            propertyHeight1 -= 10.0f
        }

        if (propertyHeight2 <= 5.0f){
            propertyHeight2 += 10.0f
        }
        if (propertyHeight2 >= height - 5.0f){
            propertyHeight2 -= 10.0f
        }

        if (incomeHeight1 <= 5.0f){
            incomeHeight1 += 10.0f
        }
        if (incomeHeight1 >= height - 5.0f){
            incomeHeight1 -= 10.0f
        }

        if (incomeHeight2 <= 5.0f){
            incomeHeight2 += 10.0f
        }
        if (incomeHeight2 >= height - 5.0f){
            incomeHeight2 -= 10.0f
        }


        if (profitHeight1 <= 5.0f){
            profitHeight1 += 10.0f
        }
        if (profitHeight1 >= height - 5.0f){
            profitHeight1 -= 10.0f
        }

        if (profitHeight2 <= 5.0f){
            profitHeight2 += 10.0f
        }
        if (profitHeight2 >= height - 5.0f){
            profitHeight2 -= 10.0f
        }

        canvas.drawCircle(width/4.toFloat(),propertyHeight1,circleRadius,paintCircleRed)
        canvas.drawCircle(width/4*3.toFloat(),propertyHeight2,circleRadius,paintCircleRed)

        canvas.drawCircle(width/4.toFloat(),incomeHeight1,circleRadius,paintCircleBlue)
        canvas.drawCircle(width/4*3.toFloat(),incomeHeight2,circleRadius,paintCircleBlue)

        canvas.drawCircle(width/4.toFloat(),profitHeight1,circleRadius,paintCircleGrreen)
        canvas.drawCircle(width/4*3.toFloat(),profitHeight2,circleRadius,paintCircleGrreen)

        val pathRed = Path()
        pathRed.moveTo(width/4.toFloat(),propertyHeight1)
        pathRed.lineTo(width/4*3.toFloat(),propertyHeight2)
        canvas.drawPath(pathRed,paintRed)

        val pathBlue = Path()
        pathBlue.moveTo(width/4.toFloat(),incomeHeight1)
        pathBlue.lineTo(width/4*3.toFloat(),incomeHeight2)
        canvas.drawPath(pathBlue,paintBlue)

        val pathGreen = Path()
        pathGreen.moveTo(width/4.toFloat(),profitHeight1)
        pathGreen.lineTo(width/4*3.toFloat(),profitHeight2)
        canvas.drawPath(pathGreen,paintGreen)

    }

    private fun drawLineChart1(canvas: Canvas) {
        val exProperty = (maxValue - property1)
        val exIncome = (maxValue - income1)
        val exProfit = (maxValue - profit1)
        var propertyHeight = exProperty / maxValue * height.toFloat()
        var incomeHeight = exIncome / maxValue * height.toFloat()
        var profitHeight = exProfit / maxValue * height.toFloat()

        if (propertyHeight <= 5.0f){
            propertyHeight += 10.0f
        }
        if (propertyHeight >= height - 5.0f){
            propertyHeight -= 10.0f
        }

        if (incomeHeight <= 5.0f){
            incomeHeight += 10.0f
        }
        if (incomeHeight >= height - 5.0f){
            incomeHeight -= 10.0f
        }

        if (profitHeight <= 5.0f){
            profitHeight += 10.0f
        }
        if (profitHeight >= height - 5.0f){
            profitHeight -= 10.0f
        }

        val pathRed = Path()
        pathRed.moveTo(0f,height.toFloat())
        pathRed.lineTo(width/2.toFloat(),propertyHeight)
        canvas.drawPath(pathRed,paintRed)
        canvas.drawCircle(width/2.toFloat(),propertyHeight,circleRadius,paintCircleRed)

        val pathBlue = Path()
        pathBlue.moveTo(0f,height.toFloat())
        pathBlue.lineTo(width/2.toFloat(),incomeHeight)
        canvas.drawPath(pathBlue,paintBlue)
        canvas.drawCircle(width/2.toFloat(),incomeHeight,circleRadius,paintCircleBlue)

        val pathGreen = Path()
        pathGreen.moveTo(0f,height.toFloat())
        pathGreen.lineTo(width/2.toFloat(),profitHeight)
        canvas.drawPath(pathGreen,paintGreen)
        canvas.drawCircle(width/2.toFloat(),profitHeight,circleRadius,paintCircleGrreen)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }


    open fun fitDataAndInvalite(frames: List<ProjectApproveInfo.ApproveInfo>, maxValue: Float, maxYear: Int, minYear: Int, size: Int, realYear1: String,
                                realYear2: String, realYear3: String){
        val infos = ArrayList<ApproveFrameInfo>()
        this.maxValue = maxValue
        if (null != frames && frames.size == 3){
            for (frame in frames) {
                val info = ApproveFrameInfo()
                info.year = frame.year
                if (Utils.isInteger(frame.asset) || Utils.isDouble(frame.asset)){
                    info.property_amount = frame.asset
                }else{
                    info.property_amount = "0"
                }
                if (Utils.isInteger(frame.income) || Utils.isDouble(frame.income)){
                    info.income_amount = frame.income
                }else{
                    info.income_amount = "0"
                }
                if (Utils.isInteger(frame.profit) || Utils.isDouble(frame.profit)){
                    info.profit_amount = frame.profit
                }else{
                    info.profit_amount = "0"
                }

                infos.add(info)
            }
        }

        if (size == 1){
            type = 1
            if (!"".equals(realYear1)){
                if (!infos[0].property_amount.isNullOrEmpty()){
                    property1 = infos[0].property_amount.toFloat()
                }
                if (!infos[0].income_amount.isNullOrEmpty()){
                    income1 = infos[0].income_amount.toFloat()
                }
                if (!infos[0].profit_amount.isNullOrEmpty()){
                    profit1 = infos[0].profit_amount.toFloat()
                }
            }else if (!"".equals(realYear2)){
                if (!infos[1].property_amount.isNullOrEmpty()){
                    property1 = infos[1].property_amount.toFloat()
                }
                if (!infos[1].income_amount.isNullOrEmpty()){
                    income1 = infos[1].income_amount.toFloat()
                }
                if (!infos[1].profit_amount.isNullOrEmpty()){
                    profit1 = infos[1].profit_amount.toFloat()
                }
            }else if (!"".equals(realYear3)){
                if (!infos[2].property_amount.isNullOrEmpty()){
                    property1 = infos[2].property_amount.toFloat()
                }
                if (!infos[2].income_amount.isNullOrEmpty()){
                    income1 = infos[2].income_amount.toFloat()
                }
                if (!infos[2].profit_amount.isNullOrEmpty()){
                    profit1 = infos[2].profit_amount.toFloat()
                }
            }
        }else if (size == 2){
            type = 2
            if ("".equals(realYear1)){
                if (!infos[1].property_amount.isNullOrEmpty()){
                    property1 = infos[1].property_amount.toFloat()
                }
                if (!infos[1].income_amount.isNullOrEmpty()){
                    income1 = infos[1].income_amount.toFloat()
                }
                if (!infos[1].profit_amount.isNullOrEmpty()){
                    profit1 = infos[1].profit_amount.toFloat()
                }

                if (!infos[2].property_amount.isNullOrEmpty()){
                    property2 = infos[2].property_amount.toFloat()
                }
                if (!infos[2].income_amount.isNullOrEmpty()){
                    income2 = infos[2].income_amount.toFloat()
                }
                if (!infos[2].profit_amount.isNullOrEmpty()){
                    profit2 = infos[2].profit_amount.toFloat()
                }
                if (maxYear.toString().equals(infos[2].year.subSequence(0,4))){
                    realProperty1 = property1
                    realIncome1 = income1
                    realProfit1 = profit1

                    realProperty2 = property2
                    realIncome2 = income2
                    realProfit2 = profit2
                }else{
                    realProperty1 = property2
                    realIncome1 = income2
                    realProfit1 = profit2

                    realProperty2 = property1
                    realIncome2 = income1
                    realProfit2 = profit1
                }
            }else if ("".equals(realYear2)){
                if (!infos[0].property_amount.isNullOrEmpty()){
                    property1 = infos[0].property_amount.toFloat()
                }
                if (!infos[0].income_amount.isNullOrEmpty()){
                    income1 = infos[0].income_amount.toFloat()
                }
                if (!infos[0].profit_amount.isNullOrEmpty()){
                    profit1 = infos[0].profit_amount.toFloat()
                }

                if (!infos[2].property_amount.isNullOrEmpty()){
                    property2 = infos[2].property_amount.toFloat()
                }
                if (!infos[2].income_amount.isNullOrEmpty()){
                    income2 = infos[2].income_amount.toFloat()
                }
                if (!infos[2].profit_amount.isNullOrEmpty()){
                    profit2 = infos[2].profit_amount.toFloat()
                }

                if (maxYear.toString().equals(infos[2].year.subSequence(0,4))){
                    realProperty1 = property1
                    realIncome1 = income1
                    realProfit1 = profit1

                    realProperty2 = property2
                    realIncome2 = income2
                    realProfit2 = profit2
                }else{
                    realProperty1 = property2
                    realIncome1 = income2
                    realProfit1 = profit2

                    realProperty2 = property1
                    realIncome2 = income1
                    realProfit2 = profit1
                }
            }else if ("".equals(realYear3)){
                if (!infos[0].property_amount.isNullOrEmpty()){
                    property1 = infos[0].property_amount.toFloat()
                }
                if (!infos[0].income_amount.isNullOrEmpty()){
                    income1 = infos[0].income_amount.toFloat()
                }
                if (!infos[0].profit_amount.isNullOrEmpty()){
                    profit1 = infos[0].profit_amount.toFloat()
                }

                if (!infos[1].property_amount.isNullOrEmpty()){
                    property2 = infos[1].property_amount.toFloat()
                }
                if (!infos[1].income_amount.isNullOrEmpty()){
                    income2 = infos[1].income_amount.toFloat()
                }
                if (!infos[1].profit_amount.isNullOrEmpty()){
                    profit2 = infos[1].profit_amount.toFloat()
                }

                if (maxYear.toString().equals(infos[1].year.subSequence(0,4))){
                    realProperty1 = property1
                    realIncome1 = income1
                    realProfit1 = profit1

                    realProperty2 = property2
                    realIncome2 = income2
                    realProfit2 = profit2
                }else{
                    realProperty1 = property2
                    realIncome1 = income2
                    realProfit1 = profit2

                    realProperty2 = property1
                    realIncome2 = income1
                    realProfit2 = profit1
                }
            }
        }else if (size == 3){
            type = 3
            if (!infos[0].property_amount.isNullOrEmpty()){
                property1 = infos[0].property_amount.toFloat()
            }
            if (!infos[0].income_amount.isNullOrEmpty()){
                income1 = infos[0].income_amount.toFloat()
            }
            if (!infos[0].profit_amount.isNullOrEmpty()){
                profit1 = infos[0].profit_amount.toFloat()
            }

            if (!infos[1].property_amount.isNullOrEmpty()){
                property2 = infos[1].property_amount.toFloat()
            }
            if (!infos[1].income_amount.isNullOrEmpty()){
                income2 = infos[1].income_amount.toFloat()
            }
            if (!infos[1].profit_amount.isNullOrEmpty()){
                profit2 = infos[1].profit_amount.toFloat()
            }

            if (!infos[2].property_amount.isNullOrEmpty()){
                property3 = infos[2].property_amount.toFloat()
            }
            if (!infos[2].income_amount.isNullOrEmpty()){
                income3 = infos[2].income_amount.toFloat()
            }
            if (!infos[2].profit_amount.isNullOrEmpty()){
                profit3 = infos[2].profit_amount.toFloat()
            }

            if (maxYear.toString().equals(infos[2].year.subSequence(0,4))){
                if (minYear.toString().equals(infos[0].year.subSequence(0,4))){
                    realProperty1 = property1
                    realIncome1 = income1
                    realProfit1 = profit1

                    realProperty2 = property2
                    realIncome2 = income2
                    realProfit2 = profit2

                    realProperty3 = property3
                    realIncome3 = income3
                    realProfit3 = profit3
                }else{
                    realProperty1 = property2
                    realIncome1 = income2
                    realProfit1 = profit2

                    realProperty2 = property1
                    realIncome2 = income1
                    realProfit2 = profit1

                    realProperty3 = property3
                    realIncome3 = income3
                    realProfit3 = profit3
                }
            }else if (maxYear.toString().equals(infos[1].year.subSequence(0,4))){
                if (minYear.toString().equals(infos[0].year.subSequence(0,4))){
                    realProperty1 = property1
                    realIncome1 = income1
                    realProfit1 = profit1

                    realProperty2 = property3
                    realIncome2 = income3
                    realProfit2 = profit3

                    realProperty3 = property2
                    realIncome3 = income2
                    realProfit3 = profit2
                }else{
                    realProperty1 = property3
                    realIncome1 = income3
                    realProfit1 = profit3

                    realProperty2 = property1
                    realIncome2 = income1
                    realProfit2 = profit1

                    realProperty3 = property2
                    realIncome3 = income2
                    realProfit3 = profit2
                }
            }else if (maxYear.toString().equals(infos[0].year.subSequence(0,4))){
                if (minYear.toString().equals(infos[1].year.subSequence(0,4))){
                    realProperty1 = property2
                    realIncome1 = income2
                    realProfit1 = profit2

                    realProperty2 = property3
                    realIncome2 = income3
                    realProfit2 = profit3

                    realProperty3 = property1
                    realIncome3 = income1
                    realProfit3 = profit1
                }else{
                    realProperty1 = property3
                    realIncome1 = income3
                    realProfit1 = profit3

                    realProperty2 = property2
                    realIncome2 = income2
                    realProfit2 = profit2

                    realProperty3 = property1
                    realIncome3 = income1
                    realProfit3 = profit1
                }
            }
        }
        invalidate()
    }
}