package com.lib.mthdone.rm;


/**
 * 异步线程处理
 */

public class NewThreadRunnableManager implements IRunnableManager {
    private final static String TAG = NewThreadRunnableManager.class.getName();
    @Override
    public void init() {
    }

    @Override
    public void recycle() {
    }

    @Override
    public void doRunnable(Runnable runnable) throws Exception {
        new Thread(runnable).start();
    }
}
