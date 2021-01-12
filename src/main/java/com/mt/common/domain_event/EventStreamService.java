package com.mt.common.domain_event;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface EventStreamService {
    void subscribe(String appName, boolean internal, @Nullable String queueName, String topic, Consumer<StoredEvent> consumer);

    void next(String appName, boolean internal, String topic, StoredEvent event);
}
