package com.learnd.integration.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

public class KafkaTopicConfig {

    @Bean
    public NewTopic feedbackTopic() {
        return TopicBuilder.name("recommend-feedback").build();
    }

    @Bean
    public NewTopic cardUpdateTopic() {
        return TopicBuilder.name("card-update").build();
    }

}
