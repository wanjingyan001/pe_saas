package com.sogukj.pe.service.socket

import org.greenrobot.eventbus.EventBus

/**
 * Created by CH-ZH on 2018/8/30.
 */
class BusProvider {
    companion object {
        private val BUS = EventBus.getDefault()
        fun getInstance(): EventBus {
            return BUS
        }
    }
}