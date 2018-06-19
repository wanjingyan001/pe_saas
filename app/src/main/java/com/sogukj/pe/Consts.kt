package com.sogukj.pe

import com.sogukj.pe.peExtended.getEnvironment

/**
 * Created by qinfei on 17/7/18.
 */
class Consts {
    companion object {
        //        val HTTP_HOST = when (Utils.getEnvironment()) {
//            "civc" -> {
//                "http://zhongdi.stockalert.cn"//中缔
//            }
//            "ht" -> {
////                "http://ht.stockalert.cn"
//                "http://hts.pewinner.com"
//            }
//            "kk" -> {
//                "http://kuake.stockalert.cn/"
//            }
//            "yge" -> {
//                "http://yager.stockalert.cn/"
//            }
//            else -> {
////                "http://dev.ht.stockalert.cn"
////                "http://pre.pe.stockalert.cn"
//                "http://prehts.pewinner.com"
//            }
//        }
        val HTTP_HOST = when (getEnvironment()) {
            "civc" -> when (BuildConfig.BUILD_TYPE) {
                "debug" -> "http://prezds.pewinner.com/"
                "release" -> "http://zds.pewinner.com/"
                else -> ""
            }
            "ht" -> when (BuildConfig.BUILD_TYPE) {
            //"debug" -> "http://prehts.pewinner.com/"
                "release" -> "http://hts.pewinner.com/"
                else -> ""
            }
            "kk" -> when (BuildConfig.BUILD_TYPE) {
            //"debug" -> "http://kuake.stockalert.cn/"
                "release" -> "http://kuakes.pewinner.com/"
                else -> ""
            }
            "yge" -> when (BuildConfig.BUILD_TYPE) {
            //"debug" -> "http://yager.stockalert.cn/"
                "release" -> "http://yagers.pewinner.com/"
                else -> ""
            }
            "sr" -> when (BuildConfig.BUILD_TYPE) {
                "debug" -> "http://srzbs.pewinner.com/"
                "release" -> "http://srzbs.pewinner.com/"
                else -> ""
            }
            "pe" -> when (BuildConfig.BUILD_TYPE) {
            //"debug" -> "http://dev.pe.stockalert.cn/"
//                "debug" -> "http://sales.pewinner.com/"
            "debug" -> "http://prehts.pewinner.com/"
            //"release" -> "http://yager.stockalert.cn/"
                else -> ""
            }
            else -> ""
        }
    }
}