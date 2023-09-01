/*
 *  Copyright https://github.com/yqhp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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

    public ZkDeviceWatcher(ZkTemplate zkTemplate) {
        this.zkTemplate = zkTemplate;
        zkDeviceManager = new ZkDeviceManager(zkTemplate);
    }

    public synchronized void start(ZkDeviceListener listener) {
        if (treeCache != null) {
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
    }

    public synchronized void stop() {
        if (treeCache != null) {
            treeCache.close();
        }
    }
}
