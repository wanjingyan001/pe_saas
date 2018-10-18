package com.sogukj.pe.bean

import java.io.Serializable

/**
 * Created by CH-ZH on 2018/10/17.
 */
class MineReceiptBean : Serializable {
    var title = ""
    var time = ""
    var count = ""
    var coin = 0
    var isPay = false
    constructor(title:String,time:String,count:String,coin:Int,isPay : Boolean){
        this.title = title
        this.time = time
        this.count = count
        this.coin = coin
        this.isPay = isPay
    }
}