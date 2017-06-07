package com.lib.xml;

/**
 * Html属性确认方案
 */
public interface XmlAttributes {

    /**
     * 获取属性值
     *
     * @param key
     * @return
     */
    String getAttribute(String key);

    /**
     * 获取属性值
     *
     * @param key
     * @param defaultValue
     * @return
     */
    String getAttribute(String key, String defaultValue);
}