package com.lib.http;

import com.lib.mthdone.IMethodDone;
import com.lib.mthdone.MethodTag;
import com.lib.mthdone.utils.ResetNodeManager;
import com.google.gson.Gson;
import com.lib.utils.GsonUtil;
import com.lib.utils.LogUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.FormBody;
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
        mRequestNodes.init();
        mOkHttpClient = mOkHttpClient.newBuilder()
                .connectTimeout(WAITING_TIME_MILLS, TimeUnit.SECONDS)//设置超时时间
                .readTimeout(WAITING_TIME_MILLS, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(WAITING_TIME_MILLS, TimeUnit.SECONDS)//设置写入超时时间
                .build();
    }

    @Override
    public void recycle() {
        mRequestNodes.recycle();
    }

    @Override
    public boolean request(Method method, Object callBack, Class dataCls, Object... params) {
        //在IO线程调用方法request
        MethodDone.doIt(mRequestNodes.pullT(), "request", method, callBack, dataCls, params);
        return true;
    }


    private ResetNodeManager<RequestNode> mRequestNodes = new ResetNodeManager<RequestNode>() {
        @Override
        protected RequestNode createNode() {
            return new RequestNode();
        }
    };


    private class RequestNode implements ResetNodeManager.IResetNode {
        private Object mCallBack;

        //在IO线程调用方法request
        @MethodTag(threadType = IMethodDone.THREAD_TYPE_IO)
        private void request(Method method, Object callBack, Class dataCls, Object... params) throws IOException {
            mCallBack = callBack;
            DefaultResponse response = DefaultResponse.createResponse(method, params);

            //目前只提供了两种方法(post,get),如果没有，则request为空，则会抛异常
            Request request = null;

            Post post = method.getAnnotation(Post.class);
            if (null == post) {//如果不是post方法
                Get get = method.getAnnotation(Get.class);
                if (null != get) {//get方法处理
                    request = doGet(method, get.value(), response);
                }
            } else {//post方法处理
                request = doPost(method, post.value(), response);
            }

            Call call = mOkHttpClient.newCall(request);
            okhttp3.Response rep = call.execute();

            if (rep.isSuccessful()) {
                Object data = GsonUtil.parserToJson(mGson, rep.body().string(), dataCls);
                response.setData(data);
                MethodDone.doIt(callBack, CALL_BACK_SUCCEED, response);
                mRequestNodes.resetNode(this);//回收
            } else {
                err(mCallBack, rep.code(), rep.message());
            }
        }

        @Override
        public void reset() {
            mCallBack = null;
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
                MethodDone.doItWithException(instance, CALL_BACK_ERR, null, new RepErrMsg(type, msg));
                mRequestNodes.resetNode(this);//回收
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
            String url = HttpUtils.buildUrl(mGson, mRoot, action, keys, response.getParams());
            return new Request.Builder().url(url).build();
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

            if (null != keys) {
                for (String key : keys) {
                    builder.add(key, HttpUtils.packString(mGson, response.getParameter(key)));
                }
            }
            return new Request.Builder().url(url).post(builder.build()).build();
        }
    }
}
