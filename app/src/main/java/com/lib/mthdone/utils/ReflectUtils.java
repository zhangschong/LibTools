package com.lib.mthdone.utils;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 反射工具类
 */

public class ReflectUtils {

    private final static String PKG_REG = "(java|android|com\\.android|javax|dalvik|junit|org)\\..+";

    private final static Pattern PATTERN = Pattern.compile(PKG_REG);

    private final static Class[] BASIC_TYPES = new Class[]{int.class, float.class, boolean.class, long.class,
            double.class, byte.class, short.class, char.class};
    private final static Class[] BASICE_CLSS = new Class[]{Integer.class, Float.class, Boolean.class, Long.class,
            Double.class, Byte.class, Short.class, Character.class};

    /**
     * 通过实例，获取当前的实例对像
     *
     * @param instances 不能为空
     * @return 实例的class 对像
     */
    public static Class[] getClassesFromInstances(Object... instances) {
        if (null == instances) {
            return new Class[]{Object.class};
        }
        Class[] clses = new Class[instances.length];
        for (int i = instances.length - 1; i >= 0; i--) {
            if (null == instances[i]) {
                clses[i] = Object.class;
            } else {
                clses[i] = instances[i].getClass();
            }
        }
        return clses;
    }

    private static boolean isLocalPackage(Class cls) {
        return PATTERN.matcher(cls.getName()).matches();
    }

    public static Set<Method> findAllMethodsByName(Class cls, String methodName) {
        HashSet<Method> methods = new HashSet<>(6);
        while (!isLocalPackage(cls)) {
            for (Method method : cls.getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    methods.add(method);
                }
            }
            cls = cls.getSuperclass();
        }
        return methods;
    }

    /**
     * 在 clss中是否有dst
     *
     * @param dst
     * @param clss
     * @return -1 没有,其它为有
     */
    private static int hasClassInClasses(Class dst, Class... clss) {
        int index = 0;
        for (Class cls : clss) {
            if (cls == dst) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * 获取Class的基本类型
     *
     * @param cls
     * @return -1 不是基本类型, 其它为基本类型的数据请参照{@link #BASIC_TYPES}
     */
    private static int basicType(Class cls) {
        int type = hasClassInClasses(cls, BASIC_TYPES);
        if (-1 == type) {
            return hasClassInClasses(cls, BASICE_CLSS);
        }
        return type;
    }

    /**
     * source 是否为from 或其子类
     *
     * @param source
     * @param from
     * @return true 同一个类或为子类
     */
    public static boolean isAssignableFrom(Class source, Class from) {
        final int type = basicType(source);

        if (type == basicType(from)) {
            if (-1 != type) {
                return true;
            }
        } else {
            return false;
        }
        return source.isAssignableFrom(from);
    }

    public static Method findMethodFromCls(Class cls, String methodName, Class[] parameterTypes) throws NoSuchMethodException {
        if (isLocalPackage(cls)) {
            throw new NoSuchMethodException(cls.getName() + " " + methodName);
        }
        Method method = cls.getDeclaredMethod(methodName, parameterTypes);
        if (null == method) {
            method = findMethodFromCls(cls.getSuperclass(), methodName, parameterTypes);
        }
        return method;
    }

}
