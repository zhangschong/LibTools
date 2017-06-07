package com.lib.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * 利于Gson来解析数据
 */

public class GsonUtil {

    /**
     * 打包Json
     *
     * @param gson
     * @param obj
     * @return
     */
    public static String packToJson(Gson gson, Object obj) {
        String data;
        Class cls = obj.getClass();
        if (String.class.equals(cls)) {// 散板String型成员变量数据,并设置数据
            data = obj.toString();
        } else if (Double.class.equals(cls) || cls == double.class) {//散板Double型成员变量数据,并设置数据
            data = obj.toString();
        } else if (Integer.class.equals(cls) || cls == int.class) {//散板 integer 型成员变量数据,并设置数据
            data = obj.toString();
        } else if (Float.class.equals(cls) || cls == float.class) {//散板 float 型成员变量数据,并设置数据
            data = obj.toString();
        } else if (Boolean.class.equals(cls) || cls == boolean.class) {//散板 boolean 型成员变量数据,并设置数据
            data = obj.toString();
        } else if (Long.class.equals(cls) || cls == long.class) {//散板 long 型成员变量数据,并设置数据
            data = obj.toString();
        } else {//非基本类型
            data = gson.toJson(obj);
        }
        return data;
    }

    /**
     * 解析Json
     *
     * @param gson
     * @param data
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> T parserToJson(Gson gson, String data, Class<T> cls) {
        Object obj = null;
        if (String.class.equals(cls)) {// 散板String型成员变量数据,并设置数据
            obj = data;
        } else if (Double.class.equals(cls) || cls == double.class) {//散板Double型成员变量数据,并设置数据
            try {
                obj = Double.parseDouble(data);
            } catch (Exception e) {
                obj = 0d;
            }
        } else if (Integer.class.equals(cls) || cls == int.class) {//散板 integer 型成员变量数据,并设置数据
            try {
                int radix = 10;
                if (data.startsWith("#")) {//16进制色彩值处理
                    data = data.replaceFirst("#", "");
                    radix = 16;
                } else if (data.startsWith("0x")) {//16进制色彩值处理
                    data = data.replaceFirst("0x", "");
                    radix = 16;
                }//
                obj = Integer.parseInt(data, radix);
            } catch (Exception e) {
                obj = 0;
            }
        } else if (Float.class.equals(cls) || cls == float.class) {//散板 float 型成员变量数据,并设置数据
            try {
                obj = Float.parseFloat(data);
            } catch (Exception e) {
                obj = 0f;
            }
        } else if (Boolean.class.equals(cls) || cls == boolean.class) {//散板 boolean 型成员变量数据,并设置数据
            try {
                if ("0".equals(data)) {//如果为false,0 代表false
                    data = "false";
                } else if ("1".equals(data)) {//如果为true, 1 代表true
                    data = "true";
                }
                if (!TextUtils.isEmpty(data)) {
                    obj = Boolean.parseBoolean(data);
                }
            } catch (Exception e) {
                obj = false;
            }
        } else if (Long.class.equals(cls) || cls == long.class) {//散板 long 型成员变量数据,并设置数据
            try {
                obj = Long.parseLong(data);
            } catch (Exception e) {
                obj = 0l;
            }
        } else if (List.class.isAssignableFrom(cls)) {//List型
            obj = gson.fromJson(data, TypeToken.get(cls).getType());
        } else if (Object.class.isAssignableFrom(cls)) {//对像
            obj = gson.fromJson(data, cls);
        }
        return (T) obj;
    }

}
