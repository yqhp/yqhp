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
        try {
            DocExecutionRecordMessage message = JacksonUtils.readValue(record.value(), DocExecutionRecordMessage.class);
            docExecutionRecordService.updateById(message.convertTo());
        } catch (Throwable cause) {
            log.error("consumeDocExecutionRecordMessage failed, message={}", record.value(), cause);
        }
    }

    @KafkaListener(topics = Topics.PLUGIN_EXECUTION_RECORD, concurrency = "2")
    public void consumePluginExecutionRecordMessage(ConsumerRecord<?, String> record) {
        try {
            PluginExecutionRecordMessage message = JacksonUtils.readValue(record.value(), PluginExecutionRecordMessage.class);
            pluginExecutionRecordService.updateById(message.convertTo());
        } catch (Throwable cause) {
            log.error("consumePluginExecutionRecordMessage failed, message={}", record.value(), cause);
        }
    }
}
