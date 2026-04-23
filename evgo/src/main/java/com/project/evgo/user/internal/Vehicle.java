package com.project.evgo.user.internal;

import com.project.evgo.sharedkernel.enums.ConnectorType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Vehicle entity - represents user's electric vehicle.
 * Internal - not accessible by other modules.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "brand", nullable = false)
    private String brand;

    @Column(name = "model_name", nullable = false)
    private String modelName;

    @ElementCollection(targetClass = ConnectorType.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "vehicle_connector_types", joinColumns = @JoinColumn(name = "vehicle_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "connector_type")
    private Set<ConnectorType> connectorTypes = new HashSet<>();

    @Column(name = "in_use", nullable = false)
    private Boolean inUse = false;

    // @Column(name = "license_plate")
    // private String licensePlate;

    // @Column(name = "battery_capacity")
    // private Double batteryCapacity;

    // @CreationTimestamp
    // private LocalDateTime createdAt;

    // @UpdateTimestamp
    // private LocalDateTime updatedAt;
}
