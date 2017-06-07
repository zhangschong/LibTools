package com.lib.utils;

/**
 * 状态判定器,用于对多状态进行判定的工具类.判定的相关状态必须为非互斥
 */
public class Stater {

    /**
     * 空状态
     */
    public final static int STATE_NULL = 0;

    private int mState;

    /**
     * 添加一种非互拆状态
     *
     * @param state 添加的状态
     */
    public void addState(int state) {
        mState |= state;
    }

    /**
     * 判定是否有当前状态
     *
     * @param state 当前状态
     * @return 包含当前状态
     */
    public boolean hasState(int state) {
        return (mState & state) == state;
    }

    /**
     * 移除状态
     *
     * @param state 将移除的状态
     */
    public void removeState(int state) {
        mState &= ~state;
    }

    /**
     * 获取当前状态
     *
     * @return 返回当前状态
     */
    public int getState() {
        return mState;
    }

    /**
     * 清除当前状态,默认为{@link #STATE_NULL}
     */
    public void clearState() {
        setState(STATE_NULL);
    }

    /**
     * 设置当前状态
     *
     * @param state 将设置的状态
     */
    public void setState(int state) {
        mState = state;
    }

}
