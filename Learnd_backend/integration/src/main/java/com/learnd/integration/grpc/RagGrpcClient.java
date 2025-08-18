package com.learnd.integration.grpc;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.springframework.stereotype.Service;

@Service
public class RagGrpcClient {
    private final EventDispatcherGrpc.EventDispatcherBlockingStub blockingStub;

    public RagGrpcClient() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                        .usePlaintext()
                        .build();
        System.out.println("Grpc Client started on port 50051");
        this.blockingStub = EventDispatcherGrpc.newBlockingStub(channel);
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
}

