package com.learnd.integration.kafka.config;

import com.learnd.integration.kafka.enums.KafkaTopic;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

public class KafkaTopicConfig {

    @Bean
    public NewTopic feedbackTopic() {
        return TopicBuilder.name(KafkaTopic.RECOMMEND_FEEDBACK.getName()).build();
    }

    @Bean
    public NewTopic cardUpdateTopic() {
        return TopicBuilder.name(KafkaTopic.CARD_UPDATE.getName()).build();
    }

}
