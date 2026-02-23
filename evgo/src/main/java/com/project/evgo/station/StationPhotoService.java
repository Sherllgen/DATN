package com.project.evgo.station;

import com.project.evgo.station.request.UpdateStationPhotoRequest;
import com.project.evgo.station.response.StationPhotoResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for station photo management.
 * Public API - accessible by other modules.
 */
public interface StationPhotoService {

    /**
     * Add a new photo to a station by uploading a file.
     *
     * @param stationId Station ID
     * @param file      Image file to upload
     * @param caption   Optional caption
     * @return Created photo response
     */
    StationPhotoResponse addPhoto(Long stationId, MultipartFile file, String caption);

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
     * Delete a photo (also removes from Cloudinary).
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
