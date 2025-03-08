package org.nirp.util

import com.google.api.gax.core.CredentialsProvider
import com.google.cloud.pubsub.v1.AckReplyConsumer
import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.cloud.pubsub.v1.Publisher
import com.google.cloud.pubsub.v1.Subscriber
import com.google.protobuf.ByteString
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.PubsubMessage
import com.google.pubsub.v1.TopicName
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty

@ApplicationScoped
class PubSubUtil {

    @ConfigProperty(name = "quarkus.google.cloud.project-id")
    lateinit var projectId: String

    @Inject
    lateinit var credentialsProvider: CredentialsProvider

    fun publishMessage(
        topic: String,
        message: String,
        attributes: Map<String, String>
    ) {
        val topicName = TopicName.of(projectId,topic)

        val publisher = Publisher
            .newBuilder(topicName)
            .setCredentialsProvider(credentialsProvider)
            .build()

        val pubSubMessage = PubsubMessage.newBuilder()
            .setData(ByteString.copyFromUtf8(message))
            .putAllAttributes(attributes)
            .build()

        val messageId = publisher.publish(pubSubMessage).get()

        println("Message published. ID: $messageId")

        publisher.shutdown()
    }

    fun processMessage(
        message:String,
        consumer: AckReplyConsumer
    ) {
        try {
            println("Message received : $message")

            consumer.ack()
        } catch (e: Exception) {
            println("Error processing message: $message")
            consumer.nack()
        }
    }

    fun startSubscriber(
        subscriptionId : String
    ) {
        val subscriptionName = ProjectSubscriptionName.of(projectId,subscriptionId)

        val messageReceiver = MessageReceiver { message, consumer ->
            processMessage(message.data.toStringUtf8(), consumer)
        }

        try {
            Subscriber.newBuilder(subscriptionName,messageReceiver)
                .setCredentialsProvider(credentialsProvider)
                .build()
                .startAsync()
                .awaitRunning()
        } catch (e: Exception) {
            println("Error starting subscriber: $e")
        }
    }
}