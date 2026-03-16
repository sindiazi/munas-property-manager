package com.example.rentalmanager.shared.infrastructure.seed;

import com.example.rentalmanager.leasing.domain.valueobject.LeaseStatus;
import com.example.rentalmanager.leasing.infrastructure.persistence.entity.LeaseJpaEntity;
import com.example.rentalmanager.leasing.infrastructure.persistence.entity.TenantOccupiedUnitEntity;
import com.example.rentalmanager.leasing.infrastructure.persistence.entity.UnitRentalHistoryEntity;
import com.example.rentalmanager.leasing.infrastructure.persistence.entity.UnitRentalHistoryKey;
import com.example.rentalmanager.leasing.infrastructure.persistence.repository.LeaseR2dbcRepository;
import com.example.rentalmanager.leasing.infrastructure.persistence.repository.TenantOccupiedUnitRepository;
import com.example.rentalmanager.leasing.infrastructure.persistence.repository.UnitRentalHistoryRepository;
import com.example.rentalmanager.maintenance.domain.valueobject.MaintenancePriority;
import com.example.rentalmanager.maintenance.domain.valueobject.MaintenanceStatus;
import com.example.rentalmanager.maintenance.infrastructure.persistence.entity.MaintenanceRequestJpaEntity;
import com.example.rentalmanager.maintenance.infrastructure.persistence.repository.MaintenanceRequestR2dbcRepository;
import com.example.rentalmanager.payment.domain.valueobject.PaymentStatus;
import com.example.rentalmanager.payment.domain.valueobject.PaymentType;
import com.example.rentalmanager.payment.infrastructure.persistence.entity.PaymentJpaEntity;
import com.example.rentalmanager.payment.infrastructure.persistence.repository.PaymentR2dbcRepository;
import com.example.rentalmanager.property.domain.valueobject.PropertyType;
import com.example.rentalmanager.property.domain.valueobject.UnitStatus;
import com.example.rentalmanager.property.infrastructure.persistence.entity.PropertyJpaEntity;
import com.example.rentalmanager.property.infrastructure.persistence.entity.PropertyUnitJpaEntity;
import com.example.rentalmanager.property.infrastructure.persistence.repository.PropertyR2dbcRepository;
import com.example.rentalmanager.property.infrastructure.persistence.repository.PropertyUnitR2dbcRepository;
import com.example.rentalmanager.shared.infrastructure.security.SsnEncryptionService;
import com.example.rentalmanager.tenant.domain.valueobject.TenantStatus;
import com.example.rentalmanager.tenant.infrastructure.persistence.entity.TenantJpaEntity;
import com.example.rentalmanager.tenant.infrastructure.persistence.repository.TenantR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Seed the database with 2 Nairobi properties, 22 units, 20 Kenyan tenants, and 2 years of
 * lease + payment history (with annual renewals).
 *
 * <p>Activate with Spring profile {@code seed}:
 * <pre>
 *   ./mvnw spring-boot:run -Dspring-boot.run.profiles=seed
 *   # or
 *   java -jar app.jar --spring.profiles.active=seed
 * </pre>
 *
 * <p>Timeline simulated (today = 2026-03-15):
 * <ul>
 *   <li>Year 1 leases: 2024-03-01 → 2025-02-28 (EXPIRED)  – all 20 tenants</li>
 *   <li>Year 2 leases: 2025-03-01 → 2026-02-28 (EXPIRED)  – 17 of 20 tenants renewed</li>
 *   <li>Year 3 leases: 2026-03-01 → 2027-02-28 (ACTIVE)   – 15 of 17 tenants renewed again</li>
 * </ul>
 *
 * <p>Payment outcomes for past months are probabilistically distributed:
 * 78% on-time PAID, 12% late PAID, 5% PARTIALLY_PAID, 3% OVERDUE, 2% CANCELLED.
 */
