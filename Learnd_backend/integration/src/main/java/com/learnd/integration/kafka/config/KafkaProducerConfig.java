package com.learnd.integration.kafka.config;

import com.learnd.integration.kafka.model.CardUpdateEvent;
import com.learnd.integration.kafka.model.RecommendFeedbackEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import java.util.HashMap;
import java.util.Map;
@Configuration
public class KafkaProducerConfig {


    @Bean
    public ProducerFactory<String, CardUpdateEvent> cardProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps); //DefaultKafkaProducerFactory implements ProducerFactory
    }

    @Bean
    public ProducerFactory<String, RecommendFeedbackEvent> feedbackProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }



    @Bean
    public KafkaTemplate<String, CardUpdateEvent> kafkaCardUpdateTemplate
            (ProducerFactory<String, CardUpdateEvent> cardProducerFactory) {
        return new KafkaTemplate<>(cardProducerFactory);
    }

    @Bean
    public KafkaTemplate<String, RecommendFeedbackEvent> kafkaFeedbackTemplate
            (ProducerFactory<String, RecommendFeedbackEvent> feedbackProducerFactory) {
        return new KafkaTemplate<>(feedbackProducerFactory);
    }

}