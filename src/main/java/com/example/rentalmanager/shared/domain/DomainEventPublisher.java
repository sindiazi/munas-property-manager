package com.example.rentalmanager.shared.domain;

/**
 * Abstraction for publishing domain events.
 *
 * <p>Application services depend on this interface rather than Spring's
 * {@code ApplicationEventPublisher} directly. This allows the underlying
 * transport to be swapped (e.g. in-process Spring events → Kafka, RabbitMQ)
 * without touching any application-layer code.
 *
 * <p>The default implementation, {@code SpringDomainEventPublisher}, delegates
 * to Spring's {@code ApplicationEventPublisher}. Future implementations can
 * forward events to a message broker by implementing this interface in the
 * infrastructure layer.
 */
public interface DomainEventPublisher {

    /**
     * Publishes a single domain event to all interested subscribers.
     *
     * @param event the event to publish; must not be {@code null}
     */
    void publish(DomainEvent event);
}
