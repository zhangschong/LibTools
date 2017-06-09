package com.lib.http;

import com.lib.mthdone.utils.IManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * 用于网络请求方法，此类每个方法前两个参数必须为callback实例（须要带方法onResponse(Response)
 */
public class DataNetBuilder implements InvocationHandler, IManager {
    private final static String TAG = DataNetBuilder.class.getSimpleName();

    /**
     * 通过动态代理生成实体方案
     *
     * @param interfaceCls
     * @param requester
     * @param <T>
     * @return
     */
    public static <T extends IManager> T newDataNet(Class<T> interfaceCls, IHttpRequester requester) {
        return (T) Proxy.newProxyInstance(interfaceCls.getClassLoader(), new Class[]{interfaceCls}, new DataNetBuilder(interfaceCls, requester));
    }

    private final static int PARAM_START_INDEX = 1;

    protected final IHttpRequester mHttpRequester;
    protected final Class mInterfaceCls;

    private DataNetBuilder(Class interfaceCls, IHttpRequester requester) {
        mInterfaceCls = interfaceCls;
        mHttpRequester = requester;
    }

    @Override
    public final Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        Class<?> cls = method.getDeclaringClass();
        if (cls.equals(IManager.class)) {//在HttpRequester中调用Imanager方法
            Object obj = method.invoke(mHttpRequester, objects);
            method.invoke(this, objects);
            return obj;
        }
        Object[] params = new Objects[objects.length - PARAM_START_INDEX];
        System.arraycopy(objects, PARAM_START_INDEX, params, 0, params.length);
        return mHttpRequester.request(method, objects[0], method.getReturnType(), params);
    }

    @Override
    public void init() {
        IHttpRequester.Settings settings = (IHttpRequester.Settings) mInterfaceCls.getAnnotation(IHttpRequester.Settings.class);
        if (null != settings) {
            mHttpRequester.setRootUrl(settings.rootUrl());
            mHttpRequester.setTimeOut(settings.timeOutSeconds());
        }
    }

    @Override
    public void recycle() {

    }
}
