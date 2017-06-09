package com.lib.http;

import com.lib.mthdone.utils.IManager;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Map;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 网络请求接口
 */
public interface IHttpRequester extends IManager {

    /**
     * 请求成功后，将会调用callback实例中的方法onResponse(RequestCall)
     */
    String CALL_BACK_SUCCEED = "onResponse";

    /**
     * 请求失败后，将会调用callback实例中的方法onResponseErr(RequestCall,RepErrMsg)
     */
    String CALL_BACK_ERR = "onResponseErr";

    /** 请求出错 */
    int REQUESTER_ERR = 10001;
    /** 网络错误 */
    int NETWORK_ERR = 10002;
    /** 数据错误 */
    int DATA_ERR = 10003;
    /** 请求取消 */
    int CANCELED_ERR = 10004;

    /**
     * 设置默认超时
     *
     * @param timeMills
     */
    void setTimeOut(int timeMills);

    /**
     * 设置根地址
     *
     * @param rootUrl
     */
    void setRootUrl(String rootUrl);

    /**
     * 请求数据时调用
     *
     * @param method
     * @param callBack
     * @param dataCls
     * @param params
     * @return
     */
    <T> RequestCall<T> request(Method method, Object callBack, Class dataCls, Object... params);


    /**
     * 默认的callback
     */
    interface Callback {
        void onResponse(RequestCall call);

        void onResponseErr(RequestCall call, RepErrMsg msg);
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
    @Target(TYPE)
    @Retention(RUNTIME)
    @interface Settings {
        /**
         * 请求中断时间(秒)
         *
         * @return
         */
        int timeOutSeconds() default 20;

        /**
         * 默认的地址
         *
         * @return
         */
        String rootUrl() default "";
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

class DefaultResponse {

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

    String url() {
        return mUrl;
    }

    <T> T getParameter(String key) {
        return (T) mParams.get(key);
    }

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
