package com.lib.http;

import java.lang.reflect.Method;

public interface RequestCall<T> {
    /**
     * 请求的url
     *
     * @return
     */
    String url();

    /**
     * 获取请求参数,与{@link IHttpRequester#request(Method, Object, Class, Object...)}中的Object[]参数相同
     *
     * @param key
     * @param <T>
     * @return
     */
    <T> T getParameter(String key);

    /**
     * 取消请求
     */
    void cancel();

    /**
     * 是否已被取消
     *
     * @return
     */
    boolean isCanceled();

    /**
     * 获取数据
     *
     * @return
     */
    T getData();
}