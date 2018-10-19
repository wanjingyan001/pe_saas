package com.sogukj.pe.module.approve.utils

import com.sogukj.pe.baselibrary.Extended.no
import com.sogukj.pe.baselibrary.Extended.otherWise
import com.sogukj.pe.baselibrary.Extended.yes
import java.math.BigDecimal

/**
 * Created by admin on 2018/10/8.
 */
class NumberToCN {
    companion object {
        /**
         * 汉语中数字大写
         */
        private val CN_UPPER_NUMBER = arrayOf("零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖")
        /**
         * 汉语中货币单位大写
         */
        private val CN_UPPER_MONETRAY_UNIT = arrayOf("分", "角", "元",
                "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟", "兆", "拾",
                "佰", "仟")
        /**
         * 特殊字符"整"
         */
        private val CN_FULL = "整"
        /**
         * 特殊字符"负"
         */
        private val CN_NEGATIVE = "负"
        /**
         *  金额的精度，默认值为2
         */
        private val MONEY_PRECISION = 2
        /**
         * 特殊字符：零元整
         */
        private val CN_ZERO_FULL = "零元" + CN_FULL

        @JvmStatic
        fun money2CNUnit(money: String): String {
            money.isEmpty().yes {
                return ""
            }
            val numberOfMoney = BigDecimal.valueOf(money.toDouble())
            val buffer = StringBuffer()
            val signum = numberOfMoney.signum()
            // 零元整的情况
            if (signum == 0) {
                return CN_ZERO_FULL
            }
            //这里会进行金额的四舍五入
            var number = numberOfMoney.movePointRight(MONEY_PRECISION).setScale(0, 4).abs().toLong()
            // 得到小数点后两位值
            val scale = number % 100
            var numUnit = 0
            var numIndex = 0
            var getZero = false
            // 判断最后两位数，一共有四中情况：00 = 0, 01 = 1, 10, 11
            if (scale <= 0) {
                numIndex = 2
                number /= 100
                getZero = true
            }
            if ((scale > 0) && (scale % 10 <= 0)) {
                numIndex = 1
                number /= 10
                getZero = true
            }
            var zeroSize = 0
            while (true) {
                if (number <= 0) {
                    break
                }
                // 每次获取到最后一个数
                numUnit = ((number % 10).toInt())
                (numUnit > 0).yes {
                    ((numIndex == 9) && (zeroSize >= 3)).yes {
                        buffer.insert(0, CN_UPPER_MONETRAY_UNIT[6])
                    }
                    ((numIndex == 13) && (zeroSize >= 3)).yes {
                        buffer.insert(0, CN_UPPER_MONETRAY_UNIT[10])
                    }
                    buffer.insert(0, CN_UPPER_MONETRAY_UNIT[numIndex])
                    buffer.insert(0, CN_UPPER_NUMBER[numUnit])
                    getZero = false
                    zeroSize = 0
                }.otherWise {
                    ++zeroSize
                    getZero.no {
                        buffer.insert(0, CN_UPPER_NUMBER[numUnit])
                    }
                    if (numIndex == 2) {
                        (number > 0).yes {
                            buffer.insert(0, CN_UPPER_MONETRAY_UNIT[numIndex])
                        }
                    } else if (((numIndex - 2) % 4 == 0) && (number % 1000 > 0)) {
                        buffer.insert(0, CN_UPPER_MONETRAY_UNIT[numIndex])
                    }
                    getZero = true
                }
                // 让number每次都去掉最后一个数
                number /= 10
                ++numIndex
            }
            // 如果signum == -1，则说明输入的数字为负数，就在最前面追加特殊字符：负
            (signum == -1).yes {
                buffer.insert(0, CN_NEGATIVE)
            }
            // 输入的数字小数点后两位为"00"的情况，则要在最后追加特殊字符：整
            (scale <= 0).yes {
                buffer.append(CN_FULL)
            }
            return buffer.toString()
        }
    }
}