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
package com.yqhp.common.zookeeper;

import com.yqhp.common.zookeeper.exception.ZkException;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.zookeeper.CreateMode;

import java.util.List;

/**
 * @author jiangyitao
 */
@Slf4j
public class ZkTemplate {

    private final CuratorFramework curator;

    public ZkTemplate(CuratorFrameworkFactory.Builder builder) {
        curator = builder.build();
    }

    public void start() {
        log.info("curator start");
        curator.start();
    }

    public void close() {
        log.info("curator close");
        curator.close();
    }

    public void create(CreateMode mode, String path) {
        create(mode, path, (byte[]) null);
    }

    public void create(CreateMode mode, String path, String data) {
        create(mode, path, data.getBytes());
    }

    public void create(CreateMode mode, String path, byte[] data) {
        try {
            if (data == null) {
                curator.create().creatingParentsIfNeeded().withMode(mode).forPath(path);
            } else {
                curator.create().creatingParentsIfNeeded().withMode(mode).forPath(path, data);
            }
        } catch (Exception e) {
            throw new ZkException(e);
        }
    }

    public void delete(String path) {
        try {
            delete(path, true);
        } catch (Exception e) {
            throw new ZkException(e);
        }
    }

    public void delete(String path, boolean deleteChildren) {
        try {
            if (deleteChildren) {
                curator.delete().guaranteed().deletingChildrenIfNeeded().forPath(path);
            } else {
                curator.delete().guaranteed().forPath(path);
            }
        } catch (Exception e) {
            throw new ZkException(e);
        }
    }

    public void setData(String path, String data) {
        setData(path, data.getBytes());
    }

    public void setData(String path, byte[] data) {
        try {
            curator.setData().forPath(path, data);
        } catch (Exception e) {
            throw new ZkException(e);
        }
    }

    public String getStringData(String path) {
        byte[] data = getData(path);
        return data != null ? new String(data) : null;
    }

    public byte[] getData(String path) {
        try {
            return curator.getData().forPath(path);
        } catch (Exception e) {
            throw new ZkException(e);
        }
    }

    public boolean exists(String path) {
        try {
            return curator.checkExists().forPath(path) != null;
        } catch (Exception e) {
            throw new ZkException(e);
        }
    }

    public List<String> getChildren(String path) {
        try {
            return curator.getChildren().forPath(path);
        } catch (Exception e) {
            throw new ZkException(e);
        }
    }

    public CuratorCache watch(String path, CuratorCacheListener listener) {
        CuratorCache cache = CuratorCache.build(curator, path);
        cache.listenable().addListener(listener);
        cache.start();
        return cache;
    }
}
