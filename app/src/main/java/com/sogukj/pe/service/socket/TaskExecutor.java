package com.sogukj.pe.service.socket;

import android.app.Activity;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by CH-ZH on 2018/8/30.
 */
public class TaskExecutor {
    public static final String TAG = TaskExecutor.class.getSimpleName();
    static TaskExecutor sExecutor = null;
    final ThreadPoolExecutor POOL = new ThreadPoolExecutor(2, 10,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

    public static TaskExecutor getExecutor() {
        synchronized (TaskExecutor.class) {
            if (null == sExecutor)
                sExecutor = new TaskExecutor();
            return sExecutor;
        }
    }

    public Future<?> async(final Runnable task) {
        if (null != task)
            return POOL.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        task.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        return null;
    }

    public void uiThread(final Runnable task, Activity activity) {
        if (null != activity && task != null) activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


}
