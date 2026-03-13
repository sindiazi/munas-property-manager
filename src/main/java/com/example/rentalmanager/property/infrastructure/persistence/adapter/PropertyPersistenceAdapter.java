package com.example.rentalmanager.property.infrastructure.persistence.adapter;

import com.example.rentalmanager.property.application.port.output.PropertyPersistencePort;
import com.example.rentalmanager.property.domain.aggregate.Property;
import com.example.rentalmanager.property.domain.valueobject.OwnerId;
import com.example.rentalmanager.property.domain.valueobject.PropertyId;
import com.example.rentalmanager.property.infrastructure.persistence.mapper.PropertyPersistenceMapper;
import com.example.rentalmanager.property.infrastructure.persistence.repository.PropertyR2dbcRepository;
import com.example.rentalmanager.property.infrastructure.persistence.repository.PropertyUnitR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Secondary (driven) adapter — implements {@link PropertyPersistencePort} using
 * Spring Data R2DBC.
 *
 * <p>The two-table strategy (properties + property_units) requires orchestrating
 * multiple reactive streams. All saves are performed transactionally by the
 * application service's {@code @Transactional} boundary.
 */
@Component
@RequiredArgsConstructor
public class PropertyPersistenceAdapter implements PropertyPersistencePort {

    private final PropertyR2dbcRepository     propertyRepo;
    private final PropertyUnitR2dbcRepository unitRepo;
    private final PropertyPersistenceMapper   mapper;

    @Override
    public Mono<Property> save(Property property) {
        var propertyEntity = mapper.toEntity(property);
        var unitEntities   = property.getUnits().stream()
                .map(u -> mapper.toUnitEntity(u, property.getId()))
                .toList();

        return propertyRepo.save(propertyEntity)
                // Delete and re-insert units to handle additions/removals cleanly
                .flatMap(saved -> unitRepo.deleteByPropertyId(saved.getId())
                        .thenMany(Flux.fromIterable(unitEntities))
                        .flatMap(unitRepo::save)
                        .collectList()
                        .map(savedUnits -> mapper.toDomain(saved, savedUnits)));
    }

    @Override
    public Mono<Property> findById(PropertyId id) {
        return propertyRepo.findById(id.value())
                .flatMap(entity -> unitRepo.findByPropertyId(entity.getId())
                        .collectList()
                        .map(units -> mapper.toDomain(entity, units)));
    }

    @Override
    public Flux<Property> findByOwnerId(OwnerId ownerId) {
        return propertyRepo.findByOwnerId(ownerId.value())
                .flatMap(entity -> unitRepo.findByPropertyId(entity.getId())
                        .collectList()
                        .map(units -> mapper.toDomain(entity, units)));
    }

    @Override
    public Flux<Property> findAll() {
        return propertyRepo.findAll()
                .flatMap(entity -> unitRepo.findByPropertyId(entity.getId())
                        .collectList()
                        .map(units -> mapper.toDomain(entity, units)));
    }

    @Override
    public Mono<Boolean> existsById(PropertyId id) {
        return propertyRepo.existsById(id.value());
    }

    @Override
    public Mono<Void> deleteById(PropertyId id) {
        return unitRepo.deleteByPropertyId(id.value())
                .then(propertyRepo.deleteById(id.value()));
    }
}