@Slf4j
@Component
@Profile("seed")
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    // Fixed owner – represents the property management company / landlord
    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    // Simulation anchor date
    private static final LocalDate TODAY = LocalDate.of(2026, 3, 15);

    private static final String CURRENCY = "KES";

    // Deterministic RNG so re-runs produce identical data
    private final Random rng = new Random(42L);

    private final PropertyR2dbcRepository           propertyRepo;
    private final PropertyUnitR2dbcRepository       unitRepo;
    private final TenantR2dbcRepository             tenantRepo;
    private final LeaseR2dbcRepository              leaseRepo;
    private final PaymentR2dbcRepository            paymentRepo;
    private final MaintenanceRequestR2dbcRepository maintenanceRepo;
    private final TenantOccupiedUnitRepository      occupancyRepo;
    private final UnitRentalHistoryRepository       rentalHistoryRepo;
    private final SsnEncryptionService              ssnEncryptionService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("=== DataSeeder: starting ===");
        seedAll()
            .doOnSuccess(v -> log.info("=== DataSeeder: complete ==="))
            .doOnError(e -> log.error("=== DataSeeder: failed ===", e))
            .block();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Orchestration
    // ─────────────────────────────────────────────────────────────────────────

    private Mono<Void> seedAll() {
        List<PropertyJpaEntity>     properties = buildProperties();
        List<PropertyUnitJpaEntity> units      = buildUnits(properties);
        List<TenantJpaEntity>       tenants    = buildTenants();

        List<LeaseJpaEntity>          allLeases     = new ArrayList<>();
        List<PaymentJpaEntity>        allPayments   = new ArrayList<>();
        List<TenantOccupiedUnitEntity> occupancies  = new ArrayList<>();
        List<UnitRentalHistoryEntity>  rentalHistory = new ArrayList<>();

        // Assign tenants 0-19 to units 0-19 (units 20-21 remain vacant)
        for (int i = 0; i < 20; i++) {
            TenantJpaEntity       tenant = tenants.get(i);
            PropertyUnitJpaEntity unit   = units.get(i);

            // Year 1 – all 20 tenants
            LeaseJpaEntity lease1 = buildLease(
                tenant.getId(), unit.getPropertyId(), unit.getId(),
                LocalDate.of(2024, 3, 1), LocalDate.of(2025, 2, 28),
                unit.getMonthlyRentAmount(), LeaseStatus.EXPIRED);
            allLeases.add(lease1);
            allPayments.addAll(buildPayments(lease1, tenant.getId()));
            rentalHistory.add(buildHistoryRow(lease1));

            if (i < 17) {
                // Year 2 – 17 tenants renewed
                LeaseJpaEntity lease2 = buildLease(
                    tenant.getId(), unit.getPropertyId(), unit.getId(),
                    LocalDate.of(2025, 3, 1), LocalDate.of(2026, 2, 28),
                    unit.getMonthlyRentAmount(), LeaseStatus.EXPIRED);
                allLeases.add(lease2);
                allPayments.addAll(buildPayments(lease2, tenant.getId()));
                rentalHistory.add(buildHistoryRow(lease2));

                if (i < 15) {
                    // Year 3 – 15 tenants renewed again (currently ACTIVE)
                    LeaseJpaEntity lease3 = buildLease(
                        tenant.getId(), unit.getPropertyId(), unit.getId(),
                        LocalDate.of(2026, 3, 1), LocalDate.of(2027, 2, 28),
                        unit.getMonthlyRentAmount(), LeaseStatus.ACTIVE);
                    allLeases.add(lease3);
                    allPayments.addAll(buildPayments(lease3, tenant.getId()));
                    rentalHistory.add(buildHistoryRow(lease3));
                    // Active lease → tenant occupies the unit
                    occupancies.add(buildOccupancyRow(lease3, tenant.getId()));
                }
            }
        }

        // Units 0-14 are OCCUPIED (active year-3 tenants); 15-21 AVAILABLE
        for (int i = 0; i < units.size(); i++) {
            units.get(i).setStatus(i < 15 ? UnitStatus.OCCUPIED : UnitStatus.AVAILABLE);
        }

        List<MaintenanceRequestJpaEntity> maintenance = buildMaintenance(units, tenants);

        log.info("Seeding: {} properties, {} units, {} tenants, {} leases, {} payments, {} maintenance, " +
                 "{} occupancy rows, {} rental history rows",
            properties.size(), units.size(), tenants.size(),
            allLeases.size(), allPayments.size(), maintenance.size(),
            occupancies.size(), rentalHistory.size());

        // Truncate non-idempotent tables first so repeated seed runs don't create duplicates
        return rentalHistoryRepo.deleteAll()
            .then(occupancyRepo.deleteAll())
            .then(maintenanceRepo.deleteAll())
            .then(paymentRepo.deleteAll())
            .then(leaseRepo.deleteAll())
            .then(Flux.fromIterable(properties).flatMap(propertyRepo::save).then())
            .then(Flux.fromIterable(units).flatMap(unitRepo::save).then())
            .then(Flux.fromIterable(tenants).flatMap(tenantRepo::save).then())
            .then(Flux.fromIterable(allLeases).flatMap(leaseRepo::save).then())
            .then(Flux.fromIterable(allPayments).flatMap(paymentRepo::save).then())
            .then(Flux.fromIterable(maintenance).flatMap(maintenanceRepo::save).then())
            .then(Flux.fromIterable(occupancies).flatMap(occupancyRepo::save).then())
            .then(Flux.fromIterable(rentalHistory).flatMap(rentalHistoryRepo::save).then());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Properties
    // ─────────────────────────────────────────────────────────────────────────

    private List<PropertyJpaEntity> buildProperties() {
        return List.of(
            PropertyJpaEntity.builder()
                .id(UUID.fromString("10000000-0000-0000-0000-000000000001"))
                .ownerId(OWNER_ID)
                .name("Kilimani Court")
                .street("45 Ngong Road")
                .city("Nairobi")
                .state("Nairobi County")
                .zipCode("00100")
                .country("Kenya")
                .type(PropertyType.APARTMENT_BUILDING)
                .createdAt(Instant.parse("2024-01-15T08:00:00Z"))
                .build(),
            PropertyJpaEntity.builder()
                .id(UUID.fromString("10000000-0000-0000-0000-000000000002"))
                .ownerId(OWNER_ID)
                .name("Westlands Residences")
                .street("12 Waiyaki Way")
                .city("Nairobi")
                .state("Nairobi County")
                .zipCode("00800")
                .country("Kenya")
                .type(PropertyType.CONDOMINIUM)
                .createdAt(Instant.parse("2024-01-20T08:00:00Z"))
                .build()
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Units  (12 in Sunset Apartments + 10 in Oakwood Heights = 22 total)
    // ─────────────────────────────────────────────────────────────────────────

    private List<PropertyUnitJpaEntity> buildUnits(List<PropertyJpaEntity> properties) {
        UUID prop1 = properties.get(0).getId(); // Sunset Apartments
        UUID prop2 = properties.get(1).getId(); // Oakwood Heights

        record Spec(String num, int bed, int bath, double sqm, String rent) {}

        List<Spec> kilimaniSpecs = List.of(
            new Spec("101", 1, 1,  52.0,  "35000"),
            new Spec("102", 1, 1,  52.0,  "35000"),
            new Spec("103", 1, 1,  54.0,  "37000"),
            new Spec("104", 1, 1,  54.0,  "37000"),
            new Spec("201", 2, 2,  74.0,  "55000"),
            new Spec("202", 2, 2,  74.0,  "55000"),
            new Spec("203", 2, 2,  76.0,  "58000"),
            new Spec("204", 2, 2,  76.0,  "58000"),
            new Spec("301", 2, 2,  82.0,  "65000"),
            new Spec("302", 2, 2,  82.0,  "65000"),
            new Spec("303", 3, 2,  95.0,  "85000"),
            new Spec("304", 3, 2,  95.0,  "85000")
        );

        List<Spec> westlandsSpecs = List.of(
            new Spec("A101", 1, 1,  58.0,  "42000"),
            new Spec("A102", 1, 1,  58.0,  "42000"),
            new Spec("A103", 1, 1,  60.0,  "45000"),
            new Spec("A104", 1, 1,  60.0,  "45000"),
            new Spec("A105", 1, 1,  62.0,  "48000"),
            new Spec("B101", 2, 2,  84.0,  "72000"),
            new Spec("B102", 2, 2,  84.0,  "72000"),
            new Spec("B103", 2, 2,  86.0,  "75000"),
            new Spec("B104", 2, 2,  86.0,  "75000"),
            new Spec("B105", 2, 2,  90.0,  "80000")
        );

        List<PropertyUnitJpaEntity> units = new ArrayList<>();
        int idx = 0;
        for (Spec s : kilimaniSpecs) {
            units.add(unit(idx++, prop1, s.num(), s.bed(), s.bath(), s.sqm(), s.rent()));
        }
        for (Spec s : westlandsSpecs) {
            units.add(unit(idx++, prop2, s.num(), s.bed(), s.bath(), s.sqm(), s.rent()));
        }
        return units;
    }

    private PropertyUnitJpaEntity unit(int idx, UUID propertyId, String number,
                                        int bed, int bath, double sqm, String rent) {
        return PropertyUnitJpaEntity.builder()
            .id(UUID.fromString(String.format("20000000-0000-0000-0000-%012d", idx)))
            .propertyId(propertyId)
            .unitNumber(number)
            .bedrooms(bed)
            .bathrooms(bath)
            .squareFootage(sqm)
            .monthlyRentAmount(new BigDecimal(rent))
            .currencyCode(CURRENCY)
            .status(UnitStatus.AVAILABLE) // overwritten after lease assignment
            .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tenants  (20 Kenyan residents)
    // ─────────────────────────────────────────────────────────────────────────

    private List<TenantJpaEntity> buildTenants() {
        record Row(String first, String last, String nid, String email, String phone, int score) {}

        List<Row> rows = List.of(
            new Row("Wanjiru",   "Kamau",     "28801015001",  "wanjiru.kamau@email.co.ke",     "0721234567", 720),
            new Row("Otieno",    "Odhiambo",  "29003025002",  "otieno.odhiambo@email.co.ke",   "0731234568", 685),
            new Row("Achieng",   "Onyango",   "28705145003",  "achieng.onyango@email.co.ke",   "0711234569", 745),
            new Row("Kipchoge",  "Mutai",     "29210085004",  "kipchoge.mutai@email.co.ke",    "0722334570", 660),
            new Row("Njeri",     "Mwangi",    "29407145005",  "njeri.mwangi@email.co.ke",      "0714534571", 700),
            new Row("Kamande",   "Kariuki",   "28812025006",  "kamande.kariuki@email.co.ke",   "0724534572", 730),
            new Row("Zawadi",    "Mutua",     "29101015007",  "zawadi.mutua@email.co.ke",      "0734534573", 710),
            new Row("Baraka",    "Kiprotich", "29306105008",  "baraka.kiprotich@email.co.ke",  "0744534574", 695),
            new Row("Imani",     "Chebet",    "29502025009",  "imani.chebet@email.co.ke",      "0754534575", 765),
            new Row("Muthoni",   "Ndungu",    "28809185010",  "muthoni.ndungu@email.co.ke",    "0764534576", 640),
            new Row("Adhiambo",  "Owino",     "29208225011",  "adhiambo.owino@email.co.ke",    "0774534577", 755),
            new Row("Kiplagat",  "Rono",      "29004035012",  "kiplagat.rono@email.co.ke",     "0784534578", 718),
            new Row("Wambui",    "Gitau",     "29311155013",  "wambui.gitau@email.co.ke",      "0794534579", 672),
            new Row("Jomo",      "Kinyanjui", "28806065014",  "jomo.kinyanjui@email.co.ke",    "0704534580", 741),
            new Row("Makena",    "Maina",     "29107205015",  "makena.maina@email.co.ke",      "0714634581", 698),
            new Row("Ochieng",   "Aluoch",    "29403115016",  "ochieng.aluoch@email.co.ke",    "0724634582", 762),
            new Row("Nyambura",  "Waithaka",  "28710055017",  "nyambura.waithaka@email.co.ke", "0734634583", 634),
            new Row("Korir",     "Bett",      "29206175018",  "korir.bett@email.co.ke",        "0744634584", 710),
            new Row("Amina",     "Hassan",    "29508085019",  "amina.hassan@email.co.ke",      "0754634585", 688),
            new Row("Mugo",      "Waweru",    "28903255020",  "mugo.waweru@email.co.ke",       "0764634586", 727)
        );

        List<TenantJpaEntity> tenants = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            Row r = rows.get(i);
            // Deterministic 9-digit National ID number using fixed-seed RNG
            String plainNationalIdNo  = String.format("%09d", 100_000_000 + rng.nextInt(900_000_000));
            String encryptedNationalId = ssnEncryptionService.encrypt(plainNationalIdNo);
            String nationalIdNoHash   = ssnEncryptionService.computeLookupHash(plainNationalIdNo);
            tenants.add(TenantJpaEntity.builder()
                .id(UUID.fromString(String.format("30000000-0000-0000-0000-%012d", i)))
                .firstName(r.first())
                .lastName(r.last())
                .nationalId(r.nid())
                .email(r.email())
                .phoneNumber(r.phone())
                .creditScore(r.score())
                .status(TenantStatus.ACTIVE)
                .registeredAt(Instant.parse("2024-02-01T08:00:00Z").plusSeconds(i * 3600L))
                .nationalIdNo(encryptedNationalId)
                .nationalIdNoHash(nationalIdNoHash)
                .build());
        }
        return tenants;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CQRS Projection Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private TenantOccupiedUnitEntity buildOccupancyRow(LeaseJpaEntity lease, UUID tenantId) {
        return TenantOccupiedUnitEntity.builder()
            .tenantId(tenantId)
            .unitId(lease.getUnitId())
            .propertyId(lease.getPropertyId())
            .leaseId(lease.getId())
            .monthlyRent(lease.getMonthlyRent())
            .leaseStart(lease.getStartDate())
            .leaseEnd(lease.getEndDate())
            .occupiedSince(lease.getCreatedAt())
            .build();
    }

    private UnitRentalHistoryEntity buildHistoryRow(LeaseJpaEntity lease) {
        var key = new UnitRentalHistoryKey(lease.getUnitId(), lease.getStartDate(), lease.getId());
        return UnitRentalHistoryEntity.builder()
            .key(key)
            .tenantId(lease.getTenantId())
            .propertyId(lease.getPropertyId())
            .monthlyRent(lease.getMonthlyRent())
            .leaseEnd(lease.getEndDate())
            .status(lease.getStatus().name())
            .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Leases
    // ─────────────────────────────────────────────────────────────────────────

    private LeaseJpaEntity buildLease(UUID tenantId, UUID propertyId, UUID unitId,
                                       LocalDate start, LocalDate end,
                                       BigDecimal monthlyRent, LeaseStatus status) {
        return LeaseJpaEntity.builder()
            .id(UUID.randomUUID())
            .tenantId(tenantId)
            .propertyId(propertyId)
            .unitId(unitId)
            .startDate(start)
            .endDate(end)
            .monthlyRent(monthlyRent)
            .securityDeposit(monthlyRent.multiply(BigDecimal.TWO))
            .status(status)
            .terminationReason(null)
            .createdAt(start.minusDays(14).atStartOfDay().toInstant(ZoneOffset.UTC))
            .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Payments  (monthly rent for every month of each lease)
    // ─────────────────────────────────────────────────────────────────────────

    private List<PaymentJpaEntity> buildPayments(LeaseJpaEntity lease, UUID tenantId) {
        List<PaymentJpaEntity> payments = new ArrayList<>();
        LocalDate month = lease.getStartDate();

        while (!month.isAfter(lease.getEndDate())) {
            LocalDate dueDate = month.withDayOfMonth(1);
            payments.add(resolvePayment(lease, tenantId, dueDate));
            month = month.plusMonths(1);
        }
        return payments;
    }

    /**
     * Determines payment outcome based on whether the due date is in the past,
     * the current month, or the future — with realistic probabilistic variation.
     *
     * <p>Past months: 78% on-time PAID, 12% late PAID, 5% PARTIALLY_PAID,
     * 3% OVERDUE, 2% CANCELLED.
     */
    private PaymentJpaEntity resolvePayment(LeaseJpaEntity lease, UUID tenantId, LocalDate dueDate) {
        BigDecimal due = lease.getMonthlyRent();

        boolean isFuture      = dueDate.isAfter(TODAY);
        boolean isCurrentMonth = !dueDate.isAfter(TODAY) && dueDate.plusMonths(1).isAfter(TODAY);

        if (isFuture) {
            return payment(lease, tenantId, dueDate, due, BigDecimal.ZERO, null, PaymentStatus.PENDING);
        }

        if (isCurrentMonth) {
            // 60% chance the tenant has already paid for this month
            boolean alreadyPaid = rng.nextInt(10) < 6;
            return alreadyPaid
                ? payment(lease, tenantId, dueDate, due, due, dueDate.plusDays(rng.nextInt(5)), PaymentStatus.PAID)
                : payment(lease, tenantId, dueDate, due, BigDecimal.ZERO, null, PaymentStatus.PENDING);
        }

        // Past month – weighted outcome
        int roll = rng.nextInt(100);
        if (roll < 78) {
            // On-time (paid within first 5 days of the month)
            return payment(lease, tenantId, dueDate, due, due, dueDate.plusDays(rng.nextInt(5)), PaymentStatus.PAID);
        } else if (roll < 90) {
            // Late (paid 6-15 days after the 1st)
            return payment(lease, tenantId, dueDate, due, due, dueDate.plusDays(6 + rng.nextInt(10)), PaymentStatus.PAID);
        } else if (roll < 95) {
            // Partial payment (50–90% of amount due)
            BigDecimal partial = due
                .multiply(BigDecimal.valueOf(0.5 + rng.nextDouble() * 0.4))
                .setScale(2, RoundingMode.HALF_UP);
            return payment(lease, tenantId, dueDate, due, partial, dueDate.plusDays(rng.nextInt(20)), PaymentStatus.PARTIALLY_PAID);
        } else if (roll < 98) {
            // Overdue – not paid at all
            return payment(lease, tenantId, dueDate, due, BigDecimal.ZERO, null, PaymentStatus.OVERDUE);
        } else {
            // Cancelled (e.g. billing error, credit applied)
            return payment(lease, tenantId, dueDate, due, BigDecimal.ZERO, null, PaymentStatus.CANCELLED);
        }
    }

    private PaymentJpaEntity payment(LeaseJpaEntity lease, UUID tenantId,
                                      LocalDate dueDate, BigDecimal amountDue,
                                      BigDecimal amountPaid, LocalDate paidDate,
                                      PaymentStatus status) {
        return PaymentJpaEntity.builder()
            .id(UUID.randomUUID())
            .leaseId(lease.getId())
            .tenantId(tenantId)
            .amountDue(amountDue)
            .amountPaid(amountPaid)
            .currencyCode(CURRENCY)
            .dueDate(dueDate)
            .paidDate(paidDate)
            .status(status)
            .type(PaymentType.RENT)
            .createdAt(dueDate.atStartOfDay().toInstant(ZoneOffset.UTC))
            .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Maintenance  (~30 requests spread across tenants over 2 years)
    // ─────────────────────────────────────────────────────────────────────────

    private List<MaintenanceRequestJpaEntity> buildMaintenance(
            List<PropertyUnitJpaEntity> units, List<TenantJpaEntity> tenants) {

        record Issue(String desc, String resolution, MaintenancePriority priority) {}

        List<Issue> pool = List.of(
            new Issue("Leaking tap in kitchen",             "Tap washers replaced",                        MaintenancePriority.LOW),
            new Issue("Bathroom extractor fan not working", "Fan motor replaced",                          MaintenancePriority.LOW),
            new Issue("Bedroom door handle loose",          "Handle tightened and re-secured",             MaintenancePriority.LOW),
            new Issue("Geyser not heating water",           "Thermostat replaced",                         MaintenancePriority.HIGH),
            new Issue("Kitchen light fitting broken",       "Fitting replaced",                            MaintenancePriority.MEDIUM),
            new Issue("Stove burner not igniting",          "Ignition unit replaced",                      MaintenancePriority.MEDIUM),
            new Issue("Window latch broken in lounge",      "Latch replaced",                              MaintenancePriority.LOW),
            new Issue("Blocked drain in bathroom",          "Drain cleared",                               MaintenancePriority.MEDIUM),
            new Issue("Ceiling leak after heavy rain",      "Roof sealed and ceiling patched",             MaintenancePriority.EMERGENCY),
            new Issue("No power to wall sockets",           "Faulty circuit breaker replaced",             MaintenancePriority.HIGH),
            new Issue("Balcony sliding door stiff",         "Rollers cleaned and lubricated",              MaintenancePriority.LOW),
            new Issue("Toilet running continuously",        "Cistern valve replaced",                      MaintenancePriority.MEDIUM),
            new Issue("Oven temperature inaccurate",        "Thermostat recalibrated",                     MaintenancePriority.LOW),
            new Issue("Security gate motor faulty",         "Motor serviced and reprogrammed",             MaintenancePriority.HIGH),
            new Issue("Low hot water pressure in shower",   "Pressure regulator adjusted",                 MaintenancePriority.MEDIUM)
        );

        List<MaintenanceRequestJpaEntity> requests = new ArrayList<>();

        // Generate ~30 requests at irregular intervals starting April 2024
        LocalDate cursor = LocalDate.of(2024, 4, 1);
        int count = 0;
        while (count < 30 && !cursor.isAfter(TODAY)) {
            cursor = cursor.plusDays(18 + rng.nextInt(22));
            if (cursor.isAfter(TODAY)) break;

            int tenantIdx = rng.nextInt(20);
            Issue issue   = pool.get(rng.nextInt(pool.size()));
            PropertyUnitJpaEntity unit   = units.get(tenantIdx);
            TenantJpaEntity       tenant = tenants.get(tenantIdx);

            Instant requestedAt = cursor.atTime(8 + rng.nextInt(9), 0).toInstant(ZoneOffset.UTC);
            // Requests older than 14 days are resolved
            boolean resolved = requestedAt.isBefore(TODAY.minusDays(14).atStartOfDay().toInstant(ZoneOffset.UTC));
            Instant completedAt = resolved
                ? requestedAt.plusSeconds(86_400L * (3 + rng.nextInt(10)))
                : null;

            requests.add(MaintenanceRequestJpaEntity.builder()
                .id(UUID.randomUUID())
                .propertyId(unit.getPropertyId())
                .unitId(unit.getId())
                .tenantId(tenant.getId())
                .problemDescription(issue.desc())
                .resolutionNotes(resolved ? issue.resolution() : null)
                .priority(issue.priority())
                .status(resolved ? MaintenanceStatus.COMPLETED : MaintenanceStatus.IN_PROGRESS)
                .requestedAt(requestedAt)
                .completedAt(completedAt)
                .build());

            count++;
        }
        return requests;
    }
}