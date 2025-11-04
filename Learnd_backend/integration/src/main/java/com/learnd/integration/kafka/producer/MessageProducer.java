package com.learnd.integration.kafka.producer;

import com.learnd.integration.kafka.model.CardUpdateEvent;
import com.learnd.integration.kafka.model.RecommendFeedbackEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Component
public class MessageProducer {

    @Autowired
    private KafkaTemplate<String, CardUpdateEvent> kafkaCardUpdateTemplate;
    @Autowired
    private KafkaTemplate<String, RecommendFeedbackEvent> kafkaFeedbackTemplate;


    //uses kafkaTemplate bean to send message to the following topic with the following  message in payload
    public void sendCardUpdateMsg(String topic, CardUpdateEvent event) {
        kafkaCardUpdateTemplate.send(topic, event);
    }


    public void sendFeedbackMsg(String topic, RecommendFeedbackEvent event) {
        kafkaFeedbackTemplate.send(topic, event);
    }

}