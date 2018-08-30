package com.sogukj.pe.service.socket

/**
 * Created by CH-ZH on 2018/8/30.
 */
class WsEvent {
    var state:Int = 0
    var data:String = ""

    constructor(state : Int){
        this.state = state
    }

    constructor(state : Int, data : String){
        this.state = state
        this.data = data
    }
}