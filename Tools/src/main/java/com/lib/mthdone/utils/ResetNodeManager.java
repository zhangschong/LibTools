package com.lib.mthdone.utils;

import android.support.annotation.NonNull;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 实例回收器，用享元模式处理
 */

public abstract class ResetNodeManager<T extends ResetNodeManager.IResetNode> implements IManager {

    private Queue<T> mResetNodes = new LinkedList<>();

    @Override
    public void init() {

    }

    /**
     * 新生成一个T 的实例
     * @return T 的实例
     */
    protected abstract T createNode();

    /**
     * 获取一个 T的实例
     * @return 返回T的实例，不可能为空
     */
    public T pullT(){
        T t;
        synchronized (mResetNodes){
            t = mResetNodes.poll();
        }
        if(null == t){
            t = createNode();
        }
        return t;
    }

    /**
     * 重置 T　的实例
     * @param node T的实例
     */
    public void resetNode(@NonNull T node){
        node.reset();
        synchronized (mResetNodes){
            mResetNodes.add(node);
        }
    }


    @Override
    public void recycle() {

    }


    /**
     * 可用于重置的 Node
     */
    public static interface IResetNode{
        void reset();
    }
}
