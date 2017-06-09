package com.lib.mthdone;

import android.support.annotation.NonNull;
import android.util.Log;

import com.lib.mthdone.mf.IMethodFinder;
import com.lib.mthdone.rm.IRunnableManager;
import com.lib.mthdone.utils.ResetNodeManager;

import java.lang.reflect.Method;

/**
 * {@link IMethodDone}默认实例对象
 */

class MethodDone implements IMethodDone {
    private final static String TAG = MethodDone.class.getSimpleName();

    private IMethodFinder mMethodFinder;

    private IRunnableManager[] mRunnableManagers;

    @Override
    public void init() {
        mRunnableManagers = new IRunnableManager[]{IRunnableManager.Factory.createRunnableManager(THREAD_TYPE_MAIN),
                IRunnableManager.Factory.createRunnableManager(THREAD_TYPE_IO),
                IRunnableManager.Factory.createRunnableManager(THREAD_TYPE_THREAD)};

        for (IRunnableManager manager : mRunnableManagers) {
            manager.init();
        }

        mMethodFinder = IMethodFinder.Factory.createMethodFinder(IMethodFinder.Factory.TYPE_DEFAULT);
        mMethodFinder.init();
    }

    @Override
    public void recycle() {
        mMethodFinder.recycle();
        for (IRunnableManager manager : mRunnableManagers) {
            manager.recycle();
        }
    }

    /**
     * 当出现异常错误时,回调此方法
     *
     * @param instance
     * @param mth
     * @param throwable
     */
    protected void err(Object instance, String mth, Throwable throwable) {
        try {
            doItWithException(instance, CALL_BACK_ERR, null, new Error(instance, mth, throwable));
        } catch (Exception e) {
            //没有错误回调方法则打印
            Log.e(TAG, "DO METHOD ERR!");
            if (null != throwable) {
                throwable.printStackTrace();
            }
        }
    }

    @Override
    public void doIt(Object instance, String mthTag, Object... parameters) {
        doItWithParamTypes(instance, mthTag, null, parameters);
    }

    @Override
    public void doItWithParamTypes(@NonNull final Object instance, @NonNull final String mthTag, final Class[] parametersType, final Object... parameters) {
        try {
            Class cls;
            if (instance instanceof Class) {
                cls = (Class) instance;
            } else {
                cls = instance.getClass();
            }
            boolean hasMethodSerched = mMethodFinder.hasMethodSerched(cls, mthTag);
            if (hasMethodSerched) {//如果已经搜索过,则直接执行
                doItWithException(instance, mthTag, parametersType, parameters);
            } else {//没有搜索过,则进入异线程搜索
                postToThreadSearch(instance, mthTag, parametersType, parameters);
            }
        } catch (Exception e) {
            err(instance, mthTag, e);
        }
    }

    private void postToThreadSearch(@NonNull final Object instance, @NonNull final String mthTag, final Class[] parametersType, final Object... parameters) throws Exception {
        mRunnableManagers[THREAD_TYPE_THREAD].doRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    doItWithException(instance, mthTag, parametersType, parameters);
                } catch (Exception e) {
                    err(instance, mthTag, e);
                }
            }
        });
    }

    @Override
    public void doItWithException(@NonNull Object instance, @NonNull final String mthTag, final Class[] parametersType, Object... parameters) throws Exception {
        Class cls;
        if (instance instanceof Class) {//如果为Class对像,则为静态方法请求
            cls = (Class) instance;
            instance = null;
        } else {
            cls = instance.getClass();
        }
        if (null == parameters) {//如果为空,则必然有一个参数,如果没有参数，应为空数组
            parameters = new Object[]{null};
        }
        Method method = mMethodFinder.findMethod(cls, mthTag, parametersType, parameters);
        doMethodInner(instance, method, parameters);
    }

    private void doMethodInner(@NonNull Object instance, Method method, Object... parameters) {
        try {
            int threadType = THREAD_TYPE_MAIN;
            MethodTag tag = method.getAnnotation(MethodTag.class);
            if (null != tag) {
                threadType = tag.threadType();
            }
            mRunnableManagers[threadType].doRunnable(mNodeManager.pullT().setData(instance, method, parameters));
        } catch (Exception e) {
            err(instance, method.getName(), e);
        }
    }

    private ResetNodeManager<RunNode> mNodeManager = new ResetNodeManager<RunNode>() {
        @Override
        protected RunNode createNode() {
            return new RunNode();
        }
    };


    /**
     * 执行的Node元素
     */
    private class RunNode implements Runnable, ResetNodeManager.IResetNode {

        private Method mMethod;
        private Object mInstance;
        private Object[] mParameters;

        RunNode setData(Object instance, Method method, Object... parameters) {
            mMethod = method;
            mInstance = instance;
            mParameters = parameters;
            return this;
        }


        @Override
        public void run() {
            try {
                if (mMethod.isAccessible()) {
                    mMethod.invoke(mInstance, mParameters);
                } else {
                    mMethod.setAccessible(true);
                    mMethod.invoke(mInstance, mParameters);
                    mMethod.setAccessible(false);
                }
            } catch (Exception e) {
                StringBuffer builder = new StringBuffer(mMethod.getName());
                for (Object param : mParameters) {
                    builder.append(" ");
                    builder.append(param);
                }
                err(mInstance, builder.toString(), e);
            } finally {
                mNodeManager.resetNode(this);
            }
        }

        @Override
        public void reset() {
            setData(null, null);
        }
    }
}
