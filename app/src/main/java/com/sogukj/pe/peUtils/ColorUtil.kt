package com.sogukj.pe.util

import android.graphics.Color
import android.widget.ImageView
import android.widget.TextView
import com.sogukj.pe.bean.ApprovalBean
import com.sogukj.pe.bean.MessageBean
import com.sogukj.pe.bean.VacationBean
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.textColor

/**
 * Created by qinfei on 17/11/7.
 */
object ColorUtil {
    fun setColorStatus(view: TextView, bean: MessageBean) {
//        -1 -> "审批未通过"
//        1 -> "待审批"
//        2 -> "已审批"
//        4 -> "审批通过"
        view.text = bean.status_str
        view.textColor = when (bean.status) {
            4 -> Color.parseColor("#50d59d")
            2 -> Color.parseColor("#a0a4aa")
            1 -> Color.parseColor("#ffa715")
            -1 -> Color.parseColor("#ff5858")
            else -> Color.parseColor("#ffa715")
        }
    }

    fun setColorStatus(view: TextView, bean: ApprovalBean) {
        view.text = bean.status_str
//        view.textColor = when (bean.status_str) {
//            "完成用印" -> Color.parseColor("#A0A4AA")
//            "签发完成" -> Color.parseColor("#A0A4AA")
//            "签发中" -> Color.parseColor("#806af2")
//            "待用印" -> Color.parseColor("#806af2")
//            "审批完成" -> Color.parseColor("#50d59d")
//            "审批通过" -> Color.parseColor("#50d59d")
//            "审批未通过" -> Color.parseColor("#ff5858")
//            "签字中" -> Color.parseColor("#4aaaf4")
//            "审批中" -> Color.parseColor("#4aaaf4")
//            "待签字" -> Color.parseColor("#ffa715")
//            "待审批" -> Color.parseColor("#ffa715")
//            else -> Color.parseColor("#ffa715")
//        }
        view.textColor = when (bean.status) {//0=>待审批，1=>审批中，4=>审批通过
            -1 -> Color.parseColor("#ffff5858")
            0 -> Color.parseColor("#ffffa715")
            1 -> Color.parseColor("#ff4aaaf4")
            2 -> Color.parseColor("#50D59D")
            3 -> Color.parseColor("#806AF2")
            4 -> Color.parseColor("#50d59d")
            5 -> Color.parseColor("#d9d9d9")
            else -> Color.parseColor("#50d59d")
        }
    }

    // 出差请假=>（0待审批，1审批中，4审批通过，5已撤销）
    // 用印审批=>（0待审批，1审批中，2审批通过，3待用印，4完成用印 ）
    // 签字审批=>（0待签字 1签字中 3签发中 4签发完成

    fun setColorStatus(view: ImageView, bean: VacationBean) {
        view.backgroundColor = when (bean.id) {
            1 -> Color.parseColor("#a2c0e3")//事假
            2 -> Color.parseColor("#aed4ab")//病假
            3 -> Color.parseColor("#e4c2c2")//年假
            4 -> Color.parseColor("#dfbfe6")//婚假
            5 -> Color.parseColor("#e8e2b7")//丧假
            else -> Color.parseColor("#eacec0")
        }
    }
}