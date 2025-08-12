package com.learnd.integration.kafka.controller;

import com.learnd.integration.kafka.consumer.MessageConsumer;
import com.learnd.integration.kafka.model.CardUpdateEvent;
import com.learnd.integration.kafka.model.RecommendFeedbackEvent;
import com.learnd.integration.kafka.producer.MessageProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class KafkaController {


    private final MessageProducer messageProducer;
    private final MessageConsumer messageConsumer;

    public KafkaController(MessageProducer msgProducer, MessageConsumer msgConsumer) {
        this.messageProducer = msgProducer;
        this.messageConsumer = msgConsumer;
    }

    @PostMapping("/sendCardUpdate")
    public String sendCardUpdateMessage(@RequestBody CardUpdateEvent event) {
        messageProducer.sendCardUpdateMsg("card-update", event);
        return "Message of event sent: " + event;
    }

    @PostMapping("/sendRecommendFeedback")
    public String sendRecommendFeedbackMessage(@RequestBody RecommendFeedbackEvent event) {
        messageProducer.sendFeedbackMsg("recommend-feedback", event);
        return "Message of event sent: " + event;
    }




}