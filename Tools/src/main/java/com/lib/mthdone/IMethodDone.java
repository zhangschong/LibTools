package com.lib.mthdone;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.lib.mthdone.utils.IManager;

import static com.lib.mthdone.rm.IRunnableManager.Factory.TYPE_IO;
import static com.lib.mthdone.rm.IRunnableManager.Factory.TYPE_MAIN;
import static com.lib.mthdone.rm.IRunnableManager.Factory.TYPE_THREAD;


/**
 * 方法执行者
 */

public interface IMethodDone extends IManager {

    /**
     * 当发生错误时会回调此方法,如果该instance没有,则不回调
     */
    String CALL_BACK_ERR = "onMethodDoneErr";

    /** 主线程 */
    int THREAD_TYPE_MAIN = TYPE_MAIN;
    /** 异步数据流处理 */
    int THREAD_TYPE_IO = TYPE_IO;
    /** 异步线程处理 */
    int THREAD_TYPE_THREAD = TYPE_THREAD;


    /**
     * 执行方法
     *
     * @param instance   执行的实例
     * @param mthTag     方法标签，默认为方法名，暂不支持其它标签
     * @param parameters 执行参数实例,可以不添加
     */
    void doIt(@NonNull Object instance, @NonNull String mthTag, @Nullable Object... parameters);

    /**
     * 执行方法
     *
     * @param instance
     * @param mthTag
     * @param parametersType
     * @param parameters
     */
    void doItWithParamTypes(@NonNull Object instance, @NonNull String mthTag, @Nullable Class[] parametersType, @Nullable Object... parameters);

    /**
     * 执行方法
     * @param instance
     * @param mthTag
     * @param parametersType
     * @param parameters
     * @throws Exception
     */
    void doItWithException(@NonNull Object instance, @NonNull String mthTag, @Nullable Class[] parametersType, @Nullable Object... parameters) throws Exception;


    class Error {
        public final Object mInstance;
        public final String mMethodTag;
        public final Throwable mThrowable;

        Error(Object instance, String mthTag, Throwable throwable) {
            this.mInstance = instance;
            this.mMethodTag = mthTag;
            this.mThrowable = throwable;
        }

    }


    class Factory {
        public static final IMethodDone MethodDone = new MethodDone();

        static {
            MethodDone.init();
        }
    }
}
