package com.yqhp.console.web.kafka;

import com.yqhp.common.commons.util.JacksonUtils;
import com.yqhp.common.kafka.message.DeviceTaskMessage;
import com.yqhp.common.kafka.message.Topics;
import com.yqhp.console.repository.entity.DeviceTask;
import com.yqhp.console.web.service.DeviceTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author jiangyitao
 */
@Slf4j
@Component
public class MessageConsumer {

    @Autowired
    private DeviceTaskService deviceTaskService;

    @KafkaListener(topics = Topics.DEVICE_TASK, concurrency = "2")
    public void consumeDeviceTaskMessage(ConsumerRecord<?, String> record) {
        DeviceTaskMessage message = JacksonUtils.readValue(record.value(), DeviceTaskMessage.class);
        DeviceTask deviceTask = message.convertTo();
        deviceTaskService.updateById(deviceTask);
    }
}
