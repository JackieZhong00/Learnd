package com.learnd.integration.kafka.controller;

import com.learnd.integration.kafka.producer.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KafkaController {


    private final MessageProducer messageProducer;

    public KafkaController(MessageProducer msgProducer) {
        this.messageProducer = msgProducer;
    }

    @PostMapping("/send")
    public String sendMessage(@RequestParam("message") String message) {
        messageProducer.sendMessage("my-topic", message);
        return "Message sent: " + message;
    }

}