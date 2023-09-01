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
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.*;

/**
 * @author jiangyitao
 */
@Slf4j
public class ZkDeviceWatcher {

    private final ZkTemplate zkTemplate;
    private final ZkDeviceManager zkDeviceManager;

    private CuratorCache curatorCache;

    public ZkDeviceWatcher(ZkTemplate zkTemplate) {
        this.zkTemplate = zkTemplate;
        zkDeviceManager = new ZkDeviceManager(zkTemplate);
    }

    public synchronized void start(ZkDeviceListener listener) {
        if (curatorCache != null) {
            throw new IllegalStateException("ZkDeviceWatcher is running");
        }

        CuratorCacheListener cacheListener = CuratorCacheListener.builder()
                .forCreates((node) -> {
                    log.info("zk node added, path={}", node.getPath());
                    ZkDevice zkDevice = zkDeviceManager.toZkDevice(node.getData());
                    if (zkDevice != null) {
                        listener.added(zkDevice);
                    }
                })
                .forChanges(((oldNode, node) -> {
                    log.info("zk node updated, path={}", node.getPath());
                    ZkDevice zkDevice = zkDeviceManager.toZkDevice(node.getData());
                    if (zkDevice != null) {
                        listener.updated(zkDevice);
                    }
                }))
                .forDeletes((node) -> {
                    log.info("zk node removed, path={}", node.getPath());
                    ZkDevice zkDevice = zkDeviceManager.toZkDevice(node.getData());
                    if (zkDevice != null) {
                        listener.removed(zkDevice);
                    }
                }).build();
        curatorCache = zkTemplate.watch(zkDeviceManager.getWatchPath(), cacheListener);
    }

    public synchronized void stop() {
        if (curatorCache != null) {
            log.info("Close CuratorCache");
            curatorCache.close();
            curatorCache = null;
        }
    }
}
