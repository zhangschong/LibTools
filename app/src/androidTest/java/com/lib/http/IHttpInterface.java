package com.lib.http;

import com.lib.mthdone.utils.IManager;

/**
 * 测试的网络请求入口
 */
public interface IHttpInterface extends IManager {

    @IHttpRequester.Get("http://www.baidu.com")
    String getBaidu(Object callback);

    @IHttpRequester.Post("http://www.baidu.com")
    String postBaidu(Object callback);

    @IHttpRequester.Get("http://www.baidu.com")
    Node getBaiduNode(Object callback);

    class Node{
        public String item;
    }

}
