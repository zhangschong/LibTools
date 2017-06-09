package com.lib.xml.parser2;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.lib.xml.XmlAttributes;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于记录XmlItemNode的相关数据
 */
public class XmlReadNode implements XmlAttributes {
    protected StringBuilder mDataBuilder;
    protected int mId;
    protected String mName;
    private HashMap<String, String> mAttributes;
    protected XmlReadNode mParent;
    //    protected final ArrayList<XmlReadNode> mChildren = new ArrayList<>(100);
    private Map<String, List<XmlReadNode>> mChildren = new ConcurrentHashMap<>(10);

    public String getName() {
        return mName;
    }


    public String getData() {
        return mDataBuilder.toString();
    }

    public int getId() {
        return mId;
    }

    /**
     * 添加子Node
     *
     * @param node
     */
    final void addChildNode(XmlReadNode node) {
        List<XmlReadNode> nodes = mChildren.get(node.getName());
        if (null == nodes) {
            nodes = new ArrayList<>(20);
            mChildren.put(node.getName(), nodes);
        }
        nodes.add(node);
    }

    /**
     * 通过名字获取子列表
     *
     * @param name
     * @return
     */
    public final List<XmlReadNode> getChildNodesByName(String name) {
        return mChildren.get(name);
    }

    /**
     * 通过名字获取子节点
     *
     * @param name
     * @return
     */
    private final XmlReadNode getChildNodeByNameInner(String name) {
        List<XmlReadNode> nodes = mChildren.get(name);
        if (null == nodes) {
            return null;
        }
        return nodes.get(0);
    }


    public final List<XmlReadNode> getAllChildNodes() {
        List<XmlReadNode> readNodes = new ArrayList<>(100);
        for (List<XmlReadNode> nodes : mChildren.values()) {
            readNodes.addAll(nodes);
        }
        return readNodes;
    }

    /**
     * 通过名字获取子节点, 比如:
     * <p>
     * names = {Placemark,styleUrl}
     * <p>
     * 1. 先查询 Placemark,如果没有,则查询styleUrl
     * <p>
     * 2. 先查询 Placemark, 如果有,继续从Placemark查找的结果中查找 styleUrl
     *
     * @param names
     * @return
     */
    public final XmlReadNode getChildNodeByName(String... names) {
        XmlReadNode readNode = this;
        XmlReadNode reading;
        for (String name : names) {
            reading = readNode.getChildNodeByNameInner(name);
            if (null != reading) {
                readNode = reading;
            }
        }
        return readNode == this ? null : readNode;
    }

    XmlReadNode() {
    }

    final void set(@NonNull XmlReadNode parent, int id, String name, Attributes attributes) {
        this.mId = id;
        this.mName = name;
//        this.mAttributes = attributes;
        final int size = attributes.getLength();
        if (size > 0) {
            mAttributes = new HashMap<>(size);
            for (int i = 0; i < size; i++) {
                mAttributes.put(attributes.getQName(i), attributes.getValue(i));
            }
        }
        this.mParent = parent;
        mDataBuilder = new StringBuilder();
    }

    final void readCharacters(char[] ch, int start, int length) throws SAXException {
        if (length > 1) {
            mDataBuilder.append(ch, start, length);
        } else if (length == 1 && '\n' != ch[start]) {
            mDataBuilder.append(ch, start, length);
        }
    }

    final void end() throws SAXException {
    }

    @Override
    public String getAttribute(String key) {
        return getAttribute(key, "");
    }

    @Override
    public String getAttribute(String key, String defaultValue) {
        if (null == mAttributes) {
            return defaultValue;
        }
        String value = mAttributes.get(key);
        return TextUtils.isEmpty(value) ? defaultValue : value;
    }
}