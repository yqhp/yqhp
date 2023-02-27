package com.yqhp.agent.web.kafka;

import com.yqhp.common.commons.util.JacksonUtils;
import com.yqhp.common.kafka.message.DeviceTaskMessage;
import com.yqhp.common.kafka.message.StepExecutionRecordMessage;
import com.yqhp.common.kafka.message.Topics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author jiangyitao
 */
@Component
public class MessageProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * deviceId维度需要保证有序消费，所以这里用deviceId作为key
     */
    public void sendDeviceTaskMessage(DeviceTaskMessage message) {
        Assert.isTrue(message != null
                && StringUtils.hasText(message.getId())
                && StringUtils.hasText(message.getDeviceId()), "Illegal message");
        kafkaTemplate.send(
                Topics.DEVICE_TASK,
                message.getDeviceId(),
                JacksonUtils.writeValueAsString(message)
        );
    }

    /**
     * deviceId维度需要保证有序消费，所以这里用deviceId作为key
     */
    public void sendStepExecutionRecordMessage(StepExecutionRecordMessage message) {
        Assert.isTrue(message != null
                && StringUtils.hasText(message.getId())
                && StringUtils.hasText(message.getDeviceId()), "Illegal message");
        kafkaTemplate.send(
                Topics.STEP_EXECUTION_RECORD,
                message.getDeviceId(),
                JacksonUtils.writeValueAsString(message)
        );
    }
}
