package com.project.evgo.station;

import com.project.evgo.sharedkernel.dto.FileUploadResult;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.sharedkernel.infra.FileStorageService;
import com.project.evgo.station.internal.StationPhoto;
import com.project.evgo.station.internal.StationPhotoDtoConverter;
import com.project.evgo.station.internal.StationPhotoRepository;
import com.project.evgo.station.internal.StationPhotoServiceImpl;
import com.project.evgo.station.request.UpdateStationPhotoRequest;
import com.project.evgo.station.response.StationPhotoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StationPhotoService.
 */
@ExtendWith(MockitoExtension.class)
class StationPhotoServiceTest {

    @InjectMocks
    private StationPhotoServiceImpl stationPhotoService;

    @Mock
    private StationPhotoRepository stationPhotoRepository;

    @Mock
    private StationPhotoDtoConverter stationPhotoDtoConverter;

    @Mock
    private StationService stationService;

    @Mock
    private FileStorageService fileStorageService;

    private static final Long STATION_ID = 100L;
    private static final Long PHOTO_ID = 200L;
    private static final Long OTHER_PHOTO_ID = 201L;

    private StationPhoto testPhoto;
    private StationPhotoResponse testPhotoResponse;
    private MockMultipartFile testFile;
    private FileUploadResult testUploadResult;

    @BeforeEach
    void setUp() {
        testPhoto = new StationPhoto();
        testPhoto.setId(PHOTO_ID);
        testPhoto.setStationId(STATION_ID);
        testPhoto.setImageUrl("https://res.cloudinary.com/demo/image/upload/photo1.jpg");
        testPhoto.setCloudinaryPublicId("stations/photos/abc123");
        testPhoto.setCaption("Main entrance");
        testPhoto.setDisplayOrder(0);
        testPhoto.setCreatedAt(LocalDateTime.now());

        testPhotoResponse = StationPhotoResponse.builder()
                .id(PHOTO_ID)
                .stationId(STATION_ID)
                .imageUrl("https://res.cloudinary.com/demo/image/upload/photo1.jpg")
                .caption("Main entrance")
                .displayOrder(0)
                .createdAt(testPhoto.getCreatedAt())
                .build();

        testFile = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", "fake-image-bytes".getBytes());

        testUploadResult = FileUploadResult.builder()
                .fileUrl("https://res.cloudinary.com/demo/image/upload/photo1.jpg")
                .publicId("stations/photos/abc123")
                .build();
    }

    // ==================== ADD PHOTO TESTS ====================

    @Nested
    @DisplayName("Add Photo Tests")
    class AddPhotoTests {

        @Test
        @DisplayName("Should upload and add photo successfully")
        void addPhoto_ValidFile_UploadsAndReturnsPhotoResponse() {
            // Given
            doNothing().when(stationService).verifyOwnership(STATION_ID);
            when(stationPhotoRepository.countByStationId(STATION_ID)).thenReturn(3);
            when(fileStorageService.saveImageFile(any(MultipartFile.class), anyString()))
                    .thenReturn(testUploadResult);
            when(stationPhotoRepository.save(any(StationPhoto.class))).thenReturn(testPhoto);
            when(stationPhotoDtoConverter.convert(any(StationPhoto.class))).thenReturn(testPhotoResponse);

            // When
            StationPhotoResponse result = stationPhotoService.addPhoto(STATION_ID, testFile, "Main entrance");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.imageUrl()).contains("cloudinary");
            verify(fileStorageService).saveImageFile(testFile, "stations/photos");
            verify(stationPhotoRepository).save(any(StationPhoto.class));
        }

        @Test
        @DisplayName("Should store cloudinaryPublicId on saved photo")
        void addPhoto_ValidFile_StoresPublicId() {
            // Given
            doNothing().when(stationService).verifyOwnership(STATION_ID);
            when(stationPhotoRepository.countByStationId(STATION_ID)).thenReturn(0);
            when(fileStorageService.saveImageFile(any(MultipartFile.class), anyString()))
                    .thenReturn(testUploadResult);
            when(stationPhotoRepository.save(any(StationPhoto.class))).thenAnswer(inv -> inv.getArgument(0));
            when(stationPhotoDtoConverter.convert(any(StationPhoto.class))).thenReturn(testPhotoResponse);

            // When
            stationPhotoService.addPhoto(STATION_ID, testFile, "Caption");

            // Then
            verify(stationPhotoRepository)
                    .save(argThat(photo -> "stations/photos/abc123".equals(photo.getCloudinaryPublicId())
                            && photo.getImageUrl().contains("cloudinary")));
        }

