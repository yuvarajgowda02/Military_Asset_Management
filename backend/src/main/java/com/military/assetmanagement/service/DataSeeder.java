package com.military.assetmanagement.service;

import com.military.assetmanagement.entity.*;
import com.military.assetmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Seeds a minimal, self-consistent demo dataset on first boot so the
 * application is usable immediately after `mvn spring-boot:run`, without
 * requiring the separate database/seed.sql to be loaded (that file is
 * provided for direct MySQL import / evaluation as well).
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final BaseRepository baseRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;
    private final UserRepository userRepository;
    private final PurchaseRepository purchaseRepository;
    private final TransferRepository transferRepository;
    private final AssignmentRepository assignmentRepository;
    private final ExpenditureRepository expenditureRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // already seeded
        }

        Base alpha = baseRepository.save(new Base(null, "Fort Alpha", "Northern Command", null));
        Base bravo = baseRepository.save(new Base(null, "Fort Bravo", "Eastern Command", null));
        Base charlie = baseRepository.save(new Base(null, "Fort Charlie", "Western Command", null));

        EquipmentType rifle = equipmentTypeRepository.save(new EquipmentType(null, "M4 Rifle", EquipmentCategory.WEAPON, "units", null));
        EquipmentType humvee = equipmentTypeRepository.save(new EquipmentType(null, "Humvee", EquipmentCategory.VEHICLE, "units", null));
        EquipmentType ammo = equipmentTypeRepository.save(new EquipmentType(null, "5.56mm Rounds", EquipmentCategory.AMMUNITION, "rounds", null));

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        admin.setFullName("System Administrator");
        admin.setRole(RoleName.ADMIN);
        admin.setEnabled(true);
        userRepository.save(admin);

        User commanderAlpha = new User();
        commanderAlpha.setUsername("commander.alpha");
        commanderAlpha.setPassword(passwordEncoder.encode("Commander@123"));
        commanderAlpha.setFullName("Col. Sarah Mitchell");
        commanderAlpha.setRole(RoleName.BASE_COMMANDER);
        commanderAlpha.setBase(alpha);
        commanderAlpha.setEnabled(true);
        userRepository.save(commanderAlpha);

        User logisticsAlpha = new User();
        logisticsAlpha.setUsername("logistics.alpha");
        logisticsAlpha.setPassword(passwordEncoder.encode("Logistics@123"));
        logisticsAlpha.setFullName("Lt. James Carter");
        logisticsAlpha.setRole(RoleName.LOGISTICS_OFFICER);
        logisticsAlpha.setBase(alpha);
        logisticsAlpha.setEnabled(true);
        userRepository.save(logisticsAlpha);

        User commanderBravo = new User();
        commanderBravo.setUsername("commander.bravo");
        commanderBravo.setPassword(passwordEncoder.encode("Commander@123"));
        commanderBravo.setFullName("Col. Raj Patel");
        commanderBravo.setRole(RoleName.BASE_COMMANDER);
        commanderBravo.setBase(bravo);
        commanderBravo.setEnabled(true);
        userRepository.save(commanderBravo);

        // Purchases
        Purchase p1 = new Purchase();
        p1.setBase(alpha); p1.setEquipmentType(rifle); p1.setQuantity(150);
        p1.setUnitCost(new BigDecimal("1200.00")); p1.setTotalCost(new BigDecimal("180000.00"));
        p1.setPurchaseDate(LocalDate.now().minusDays(40)); p1.setRemarks("Initial procurement");
        p1.setCreatedBy(admin);
        purchaseRepository.save(p1);

        Purchase p2 = new Purchase();
        p2.setBase(alpha); p2.setEquipmentType(ammo); p2.setQuantity(50000);
        p2.setUnitCost(new BigDecimal("0.35")); p2.setTotalCost(new BigDecimal("17500.00"));
        p2.setPurchaseDate(LocalDate.now().minusDays(20)); p2.setRemarks("Quarterly ammo resupply");
        p2.setCreatedBy(logisticsAlpha);
        purchaseRepository.save(p2);

        Purchase p3 = new Purchase();
        p3.setBase(bravo); p3.setEquipmentType(humvee); p3.setQuantity(10);
        p3.setUnitCost(new BigDecimal("75000.00")); p3.setTotalCost(new BigDecimal("750000.00"));
        p3.setPurchaseDate(LocalDate.now().minusDays(15)); p3.setRemarks("Fleet expansion");
        p3.setCreatedBy(admin);
        purchaseRepository.save(p3);

        // Transfer: Bravo -> Alpha
        Transfer t1 = new Transfer();
        t1.setFromBase(bravo); t1.setToBase(alpha); t1.setEquipmentType(humvee);
        t1.setQuantity(3); t1.setTransferDate(LocalDate.now().minusDays(10));
        t1.setStatus(TransferStatus.COMPLETED); t1.setRemarks("Reinforcement for training exercise");
        t1.setCreatedBy(admin);
        transferRepository.save(t1);

        // Transfer: Alpha -> Charlie
        Transfer t2 = new Transfer();
        t2.setFromBase(alpha); t2.setToBase(charlie); t2.setEquipmentType(rifle);
        t2.setQuantity(20); t2.setTransferDate(LocalDate.now().minusDays(5));
        t2.setStatus(TransferStatus.COMPLETED); t2.setRemarks("Support new deployment");
        t2.setCreatedBy(commanderAlpha);
        transferRepository.save(t2);

        // Assignment
        Assignment a1 = new Assignment();
        a1.setBase(alpha); a1.setEquipmentType(rifle); a1.setPersonnelName("Sgt. Daniel Reyes");
        a1.setPersonnelServiceNumber("SVC-10234"); a1.setQuantity(1);
        a1.setAssignedDate(LocalDate.now().minusDays(8)); a1.setStatus(AssignmentStatus.ASSIGNED);
        a1.setCreatedBy(commanderAlpha);
        assignmentRepository.save(a1);

        Assignment a2 = new Assignment();
        a2.setBase(alpha); a2.setEquipmentType(humvee); a2.setPersonnelName("Cpl. Maria Gomez");
        a2.setPersonnelServiceNumber("SVC-10877"); a2.setQuantity(1);
        a2.setAssignedDate(LocalDate.now().minusDays(6)); a2.setStatus(AssignmentStatus.ASSIGNED);
        a2.setCreatedBy(commanderAlpha);
        assignmentRepository.save(a2);

        // Expenditure
        Expenditure e1 = new Expenditure();
        e1.setBase(alpha); e1.setEquipmentType(ammo); e1.setQuantity(5000);
        e1.setExpendedDate(LocalDate.now().minusDays(3)); e1.setReason("Live-fire training exercise");
        e1.setCreatedBy(logisticsAlpha);
        expenditureRepository.save(e1);
    }
}
