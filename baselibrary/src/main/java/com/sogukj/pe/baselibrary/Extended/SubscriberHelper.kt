package com.sogukj.pe.baselibrary.Extended

import com.sogukj.pe.baselibrary.utils.Trace
import com.tencent.bugly.crashreport.CrashReport
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 * Created by admin on 2018/4/9.
 */
class SubscriberHelper<T> : Observer<T> {


    private var onNextListener: T1_Unit<T>? = null
    private var onErrorListener: Throwable_Unit? = null
    private var onCompleteListener: T0_Unit? = null
    private var onSubscribeListener: T1_Unit<Disposable>? = null

    fun onNext(next: T1_Unit<T>) {
        onNextListener = next
    }

    fun onError(error: Throwable_Unit) {
        onErrorListener = error
    }

    fun onComplete(complete: T0_Unit) {
        onCompleteListener = complete
    }

    fun onSubscribe(subscribe: T1_Unit<Disposable>) {
        onSubscribeListener = subscribe
    }

    override fun onNext(t: T) {
        onNextListener?.invoke(t)
    }

    override fun onError(e: Throwable) {
        Trace.e(e)
        CrashReport.postCatchedException(e)
        onErrorListener?.invoke(e)
    }

    override fun onComplete() {
        onCompleteListener?.invoke()
    }

    override fun onSubscribe(d: Disposable) {
        onSubscribeListener?.invoke(d)
    }

}