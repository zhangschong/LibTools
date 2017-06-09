package com.lib.http;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.lib.mthdone.IMethodDone;
import com.lib.mthdone.MethodTag;
import com.lib.utils.GsonUtil;
import com.lib.utils.LogUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static com.lib.mthdone.IMethodDone.Factory.MethodDone;

/**
 * OKHttp请求网络
 */

public class OkHttpRequester implements IHttpRequester {
    private final static String TAG = OkHttpRequester.class.getSimpleName();

    private final static int WAITING_TIME_MILLS = 20;

    private String mRoot;
    private OkHttpClient mOkHttpClient = new OkHttpClient();
    private Gson mGson = new Gson();
    private int mWaitingSeconds = WAITING_TIME_MILLS;

    @Override
    public void setTimeOut(int seconds) {
        if (mWaitingSeconds != seconds) {
            mWaitingSeconds = seconds;
            createHttpClient(true);
        }
    }

    private void createHttpClient(boolean isForce) {
        if (null == mOkHttpClient || isForce) {
            mOkHttpClient = mOkHttpClient.newBuilder()
                    .connectTimeout(mWaitingSeconds, TimeUnit.SECONDS)//设置超时时间
                    .readTimeout(mWaitingSeconds, TimeUnit.SECONDS)//设置读取超时时间
                    .writeTimeout(mWaitingSeconds, TimeUnit.SECONDS)//设置写入超时时间
                    .build();
        }
    }

    /**
     * 设置根目录
     *
     * @param rootUrl
     */
    public void setRootUrl(String rootUrl) {
        mRoot = rootUrl;
    }


    @Override
    public void init() {
        createHttpClient(false);
    }

    @Override
    public void recycle() {
    }

    /**
     * 处理参数据时回调
     *
     * @param call   当前的请求
     * @param params 请求参数
     * @return 更改后的请求参数不能为空
     */
    @NonNull
    protected Map<String, Object> onProduceParams(RequestCall call, Map<String, Object> params) {
        return params;
    }

    /**
     * Header添加时调用
     *
     * @param call 当前的请求
     * @return
     */
    protected Map<String, String> onProduceHeader(RequestCall call) {
        return null;
    }

    @Override
    public RequestCall request(Method method, Object callBack, Class dataCls, Object... params) {
        if (RequestCall.class.isAssignableFrom(dataCls)) {
            //从泛型中获取返回数据类型
            Type genType = method.getGenericReturnType();
            genType = ((ParameterizedType) genType).getActualTypeArguments()[0];
            if (genType instanceof ParameterizedType) {
                dataCls = (Class) ((ParameterizedType) genType).getRawType();
            } else {
                dataCls = (Class) genType;
            }
            RequestNode call = newRequestNode(dataCls);
            DefaultResponse response = DefaultResponse.createResponse(method, params);
            call.mResponse = response;
            MethodDone.doIt(call, "request", method, callBack, genType, params);
            return call;
        } else {
            try {
                MethodDone.doItWithException(callBack, CALL_BACK_ERR, null, this, new RepErrMsg(CANCELED_ERR, dataCls + " is not subclass of RequestCall"));
            } catch (Exception e) {
                LogUtils.e(TAG, dataCls + " is not subclass of RequestCall");
            }
        }
        return null;
    }

    /**
     * 创建新的请求node
     *
     * @param cls
     * @param <T>
     * @return
     */
    private <T> RequestNode<T> newRequestNode(Class<T> cls) {
        return new RequestNode<T>();
    }

    private class RequestNode<T> implements RequestCall<T> {

        private Object mCallBack;
        private DefaultResponse mResponse;
        private Call mCall;
        private boolean isCanceled;

