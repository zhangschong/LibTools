package com.lib.main;

/**
 * a locker for tester
 */
public class Locker {
    private final static int TIME_MILLS = 10000;
    private final static int WAIT_TIME = 100;

    private boolean isLock;

    public void lock() {
        lock(TIME_MILLS);
    }

    public void lock(long timeMills) {
        synchronized (this) {
            try {
                isLock = true;
                this.wait(timeMills);
            } catch (InterruptedException e) {
            }
        }
    }

    public boolean isUnLock(){
        return !isLock;
    }


    public void unlock() {
        synchronized (this) {
            try {
                this.wait(WAIT_TIME);
            } catch (InterruptedException e) {
            }
            if (isLock) {
                isLock = false;
                this.notifyAll();
            }
        }
    }

}
