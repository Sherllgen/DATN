package com.project.evgo.station.internal;

import com.project.evgo.sharedkernel.enums.StationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

/**
 * Station entity.
 * Internal - not accessible by other modules.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stations")
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private String address;

    private Double latitude;

    private Double longitude;

    @Column(precision = 2)
    private Double rate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StationStatus status = StationStatus.PENDING;

    @Column(name = "is_flagged_low_quality", nullable = false)
    private Boolean isFlaggedLowQuality = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
