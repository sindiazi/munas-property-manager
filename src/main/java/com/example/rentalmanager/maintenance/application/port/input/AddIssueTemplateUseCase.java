package com.example.rentalmanager.maintenance.application.port.input;

import com.example.rentalmanager.maintenance.application.dto.command.AddIssueTemplateCommand;
import com.example.rentalmanager.maintenance.application.dto.response.MaintenanceCategoryResponse;
import reactor.core.publisher.Mono;

public interface AddIssueTemplateUseCase {

    Mono<MaintenanceCategoryResponse> addIssue(AddIssueTemplateCommand cmd);
}
