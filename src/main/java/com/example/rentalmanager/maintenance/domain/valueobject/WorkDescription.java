package com.example.rentalmanager.maintenance.domain.valueobject;

/**
 * Value Object encapsulating the description of work needed and any
 * resolution notes added when the request is completed.
 */
public record WorkDescription(
        String problemDescription,
        String resolutionNotes
) {
    public WorkDescription {
        if (problemDescription == null || problemDescription.isBlank()) {
            throw new IllegalArgumentException("problemDescription must not be blank");
        }
    }

    /** Returns an updated description with resolution notes. */
    public WorkDescription withResolution(String notes) {
        return new WorkDescription(problemDescription, notes);
    }
}
