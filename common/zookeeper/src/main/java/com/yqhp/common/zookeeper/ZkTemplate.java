package com.yqhp.common.zookeeper;

import com.yqhp.common.zookeeper.exception.ZkException;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.zookeeper.CreateMode;

import java.util.List;
import java.util.concurrent.Executor;

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

    public boolean exist(String path) {
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

    public TreeCache watch(String path, TreeCacheListener listener) {
        return watch(path, listener, null);
    }

    public TreeCache watch(String path, TreeCacheListener listener, Executor executor) {
        TreeCache treeCache = new TreeCache(curator, path);
        Listenable<TreeCacheListener> listenable = treeCache.getListenable();
        if (executor != null) {
            listenable.addListener(listener, executor);
        } else {
            listenable.addListener(listener);
        }

        try {
            treeCache.start();
            return treeCache;
        } catch (Exception e) {
            throw new ZkException(e);
        }
    }
}
