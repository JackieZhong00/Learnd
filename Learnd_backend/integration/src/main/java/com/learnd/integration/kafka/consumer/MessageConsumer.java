package com.learnd.integration.kafka.consumer;

import com.learnd.integration.kafka.config.KafkaConsumerConfig;
import com.learnd.integration.kafka.enums.KafkaTopic;
import com.learnd.integration.kafka.model.CardUpdateEvent;
import com.learnd.integration.kafka.model.RecommendFeedbackEvent;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

@Component
public class MessageConsumer {

    private final ConsumerFactory<String,CardUpdateEvent> cardUpdateEventConsumerFactory;
    private final ConsumerFactory<String,RecommendFeedbackEvent> feedbackFactory;

    public MessageConsumer
            (ConsumerFactory<String,CardUpdateEvent> cardUpdateEventConsumerFactory, ConsumerFactory<String,RecommendFeedbackEvent> feedbackFactory) {
        this.cardUpdateEventConsumerFactory = cardUpdateEventConsumerFactory;
        this.feedbackFactory = feedbackFactory;
    }

    public List<CardUpdateEvent> consumeCardUpdateEvents(int userId, int deckId) {
        List<CardUpdateEvent> listOfCardEvents = new ArrayList<>();
        try (Consumer<String, CardUpdateEvent> consumer = cardUpdateEventConsumerFactory.createConsumer()) {
            consumer.subscribe(Collections.singletonList(KafkaTopic.CARD_UPDATE.getName()));
            ConsumerRecords<String, CardUpdateEvent> records = consumer.poll(Duration.ofSeconds(1));
            Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = new HashMap<>();
            for (ConsumerRecord<String, CardUpdateEvent> record : records) {
                CardUpdateEvent event = record.value();
                if (event.getUserId() == userId && event.getDeckId() == deckId) {
                    listOfCardEvents.add(event);
                    TopicPartition tp = new TopicPartition(record.topic(), record.partition());
                    offsetsToCommit.put(tp, new OffsetAndMetadata(record.offset() + 1));
                }
            }
            if (!offsetsToCommit.isEmpty()) {
                consumer.commitSync(offsetsToCommit);
            }
        } catch (Exception e) {
            System.out.println("exception reached: " + e.getMessage());
        }
        return listOfCardEvents;
    }

    public List<RecommendFeedbackEvent> consumeFeedbackEvents(int userId, int deckId) {
        List<RecommendFeedbackEvent> listOfFeedbackEvents = new ArrayList<>();

        try (Consumer<String, RecommendFeedbackEvent> consumer = feedbackFactory.createConsumer()) {
            consumer.subscribe(Collections.singletonList(KafkaTopic.RECOMMEND_FEEDBACK.getName()));
            ConsumerRecords<String, RecommendFeedbackEvent> records = consumer.poll(Duration.ofSeconds(1));
            Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = new HashMap<>();
            for (ConsumerRecord<String, RecommendFeedbackEvent> record : records) {
                RecommendFeedbackEvent event = record.value();
                if (event.getUserId() == userId && event.getDeckId() == deckId) {
                    listOfFeedbackEvents.add(event);
                    TopicPartition tp = new TopicPartition(record.topic(), record.partition());
                    offsetsToCommit.put(tp, new OffsetAndMetadata(record.offset() + 1));
                }
            }
            if (!offsetsToCommit.isEmpty()) {
                consumer.commitSync(offsetsToCommit);
            }
        } catch (Exception e) {
            System.out.println("exception reached: " + e.getMessage());
        }
        return listOfFeedbackEvents;
    }

}