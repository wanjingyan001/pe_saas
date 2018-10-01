package com.sogukj.pe.baselibrary.base

import android.os.Bundle
import android.support.v4.app.Fragment
import io.reactivex.subjects.PublishSubject
import io.reactivex.disposables.Disposable
import android.content.Intent
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import com.android.dingtalk.share.ddsharemodule.message.DDMessage.Receiver.mCallbacks
import com.sogukj.pe.baselibrary.Extended.ifNotNull


/**
 * Created by admin on 2018/9/26.
 */
class AvoidOnResultFragment : Fragment() {
    private val mSubjects = HashMap<Int, PublishSubject<ActivityResultInfo>>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    fun startForResult(intent: Intent): Observable<ActivityResultInfo> {
        val subject = PublishSubject.create<ActivityResultInfo>()
        return subject.doOnSubscribe {
            mSubjects.put(subject.hashCode(), subject)
            startActivityForResult(intent, subject.hashCode())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //rxjava方式的处理
        val subject = mSubjects.remove(requestCode)
        ifNotNull(subject, data) { sub, intent ->
            sub.onNext(ActivityResultInfo(resultCode, intent))
            sub.onComplete()
        }
    }
}