package com.yqhp.agent.devicediscovery;

/**
 * @author jiangyitao
 */
public abstract class DeviceDiscovery {

    private volatile boolean running = false;

    /**
     * 启动设备发现
     */
    public synchronized void start(DeviceChangeListener listener) {
        if (running) {
            throw new IllegalStateException("running");
        }

        run(listener);
        running = true;
    }

    /**
     * 停止设备发现
     */
    public synchronized boolean stop() {
        if (running) {
            terminate();
            running = false;
            return true;
        }
        return false;
    }

    protected abstract void run(DeviceChangeListener listener);

    protected abstract void terminate();
}
