package com.example.rentalmanager.leasing.infrastructure.persistence.adapter;

import com.example.rentalmanager.leasing.application.dto.response.TenantOccupancyResponse;
import com.example.rentalmanager.leasing.application.dto.response.UnitRentalHistoryResponse;
import com.example.rentalmanager.leasing.application.port.output.OccupancyProjectionPort;
import com.example.rentalmanager.leasing.infrastructure.persistence.entity.TenantOccupiedUnitEntity;
import com.example.rentalmanager.leasing.infrastructure.persistence.entity.UnitRentalHistoryEntity;
import com.example.rentalmanager.leasing.infrastructure.persistence.repository.TenantOccupiedUnitRepository;
import com.example.rentalmanager.leasing.infrastructure.persistence.repository.UnitRentalHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Secondary adapter implementing {@link OccupancyProjectionPort} against Cassandra projection tables. */
@Component
@RequiredArgsConstructor
public class OccupancyProjectionAdapter implements OccupancyProjectionPort {

    private final TenantOccupiedUnitRepository occupancyRepo;
    private final UnitRentalHistoryRepository  historyRepo;

    @Override
    public Mono<TenantOccupancyResponse> findCurrentOccupancy(UUID tenantId) {
        return occupancyRepo.findById(tenantId).map(this::toOccupancyResponse);
    }

    @Override
    public Flux<UnitRentalHistoryResponse> findUnitHistory(UUID unitId) {
        return historyRepo.findByKeyUnitId(unitId).map(this::toHistoryResponse);
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
