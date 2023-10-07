package com.poldroc.rpc.framework.core.common;


import java.util.concurrent.Semaphore;
/**
 * 服务端信号量包装类，用于限制服务端最大连接数
 * @author Poldroc
 * @date 2023/10/5
 */

public class ServerServiceSemaphoreWrapper {

    /**
     * 控制并发访问
     */
    private Semaphore semaphore;

    /**
     * 信号量允许的最大许可数量
     */
    private int maxNums;

    public ServerServiceSemaphoreWrapper(int maxNums) {
        this.maxNums = maxNums;
        this.semaphore = new Semaphore(maxNums);
    }

    public Semaphore getSemaphore() {
        return semaphore;
    }

    public void setSemaphore(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    public int getMaxNums() {
        return maxNums;
    }

    public void setMaxNums(int maxNums) {
        this.maxNums = maxNums;
    }
}