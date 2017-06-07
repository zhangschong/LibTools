package com.lib.mthdone.rm;


import com.lib.mthdone.utils.IManager;

/**
 * 处理{@link Runnable}的管理者
 */

public interface IRunnableManager extends IManager {

    /**
     * 执行Runnable
     * @param runnable
     */
    void doRunnable(Runnable runnable) throws Exception;

    class Factory{

        public final static int TYPE_MAIN = 0;
        /** 异步数据流处理 */
        public final static int TYPE_IO = 1;
        /** 异步线程处理 */
        public final static int TYPE_THREAD = 2;

        public static IRunnableManager createRunnableManager(int type){
            switch (type){
                case TYPE_IO:
                    return new FixedThreadRunnableManager();
//                    return new NewThreadRunnableManager();
                case TYPE_THREAD:
                    return new ThreadRunnableManager();
            }
            return new MainThreadRunnableManager();
        }

    }
}
