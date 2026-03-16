package com.example.rentalmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Rental Property Manager application.
 *
 * <p>Architecture overview:
 * <pre>
 *  ┌─────────────────────────────────────────────────────────┐
 *  │                   Bounded Contexts                      │
 *  │  ┌──────────┐ ┌────────┐ ┌─────────┐ ┌──────────────┐ │
 *  │  │ property │ │ tenant │ │ leasing │ │   payment    │ │
 *  │  └──────────┘ └────────┘ └─────────┘ └──────────────┘ │
 *  │                   ┌─────────────┐                      │
 *  │                   │ maintenance │                      │
 *  │                   └─────────────┘                      │
 *  │                                                         │
 *  │  Each BC follows Hexagonal (Ports & Adapters):          │
 *  │                                                         │
 *  │   ┌──────────────────────────────────────────────────┐  │
 *  │   │  .domain        – Aggregates, VOs, Repo ports    │  │
 *  │   │  .application   – Use Cases, App Services, DTOs  │  │
 *  │   │  .infrastructure– R2DBC adapters, Controllers    │  │
 *  │   │  .config        – Spring configuration           │  │
 *  │   └──────────────────────────────────────────────────┘  │
 *  └─────────────────────────────────────────────────────────┘
 * </pre>
 */
@SpringBootApplication
@EnableScheduling
public class RentalPropertyManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RentalPropertyManagerApplication.class, args);
    }
}
