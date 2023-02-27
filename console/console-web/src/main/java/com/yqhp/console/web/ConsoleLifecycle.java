package com.yqhp.console.web;

import com.yqhp.common.zkdevice.ZkDevice;
import com.yqhp.common.zkdevice.ZkDeviceListener;
import com.yqhp.common.zkdevice.ZkDeviceWatcher;
import com.yqhp.common.zookeeper.ZkTemplate;
import com.yqhp.console.web.common.ZkDeviceContainer;
import com.yqhp.console.web.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author jiangyitao
 */
@Slf4j
@Component
public class ConsoleLifecycle implements SmartLifecycle {

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Autowired
    private ZkTemplate zkTemplate;
    @Autowired
    private DeviceService deviceService;

    private ZkDeviceWatcher zkDeviceWatcher;

    @Override
    public void start() {
        if (!running.compareAndSet(false, true))
            return;

        zkDeviceWatcher = new ZkDeviceWatcher(zkTemplate);
        zkDeviceWatcher.start(new ZkDeviceListener() {
            @Override
            public void added(ZkDevice device) {
                log.info("[zk-device][online]{}", device);
                ZkDeviceContainer.add(device);
                deviceService.saveIfAbsent(device);
            }

            @Override
            public void removed(ZkDevice device) {
                log.info("[zk-device][offline]{}", device);
                ZkDeviceContainer.removeById(device.getId());
            }

            @Override
            public void updated(ZkDevice device) {
                log.info("[zk-device][updated]{}", device);
                ZkDeviceContainer.add(device);
            }
        });
    }

    @Override
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        if (zkDeviceWatcher != null) {
            zkDeviceWatcher.stop();
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }
}

