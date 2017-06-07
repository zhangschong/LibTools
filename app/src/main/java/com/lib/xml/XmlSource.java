package com.lib.xml;

import android.content.Context;

import com.lib.mthdone.utils.IManager;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zhanghong on 17-5-27.
 * <p>
 * 为{@link HtmlSaxParser}提供数据
 */
public interface XmlSource extends IManager {

    /**
     * 获取输入流
     * @return
     * @throws Exception
     */
    InputStream getInputStream() throws Exception;

    class Factory {
        /**
         * 资源方案
         *
         * @param context
         * @param assetPath
         * @return
         */
        public static XmlSource createAssetSource(final Context context, final String assetPath) {
            return new XmlSource() {

                private InputStream mInputStream;

                @Override
                public InputStream getInputStream() throws Exception {
                    mInputStream = context.getResources().getAssets().open(assetPath);
                    return mInputStream;
                }

                @Override
                public void init() {
                }

                @Override
                public void recycle() {
                    if (null != mInputStream) {
                        try {
                            mInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
        }
    }
}
