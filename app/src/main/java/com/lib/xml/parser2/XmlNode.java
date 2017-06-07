package com.lib.xml.parser2;

import com.lib.utils.LogUtils;

/**
 * Created by zhanghong on 17-6-3.
 */

public class XmlNode {
    private final static String TAG = XmlNode.class.getSimpleName();


    public static <T extends XmlNode> T getChildXmlNodeByNames(SaxNodeManager nodeManager, XmlReadNode readNode, String... names) {
        XmlReadNode childNode = readNode.getChildNodeByName(names);
        if (null != childNode) {
            return nodeManager.getXmlNodeById(childNode.getId());
        }
        return null;
    }

    public static double parserDouble(String doubleString) {
        return parserDouble(doubleString, 0d);
    }

    public static double parserDouble(String doubleString, double defaultValue) {
        try {
            return Double.parseDouble(doubleString.trim());
        } catch (Exception e) {
            LogUtils.e(TAG, e.toString());
            e.printStackTrace();
        }
        return defaultValue;
    }

    public static int parserInt10(String integer) {
        return parserInt10(integer, 0);
    }

    public static int parserInt10(String integer, int defaultValue) {
        try {
            return Integer.parseInt(integer, 10);
        } catch (Exception e) {
            LogUtils.e(TAG, e.toString());
            e.printStackTrace();
        }
        return defaultValue;

    }

    public static int parserInt16(String color) {
        return parserInt16(color, 0xff000000);
    }

    public static int parserInt16(String color, int defaultValue) {
        try {
            return (int) Long.parseLong(color, 16);
        } catch (Exception e) {
            LogUtils.e(TAG, e.toString());
            e.printStackTrace();
        }
        return defaultValue;
    }

    public static boolean parserBoolean(String bool) {
        return parserBoolean(bool, false);
    }

    public static boolean parserBoolean(String bool, boolean defaultValue) {
        try {
            if ("1".equals(bool)) {
                bool = "true";
            }
            return Boolean.parseBoolean(bool);
        } catch (Exception e) {
            LogUtils.e(TAG, e.toString());
            e.printStackTrace();
        }
        return defaultValue;
    }

    private XmlReadNode mReadNode;

    public final int getId() {
        return mReadNode.mId;
    }

    public final String name() {
        return mReadNode.mName;
    }

    void parserSelf(SaxNodeManager nodeManager, XmlReadNode readNode) {
        mReadNode = readNode;
        onParser(nodeManager, mReadNode);
    }

    protected void onParser(SaxNodeManager nodeManager, XmlReadNode node) {
    }
}
