package com.lib.mthdone.rm;

import android.os.Handler;
import android.os.Looper;

/**
 * 默认的{@link IRunnableManager}
 */

class MainThreadRunnableManager implements IRunnableManager{

    private Handler mMainHandler = new Handler(Looper.getMainLooper());
    private Thread mMainThread = Looper.getMainLooper().getThread();

    @Override
    public void init() {

    }

    @Override
    public void recycle() {

    }

    @Override
    public void doRunnable(Runnable runnable) {
        if(Thread.currentThread() != mMainThread){
            mMainHandler.post(runnable);
        }else{
            runnable.run();
        }
    }
}
