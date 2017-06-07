package com.lib.xml.parser2;

import android.util.SparseArray;

import com.lib.mthdone.IMethodDone;
import com.lib.mthdone.MethodTag;
import com.lib.utils.LogUtils;
import com.lib.utils.Stater;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.lib.mthdone.IMethodDone.Factory.MethodDone;

/**
 * Created by zhanghong on 17-6-3.
 */

public abstract class SaxNodeManager {
    private final static String TAG = SaxNodeManager.class.getSimpleName();
    private final static boolean isDebug = true;

    private final static int STATE_FAILED = 1;
    private final static int STATE_READ_OVER = 2;

    private final static String POINT_SIGN = ".";
    private String mPackageName;
    private HashMap<String, Class> mClasses = new HashMap<>(100);
    private SparseArray<XmlNode> mNodes = new SparseArray<>(5000);

    private Stater mStater = new Stater();

    private int mReadCounter;
    private int mParsedCounter;

    protected SaxNodeManager(String defaultPkgName) {
        mPackageName = defaultPkgName;
    }

    /**
     * 获取{@link XmlNode} 列表
     *
     * @param name
     * @return
     */
    public final <T extends XmlNode> List<T> getNodesByName(String name) {
        final int size = mNodes.size();
        List<T> nodes = new ArrayList<>(size / 2);
        XmlNode node;
        for (int i = 0; i < size; i++) {
            node = mNodes.valueAt(i);
            if (node.name().equals(name)) {
                nodes.add((T) node);
            }
        }
        return nodes;
    }

    /**
     * 开始时被调用
     */
    void onReadStart() {
        mReadCounter = 0;
        mParsedCounter = 0;
    }

    /**
     * 通过id 获取{@link XmlNode}
     *
     * @param id
     * @param <T>
     * @return
     */
    public final <T extends XmlNode> T getXmlNodeById(int id) {
        return (T) mNodes.get(id);
    }

    /**
     * 此方法在 {@link XmlSaxNodeReader} 被调用
     *
     * @param node
     */
    void onNodeReaded(XmlReadNode node) {
        mReadCounter++;
        MethodDone.doIt(this, "parserReadNode", node);
    }

    /**
     * 异线程处理解析
     *
     * @param node
     */
    @MethodTag(threadType = IMethodDone.THREAD_TYPE_THREAD)
    void parserReadNode(XmlReadNode node) {
        LogUtils.d(isDebug, TAG, "parserReadNode name:", node.getName());
        try {
            if (mStater.hasState(STATE_FAILED)) {
                return;
            }
            XmlNode xmlNode = onCreateChild(node);
            if (null != xmlNode) {
                xmlNode.parserSelf(this, node);
                mNodes.put(xmlNode.getId(), xmlNode);
                onAddXmlNode(xmlNode);
            }
        } catch (RuntimeException e) {
            throw e;
        } finally {
            mParsedCounter++;
            checkParsedOver();
        }
    }

    protected void onAddXmlNode(XmlNode node) {

    }

    /**
     * 检查是否已解析完成
     */
    private void checkParsedOver() {
        LogUtils.d(isDebug, TAG, " checkParsedOver readCounter: ", mReadCounter, " paredCounter:", mParsedCounter);
        if (mReadCounter != mParsedCounter || !mStater.hasState(STATE_READ_OVER)) {
            return;
        }
        onParsedOver(!mStater.hasState(STATE_FAILED));
    }

    /**
     * 解析完成时调用
     *
     * @param isSucceed
     */
    protected abstract void onParsedOver(boolean isSucceed);

    /**
     * create child
     *
     * @param node
     * @return
     */
    private final XmlNode onCreateChild(XmlReadNode node) {
        try {
            return newChildItemInstance(node.getName());
        } catch (Exception e) {
            LogUtils.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 生成一个{@link XmlNode}
     *
     * @param childName
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    private final XmlNode newChildItemInstance(String childName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException {
        String clsName = childName;
        if (!clsName.contains(POINT_SIGN)) {
            StringBuilder builder = new StringBuilder(mPackageName);
            builder.append(POINT_SIGN);
            clsName = childName.substring(0, 1).toUpperCase() + childName.substring(1);//首字母大写
            builder.append(clsName);
            clsName = builder.toString();
        }
        LogUtils.d(false, TAG, "newChildItemInstance: ", clsName);
        XmlNode node = null;
        Class<?> cls = mClasses.get(clsName);
        if (null == cls) {
            try {
                cls = Class.forName(clsName);
                mClasses.put(clsName, cls);
            } catch (ClassNotFoundException e) {
                cls = Object.class;//如果没有此类不再查询
                mClasses.put(clsName, cls);
                throw e;
            }
        }
        if (XmlNode.class.isAssignableFrom(cls)) {
            Constructor<?> constructor = cls.getDeclaredConstructor();
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
                node = (XmlNode) constructor.newInstance();
                constructor.setAccessible(false);
            } else {
                node = (XmlNode) constructor.newInstance();
            }
        }
        return node;
    }

    /**
     * 读取失败时调用
     *
     * @param e
     */
    void onReadFailed(Exception e) {
        LogUtils.e(TAG, e.getMessage(), "\n", e.getCause());
        e.printStackTrace();
        mStater.addState(STATE_FAILED);
    }

    /**
     * 读取完成时调用
     */
    void onReadEnd() {
        mStater.addState(STATE_READ_OVER);
        if (mStater.hasState(STATE_FAILED)) {
            onParsedOver(false);
        } else {
            checkParsedOver();
        }
    }
}
