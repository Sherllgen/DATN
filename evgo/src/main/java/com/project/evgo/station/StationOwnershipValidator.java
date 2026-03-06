package com.project.evgo.station;

/**
 * Interface for validating station ownership.
 * Public API - accessible by other modules.
 */
public interface StationOwnershipValidator {

    /**
     * Verify current user owns the station. Throws AppException if not.
     *
     * @param stationId Station ID to verify
     * @throws com.project.evgo.sharedkernel.exceptions.AppException if station not
     *                                                               found or not
     *                                                               owned
     */
    void verifyOwnership(Long stationId);

    /**
     * Check if current user owns the station.
     *
     * @param stationId Station ID to check
     * @return true if current user owns the station
     */
    boolean isOwner(Long stationId);
}
