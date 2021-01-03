package com.mt.common.domain_event;

import com.mt.common.domain_event.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@EnableScheduling
public class EventApplicationServiceScheduler {
    @Autowired
    private EventPublisher eventPublisher;

    @Scheduled(fixedRateString = "${fixedRate.in.milliseconds.notification}")
    @Transactional
    public void publishEvents() {
        eventPublisher.publishNotifications();
    }

}
