package com.example.rentalmanager.maintenance.application.port.input;

import com.example.rentalmanager.maintenance.application.dto.command.UpdateIssueTemplateCommand;
import com.example.rentalmanager.maintenance.application.dto.response.MaintenanceCategoryResponse;
import reactor.core.publisher.Mono;

public interface UpdateIssueTemplateUseCase {

    Mono<MaintenanceCategoryResponse> updateIssue(UpdateIssueTemplateCommand cmd);
}
