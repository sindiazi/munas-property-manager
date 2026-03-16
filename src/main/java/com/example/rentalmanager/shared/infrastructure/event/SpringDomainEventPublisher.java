package com.example.rentalmanager.shared.infrastructure.event;

import com.example.rentalmanager.shared.domain.DomainEvent;
import com.example.rentalmanager.shared.domain.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Spring-backed implementation of {@link DomainEventPublisher}.
 *
 * <p>Delegates to Spring's {@link ApplicationEventPublisher}, which routes
 * events to all {@code @EventListener}-annotated handlers in the same JVM
 * process. To switch to an external broker (Kafka, RabbitMQ, etc.), provide
 * an alternative {@link DomainEventPublisher} bean and remove this one —
 * no application-layer code needs to change.
 */
@Component
@RequiredArgsConstructor
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher springPublisher;

    @Override
    public void publish(DomainEvent event) {
        springPublisher.publishEvent(event);
    }
}
