package com.learnd.integration.kafka.service;

import com.learnd.integration.kafka.consumer.MessageConsumer;
import com.learnd.integration.kafka.model.CardUpdateEvent;
import com.learnd.integration.kafka.model.RecommendFeedbackEvent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KafkaConsumerService {

    private final MessageConsumer messageConsumer;

    public KafkaConsumerService(MessageConsumer messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    public List<CardUpdateEvent> consumeCardUpdateEvents(int userId, int deckId) {
        return messageConsumer.consumeCardUpdateEvents(userId, deckId);
    }

    public List<RecommendFeedbackEvent> consumeFeedbackEvents(int userId, int deckId) {
        return messageConsumer.consumeFeedbackEvents(userId, deckId);
    }
}
