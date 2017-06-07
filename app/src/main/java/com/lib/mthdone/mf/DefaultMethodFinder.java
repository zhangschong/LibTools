package com.lib.mthdone.mf;

import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.lib.mthdone.utils.ReflectUtils.findAllMethodsByName;
import static com.lib.mthdone.utils.ReflectUtils.getClassesFromInstances;
import static com.lib.mthdone.utils.ReflectUtils.isAssignableFrom;

/**
 * 默认的{@link IMethodFinder}
 */

public class DefaultMethodFinder implements IMethodFinder {

    private HashMap<Class, ClsMthNode> mClsMthNodes = new HashMap<>(200);


    @Override
    public void init() {

    }

    @Override
    public void recycle() {

    }

    @Override
    public Method findMethod(Class cls, String methodName, Class[] parameterTypes, Object[] parameters) throws Exception {
        ClsMthNode node = mClsMthNodes.get(cls);
        if (null == node) {
            synchronized (mClsMthNodes) {
                node = mClsMthNodes.get(cls);
                if (null == node) {
                    node = new ClsMthNode();
                    mClsMthNodes.put(cls, node);
                }
            }
        }
        return node.findMethod(cls, methodName, parameterTypes, parameters);
    }

    @Override
    public boolean hasMethodSerched(@NonNull Class cls, @NonNull String methodName) {
        ClsMthNode node = mClsMthNodes.get(cls);
        if (null == node) {
            return false;
        }
        return node.hasSerched(methodName);
    }

    private class ClsMthNode {

        private HashMap<String, MthNode> mMthNodes = new HashMap<>(20);

        public Method findMethod(Class cls, String methodName, Class[] parameterTypes, Object[] parameters) throws Exception {
            MthNode node = mMthNodes.get(methodName);
            if (null == node) {
                synchronized (mMthNodes) {
                    node = mMthNodes.get(methodName);
                    if (null == node) {
                        node = new MthNode();
                        mMthNodes.put(methodName, node);
                    }
                }
            }
            return node.findMethod(cls, methodName, parameterTypes, parameters);
        }

        private boolean hasSerched(String methodName) {
            MthNode node = mMthNodes.get(methodName);
            if (null == node) {
                return false;
            }
            return node.hasSerched();
        }
    }

    private class MthNode {
        private boolean isSearched;
        private SparseArray<List<Method>> mMethods = new SparseArray<>(10);

        private boolean hasSerched() {
            return isSearched;
        }

        //初始化方法
        private void initMethod(Class cls, String methodName) {
            if (!isSearched) {
                synchronized (mMethods) {
                    if (!isSearched) {
                        for (Method method : findAllMethodsByName(cls, methodName)) {
                            final int size = method.getParameterTypes().length;
                            List<Method> methods = mMethods.get(size);
                            if (null == methods) {
                                methods = new ArrayList<>(5);
                                mMethods.put(size, methods);
                            }
                            methods.add(method);
                        }
                    }
                    isSearched = true;
                }
            }
        }

        public Method findMethod(Class cls, String methodName, Class[] parameterTypes, Object[] parameters) throws Exception {
            initMethod(cls, methodName);

            if (null == parameterTypes) {
                parameterTypes = getClassesFromInstances(parameters);
            }

            final int size = parameterTypes.length;
            List<Method> methods = mMethods.get(size);
            if (null != methods && !methods.isEmpty()) {
                if (methods.size() == 1) {
                    return methods.get(0);
                } else {
                    for (Method method : methods) {//一个一个方法对比
                        Class[] clss = method.getParameterTypes();
                        for (int i = 0; i < size; i++) {
                            if (!isAssignableFrom(clss[i], parameterTypes[i])) {
                                new NoSuchMethodException(cls.getName() + " " + methodName);
                            }
                        }
                        return method;
                    }
                }
            }
            throw new NoSuchMethodException(cls.getName() + " " + methodName);
        }

    }

}
