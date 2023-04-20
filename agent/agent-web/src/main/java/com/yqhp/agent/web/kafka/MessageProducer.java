package com.yqhp.agent.web.kafka;

import com.yqhp.common.commons.util.JacksonUtils;
import com.yqhp.common.kafka.message.DocExecutionRecordMessage;
import com.yqhp.common.kafka.message.PluginExecutionRecordMessage;
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

    public void sendDocExecutionRecordMessage(DocExecutionRecordMessage message) {
        Assert.isTrue(message != null && StringUtils.hasText(message.getId())
                && StringUtils.hasText(message.getDeviceId()), "Illegal message");
        kafkaTemplate.send(Topics.DOC_EXECUTION_RECORD, message.getDeviceId(), JacksonUtils.writeValueAsString(message));
    }

    public void sendPluginExecutionRecordMessage(PluginExecutionRecordMessage message) {
        Assert.isTrue(message != null && StringUtils.hasText(message.getId())
                && StringUtils.hasText(message.getDeviceId()), "Illegal message");
        kafkaTemplate.send(Topics.PLUGIN_EXECUTION_RECORD, message.getDeviceId(), JacksonUtils.writeValueAsString(message));
    }
}
