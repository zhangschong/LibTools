package com.lib.http;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.lib.utils.Locker;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

/**
 * 网络请求测试
 */
@RunWith(AndroidJUnit4.class)
public class IHttpRequesterTest  implements IHttpRequester.Callback{

    Locker mLocker = new Locker();

    @Test
    public void testHttpRequester() throws Exception{
        IHttpInterface iHttpInterface = DataNetBuilder.newDataNet(IHttpInterface.class,new OkHttpRequester());
        Assert.assertNotNull(iHttpInterface);
        iHttpInterface.init();
        iHttpInterface.getBaidu(this);
        mLocker.lock(1000*1000);
        Assert.assertTrue(mLocker.isUnLock());
        iHttpInterface.recycle();
    }

    @Test
    public void testHttpRequesterForCancel(){
        IHttpInterface iHttpInterface = DataNetBuilder.newDataNet(IHttpInterface.class,new OkHttpRequester());
        Assert.assertNotNull(iHttpInterface);
        iHttpInterface.init();
        RequestCall call = iHttpInterface.getBaidu(this);
        call.cancel();
        mLocker.lock(1000*1000);
        Assert.assertTrue(mLocker.isUnLock());
        iHttpInterface.recycle();
    }

    @Override
    public void onResponse(RequestCall call) {
        Log.d("Leon",call.getData().toString());
        mLocker.unlock();
    }

    @Override
    public void onResponseErr(RequestCall call, IHttpRequester.RepErrMsg msg) {
        Log.e("Leon",msg.msg);
        mLocker.unlock();
    }
}