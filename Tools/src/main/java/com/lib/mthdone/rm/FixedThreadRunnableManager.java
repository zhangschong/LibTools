package com.lib.mthdone.rm;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 异步处理者
 */

public class FixedThreadRunnableManager implements IRunnableManager {

    private ExecutorService mFixedThreadExecutor;

    @Override
    public void init() {
        mFixedThreadExecutor = Executors.newFixedThreadPool(4);
    }

    @Override
    public void recycle() {
        mFixedThreadExecutor.shutdownNow();
    }

    @Override
    public void doRunnable(Runnable runnable) throws Exception {
        mFixedThreadExecutor.submit(runnable);
    }
}
