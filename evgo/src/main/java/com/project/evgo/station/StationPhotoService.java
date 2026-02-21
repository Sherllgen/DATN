package com.project.evgo.station;

import com.project.evgo.station.request.AddStationPhotoRequest;
import com.project.evgo.station.request.UpdateStationPhotoRequest;
import com.project.evgo.station.response.StationPhotoResponse;

import java.util.List;

/**
 * Service interface for station photo management.
 * Public API - accessible by other modules.
 */
public interface StationPhotoService {

    /**
     * Add a new photo to a station.
     *
     * @param stationId Station ID
     * @param request   Photo details
     * @return Created photo response
     */
    StationPhotoResponse addPhoto(Long stationId, AddStationPhotoRequest request);

    /**
     * Get all photos for a station, ordered by displayOrder.
     *
     * @param stationId Station ID
     * @return List of photos
     */
    List<StationPhotoResponse> getPhotos(Long stationId);

    /**
     * Update a photo's metadata (caption, displayOrder).
     *
     * @param photoId Photo ID
     * @param request Updated metadata
     * @return Updated photo response
     */
    StationPhotoResponse updatePhoto(Long photoId, UpdateStationPhotoRequest request);

    /**
     * Delete a photo.
     *
     * @param photoId Photo ID
     */
    void deletePhoto(Long photoId);

    /**
     * Reorder photos for a station.
     *
     * @param stationId       Station ID
     * @param photoIdsInOrder Ordered list of photo IDs
     * @return Reordered photos
     */
    List<StationPhotoResponse> reorderPhotos(Long stationId, List<Long> photoIdsInOrder);
}
