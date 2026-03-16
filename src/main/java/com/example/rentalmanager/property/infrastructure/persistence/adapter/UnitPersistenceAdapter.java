package com.example.rentalmanager.property.infrastructure.persistence.adapter;

import com.example.rentalmanager.property.application.port.output.UnitPersistencePort;
import com.example.rentalmanager.property.domain.aggregate.PropertyUnit;
import com.example.rentalmanager.property.domain.valueobject.UnitStatus;
import com.example.rentalmanager.property.infrastructure.persistence.mapper.PropertyPersistenceMapper;
import com.example.rentalmanager.property.infrastructure.persistence.repository.PropertyUnitR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Secondary adapter for individual unit read/status-update operations. */
@Component
@RequiredArgsConstructor
public class UnitPersistenceAdapter implements UnitPersistencePort {

    private final PropertyUnitR2dbcRepository unitRepo;
    private final PropertyPersistenceMapper   mapper;

    @Override
    public Mono<PropertyUnit> findById(UUID unitId) {
        return unitRepo.findById(unitId).map(mapper::toDomainUnit);
    }

    @Override
    public Mono<PropertyUnit> saveStatus(UUID unitId, UnitStatus newStatus) {
        return unitRepo.findById(unitId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Unit not found: " + unitId)))
                .flatMap(entity -> {
                    entity.setStatus(newStatus);
                    return unitRepo.save(entity);
                })
                .map(mapper::toDomainUnit);
    }
}
