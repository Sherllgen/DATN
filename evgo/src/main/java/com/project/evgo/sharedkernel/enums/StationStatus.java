package com.project.evgo.sharedkernel.enums;

/**
 * Status of a charging station.
 */
public enum StationStatus {
    PENDING, // Awaiting approval
    ACTIVE, // Operating normally
    INACTIVE, // Temporarily closed
    SUSPENDED // Suspended by admin
}
