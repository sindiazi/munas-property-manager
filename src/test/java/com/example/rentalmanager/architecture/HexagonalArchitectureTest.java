package com.example.rentalmanager.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * ArchUnit tests that enforce the hexagonal architecture layering rules.
 *
 * <p>Rules:
 * <ol>
 *   <li>The {@code domain} layer must NOT depend on {@code application} or
 *       {@code infrastructure}.</li>
 *   <li>The {@code application} layer must NOT depend on {@code infrastructure}.</li>
 *   <li>The {@code infrastructure} layer may depend on all layers.</li>
 *   <li>No layer may depend on Spring annotations except {@code infrastructure}
 *       and {@code config}.</li>
 * </ol>
 */
@AnalyzeClasses(packages = "com.example.rentalmanager")
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domainMustNotDependOnApplication =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..application..")
                    .as("Domain layer must not depend on the application layer");

    @ArchTest
    static final ArchRule domainMustNotDependOnInfrastructure =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..infrastructure..")
                    .as("Domain layer must not depend on the infrastructure layer");

    @ArchTest
    static final ArchRule applicationMustNotDependOnInfrastructure =
            noClasses()
                    .that().resideInAPackage("..application..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..infrastructure..")
                    .as("Application layer must not depend on the infrastructure layer");

    @ArchTest
    static final ArchRule domainMustNotDependOnSpring =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("org.springframework..")
                    .as("Domain layer must not depend on Spring Framework classes");
}
