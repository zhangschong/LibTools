package com.lib.http;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.lib.utils.Locker;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 网络请求测试
 */
@RunWith(AndroidJUnit4.class)
public class IHttpRequesterTest {

    Locker mLocker = new Locker();

    @Test
    public void testHttpRequester() throws Exception{
        IHttpInterface iHttpInterface = DataNetBuilder.newDataNet(IHttpInterface.class,new OkHttpRequester());
        Assert.assertNotNull(iHttpInterface);
        iHttpInterface.init();
        iHttpInterface.getBaidu(this);
        mLocker.lock(1000*1000);
        iHttpInterface.postBaidu(this);
        mLocker.lock(1000*1000);
        Assert.assertTrue(mLocker.isUnLock());
        iHttpInterface.recycle();
    }

    private void onResponse(IHttpRequester.Response response){
        Log.d("Leon",response.getData().toString());
        mLocker.unlock();
    }

    private void onResponseErr(IHttpRequester.RepErrMsg msg){
        Log.e("Leon",msg.msg);
        mLocker.unlock();
    }
}