        @Test
        @DisplayName("Should auto-assign displayOrder based on current count")
        void addPhoto_NoExplicitOrder_AssignsOrderBasedOnCount() {
            // Given
            doNothing().when(stationService).verifyOwnership(STATION_ID);
            when(stationPhotoRepository.countByStationId(STATION_ID)).thenReturn(5);
            when(fileStorageService.saveImageFile(any(MultipartFile.class), anyString()))
                    .thenReturn(testUploadResult);
            when(stationPhotoRepository.save(any(StationPhoto.class))).thenAnswer(inv -> {
                StationPhoto saved = inv.getArgument(0);
                saved.setId(PHOTO_ID);
                return saved;
            });
            when(stationPhotoDtoConverter.convert(any(StationPhoto.class))).thenReturn(testPhotoResponse);

            // When
            stationPhotoService.addPhoto(STATION_ID, testFile, "Side view");

            // Then - displayOrder should be set to current count (5)
            verify(stationPhotoRepository).save(argThat(photo -> photo.getDisplayOrder() == 5));
        }

        @Test
        @DisplayName("Should throw exception when station not found")
        void addPhoto_StationNotFound_ThrowsNotFound() {
            // Given
            doThrow(new AppException(ErrorCode.STATION_NOT_FOUND))
                    .when(stationService).verifyOwnership(STATION_ID);

            // When & Then
            assertThatThrownBy(() -> stationPhotoService.addPhoto(STATION_ID, testFile, "Caption"))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.STATION_NOT_FOUND);
                    });

            verify(stationPhotoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when user is not station owner")
        void addPhoto_NotOwner_ThrowsForbidden() {
            // Given
            doThrow(new AppException(ErrorCode.STATION_NOT_OWNED))
                    .when(stationService).verifyOwnership(STATION_ID);

            // When & Then
            assertThatThrownBy(() -> stationPhotoService.addPhoto(STATION_ID, testFile, "Caption"))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.STATION_NOT_OWNED);
                    });

            verify(stationPhotoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when max photos exceeded")
        void addPhoto_MaxPhotosExceeded_ThrowsException() {
            // Given
            doNothing().when(stationService).verifyOwnership(STATION_ID);
            when(stationPhotoRepository.countByStationId(STATION_ID)).thenReturn(10); // At limit

            // When & Then
            assertThatThrownBy(() -> stationPhotoService.addPhoto(STATION_ID, testFile, "Caption"))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.STATION_PHOTO_LIMIT_EXCEEDED);
                    });

            verify(stationPhotoRepository, never()).save(any());
            verify(fileStorageService, never()).saveImageFile(any(), anyString());
        }

        @Test
        @DisplayName("Should throw exception when file is null")
        void addPhoto_NullFile_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> stationPhotoService.addPhoto(STATION_ID, null, "Caption"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when file is empty")
        void addPhoto_EmptyFile_ThrowsException() {
            // Given
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file", "empty.jpg", "image/jpeg", new byte[0]);

            // When & Then
            assertThatThrownBy(() -> stationPhotoService.addPhoto(STATION_ID, emptyFile, "Caption"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== GET PHOTOS TESTS ====================

    @Nested
    @DisplayName("Get Photos Tests")
    class GetPhotosTests {

        @Test
        @DisplayName("Should return photos ordered by displayOrder")
        void getPhotos_HasPhotos_ReturnsOrderedList() {
            // Given
            StationPhoto photo1 = new StationPhoto();
            photo1.setId(1L);
            photo1.setStationId(STATION_ID);
            photo1.setDisplayOrder(0);

            StationPhoto photo2 = new StationPhoto();
            photo2.setId(2L);
            photo2.setStationId(STATION_ID);
            photo2.setDisplayOrder(1);

            List<StationPhoto> photos = List.of(photo1, photo2);
            List<StationPhotoResponse> responses = List.of(
                    StationPhotoResponse.builder().id(1L).displayOrder(0).build(),
                    StationPhotoResponse.builder().id(2L).displayOrder(1).build());

            when(stationPhotoRepository.findByStationIdOrderByDisplayOrderAsc(STATION_ID))
                    .thenReturn(photos);
            when(stationPhotoDtoConverter.convert(photos)).thenReturn(responses);

            // When
            List<StationPhotoResponse> result = stationPhotoService.getPhotos(STATION_ID);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).displayOrder()).isEqualTo(0);
            assertThat(result.get(1).displayOrder()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return empty list when station has no photos")
        void getPhotos_NoPhotos_ReturnsEmptyList() {
            // Given
            when(stationPhotoRepository.findByStationIdOrderByDisplayOrderAsc(STATION_ID))
                    .thenReturn(List.of());
            when(stationPhotoDtoConverter.convert(List.<StationPhoto>of()))
                    .thenReturn(List.of());

            // When
            List<StationPhotoResponse> result = stationPhotoService.getPhotos(STATION_ID);

            // Then
            assertThat(result).isEmpty();
        }
    }

    // ==================== UPDATE PHOTO TESTS ====================

    @Nested
    @DisplayName("Update Photo Tests")
    class UpdatePhotoTests {

        @Test
        @DisplayName("Should update photo caption successfully")
        void updatePhoto_ValidRequest_UpdatesCaption() {
            // Given
            UpdateStationPhotoRequest request = new UpdateStationPhotoRequest("Updated caption", null);

            when(stationPhotoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(testPhoto));
            doNothing().when(stationService).verifyOwnership(STATION_ID);
            when(stationPhotoRepository.save(any(StationPhoto.class))).thenReturn(testPhoto);
            when(stationPhotoDtoConverter.convert(any(StationPhoto.class))).thenReturn(testPhotoResponse);

            // When
            StationPhotoResponse result = stationPhotoService.updatePhoto(PHOTO_ID, request);

            // Then
            assertThat(result).isNotNull();
            verify(stationPhotoRepository).save(argThat(photo -> "Updated caption".equals(photo.getCaption())));
        }

        @Test
        @DisplayName("Should update photo displayOrder successfully")
        void updatePhoto_ValidRequest_UpdatesDisplayOrder() {
            // Given
            UpdateStationPhotoRequest request = new UpdateStationPhotoRequest(null, 5);

            when(stationPhotoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(testPhoto));
            doNothing().when(stationService).verifyOwnership(STATION_ID);
            when(stationPhotoRepository.save(any(StationPhoto.class))).thenReturn(testPhoto);
            when(stationPhotoDtoConverter.convert(any(StationPhoto.class))).thenReturn(testPhotoResponse);

            // When
            StationPhotoResponse result = stationPhotoService.updatePhoto(PHOTO_ID, request);

            // Then
            assertThat(result).isNotNull();
            verify(stationPhotoRepository).save(argThat(photo -> photo.getDisplayOrder() == 5));
        }

        @Test
        @DisplayName("Should throw exception when photo not found")
        void updatePhoto_PhotoNotFound_ThrowsNotFound() {
            // Given
            UpdateStationPhotoRequest request = new UpdateStationPhotoRequest("New caption", null);
            when(stationPhotoRepository.findById(PHOTO_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> stationPhotoService.updatePhoto(PHOTO_ID, request))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.STATION_PHOTO_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("Should throw exception when user is not owner")
        void updatePhoto_NotOwner_ThrowsForbidden() {
            // Given
            UpdateStationPhotoRequest request = new UpdateStationPhotoRequest("New caption", null);

            when(stationPhotoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(testPhoto));
            doThrow(new AppException(ErrorCode.STATION_NOT_OWNED))
                    .when(stationService).verifyOwnership(STATION_ID);

            // When & Then
            assertThatThrownBy(() -> stationPhotoService.updatePhoto(PHOTO_ID, request))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.STATION_NOT_OWNED);
                    });

            verify(stationPhotoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when request is null")
        void updatePhoto_NullRequest_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> stationPhotoService.updatePhoto(PHOTO_ID, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== DELETE PHOTO TESTS ====================

    @Nested
    @DisplayName("Delete Photo Tests")
    class DeletePhotoTests {

        @Test
        @DisplayName("Should delete photo and cleanup Cloudinary")
        void deletePhoto_ValidOwner_DeletesPhotoAndCloudinary() {
            // Given
            when(stationPhotoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(testPhoto));
            doNothing().when(stationService).verifyOwnership(STATION_ID);

            // When
            stationPhotoService.deletePhoto(PHOTO_ID);

            // Then
            verify(fileStorageService).deleteFile("stations/photos/abc123");
            verify(stationPhotoRepository).delete(testPhoto);
        }

        @Test
        @DisplayName("Should still delete from DB even if Cloudinary cleanup fails")
        void deletePhoto_CloudinaryFails_StillDeletesFromDb() {
            // Given
            when(stationPhotoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(testPhoto));
            doNothing().when(stationService).verifyOwnership(STATION_ID);
            doThrow(new RuntimeException("Cloudinary error"))
                    .when(fileStorageService).deleteFile(anyString());

            // When
            stationPhotoService.deletePhoto(PHOTO_ID);

            // Then — DB delete should still happen
            verify(stationPhotoRepository).delete(testPhoto);
        }

        @Test
        @DisplayName("Should skip Cloudinary cleanup when no publicId")
        void deletePhoto_NoPublicId_SkipsCloudinaryCleanup() {
            // Given
            testPhoto.setCloudinaryPublicId(null);
            when(stationPhotoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(testPhoto));
            doNothing().when(stationService).verifyOwnership(STATION_ID);

            // When
            stationPhotoService.deletePhoto(PHOTO_ID);

            // Then
            verify(fileStorageService, never()).deleteFile(anyString());
            verify(stationPhotoRepository).delete(testPhoto);
        }

        @Test
        @DisplayName("Should throw exception when photo not found")
        void deletePhoto_PhotoNotFound_ThrowsNotFound() {
            // Given
            when(stationPhotoRepository.findById(PHOTO_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> stationPhotoService.deletePhoto(PHOTO_ID))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.STATION_PHOTO_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("Should throw exception when user is not owner")
        void deletePhoto_NotOwner_ThrowsForbidden() {
            // Given
            when(stationPhotoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(testPhoto));
            doThrow(new AppException(ErrorCode.STATION_NOT_OWNED))
                    .when(stationService).verifyOwnership(STATION_ID);

            // When & Then
            assertThatThrownBy(() -> stationPhotoService.deletePhoto(PHOTO_ID))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.STATION_NOT_OWNED);
                    });

            verify(stationPhotoRepository, never()).delete(any());
        }
    }

    // ==================== REORDER PHOTOS TESTS ====================

    @Nested
    @DisplayName("Reorder Photos Tests")
    class ReorderPhotosTests {

        @Test
        @DisplayName("Should reorder photos successfully")
        void reorderPhotos_ValidOrder_UpdatesDisplayOrders() {
            // Given
            StationPhoto photo1 = new StationPhoto();
            photo1.setId(1L);
            photo1.setStationId(STATION_ID);
            photo1.setDisplayOrder(0);

            StationPhoto photo2 = new StationPhoto();
            photo2.setId(2L);
            photo2.setStationId(STATION_ID);
            photo2.setDisplayOrder(1);

            List<StationPhoto> existingPhotos = List.of(photo1, photo2);
            List<Long> newOrder = List.of(2L, 1L); // Swap order

            doNothing().when(stationService).verifyOwnership(STATION_ID);
            when(stationPhotoRepository.findByStationIdOrderByDisplayOrderAsc(STATION_ID))
                    .thenReturn(existingPhotos);
            when(stationPhotoRepository.saveAll(anyList())).thenReturn(existingPhotos);
            when(stationPhotoDtoConverter.convert(anyList())).thenReturn(List.of(
                    StationPhotoResponse.builder().id(2L).displayOrder(0).build(),
                    StationPhotoResponse.builder().id(1L).displayOrder(1).build()));

            // When
            List<StationPhotoResponse> result = stationPhotoService.reorderPhotos(STATION_ID, newOrder);

            // Then
            assertThat(result).hasSize(2);
            verify(stationPhotoRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("Should throw exception when photo IDs don't match station photos")
        void reorderPhotos_MismatchedIds_ThrowsException() {
            // Given
            StationPhoto photo1 = new StationPhoto();
            photo1.setId(1L);
            photo1.setStationId(STATION_ID);

            List<StationPhoto> existingPhotos = List.of(photo1);
            List<Long> badOrder = List.of(1L, 999L); // 999 doesn't belong

            doNothing().when(stationService).verifyOwnership(STATION_ID);
            when(stationPhotoRepository.findByStationIdOrderByDisplayOrderAsc(STATION_ID))
                    .thenReturn(existingPhotos);

            // When & Then
            assertThatThrownBy(() -> stationPhotoService.reorderPhotos(STATION_ID, badOrder))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
                    });
        }

        @Test
        @DisplayName("Should throw exception when photo IDs list is empty")
        void reorderPhotos_EmptyList_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> stationPhotoService.reorderPhotos(STATION_ID, List.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when photo IDs list is null")
        void reorderPhotos_NullList_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> stationPhotoService.reorderPhotos(STATION_ID, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when user is not owner")
        void reorderPhotos_NotOwner_ThrowsForbidden() {
            // Given
            List<Long> order = List.of(1L, 2L);

            doThrow(new AppException(ErrorCode.STATION_NOT_OWNED))
                    .when(stationService).verifyOwnership(STATION_ID);

            // When & Then
            assertThatThrownBy(() -> stationPhotoService.reorderPhotos(STATION_ID, order))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.STATION_NOT_OWNED);
                    });
        }
    }
}
