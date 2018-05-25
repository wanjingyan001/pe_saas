package com.sogukj.pe.peUtils

import android.widget.TextView
import com.sogukj.pe.R
import java.util.*

/**
 * Created by qff on 2016/4/23.
 */
class LoginTimer(sleep: Int, internal var handler: android.os.Handler, internal var tv: TextView) : TimerTask() {
    internal var sleep = 60

    init {
        this.sleep = sleep
    }

    override fun run() {
        handler.post {
            if (sleep <= 0) {
                cancel()
                tv.setText(R.string.get_code)
                tv.isClickable = true
            } else {
                tv.text = tv.context.getString(R.string.get_code_disable, sleep)
                tv.isClickable = false
                sleep--
            }
        }
    }
}
