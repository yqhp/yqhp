package com.yqhp.console.web.kafka;

import com.yqhp.common.commons.util.JacksonUtils;
import com.yqhp.common.kafka.message.DocExecutionRecordMessage;
import com.yqhp.common.kafka.message.PluginExecutionRecordMessage;
import com.yqhp.common.kafka.message.Topics;
import com.yqhp.console.web.service.DocExecutionRecordService;
import com.yqhp.console.web.service.PluginExecutionRecordService;
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
    private DocExecutionRecordService docExecutionRecordService;
    @Autowired
    private PluginExecutionRecordService pluginExecutionRecordService;

    @KafkaListener(topics = Topics.DOC_EXECUTION_RECORD, concurrency = "2")
    public void consumeDocExecutionRecordMessage(ConsumerRecord<?, String> record) {
        DocExecutionRecordMessage message = JacksonUtils.readValue(record.value(), DocExecutionRecordMessage.class);
        docExecutionRecordService.updateById(message.convertTo());
    }

    @KafkaListener(topics = Topics.PLUGIN_EXECUTION_RECORD, concurrency = "2")
    public void consumePluginExecutionRecordMessage(ConsumerRecord<?, String> record) {
        PluginExecutionRecordMessage message = JacksonUtils.readValue(record.value(), PluginExecutionRecordMessage.class);
        pluginExecutionRecordService.updateById(message.convertTo());
    }
}
