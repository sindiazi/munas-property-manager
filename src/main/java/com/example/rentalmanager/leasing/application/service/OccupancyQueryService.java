package com.example.rentalmanager.leasing.application.service;

import com.example.rentalmanager.leasing.application.dto.response.TenantOccupancyResponse;
import com.example.rentalmanager.leasing.application.dto.response.UnitRentalHistoryResponse;
import com.example.rentalmanager.leasing.application.port.input.GetOccupancyUseCase;
import com.example.rentalmanager.leasing.infrastructure.persistence.entity.TenantOccupiedUnitEntity;
import com.example.rentalmanager.leasing.infrastructure.persistence.entity.UnitRentalHistoryEntity;
import com.example.rentalmanager.leasing.infrastructure.persistence.repository.TenantOccupiedUnitRepository;
import com.example.rentalmanager.leasing.infrastructure.persistence.repository.UnitRentalHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Query service reading directly from CQRS projection tables.
 * No domain objects are involved — these are pure read-model lookups.
 */
@Service
@RequiredArgsConstructor
public class OccupancyQueryService implements GetOccupancyUseCase {

    private final TenantOccupiedUnitRepository occupancyRepo;
    private final UnitRentalHistoryRepository  historyRepo;

    @Override
    public Mono<TenantOccupancyResponse> getCurrentOccupancy(UUID tenantId) {
        return occupancyRepo.findById(tenantId)
                .map(this::toOccupancyResponse);
    }

    @Override
    public Flux<UnitRentalHistoryResponse> getUnitHistory(UUID unitId) {
        return historyRepo.findByKeyUnitId(unitId)
                .map(this::toHistoryResponse);
    }

    private TenantOccupancyResponse toOccupancyResponse(TenantOccupiedUnitEntity e) {
        return new TenantOccupancyResponse(
                e.getTenantId(), e.getUnitId(), e.getPropertyId(), e.getLeaseId(),
                e.getMonthlyRent(), e.getLeaseStart(), e.getLeaseEnd(), e.getOccupiedSince());
    }

    private UnitRentalHistoryResponse toHistoryResponse(UnitRentalHistoryEntity e) {
        return new UnitRentalHistoryResponse(
                e.getKey().getUnitId(), e.getKey().getLeaseId(), e.getTenantId(),
                e.getPropertyId(), e.getMonthlyRent(),
                e.getKey().getLeaseStart(), e.getLeaseEnd(), e.getStatus());
    }
}
