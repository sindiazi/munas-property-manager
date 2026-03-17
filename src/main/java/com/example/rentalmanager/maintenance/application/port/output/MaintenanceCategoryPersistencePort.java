package com.example.rentalmanager.maintenance.application.port.output;

import com.example.rentalmanager.maintenance.domain.aggregate.MaintenanceCategory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MaintenanceCategoryPersistencePort {

    Mono<MaintenanceCategory> findById(String id);

    Flux<MaintenanceCategory> findAll();

    Mono<MaintenanceCategory> save(MaintenanceCategory category);

    Mono<Void> delete(String categoryId);
}
