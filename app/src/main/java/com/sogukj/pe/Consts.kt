package com.sogukj.pe

import com.sogukj.pe.peExtended.getEnvironment

/**
 * Created by qinfei on 17/7/18.
 */
class Consts {
    companion object {
        val HTTP_HOST = when (getEnvironment()) {
            "civc" -> {
                "http://zhongdi.pe.stockalert.cn/"//中缔
            }
            "ht" -> {
//                "http://ht.stockalert.cn"
                "http://hts.pewinner.com"
            }
            "kk" -> {
                "http://kuake.stockalert.cn/"
            }
            "yge" -> {
                "http://yager.stockalert.cn/"
            }
            else -> {
//                "http://dev.ht.stockalert.cn"
//                "http://pre.pe.stockalert.cn"
                "http://prehts.pewinner.com"
            }
        }

    }
}