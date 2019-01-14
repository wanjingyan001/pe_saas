package com.sogukj.pe.peUtils

import android.widget.TextView
import com.sogukj.pe.App
import com.sogukj.pe.R
import java.math.BigDecimal

/**
 * Created by CH-ZH on 2018/8/30.
 */
class StockUtil {
    companion object {
        fun setCacheZuiXinJiaText(tv: TextView?, zuixinjia: Float, compare: Float, tingpai: Int, format: String) {
            if (tv == null) return

            if (tingpai === 1) {
                tv.text = format
                tv.setTextColor(App.INSTANCE.resources.getColor(R.color.colorGray))
            } else {
                tv.text = String.format("%.2f", zuixinjia)

                when (compareTo(compare, 0.0f)) {
                    0 -> tv.setTextColor(App.INSTANCE.resources.getColor(R.color.colorGray))
                    1 -> tv.setTextColor(App.INSTANCE.resources.getColor(R.color.colorRed))
                    -1 -> tv.setTextColor(App.INSTANCE.resources.getColor(R.color.colorGreen))
                }
            }
        }
        fun compareTo(a: Float, b: Float): Int {
            val bigDecimal1 = BigDecimal(a.toDouble())
            val bigDecimal2 = BigDecimal(b.toDouble())
            return bigDecimal1.compareTo(bigDecimal2)
        }

        fun setColorText(tv: TextView?, value: Float, tingpai: Int, format: String) {
            if (tv == null) return
            if (tingpai == 1) {
                tv.text = "-"
                tv.setTextColor(App.INSTANCE.resources.getColor(R.color.colorGray))
            } else {
                tv.text = String.format("%.2f", value) + format
                when (compareTo(value, 0.0f)) {
                    0 -> tv.setTextColor(App.INSTANCE.resources.getColor(R.color.colorGray))
                    1 -> tv.setTextColor(App.INSTANCE.resources.getColor(R.color.colorRed))
                    -1 -> tv.setTextColor(App.INSTANCE.resources.getColor(R.color.colorGreen))
                }
            }
        }

        fun setColorText(tv: TextView?, value: Float, zuoshou: Float, tingpai: Int) {
            if (tv == null) return
            if (value > 0) {
                tv.text = String.format("%.2f", value)
                when (compareTo(value, zuoshou)) {
                    0 -> tv.setTextColor(App.INSTANCE.resources.getColor(R.color.colorGray))
                    1 -> tv.setTextColor(App.INSTANCE.resources.getColor(R.color.colorRed))
                    -1 -> tv.setTextColor(App.INSTANCE.resources.getColor(R.color.colorGreen))
                }
            }
            if (tingpai == 1) {
                tv.text = "-"
                tv.setTextColor(App.INSTANCE.resources.getColor(R.color.colorGray))
            }
        }

        fun setText(tv: TextView?, value: Float, tingpai: Int, format: String) {
            if (tv == null) return

            tv.text = String.format("%.2f", value) + format
            tv.setTextColor(App.INSTANCE.resources.getColor(R.color.black_28))
        }

        fun setText(tv: TextView?, value: String?, tingpai: Int, format: String) {
            if (tv == null) return

            if (tingpai == 1) {
                tv.setTextColor(tv.resources.getColor(R.color.black_28))
                tv.text = "0.00%"
            } else {
                if (null != value && "null" != value && value.length > 0) {
                    tv.text = value + format
                    tv.setTextColor(tv.resources.getColor(R.color.black_28))
                } else {
                    tv.text = "0.00%"
                }
            }
        }

        fun setColorText(tv: TextView?, value: Float, zuoshou: Float, tingpai: Int, format: String) {
            if (tv == null) return

            if (tingpai == 1) {
                tv.setTextColor(tv.resources.getColor(R.color.colorGray))
                tv.text = String.format("%.2f", value) + format
            } else {
                when (compareTo(value, zuoshou)) {
                    0 -> {
                        tv.text = String.format("%.2f", value) + format
                        tv.setTextColor(tv.resources.getColor(R.color.colorGray))
                    }
                    1 -> {
                        tv.text = String.format("%.2f", value) + format
                        tv.setTextColor(tv.resources.getColor(R.color.colorRed))
                    }
                    -1 -> {
                        tv.text = String.format("%.2f", value) + format
                        tv.setTextColor(tv.resources.getColor(R.color.colorGreen))
                    }
                }
            }
        }

        fun setTextUnit(tv: TextView?, values: Long, tingpai: Int) {
            if (tv == null) return
            tv.setTextColor(App.INSTANCE.resources.getColor(R.color.black_28))
            tv.text = coverUnit(values)
        }

        fun coverUnit(values: Long): String {
            try {
                val unit = arrayOf("", "万", "亿", "万亿")
                var value = java.lang.Float.parseFloat(values.toString()).toDouble()

                val length = if (values < 0) values.toString().length - 1 else values.toString().length
                var count = length / 4 - if (length % 4 == 0) 1 else 0
                val sum = count

                while (count > 0) {
                    value /= 10000.0
                    count--
                }

                val temp = value.toString()
                val index = temp.indexOf(".")

                if (index < 3) {
                    return String.format("%.2f", value) + unit[sum]
                } else {
                    when (index) {
                        3 -> return String.format("%.1f", value) + unit[sum]
                        else -> return temp.substring(0, index) + unit[sum]
                    }
                }
            } catch (e: Exception) {
            }

            return "" + values
        }
        fun setChengJiaoLiangText(tv: TextView?, chengjiaoliang: Long, tingpai: Int) {
            if (tv == null) return

            if (tingpai == 1) {
                tv.setTextColor(tv.resources.getColor(R.color.black_28))
                tv.text = coverUnit(chengjiaoliang / 100)
            } else {
                tv.text = coverUnit(chengjiaoliang / 100)
            }
        }

        fun setChengJiaoEText(tv: TextView?, chengjiaoe: Long, tingpai: Int) {
            if (tv == null) return

            if (tingpai == 1) {
                tv.setTextColor(tv.resources.getColor(R.color.black_28))
                tv.text = coverUnit(chengjiaoe)
            } else {
                tv.text = coverUnit(chengjiaoe)
            }
        }

        /*
        SH60####

        SH9#####

        SZ200###

        SZ00####

        SZ30####
       */
        fun isStock(code: String?): Boolean {
            return null != code && (code.startsWith("SH60") || code.startsWith("SH9") || code.startsWith("SZ200") || code.startsWith("SZ00")
                    || code.startsWith("SZ30"))
        }
    }
}