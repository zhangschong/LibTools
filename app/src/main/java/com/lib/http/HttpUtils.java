package com.lib.http;

import com.google.gson.Gson;
import com.lib.utils.GsonUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * httpUtils
 */

class HttpUtils {

    public static String packString(Gson gson, Object obj) {
        String value;
        if (null == obj) {
            value = "";
        } else if (obj instanceof Object || obj.getClass().isArray()) {
            value = GsonUtil.packToJson(gson, obj);
        } else {
            value = obj.toString();
        }
        return value;
    }

    public static Map<String, String> buildParams(Gson gson, String[] keys, HashMap<String, Object> params) {
        Map<String, String> map = new HashMap<>();
        for (String key : keys) {
            map.put(key, packString(gson, params.get(key)));
        }
        return map;
    }

    public static Map<String, Object> buildHashMapParams(String[] keys, Object[] args) {
        final int size = keys.length < args.length ? keys.length : args.length;
        Map<String, Object> map = new HashMap<>();
        for (int i = size - 1; i >= 0; i--) {
            map.put(keys[i], args[i]);
        }
        return map;
    }

    /**
     * 生成请求url,如果为post请求,请将keys设置为空值
     *
     * @param root
     * @param action
     * @param keys
     * @param params
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String buildUrl(Gson gson, String root, String action, String[] keys, Map<String, Object> params) throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        if (action.startsWith("http")) {
            builder.append(action);
        } else {
            builder.append(root);
            builder.append("/");
            builder.append(action);
        }

        if (null == keys) {
            return builder.toString();
        }
        builder.append("?");
        StringBuilder tempParams = new StringBuilder();
        for (String key : keys) {
            tempParams.append("&");
            tempParams.append(URLEncoder.encode(packString(gson, params.get(key)), "utf-8"));
        }
        //补全请求地址
        return builder.append(tempParams.substring(1)).toString();
    }
}
