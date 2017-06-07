package com.lib.mthdone.mf;


import android.support.annotation.NonNull;

import com.lib.mthdone.utils.IManager;

import java.lang.reflect.Method;

/**
 * 获取方法类
 */

public interface IMethodFinder extends IManager {

    /**
     * 方法获取
     *
     * @param cls
     * @param methodName
     * @param parameterTypes
     */
    Method findMethod(@NonNull Class cls, @NonNull String methodName, Class[] parameterTypes, Object[] parameters) throws Exception;

    /**
     * 此方法是否已经搜索过
     *
     * @param cls
     * @param methodName
     * @return
     */
    boolean hasMethodSerched(@NonNull Class cls, @NonNull String methodName);


    class Factory {
        public final static int TYPE_DEFAULT = 1;

        public static IMethodFinder createMethodFinder(int type) {
            return new DefaultMethodFinder();
        }

    }

}
