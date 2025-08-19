package com.learnd.integration.kafka.enums;


public enum KafkaTopic {
    CARD_UPDATE("card-update"),
    RECOMMEND_FEEDBACK("recommend-feedback"),
    CLUSTER_UPDATE("cluster-update");

    private final String topicName;

    KafkaTopic(String topicName) {
        this.topicName = topicName;
    }

    public String getName() {
        return topicName;
    }
}
