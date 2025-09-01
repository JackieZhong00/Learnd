package com.learnd.learnd_main.Learnd.controller;


import com.learnd.integration.grpc.*;
import com.learnd.integration.kafka.model.CardUpdateEvent;

import com.learnd.integration.kafka.model.RecommendFeedbackEvent;
import com.learnd.integration.kafka.producer.MessageProducer;
import com.learnd.integration.kafka.service.KafkaConsumerService;
import com.learnd.learnd_main.Learnd.model.Deck;
import com.learnd.learnd_main.Learnd.model.Flashcard;
import com.learnd.learnd_main.Learnd.repo.DeckRepository;
import jdk.jfr.Event;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping(path = "/api/recommend")
public class RecommendationController {

    private final KafkaConsumerService kafkaConsumerService;
    private final RagGrpcClient ragGrpcClient;
    private final DeckRepository deckRepository;
    private final MessageProducer   messageProducer;

    public RecommendationController(KafkaConsumerService kafkaConsumerService,
                                    RagGrpcClient ragGrpcClient,
                                    DeckRepository deckRepository,
                                    MessageProducer messageProducer) {
        this.kafkaConsumerService = kafkaConsumerService;
        this.ragGrpcClient = ragGrpcClient;
        this.deckRepository = deckRepository;
        this.messageProducer = messageProducer;
    }

    private CardUpdateEventGRPC toProtoCardUpdate(CardUpdateEvent event) {
        return CardUpdateEventGRPC.newBuilder()
                .setUpdateType(event.getUpdateType())
                .setCardId(event.getCardId())
                .setDeckId(event.getDeckId())
                .setUserId(event.getUserId())
                .setIsMultipleChoice(event.isMultipleChoice())
                .setQuestion(event.getQuestion())
                .addAllAnswer(event.getAnswer()) // Assuming List<String>
                .build();
    }
    private RecommendFeedbackEventGRPC toProtoFeedback(RecommendFeedbackEvent feedback) {
        return RecommendFeedbackEventGRPC.newBuilder()
                .setWasAccepted(feedback.isWasAccepted())
                .setUserId(feedback.getUserId())
                .setDeckId(feedback.getDeckId())
                .setQuestion(feedback.getQuestion())
                .addAllAnswer(feedback.getAnswer())
                .setIsMultipleChoice(feedback.isMultipleChoice())
                .build();
    }

    @GetMapping("/getRecommendations/{userId}/{deckId}/{deckName}")
    public Flashcard getRecommendations(@PathVariable("userId") int userId,
                                   @PathVariable("deckId") int deckId, @PathVariable String deckName) {
        List<CardUpdateEvent> recentCardUpdates = kafkaConsumerService.consumeCardUpdateEvents(userId, deckId);
        List<RecommendFeedbackEvent> recentFeedbackUpdate = kafkaConsumerService.consumeFeedbackEvents(userId, deckId);
        Deck deck = deckRepository.findById(deckId).orElseThrow(NoSuchElementException::new);
        List<String> listOfDecksCategories = new ArrayList<>(deck.getListOfCategories());
        BatchMessageGRPC grpcRequest = BatchMessageGRPC.newBuilder()
                                      .setUserId(userId)
                                      .setDeckId(deckId)
                                      .setDeckName(deckName)
                                      .addAllCategories(listOfDecksCategories)
                                      .addAllCardUpdate(recentCardUpdates.stream().map(this::toProtoCardUpdate).toList())
                                      .addAllFeedback(recentFeedbackUpdate.stream().map(this::toProtoFeedback).toList())
                                      .build();
        DispatchResultGRPC result = ragGrpcClient.send(grpcRequest);
        return new Flashcard(result.getQuestion(),result.getAnswer(0));
    }


    //this should be flow of logic:
    // 1. batch from kafka using the messageConsumer component's method
    // 2. grpcMessageBuilder is a custom class you need to create for the purpose of formatting the kafka events
    //    to follow the grpc message templates defined in your protobuf
    // 3. ragGrpcClient is custom class where you define endpoints for the grpc methodsdefined in protobuf
    //    - is basically the grpcController class method to reach rag server
//    @GetMapping("/api/recommendation/{userId}")
//    public ResponseEntity<RecommendationDto> recommendCard(@PathVariable String userId) {
//        List<KafkaEvent> events = messageConsumer.consumeEventsForUser(userId);
//        Recommendation request = grpcMessageBuilder.build(events); // convert events to gRPC request
//
//        Recommendation result = ragGrpcClient.getRecommendation(request);
//        return ResponseEntity.ok(new RecommendationDto(result));
//    }

}
