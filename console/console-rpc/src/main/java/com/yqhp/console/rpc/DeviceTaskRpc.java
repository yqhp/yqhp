package com.yqhp.console.rpc;

import com.yqhp.console.model.vo.ReceivedDeviceTasks;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author jiangyitao
 */
@FeignClient(name = "console-service", path = "/console/deviceTask", contextId = "deviceTask")
public interface DeviceTaskRpc {

    @GetMapping("/receive")
    ReceivedDeviceTasks receive(@RequestParam("deviceId") String deviceId);

}
