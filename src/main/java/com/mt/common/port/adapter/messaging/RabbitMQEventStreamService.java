package com.mt.common.port.adapter.messaging;

import com.mt.common.domain.CommonDomainRegistry;
import com.mt.common.domain_event.EventStreamService;
import com.mt.common.domain_event.StoredEvent;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static com.mt.common.CommonConstant.EXCHANGE_NAME;

@Slf4j
@Component
public class RabbitMQEventStreamService implements EventStreamService {
    @Override
    public void subscribe(String appName, boolean internal, @Nullable String fixedQueueName, Consumer<StoredEvent> consumer, String... topics) {
        String routingKeyWithoutTopic = appName + "." + (internal ? "internal" : "external") + ".";
        String queueName;
        if (fixedQueueName != null) {
            queueName = fixedQueueName;
        } else {
            Long id = CommonDomainRegistry.getUniqueIdGeneratorService().id();
            queueName = Long.toString(id, 36);
        }
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            log.debug("mq message received");
            String s = new String(delivery.getBody(), StandardCharsets.UTF_8);
            StoredEvent event = CommonDomainRegistry.getCustomObjectSerializer().deserialize(s, StoredEvent.class);
            consumer.accept(event);
            log.debug("mq message consumed");
        };
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare(queueName, true, false, false, null);
            for (String topic : topics) {
                channel.queueBind(queueName, EXCHANGE_NAME, routingKeyWithoutTopic + topic);
            }
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });
        } catch (IOException | TimeoutException e) {
            log.error("unable create queue for {} with routing key {} and queue name {}", appName, routingKeyWithoutTopic, queueName, e);
        }
    }

    @Override
    public void next(String appName, boolean internal, String topic, StoredEvent event) {
        String routingKey = appName + "." + (internal ? "internal" : "external") + "." + topic;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, "topic");
            channel.basicPublish(EXCHANGE_NAME, routingKey,
                    null,
                    CommonDomainRegistry.getCustomObjectSerializer().serialize(event).getBytes(StandardCharsets.UTF_8));
        } catch (IOException | TimeoutException e) {
            log.error("unable to publish message to rabbitmq", e);
        }
    }
}
