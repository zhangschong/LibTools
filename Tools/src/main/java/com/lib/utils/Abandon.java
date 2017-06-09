/* Copyright (C) 2016 Tcl Corporation Limited */
package com.lib.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JsonFeild动态注解，用于不需要{@link JSONUtil}解析，或者打包的成员变量使用
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Abandon {
}
