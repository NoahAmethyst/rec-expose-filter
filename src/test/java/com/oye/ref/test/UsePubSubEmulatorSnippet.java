package com.oye.ref.test;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.cloud.pubsublite.TopicName;
import com.google.pubsub.v1.Topic;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusException;

import java.io.IOException;

/**
 * Snippet that demonstrates creating Pub/Sub clients using the Google Cloud Pub/Sub emulator.
 *
 * <p>Note: clients cannot start/stop the emulator.
 */
public class UsePubSubEmulatorSnippet {

    public static void main(String... args) throws IOException {
        // [START pubsub_use_emulator]
        String hostport = "localhost:8103";
        ManagedChannel channel = ManagedChannelBuilder.forTarget(hostport).usePlaintext().build();
        try {
            TransportChannelProvider channelProvider =
                    FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
            CredentialsProvider credentialsProvider = NoCredentialsProvider.create();

            // Set the channel and credentials provider when creating a `TopicAdminClient`.
            // Similarly for SubscriptionAdminClient
            TopicAdminClient topicClient =
                    TopicAdminClient.create(
                            TopicAdminSettings.newBuilder()
                                    .setTransportChannelProvider(channelProvider)
                                    .setCredentialsProvider(credentialsProvider)
                                    .build());

            Topic topic = topicClient.getTopic("projects/oyechat/topics/demotopic");

            TopicName topicName = TopicName.of(topic.getName());

            // Set the channel and credentials provider when creating a `Publisher`.
            // Similarly for Subscriber
            Publisher publisher =
                    Publisher.newBuilder(String.valueOf(topicName))
                            .setChannelProvider(channelProvider)
                            .setCredentialsProvider(credentialsProvider)
                            .build();
            System.out.println(publisher.getTopicNameString());

        } catch (StatusException e) {
            e.printStackTrace();
        } finally {
            channel.shutdown();
        }
        // [END pubsub_use_emulator]
    }
}