package com.sogukj.pe.service.socket

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import com.sogukj.pe.Consts
import com.sogukj.pe.Extras.CONNECT
import com.sogukj.pe.Extras.CONNECTED
import com.sogukj.pe.Extras.MESSAGE
import com.sogukj.pe.peUtils.Store
import okhttp3.*
import okio.ByteString
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Created by CH-ZH on 2018/8/30.
 */
class DzhClientService : Service(){
    private var webSocket : WebSocket? = null
    private var realWebSocket : WebSocket? = null
    private var isExit: Boolean = false
    private var isConnecting: Boolean = false
    private var isConnected: Boolean = false
    private var handle : Handler = object : Handler(){
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when(msg!!.what){
                CONNECT -> {
                    connect()
                }

                CONNECTED -> {
                    BusProvider.getInstance().post(WsEvent(CONNECTED))
                }

                MESSAGE -> {
                    BusProvider.getInstance().post(WsEvent(MESSAGE, msg.obj as String))
                }
            }
        }
    }

    companion object {
        fun startService(context: Context){
            var intent = Intent(context,DzhClientService::class.java)
            if (context !is Activity){
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startService(intent)
        }

        fun stopService(context: Context){
            var intent = Intent(context,DzhClientService::class.java)
            if (context !is Activity){
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.stopService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        isExit = false
        BusProvider.getInstance().register(this)
        connect()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onEvent(event: DzhEvent) {
        send(event!!.data)
    }

    private fun send(message: String) {
        Log.e("TAG","message ==" + message)
        TaskExecutor.getExecutor().async(object : Runnable{
            override fun run() {
                if (isConnected) {
                    try {
                        webSocket!!.send(message)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: IllegalStateException) {
                        e.printStackTrace()
                    }
                }
                if (message.indexOf("/cancel") == -1) {
                    handle.postDelayed(timeoutRunnable, 5000)
                }
            }

        })
    }

    private fun connect() {
        if (isExit)
            return
        if (isConnecting)
            return

        isConnected = false
        isConnecting = true

        webSocketEnqueue()
    }

    private fun webSocketEnqueue() {
        val request = Request.Builder()
                .url(Consts.DZH_HOST + "ws?token=" + Store.store.getDzhToken(applicationContext))
                .build()

        val client = OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS)
                .build()
        if (null == realWebSocket){
            realWebSocket = client.newWebSocket(request,object : WebSocketListener() {
                override fun onOpen(socket: WebSocket?, response: Response?) {
                    Log.e("TAG","onOpen --")
                    isConnected = true
                    isConnecting = false
                    webSocket = socket
                    handle.sendEmptyMessage(CONNECTED)
                }

                override fun onFailure(webSocket: WebSocket?, t: Throwable?, response: Response?) {
                    Log.e("TAG","onFailure --")
                    isConnected = false
                    isConnecting = false
                    handle.sendEmptyMessageDelayed(CONNECT, 3000)
                }

                override fun onMessage(webSocket: WebSocket?, message: String) {

                }

                override fun onMessage(webSocket: WebSocket?, bytes: ByteString?) {
                    Log.e("TAG","onMessage --")
                    isConnecting = false
                    handle.sendMessage(handle.obtainMessage(MESSAGE, bytes!!.utf8()))
                    handle.removeCallbacks(timeoutRunnable)
                }

                override fun onClosed(webSocket: WebSocket?, code: Int, reason: String?) {
                    Log.e("TAG","onClosed --")
                    isConnected = false
                }

            })
        }
        client.dispatcher().executorService().shutdown()
    }

    override fun onDestroy() {
        isExit = true
        closeConnect()
        if (null != handle){
            handle.removeCallbacksAndMessages(null)
        }
        BusProvider.getInstance().unregister(this)
        super.onDestroy()
    }

    private fun closeConnect() {
        if (isConnected){
            TaskExecutor.getExecutor().async(object : Runnable{
                override fun run() {
                    try {
                        webSocket!!.close(1000, "shutdown")
                    } catch (e: IllegalStateException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }

            })
        }
    }

    private var timeoutRunnable : Runnable = object : Runnable{
        override fun run() {
            connect()
        }
    }

}