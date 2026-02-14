package com.project.evgo.station.internal;

import com.project.evgo.sharedkernel.enums.StationStatus;

import java.time.LocalDateTime;

/**
 * Interface-based projection for Station search results.
 * Spring Data JPA automatically implements this interface based on query column
 * aliases.
 */
public interface StationProjection {

    Long getId();

    Long getOwnerId();

    String getName();

    String getDescription();

    String getAddress();

    Double getLatitude();

    Double getLongitude();

    Double getRate();

    StationStatus getStatus();

    Boolean getIsFlaggedLowQuality();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    /**
     * Distance in meters from search point.
     * Can be null for text-only searches without location.
     */
    Double getDistance();

    /**
     * Total number of chargers at this station.
     */
    Integer getTotalChargersCount();

    /**
     * Number of available chargers at this station.
     */
    Integer getAvailableChargersCount();
}
