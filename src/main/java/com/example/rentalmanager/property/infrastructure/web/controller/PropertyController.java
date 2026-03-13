package com.example.rentalmanager.property.infrastructure.web.controller;

import com.example.rentalmanager.property.application.dto.command.AddPropertyUnitCommand;
import com.example.rentalmanager.property.application.dto.command.CreatePropertyCommand;
import com.example.rentalmanager.property.application.dto.command.UpdatePropertyCommand;
import com.example.rentalmanager.property.application.dto.response.PropertyResponse;
import com.example.rentalmanager.property.application.port.input.AddPropertyUnitUseCase;
import com.example.rentalmanager.property.application.port.input.CreatePropertyUseCase;
import com.example.rentalmanager.property.application.port.input.GetPropertyUseCase;
import com.example.rentalmanager.property.application.port.input.UpdatePropertyUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Primary (driving) adapter — exposes the Property bounded context over HTTP
 * using Spring WebFlux reactive endpoints.
 *
 * <p>Depends only on input-port interfaces; never on application services directly.
 */
@Tag(name = "Properties", description = "Rental property management endpoints")
@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final CreatePropertyUseCase   createPropertyUseCase;
    private final AddPropertyUnitUseCase  addPropertyUnitUseCase;
    private final UpdatePropertyUseCase   updatePropertyUseCase;
    private final GetPropertyUseCase      getPropertyUseCase;

    @Operation(summary = "Create a new rental property")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PropertyResponse> createProperty(@Valid @RequestBody CreatePropertyCommand command) {
        return createPropertyUseCase.createProperty(command);
    }

    @Operation(summary = "Get a property by ID")
    @GetMapping("/{id}")
    public Mono<PropertyResponse> getProperty(@PathVariable UUID id) {
        return getPropertyUseCase.getById(id);
    }

    @Operation(summary = "List all properties")
    @GetMapping
    public Flux<PropertyResponse> getAllProperties() {
        return getPropertyUseCase.getAll();
    }

    @Operation(summary = "List properties by owner")
    @GetMapping("/owner/{ownerId}")
    public Flux<PropertyResponse> getPropertiesByOwner(@PathVariable UUID ownerId) {
        return getPropertyUseCase.getByOwnerId(ownerId);
    }

    @Operation(summary = "Update property details")
    @PutMapping("/{id}")
    public Mono<PropertyResponse> updateProperty(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePropertyCommand command) {
        // Merge path variable into command (record allows reconstruction)
        var mergedCommand = new UpdatePropertyCommand(id, command.name(), command.street(),
                command.city(), command.state(), command.zipCode(), command.country());
        return updatePropertyUseCase.updateProperty(mergedCommand);
    }

    @Operation(summary = "Add a rentable unit to a property")
    @PostMapping("/{id}/units")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PropertyResponse> addUnit(
            @PathVariable UUID id,
            @Valid @RequestBody AddPropertyUnitCommand command) {
        var mergedCommand = new AddPropertyUnitCommand(id, command.unitNumber(), command.bedrooms(),
                command.bathrooms(), command.squareFootage(),
                command.monthlyRentAmount(), command.currencyCode());
        return addPropertyUnitUseCase.addUnit(mergedCommand);
    }
}
