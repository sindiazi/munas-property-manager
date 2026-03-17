package com.example.rentalmanager.maintenance.application.port.input;

import reactor.core.publisher.Mono;

public interface DeleteMaintenanceCategoryUseCase {

    Mono<Void> deleteCategory(String categoryId);
}
