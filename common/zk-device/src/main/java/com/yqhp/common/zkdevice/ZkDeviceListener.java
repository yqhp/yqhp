package com.yqhp.common.zkdevice;

/**
 * @author jiangyitao
 */
public interface ZkDeviceListener {
    void added(ZkDevice device);

    void removed(ZkDevice device);

    void updated(ZkDevice device);
}
