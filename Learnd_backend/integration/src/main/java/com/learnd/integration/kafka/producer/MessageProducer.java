package com.learnd.integration.kafka.producer;

import com.learnd.integration.kafka.model.CardUpdateEvent;
import com.learnd.integration.kafka.model.RecommendFeedbackEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MessageProducer {

    @Autowired
    private KafkaTemplate<String, CardUpdateEvent> kafkaCardUpdateTemplate;
    @Autowired
    private KafkaTemplate<String, RecommendFeedbackEvent> kafkaFeedbackTemplate;


    //uses kafkaTemplate bean to send message to the following topic with the following  message in payload
    public void sendCardUpdateMsg(String topic, CardUpdateEvent event) {

        //.send(String arg1, CardUpdateEvent arg2) is created by default for kafkaFlashcardTemplate
        // starts domino of:
        //   producerFactory.createProducer() --> returns kafkaProducer of type KafkaProducer
        //   kafkaProducer is used to create: producerRecord = new ProducerRecord<>(topic, message)
        //        -producer looks like serializer for key and value to serialize the message to be sent to kafka
        //   kafkaProducer calls .send(producerRecord) to send message to kafka broker
        kafkaCardUpdateTemplate.send(topic, event);
    }


    public void sendFeedbackMsg(String topic, RecommendFeedbackEvent event) {
        kafkaFeedbackTemplate.send(topic, event);
    }

}