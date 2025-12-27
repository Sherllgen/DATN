package com.project.evgo.user.internal;

import com.project.evgo.sharedkernel.enums.StationOwnerStatus;
import com.project.evgo.sharedkernel.enums.StationOwnerType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * StationOwnerProfile entity - additional profile for station owners.
 * Internal - not accessible by other modules.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "station_owner_profiles")
public class StationOwnerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registration_code", nullable = false, unique = true)
    private String registrationCode;

//    @Column(name = "user_id", nullable = false, unique = true)
//    private Long userId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false)
    private StationOwnerType ownerType;

    // ===== INDIVIDUAL =====
    @Column(name = "full_name")
    private String fullName;

    @Column(name = "identity_number")
    private String idNumber;

    // ===== ENTERPRISE only =====
    @Column(name = "business_name")
    private String businessName;
//
//    @Column(name = "business_email")
//    private String businessEmail;

    @Column(name = "tax_code")
    private String taxCode;

    // ===== COMMON =====
    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    @Column(name = "contact_phone", nullable = false)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StationOwnerStatus status;

    // ===== BANK =====
    @Column(name = "bank_account")
    private String bankAccount;

    @Column(name = "bank_name")
    private String bankName;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "pdf_file_path", nullable = false)
    private String pdfFilePath;

    @CreationTimestamp
    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;

    @Column(name = "pdf_public_id", nullable = false, unique = true)
    private String pdfPublicId;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = StationOwnerStatus.SUBMITTED;
        }
    }
}
