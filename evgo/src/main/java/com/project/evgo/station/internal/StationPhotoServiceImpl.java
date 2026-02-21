package com.project.evgo.station.internal;

import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.station.StationPhotoService;
import com.project.evgo.station.StationService;
import com.project.evgo.station.request.AddStationPhotoRequest;
import com.project.evgo.station.request.UpdateStationPhotoRequest;
import com.project.evgo.station.response.StationPhotoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of StationPhotoService.
 * Internal - not accessible by other modules.
 */
@Service
@RequiredArgsConstructor
public class StationPhotoServiceImpl implements StationPhotoService {

    private static final int MAX_PHOTOS_PER_STATION = 10;

    private final StationPhotoRepository stationPhotoRepository;
    private final StationPhotoDtoConverter stationPhotoDtoConverter;
    private final StationService stationService;

    @Override
    @Transactional
    public StationPhotoResponse addPhoto(Long stationId, AddStationPhotoRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        // Verify station exists and current user owns it
        stationService.verifyOwnership(stationId);

        // Check photo limit
        int currentCount = stationPhotoRepository.countByStationId(stationId);
        if (currentCount >= MAX_PHOTOS_PER_STATION) {
            throw new AppException(ErrorCode.STATION_PHOTO_LIMIT_EXCEEDED);
        }

        StationPhoto photo = new StationPhoto();
        photo.setStationId(stationId);
        photo.setImageUrl(request.imageUrl());
        photo.setCaption(request.caption());
        photo.setDisplayOrder(request.displayOrder() != null ? request.displayOrder() : currentCount);

        StationPhoto saved = stationPhotoRepository.save(photo);
        return stationPhotoDtoConverter.convert(saved);
    }

    @Override
    public List<StationPhotoResponse> getPhotos(Long stationId) {
        List<StationPhoto> photos = stationPhotoRepository.findByStationIdOrderByDisplayOrderAsc(stationId);
        return stationPhotoDtoConverter.convert(photos);
    }

    @Override
    @Transactional
    public StationPhotoResponse updatePhoto(Long photoId, UpdateStationPhotoRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        StationPhoto photo = stationPhotoRepository.findById(photoId)
                .orElseThrow(() -> new AppException(ErrorCode.STATION_PHOTO_NOT_FOUND));

        // Verify ownership
        stationService.verifyOwnership(photo.getStationId());

        if (request.caption() != null) {
            photo.setCaption(request.caption());
        }
        if (request.displayOrder() != null) {
            photo.setDisplayOrder(request.displayOrder());
        }

        StationPhoto updated = stationPhotoRepository.save(photo);
        return stationPhotoDtoConverter.convert(updated);
    }

    @Override
    @Transactional
    public void deletePhoto(Long photoId) {
        StationPhoto photo = stationPhotoRepository.findById(photoId)
                .orElseThrow(() -> new AppException(ErrorCode.STATION_PHOTO_NOT_FOUND));

        // Verify ownership
        stationService.verifyOwnership(photo.getStationId());

        stationPhotoRepository.delete(photo);
    }

    @Override
    @Transactional
    public List<StationPhotoResponse> reorderPhotos(Long stationId, List<Long> photoIdsInOrder) {
        if (photoIdsInOrder == null || photoIdsInOrder.isEmpty()) {
            throw new IllegalArgumentException("Photo IDs list cannot be empty");
        }

        // Verify ownership
        stationService.verifyOwnership(stationId);

        // Get existing photos for this station
        List<StationPhoto> existingPhotos = stationPhotoRepository.findByStationIdOrderByDisplayOrderAsc(stationId);

        // Verify all provided IDs belong to this station
        List<Long> existingIds = existingPhotos.stream().map(StationPhoto::getId).toList();
        if (!existingIds.containsAll(photoIdsInOrder) || existingIds.size() != photoIdsInOrder.size()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Photo IDs do not match station's photos");
        }

        // Update display order
        for (int i = 0; i < photoIdsInOrder.size(); i++) {
            Long photoId = photoIdsInOrder.get(i);
            StationPhoto photo = existingPhotos.stream()
                    .filter(p -> p.getId().equals(photoId))
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.STATION_PHOTO_NOT_FOUND));
            photo.setDisplayOrder(i);
        }

        List<StationPhoto> saved = stationPhotoRepository.saveAll(existingPhotos);
        return stationPhotoDtoConverter.convert(saved);
    }
}
