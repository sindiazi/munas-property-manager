package com.example.rentalmanager.maintenance.application.port.input;

import reactor.core.publisher.Mono;

public interface RemoveIssueTemplateUseCase {

    Mono<Void> removeIssue(String categoryId, String issueId);
}
