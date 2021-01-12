package com.mt.common.domain_event;

import com.mt.common.domain.model.CommonDomainRegistry;
import com.mt.common.notification.PublishedEventTracker;
import com.mt.common.notification.PublishedEventTrackerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@EnableScheduling
public class EventApplicationServiceScheduler {
    @Autowired
    private EventStreamService eventStreamService;
    @Autowired
    private EventRepository eventStore;

    @Autowired
    private PublishedEventTrackerRepository trackerRepository;

    @Value("${spring.application.name}")
    private String appName;

    @Transactional
    @Scheduled(fixedRateString = "${fixedRate.in.milliseconds.notification}")
    public void streaming() {
        PublishedEventTracker eventTracker =
                trackerRepository.publishedNotificationTracker();
        List<StoredEvent> storedEvents = eventStore.allStoredEventsSince(eventTracker.getLastPublishedEventId());
        if (!storedEvents.isEmpty()) {
            for (StoredEvent event : storedEvents) {
                CommonDomainRegistry.eventStreamService().next(appName, event.isInternal(), event.getTopic(), event);
            }
            trackerRepository
                    .trackMostRecentPublishedNotification(eventTracker, storedEvents);
        }
    }

}
