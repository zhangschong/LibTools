package com.lib.http;

import com.lib.mthdone.utils.IManager;

/**
 * 测试的网络请求入口
 */
@IHttpRequester.Settings(timeOutSeconds = 15, rootUrl = "http://www.baidu.com")
public interface IHttpInterface extends IManager {

    @IHttpRequester.Get("")
    RequestCall<String> getBaidu(Object callback);
}
