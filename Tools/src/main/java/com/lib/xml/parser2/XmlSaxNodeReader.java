package com.lib.xml.parser2;

import com.lib.mthdone.IMethodDone;
import com.lib.mthdone.MethodTag;
import com.lib.utils.LogUtils;
import com.lib.xml.IXmlParser;
import com.lib.xml.XmlSource;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.util.LinkedList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static com.lib.mthdone.IMethodDone.Factory.MethodDone;

public final class XmlSaxNodeReader implements IXmlParser {

    private final static String TAG = XmlSaxNodeReader.class.getSimpleName();

    private XmlSource mResources;
    private XmlReadNode mCurrentNode = new XmlReadNode();
    private SaxNodeManager mSaxNodeParser;

    public XmlSaxNodeReader(XmlSource resources , SaxNodeManager saxNodeManager) {
        mResources = resources;
        mSaxNodeParser = saxNodeManager;
    }

    /**
     * 解析数据
     *
     * @return
     */
    public boolean parser() {
        MethodDone.doIt(this, "parserInner");
        return true;
    }

    @MethodTag(threadType = IMethodDone.THREAD_TYPE_IO)
    private void parserInner() {
        // 初始化当前的Node
        long timeMills = System.currentTimeMillis();
        LogUtils.d(false,TAG, "parser start: ", timeMills);
        mSaxNodeParser.onReadStart();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            XMLReader xmlReader = parser.getXMLReader();
            xmlReader.setContentHandler(mContentHandler);
            mResources.init();
            InputStream is = mResources.getInputStream();
            xmlReader.parse(new InputSource(is));
        } catch (Exception e) {
            mSaxNodeParser.onReadFailed(e);
        } finally {
            mResources.recycle();
            mSaxNodeParser.onReadEnd();
            LogUtils.d(false,TAG, "parser end: ", (System.currentTimeMillis() - timeMills));
        }
    }

    private DefaultHandler mContentHandler = new DefaultHandler() {
        private LinkedList<XmlReadNode> mItemStack = new LinkedList<>();
        private int mCurrentId = 0;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            LogUtils.d(false,TAG, "start name: ", localName);
            mCurrentId++;
            XmlReadNode node = new XmlReadNode();
            node.set(mCurrentNode, mCurrentId, localName, attributes);
            mCurrentNode.addChildNode(node);
            mItemStack.addFirst(mCurrentNode);//添加到头部
            mCurrentNode = node;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            LogUtils.d(false,TAG, "characters name: ", mCurrentNode.mName);
            mCurrentNode.readCharacters(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            LogUtils.d(false,TAG, "end localName: ", localName, " currentNode:", mCurrentNode.mName);
            mCurrentNode.end();
            mSaxNodeParser.onNodeReaded(mCurrentNode);
//            MethodDone.doIt(mSaxNodeParser,"onNodeReaded",mCurrentNode);
            mCurrentNode = mItemStack.removeFirst();//取出头部
        }
    };
}
