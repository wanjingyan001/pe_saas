package com.sogukj.pe.widgets

import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils
import com.sogukj.pe.R.id.point
import java.util.regex.Pattern

/**
 * Created by admin on 2018/9/27.
 */
class MoneyFilter:InputFilter {
    private val mPattern = Pattern.compile("([0-9]|\\.)*")
    private val POINTER_AFTER_LENGTH = 2
    private val POINTER = "."
    private val ZERO = "0"

    override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence {
        var sourceText = source.toString()
        val destText = dest.toString()

        //验证删除等按键
        if (TextUtils.isEmpty(sourceText)) {
            return  ""
        }

        //如果是0开头，则下一位必须是点
        if (destText.startsWith(ZERO)){
            if (!TextUtils.equals(POINTER,source) && destText.length == 1){
                sourceText = ""
            }
            //0和点之间不能输入任何数字
            else if (destText.contains(POINTER)){
                val point = destText.indexOf(POINTER)
                if (dstart in 1..point){
                    sourceText = ""
                }
            }
            else if (!destText.contains(POINTER)
                    && dstart != 0 && destText.length != 1){
                sourceText = ""
            }
        }

        //不能将0放在字符串的第一位
        if (!TextUtils.isEmpty(destText) && dstart == 0
                && TextUtils.equals(ZERO,sourceText)
                && !destText.startsWith(POINTER)){
            sourceText = ""
        }

        //第一位不能输入“.”
        if (TextUtils.isEmpty(destText) && TextUtils.equals(sourceText,POINTER)){
            sourceText = ""
        }

        //如果第一位为“.”,则点号后面不能继续输入
        if (destText.startsWith(POINTER) && dstart != 0){
            sourceText = ""
        }

        //保留POINTER_AFTER_LENGTH位小数
        if (!destText.contains(POINTER) && TextUtils.equals(POINTER,sourceText)){
            val decimal = destText.length - dstart
            if (decimal > 2){
                sourceText = ""
            }
        }else if (destText.contains(POINTER)){
            val point = destText.indexOf(POINTER)
            if (dstart > point && (destText.length - point) >= 3){
                sourceText = ""
            }
        }

        return  dest?.subSequence(dstart, dend) + sourceText
    }



}

private operator fun CharSequence?.plus(sourceText: String): CharSequence {
    return StringBuilder(this).append(sourceText)
}
