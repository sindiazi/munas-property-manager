package com.example.rentalmanager.tenant.infrastructure.persistence.adapter;

import com.example.rentalmanager.leasing.infrastructure.persistence.repository.TenantOccupiedUnitRepository;
import com.example.rentalmanager.tenant.application.port.output.TenantActiveLeasePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Secondary adapter: implements {@link TenantActiveLeasePort} by querying the
 * {@code tenant_occupied_unit} CQRS read projection.
 *
 * <p>A row existing in that table for a given tenant means they currently hold
 * an active lease. The projection is maintained by {@code LeaseProjectionHandler}
 * in the Leasing context via domain events.
 */
@Component
@RequiredArgsConstructor
public class TenantActiveLeaseAdapter implements TenantActiveLeasePort {

    private final TenantOccupiedUnitRepository occupiedUnitRepository;

    @Override
    public Mono<Boolean> hasActiveLease(UUID tenantId) {
        return occupiedUnitRepository.existsById(tenantId);
    }
}
