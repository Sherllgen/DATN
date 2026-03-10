package com.project.evgo.station.internal;

import com.project.evgo.sharedkernel.dto.FileUploadResult;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.sharedkernel.infra.FileStorageService;
import com.project.evgo.station.StationOwnershipValidator;
import com.project.evgo.station.StationPhotoService;
import com.project.evgo.station.request.UpdateStationPhotoRequest;
import com.project.evgo.station.response.StationPhotoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Implementation of StationPhotoService.
 * Internal - not accessible by other modules.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StationPhotoServiceImpl implements StationPhotoService {

    private static final int MAX_PHOTOS_PER_STATION = 10;
    private static final String CLOUDINARY_FOLDER = "stations/photos";

    private final StationPhotoRepository stationPhotoRepository;
    private final StationPhotoDtoConverter stationPhotoDtoConverter;
    private final StationOwnershipValidator stationOwnershipValidator;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public StationPhotoResponse addPhoto(Long stationId, MultipartFile file, String caption) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }

        // Verify station exists and current user owns it
        stationOwnershipValidator.verifyOwnership(stationId);

        // Check photo limit
        int currentCount = stationPhotoRepository.countByStationId(stationId);
        if (currentCount >= MAX_PHOTOS_PER_STATION) {
            throw new AppException(ErrorCode.STATION_PHOTO_LIMIT_EXCEEDED);
        }

        // Upload to Cloudinary
        FileUploadResult uploadResult = fileStorageService.saveImageFile(file, CLOUDINARY_FOLDER);

        StationPhoto photo = new StationPhoto();
        photo.setStationId(stationId);
        photo.setImageUrl(uploadResult.fileUrl());
        photo.setCloudinaryPublicId(uploadResult.publicId());
        photo.setCaption(caption);
        photo.setDisplayOrder(currentCount);

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
        stationOwnershipValidator.verifyOwnership(photo.getStationId());

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
        stationOwnershipValidator.verifyOwnership(photo.getStationId());

        // Delete from Cloudinary
        if (photo.getCloudinaryPublicId() != null) {
            try {
                fileStorageService.deleteFile(photo.getCloudinaryPublicId());
            } catch (Exception e) {
                log.warn("Failed to delete image from Cloudinary: {}", photo.getCloudinaryPublicId(), e);
            }
        }

        stationPhotoRepository.delete(photo);
    }

    @Override
    @Transactional
    public List<StationPhotoResponse> reorderPhotos(Long stationId, List<Long> photoIdsInOrder) {
        if (photoIdsInOrder == null || photoIdsInOrder.isEmpty()) {
            throw new IllegalArgumentException("Photo IDs list cannot be empty");
        }

        // Verify ownership
        stationOwnershipValidator.verifyOwnership(stationId);

        // Get existing photos for this station
        List<StationPhoto> existingPhotos = stationPhotoRepository.findByStationIdOrderByDisplayOrderAsc(stationId);

        // Verify all provided IDs belong to this station
        List<Long> existingIds = existingPhotos.stream().map(StationPhoto::getId).toList();
        if (!existingIds.containsAll(photoIdsInOrder) || existingIds.size() != photoIdsInOrder.size()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Photo IDs do not match station's photos");
        }

        // Update display order
        for (int i = 0; i < photoIdsInOrder.size(); i++) {
            Long photoIdVal = photoIdsInOrder.get(i);
            StationPhoto p = existingPhotos.stream()
                    .filter(ep -> ep.getId().equals(photoIdVal))
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.STATION_PHOTO_NOT_FOUND));
            p.setDisplayOrder(i);
        }

        List<StationPhoto> saved = stationPhotoRepository.saveAll(existingPhotos);
        return stationPhotoDtoConverter.convert(saved);
    }
}
