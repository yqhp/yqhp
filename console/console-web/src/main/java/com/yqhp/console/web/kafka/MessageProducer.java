package com.yqhp.console.web.kafka;

import com.yqhp.common.commons.util.JacksonUtils;
import com.yqhp.common.kafka.message.Topics;
import com.yqhp.console.model.vo.ExecutionReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author jiangyitao
 */
@Component
public class MessageProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendExecutionReport(ExecutionReport report) {
        Assert.notNull(report, "report cannot be null");
        kafkaTemplate.send(Topics.EXECUTION_REPORT, JacksonUtils.writeValueAsString(report));
    }

}
