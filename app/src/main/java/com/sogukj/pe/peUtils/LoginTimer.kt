package com.sogukj.pe.peUtils

import android.widget.TextView
import com.sogukj.pe.R
import java.util.*

/**
 * Created by qff on 2016/4/23.
 */
class LoginTimer(sleep: Int, internal var handler: android.os.Handler, internal var tv: TextView) : TimerTask() {
    private var sleep = 45

    init {
        this.sleep = sleep
    }

    override fun run() {
        handler.post {
            if (sleep <= 0) {
                cancel()
                tv.text = "重新获取验证码"
                tv.isEnabled = true
            } else {
                tv.text ="${sleep}s 重新发送"
                tv.isEnabled = false
                sleep--
            }
        }
    }
}
