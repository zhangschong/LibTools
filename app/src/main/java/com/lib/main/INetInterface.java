package com.lib.main;

import com.lib.http.IHttpRequester;
import com.lib.mthdone.utils.IManager;

import java.util.List;

/**
 * $desc
 */

public interface INetInterface extends IManager {

    @IHttpRequester.Get("http://www.baidu.com")
    String getBaidu(Object callback);


    @IHttpRequester.Get("xxxxx/requestNode")
    RetrunNode requestRetrunNode(Object callback);


    public class RetrunNode{
        private String key;
        private List<String> values;
    }

}
