package com.example.rentalmanager.property.infrastructure.persistence.adapter;

import com.example.rentalmanager.property.application.port.output.UnitUnavailabilityPersistencePort;
import com.example.rentalmanager.property.domain.valueobject.UnitUnavailability;
import com.example.rentalmanager.property.infrastructure.persistence.entity.UnitUnavailabilityJpaEntity;
import com.example.rentalmanager.property.infrastructure.persistence.repository.UnitUnavailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.UUID;

/** Secondary adapter implementing {@link UnitUnavailabilityPersistencePort} via Cassandra. */
@Component
@RequiredArgsConstructor
public class UnitUnavailabilityPersistenceAdapter implements UnitUnavailabilityPersistencePort {

    private final UnitUnavailabilityRepository repository;

    @Override
    public Mono<UnitUnavailability> save(UnitUnavailability u) {
        return repository.save(toEntity(u)).map(this::toDomain);
    }

    @Override
    public Flux<UnitUnavailability> findByUnitId(UUID unitId) {
        return repository.findByUnitId(unitId)
                .map(this::toDomain)
                .sort(Comparator.comparing(UnitUnavailability::createdAt).reversed());
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private UnitUnavailabilityJpaEntity toEntity(UnitUnavailability u) {
        return UnitUnavailabilityJpaEntity.builder()
                .id(u.id())
                .unitId(u.unitId())
                .reason(u.reason())
                .startDate(u.startDate())
                .endDate(u.endDate())
                .createdAt(u.createdAt())
                .build();
    }

    private UnitUnavailability toDomain(UnitUnavailabilityJpaEntity e) {
        return new UnitUnavailability(e.getId(), e.getUnitId(), e.getReason(),
                e.getStartDate(), e.getEndDate(), e.getCreatedAt());
    }
}
