package com.lib.mthdone;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解在方法类上
 */

@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodTag {
    /**
     * 获取当前的线程类型
     * @return 线程类型，默认为 {@link IMethodDone#THREAD_TYPE_MAIN}
     *
     * @see IMethodDone#THREAD_TYPE_MAIN
     * @see IMethodDone#THREAD_TYPE_IO
     * @see IMethodDone#THREAD_TYPE_THREAD
     *
     */
    int threadType() default IMethodDone.THREAD_TYPE_MAIN;
}
