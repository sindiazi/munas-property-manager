package com.example.rentalmanager.property.infrastructure.persistence.adapter;

import com.example.rentalmanager.leasing.application.port.output.UnitStatusPort;
import com.example.rentalmanager.property.domain.valueobject.UnitStatus;
import com.example.rentalmanager.property.infrastructure.persistence.repository.PropertyUnitR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Implements the {@link UnitStatusPort} from the Leasing context.
 * Lives in Property infrastructure to keep Property as the single source of truth
 * for unit state.
 */
@Component
@RequiredArgsConstructor
public class UnitStatusAdapter implements UnitStatusPort {

    private final PropertyUnitR2dbcRepository unitRepo;

    @Override
    public Mono<Void> markOccupied(UUID unitId) {
        return updateStatus(unitId, UnitStatus.OCCUPIED);
    }

    @Override
    public Mono<Void> markAvailable(UUID unitId) {
        return updateStatus(unitId, UnitStatus.AVAILABLE);
    }

    private Mono<Void> updateStatus(UUID unitId, UnitStatus status) {
        return unitRepo.findById(unitId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Unit not found: " + unitId)))
                .flatMap(entity -> {
                    entity.setStatus(status);
                    return unitRepo.save(entity);
                })
                .then();
    }
}