        //在IO线程调用方法request
        @MethodTag(threadType = IMethodDone.THREAD_TYPE_IO)
        private void request(Method method, Object callBack, Type dataType, Object... params) throws IOException {
            if (isCanceled) {//如果被Cancel
                err(callBack, CANCELED_ERR, "request canceled!");
                return;
            }

            mCallBack = callBack;
            //目前只提供了两种方法(post,get),如果没有，则request为空，则会抛异常
            Request request = null;
            Post post = method.getAnnotation(Post.class);
            if (null == post) {//如果不是post方法
                Get get = method.getAnnotation(Get.class);
                if (null != get) {//get方法处理
                    request = doGet(method, get.value(), mResponse);
                }
            } else {//post方法处理
                request = doPost(method, post.value(), mResponse);
            }

            if (isCanceled) {//如果被Cancel
                err(callBack, CANCELED_ERR, "request canceled!");
                return;
            }

            Call call = mOkHttpClient.newCall(request);
            mCall = call;
            okhttp3.Response rep = call.execute();

            if (isCanceled) {//如果被Cancel
                err(callBack, CANCELED_ERR, "request canceled!");
                return;
            }

            if (rep.isSuccessful()) {
                Object data = GsonUtil.parserToJson(mGson, rep.body().string(), dataType);
                mResponse.setData(data);
                if (isCanceled) {//如果被Cancel
                    err(callBack, CANCELED_ERR, "request canceled!");
                    return;
                }
                MethodDone.doIt(callBack, CALL_BACK_SUCCEED, this);
            } else {
                err(mCallBack, rep.code(), rep.message());
            }
        }

        /**
         * {@link IMethodDone}出错异常时回调此方法
         *
         * @param error
         */
        private void onMethodDoneErr(IMethodDone.Error error) {
            StringBuilder builder = new StringBuilder();
            builder.append("class: ");
            builder.append(error.mInstance.getClass().getName());
            builder.append(" method: ");
            builder.append(error.mMethodTag);
            builder.append(" err: \n");
            builder.append(error.mThrowable.toString());
            builder.append(" \n");
            builder.append(error.mThrowable.getCause());
            builder.append(" \n");
            builder.append(error.mThrowable.getStackTrace());
            err(mCallBack, REQUESTER_ERR, builder.toString());
        }

        private void err(Object instance, int type, String msg) {
            try {
                MethodDone.doItWithException(instance, CALL_BACK_ERR, null, this, new RepErrMsg(type, msg));
            } catch (Exception e) {
                LogUtils.e(TAG, msg);
            }
        }

        /**
         * 做get请求
         *
         * @param method
         * @param action
         * @param response
         * @return
         * @throws UnsupportedEncodingException
         */
        private Request doGet(Method method, String action, DefaultResponse response) throws UnsupportedEncodingException {
            String[] keys = null;
            RequestKes reqKey = method.getAnnotation(RequestKes.class);
            if (null != reqKey) {
                keys = reqKey.value();
            }
            Map<String, String> mHeader = onProduceHeader(this);
            String url = HttpUtils.buildUrl(mGson, mRoot, action, keys, onProduceParams(this, response.getParams()));
            if (null == mHeader) {
                return new Request.Builder().url(url).build();
            } else {
                return new Request.Builder().headers(Headers.of(mHeader)).url(url).build();
            }
        }

        /**
         * 做post请求
         *
         * @param method
         * @param action
         * @param response
         * @return
         * @throws UnsupportedEncodingException
         */
        private Request doPost(Method method, String action, DefaultResponse response) throws UnsupportedEncodingException {
            String url = HttpUtils.buildUrl(mGson, mRoot, action, null, null);
            String[] keys = null;
            RequestKes reqKey = method.getAnnotation(RequestKes.class);
            if (null != reqKey) {
                keys = reqKey.value();
            }

            FormBody.Builder builder = new FormBody.Builder();
            Map<String, String> mHeader = onProduceHeader(this);
            if (null != keys) {
                Map<String,Object> params = onProduceParams(this,response.getParams());
                for (String key : keys) {
                    builder.add(key, HttpUtils.packString(mGson, params.get(key)));
                }
            }
            if(null == mHeader) {
                return new Request.Builder().url(url).post(builder.build()).build();
            }else{
                return new Request.Builder().headers(Headers.of(mHeader)).url(url).post(builder.build()).build();
            }
        }

        @Override
        public String url() {
            if (null != mResponse) {
                return mResponse.url();
            }
            return "";
        }

        @Override
        public <T1> T1 getParameter(String key) {
            if (null != mResponse) {
                return mResponse.getParameter(key);
            }
            return null;
        }

        @Override
        public void cancel() {
            isCanceled = true;
            if (null != mCall) {
                mCall.cancel();
            }
        }

        @Override
        public boolean isCanceled() {
            return isCanceled;
        }

        @Override
        public T getData() {
            if (null == mResponse) {
                return null;
            }
            return mResponse.getData();
        }
    }
}
