package com.lib.http;

import com.lib.mthdone.utils.IManager;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Map;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 网络请求接口
 */
public interface IHttpRequester extends IManager {

    /**
     * 请求成功后，将会调用callback实例中的方法onResponse(Response)
     */
    String CALL_BACK_SUCCEED = "onResponse";

    /**
     * 请求失败后，将会调用callback实例中的方法onResponseErr(RepErrMsg)
     */
    String CALL_BACK_ERR = "onResponseErr";

    /** 请求出错 */
    int REQUESTER_ERR = 10001;
    /** 网络错误 */
    int NETWORK_ERR = 10002;
    /** 数据错误 */
    int DATA_ERR = 10003;

    /**
     * 请求数据时调用
     *
     * @param method
     * @param callBack
     * @param dataCls
     * @param params
     * @return
     */
    boolean request(Method method, Object callBack, Class dataCls, Object... params);


    /**
     * 请求后的数据回调,在{@link IHttpRequester#request(Method, Object, Class, Object...)}中的instance的类onRespose(Response rs)中返回数据
     */
    interface Response {

        /**
         * 请求的url
         *
         * @return
         */
        String url();

        /**
         * 获取请求参数,与{@link IHttpRequester#request(Method, Object, Class, Object...)}中的Object[]参数相同
         *
         * @param key 与{@link RequestKes#value()}相同
         * @param <T>
         * @return
         */
        <T> T getParameter(String key);

        /**
         * 获取返回数据
         *
         * @param <T>
         * @return
         */
        <T> T getData();
    }

    /**
     * 错误数据
     */
    class RepErrMsg {
        public final int type;
        public final String msg;

        RepErrMsg(int type, String msg) {
            this.type = type;
            this.msg = msg;
        }
    }


    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    @interface RequestKes {
        /**
         * 请求参数
         *
         * @return
         */
        String[] value() default {};
    }

    @interface Post {
        /**
         * 请求参数
         *
         * @return
         */
        String value();
    }

    @interface Get {
        /**
         * 请求参数
         *
         * @return
         */
        String value();
    }
}

class DefaultResponse implements IHttpRequester.Response {

    private final String mUrl;
    private final Map<String, Object> mParams;
    private Object mData;

    private DefaultResponse(String url, Map<String, Object> params) {
        mUrl = url;
        mParams = params;
    }

    void setData(Object data) {
        mData = data;
    }

    Map<String, Object> getParams() {
        return mParams;
    }

    @Override
    public String url() {
        return mUrl;
    }

    @Override
    public <T> T getParameter(String key) {
        return (T) mParams.get(key);
    }

    @Override
    public <T> T getData() {
        return (T) mData;
    }

    /**
     * 通过方法和参数，生成response
     *
     * @param method
     * @param objs
     * @return
     */
    static DefaultResponse createResponse(Method method, Object[] objs) {
        IHttpRequester.Post post = method.getAnnotation(IHttpRequester.Post.class);
        String url = null;
        if (null != post) {
            url = post.value();
        } else {
            IHttpRequester.Get get = method.getAnnotation(IHttpRequester.Get.class);
            url = get.value();
        }

        if (null == url) {
            return null;
        }

        IHttpRequester.RequestKes rKeys = method.getAnnotation(IHttpRequester.RequestKes.class);
        String[] keys;
        if (null != rKeys) {
            keys = rKeys.value();
        } else {
            keys = new String[0];
        }
        final Map<String, Object> params = HttpUtils.buildHashMapParams(keys, objs);

        return new DefaultResponse(url, params);
    }


}
