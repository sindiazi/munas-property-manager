package com.example.rentalmanager.maintenance.application.port.input;

import com.example.rentalmanager.maintenance.application.dto.response.MaintenanceCategoryResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GetMaintenanceCategoriesUseCase {

    Flux<MaintenanceCategoryResponse> getAllCategories();

    Mono<MaintenanceCategoryResponse> getCategory(String categoryId);
}
