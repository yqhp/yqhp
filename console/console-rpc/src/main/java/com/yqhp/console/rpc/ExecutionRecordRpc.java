package com.yqhp.console.rpc;

import com.yqhp.console.model.vo.DeviceTask;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author jiangyitao
 */
@FeignClient(name = "console-service", path = "/console/executionRecord", contextId = "executionRecord")
public interface ExecutionRecordRpc {

    @GetMapping("/receive")
    DeviceTask receive(@RequestParam("deviceId") String deviceId);

}
