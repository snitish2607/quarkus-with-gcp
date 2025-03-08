package org.nirp.config

import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.protobuf.Duration
import com.google.pubsub.v1.DeadLetterPolicy
import com.google.pubsub.v1.RetryPolicy
import com.google.pubsub.v1.Subscription
import com.google.pubsub.v1.SubscriptionName
import com.google.pubsub.v1.TopicName
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.nirp.util.PubSubUtil


class PubSubConfig {

    @Inject
    lateinit var pubSubUtil: PubSubUtil

    @Inject
    lateinit var topicAdminClient: TopicAdminClient

    @Inject
    lateinit var subscriptionAdminClient: SubscriptionAdminClient

    @ConfigProperty(name = "quarkus.google.cloud.project-id")
    lateinit var projectId: String

    fun initPubSub(@Observes event: StartupEvent) {

        println("Initializing PubSub")

        createTopicIfNotExists("second-topic")
        createSubscriptionIfNotExists("filter-subs", "first-topic", "filter")

//        pubSubUtil.startSubscriber("first-subs")
//        pubSubUtil.startSubscriber("second-subs")
        pubSubUtil.startSubscriber("filter-subs")
    }

    fun createTopicIfNotExists(topicName: String){
        val topic = TopicName.of(projectId, topicName)

        try {
            topicAdminClient.getTopic(topic)
        } catch (e: Exception) {
            println("Creating topic $topicName")
            topicAdminClient.createTopic(topic)
        }
    }

    fun createSubscriptionIfNotExists(
        subscriptionName: String,
        topicName: String,
        feature: String
    ) {
        val topic = TopicName.of(projectId,topicName)

        val subscription = SubscriptionName.of(projectId, subscriptionName)

        try {
            subscriptionAdminClient.getSubscription(subscription)
        } catch (e: Exception) {
            println("Creating subscription $subscriptionName")
            subscriptionAdminClient.createSubscription(
                Subscription.newBuilder()
                    .setName(subscription.toString())
                    .setTopic(topic.toString())
                    .setAckDeadlineSeconds(10)
                    .apply {
                        when(feature) {
                            "filter" -> setFilter("attributes.feature = \"important\"")
                            "retry" -> setRetryPolicy(
                                RetryPolicy.newBuilder()
                                    .setMinimumBackoff(Duration.newBuilder().setSeconds(1).build())
                                    .setMaximumBackoff(Duration.newBuilder().setSeconds(10).build())
                                    .build()
                            )
                            "ordering" -> setEnableMessageOrdering(true)
                            "once-delivery" -> setEnableExactlyOnceDelivery(true)
                            "dead-letter" -> setDeadLetterPolicy(
                                DeadLetterPolicy.newBuilder()
                                    .setDeadLetterTopic("dead-letter-topic")
                                    .setMaxDeliveryAttempts(5)
                                    .build()
                            )
                        }
                    }
                    .build()
            )
        }
    }
}