package com.yqhp.common.zkdevice;

import com.yqhp.common.zookeeper.ZkTemplate;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;

/**
 * @author jiangyitao
 */
public class ZkDeviceWatcher {

    private final ZkTemplate zkTemplate;
    private final ZkDeviceManager zkDeviceManager;

    private TreeCache treeCache;

    private volatile boolean running = false;

    public ZkDeviceWatcher(ZkTemplate zkTemplate) {
        this.zkTemplate = zkTemplate;
        zkDeviceManager = new ZkDeviceManager(zkTemplate);
    }

    public synchronized void start(ZkDeviceListener listener) {
        if (running) {
            throw new IllegalStateException("ZkDeviceWatcher is running");
        }

        treeCache = zkTemplate.watch(zkDeviceManager.getWatchPath(), ((client, event) -> {
            TreeCacheEvent.Type eventType = event.getType();
            if (eventType != TreeCacheEvent.Type.NODE_ADDED
                    && eventType != TreeCacheEvent.Type.NODE_REMOVED
                    && eventType != TreeCacheEvent.Type.NODE_UPDATED) {
                return;
            }

            ChildData node = event.getData();
            if (node == null) {
                return;
            }

            ZkDevice zkDevice = zkDeviceManager.toZkDevice(node.getData());
            if (zkDevice == null) {
                return;
            }

            switch (eventType) {
                case NODE_ADDED:
                    listener.added(zkDevice);
                    break;
                case NODE_REMOVED:
                    listener.removed(zkDevice);
                    break;
                case NODE_UPDATED:
                    listener.updated(zkDevice);
                    break;
            }
        }));

        running = true;
    }

    public synchronized void stop() {
        if (running) {
            treeCache.close();
            running = false;
        }
    }
}
