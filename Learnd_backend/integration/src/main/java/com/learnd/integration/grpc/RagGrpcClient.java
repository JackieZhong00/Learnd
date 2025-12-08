package com.learnd.integration.grpc;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class RagGrpcClient {
    private final EventDispatcherGrpc.EventDispatcherBlockingStub blockingStub;
    private final FlashcardToGradeDispatcherGrpc.FlashcardToGradeDispatcherStub flashcardToGradeStub;
    public RagGrpcClient() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                        .usePlaintext()
                        .build();
        System.out.println("Grpc Client started on port 50051");
        this.blockingStub = EventDispatcherGrpc.newBlockingStub(channel);
        this.flashcardToGradeStub = FlashcardToGradeDispatcherGrpc.newStub(channel);
    }

    /** Construct client for accessing RouteGuide server using the existing channel. */
//    public RagGrpcClient(ManagedChannelBuilder<?> channelBuilder) {
//        ManagedChannel channel = channelBuilder.build();
//        blockingStub = EventDispatcherGrpc.newBlockingStub(channel);
//    }

    /** Construct client for accessing RouteGuide server using the existing channel. */
//    public RagGrpcClient(String host, int port) {
//        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
//    }

    public DispatchResultGRPC send(BatchMessageGRPC batchMessage) {
        try {
            return blockingStub.dispatchCardUpdates(batchMessage);
        } catch (StatusRuntimeException e) {
            System.out.println("couldn't make grpc call");
            System.out.println("exception message: " + e.getMessage());
            throw new RuntimeException("couldn't make grpc call");
        }
    };
    public CompletableFuture<FlashcardGrade> sendCardToGrade(FlashcardToGrade flashcardToGrade) {
        CompletableFuture<FlashcardGrade> future = new CompletableFuture<>();
        try {
            StreamObserver<FlashcardGrade> responseObserver = new StreamObserver<>() {

                @Override
                public void onNext(FlashcardGrade value) {
                    // Called when server responds
                    System.out.println("Received grade: " + value.getGrade());
                    future.complete(value);
                    // You could also update a database, notify another service, etc.
                }

                @Override
                public void onError(Throwable t) {
                    // Called if something goes wrong
                    System.out.println("ran into error after receiving grade from RAG server\n");
                    // You can log, retry, or notify the user here
                }

                @Override
                public void onCompleted() {
                    // Called when the server finishes sending responses
                    System.out.println("gRPC call completed");
                }
            };
            flashcardToGradeStub.dispatchFlashcardToGrade(flashcardToGrade, responseObserver);
        } catch (StatusRuntimeException e) {
            System.out.println("couldn't make grpc call");
            System.out.println("exception message: " + e.getMessage());
            throw new RuntimeException("couldn't make grpc call");
        }
        return future;
    }
}

