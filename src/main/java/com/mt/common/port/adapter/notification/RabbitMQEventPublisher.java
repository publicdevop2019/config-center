//   Copyright 2012,2013 Vaughn Vernon
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.mt.common.port.adapter.notification;

import com.mt.common.domain.model.CommonDomainRegistry;
import com.mt.common.domain_event.DomainEvent;
import com.mt.common.domain_event.EventPublisher;
import com.mt.common.domain_event.EventRepository;
import com.mt.common.notification.PublishedEventTracker;
import com.mt.common.notification.PublishedEventTrackerRepository;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static com.mt.common.CommonConstant.EXCHANGE_NAME;


@Component
@Slf4j
public class RabbitMQEventPublisher implements EventPublisher {

    @Autowired
    private EventRepository eventStore;

    @Autowired
    private PublishedEventTrackerRepository publishedEventTrackerRepository;

    @Override
    public void publishNotifications() {
        PublishedEventTracker publishedNotificationTracker =
                publishedEventTrackerRepository.publishedNotificationTracker();
        List<DomainEvent> storedEvents =
                eventStore.allStoredEventsSince(publishedNotificationTracker.getLastPublishedEventId());
        if (!storedEvents.isEmpty()) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {
                channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
                for (DomainEvent domainEvent : storedEvents) {
                    log.debug("publishing event with id {}", domainEvent.getId());
                    channel.basicPublish(EXCHANGE_NAME, "",
                            null,
                            CommonDomainRegistry.customObjectSerializer().nativeSerialize(domainEvent));
                }

            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
            publishedEventTrackerRepository
                    .trackMostRecentPublishedNotification(
                            publishedNotificationTracker,
                            storedEvents);
        }
    }
}
