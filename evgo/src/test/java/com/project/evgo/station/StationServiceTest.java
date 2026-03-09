package com.project.evgo.station;

import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.enums.StationStatus;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.station.internal.Station;
import com.project.evgo.station.internal.StationDtoConverter;
import com.project.evgo.station.internal.StationRepository;
import com.project.evgo.station.internal.StationServiceImpl;
import com.project.evgo.station.request.CreateStationRequest;
import com.project.evgo.station.request.StationOpeningHoursRequest;
import com.project.evgo.station.request.UpdateStationRequest;
import com.project.evgo.station.response.StationResponse;
import com.project.evgo.user.security.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StationService.
 * Tests are written BEFORE implementation (TDD approach).
 */
@ExtendWith(MockitoExtension.class)
class StationServiceTest {

    @InjectMocks
    private StationServiceImpl stationService;

    @Mock
    private StationRepository stationRepository;

    @Mock
    private StationDtoConverter stationDtoConverter;

    private static final Long OWNER_ID = 1L;
    private static final Long OTHER_OWNER_ID = 2L;
    private static final Long STATION_ID = 100L;

    private Station testStation;
    private StationResponse testStationResponse;

    @BeforeEach
    void setUp() {
        testStation = new Station();
        testStation.setId(STATION_ID);
        testStation.setOwnerId(OWNER_ID);
        testStation.setName("Test Station");
        testStation.setAddress("123 Test Street");
        testStation.setLatitude(10.123);
        testStation.setLongitude(106.456);
        testStation.setStatus(StationStatus.PENDING);
        testStation.setCreatedAt(LocalDateTime.now());

        testStationResponse = StationResponse.builder()
                .id(STATION_ID)
                .name("Test Station")
                .address("123 Test Street")
                .latitude(10.123)
                .longitude(106.456)
                .build();
    }

    // ==================== CREATE STATION TESTS ====================

    @Nested
    @DisplayName("Create Station Tests")
    class CreateStationTests {

        @Test
        @DisplayName("Should create station successfully with valid request")
        void create_ValidRequest_ReturnsStationResponse() {
            // Given
            CreateStationRequest request = new CreateStationRequest(
                    "New Station",
                    "New station description",
                    "New Address",
                    10.123,
                    106.456,
                    List.of(),
                    null);

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(OWNER_ID);
                when(stationRepository.existsByNameAndOwnerIdAndDeletedAtIsNull(anyString(), anyLong()))
                        .thenReturn(false);
                when(stationRepository.save(any(Station.class))).thenReturn(testStation);
                when(stationDtoConverter.convert(any(Station.class))).thenReturn(testStationResponse);

                // When
                StationResponse result = stationService.create(request);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.name()).isEqualTo("Test Station");
                verify(stationRepository).save(any(Station.class));
            }
        }

        @Test
        @DisplayName("Should throw exception when station name already exists for owner")
        void create_DuplicateName_ThrowsException() {
            // Given
            CreateStationRequest request = new CreateStationRequest(
                    "Existing Station",
                    "Description",
                    "New Address",
                    10.123,
                    106.456,
                    List.of(),
                    null);

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(OWNER_ID);
                when(stationRepository.existsByNameAndOwnerIdAndDeletedAtIsNull("Existing Station", OWNER_ID))
                        .thenReturn(true);

                // When & Then
                assertThatThrownBy(() -> stationService.create(request))
                        .isInstanceOf(AppException.class)
                        .satisfies(ex -> {
                            AppException appEx = (AppException) ex;
                            assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.STATION_NAME_ALREADY_EXISTS);
                        });

                verify(stationRepository, never()).save(any());
            }
        }

        @Test
        @DisplayName("Should throw exception when request is null")
        void create_NullRequest_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> stationService.create(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== UPDATE STATION TESTS ====================

    @Nested
    @DisplayName("Update Station Tests")
    class UpdateStationTests {

        @Test
        @DisplayName("Should update station successfully when owner is correct")
        void update_ValidRequest_ReturnsUpdatedStation() {
            // Given
            UpdateStationRequest request = new UpdateStationRequest(
                    "Updated Station",
                    "Updated description",
                    "Updated Address",
                    10.999,
                    106.999,
                    List.of("https://example.com/image1.jpg"),
                    null);

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(OWNER_ID);
                when(stationRepository.findByIdAndDeletedAtIsNull(STATION_ID))
                        .thenReturn(Optional.of(testStation));
                when(stationRepository.existsByNameAndOwnerIdAndIdNotAndDeletedAtIsNull(
                        anyString(), anyLong(), anyLong())).thenReturn(false);
                when(stationRepository.save(any(Station.class))).thenReturn(testStation);
                when(stationDtoConverter.convert(any(Station.class))).thenReturn(testStationResponse);

                // When
                StationResponse result = stationService.update(STATION_ID, request);

                // Then
                assertThat(result).isNotNull();
                verify(stationRepository).save(any(Station.class));
            }
        }

        @Test
        @DisplayName("Should throw FORBIDDEN when user is not the owner")
        void update_NotOwner_ThrowsForbidden() {
            // Given
            UpdateStationRequest request = new UpdateStationRequest(
                    "Updated Station",
                    "Updated description",
                    "Updated Address",
                    10.999,
                    106.999,
                    List.of(),
                    null);

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(OTHER_OWNER_ID);
                when(stationRepository.findByIdAndDeletedAtIsNull(STATION_ID))
                        .thenReturn(Optional.of(testStation));

                // When & Then
                assertThatThrownBy(() -> stationService.update(STATION_ID, request))
                        .isInstanceOf(AppException.class)
                        .satisfies(ex -> {
                            AppException appEx = (AppException) ex;
                            assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.STATION_NOT_OWNED);
                        });

                verify(stationRepository, never()).save(any());
            }
        }

        @Test
        @DisplayName("Should throw NOT_FOUND when station does not exist")
        void update_StationNotFound_ThrowsNotFound() {
            // Given
            UpdateStationRequest request = new UpdateStationRequest(
                    "Updated Station",
                    "Updated description",
                    "Updated Address",
                    10.999,
                    106.999,
                    List.of(),
                    null);

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(OWNER_ID);
                when(stationRepository.findByIdAndDeletedAtIsNull(STATION_ID))
                        .thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> stationService.update(STATION_ID, request))
                        .isInstanceOf(AppException.class)
                        .satisfies(ex -> {
                            AppException appEx = (AppException) ex;
                            assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.STATION_NOT_FOUND);
                        });
            }
        }
    }

    // ==================== GET MY STATIONS TESTS ====================

    @Nested
    @DisplayName("Get My Stations Tests")
    class GetMyStationsTests {

        @Test
        @DisplayName("Should return only stations owned by current user")
        void getMyStations_ReturnsOnlyOwnerStations() {
            // Given
            List<Station> ownerStations = List.of(testStation);

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(OWNER_ID);
                when(stationRepository.findByOwnerIdAndDeletedAtIsNull(OWNER_ID))
                        .thenReturn(ownerStations);
                when(stationDtoConverter.convert(ownerStations))
                        .thenReturn(List.of(testStationResponse));

                // When
                List<StationResponse> result = stationService.getMyStations();

                // Then
                assertThat(result).hasSize(1);
                verify(stationRepository).findByOwnerIdAndDeletedAtIsNull(OWNER_ID);
            }
        }

        @Test
        @DisplayName("Should return empty list when owner has no stations")
        void getMyStations_NoStations_ReturnsEmptyList() {
            // Given
            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(OWNER_ID);
                when(stationRepository.findByOwnerIdAndDeletedAtIsNull(OWNER_ID))
                        .thenReturn(List.of());
                when(stationDtoConverter.convert(List.<Station>of()))
                        .thenReturn(List.of());

                // When
                List<StationResponse> result = stationService.getMyStations();

                // Then
                assertThat(result).isEmpty();
            }
        }
    }

    // ==================== DELETE STATION TESTS ====================

    @Nested
    @DisplayName("Delete Station Tests (Soft Delete)")
    class DeleteStationTests {

        @Test
        @DisplayName("Should soft delete station successfully when owner is correct")
        void delete_ValidOwner_SoftDeletesStation() {
            // Given
            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(OWNER_ID);
                when(stationRepository.findByIdAndDeletedAtIsNull(STATION_ID))
                        .thenReturn(Optional.of(testStation));
                when(stationRepository.save(any(Station.class))).thenReturn(testStation);

                // When
                stationService.delete(STATION_ID);

                // Then
                verify(stationRepository).save(argThat(station -> station.getDeletedAt() != null));
            }
        }

        @Test
        @DisplayName("Should throw FORBIDDEN when user is not the owner")
        void delete_NotOwner_ThrowsForbidden() {
            // Given
            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(OTHER_OWNER_ID);
                when(stationRepository.findByIdAndDeletedAtIsNull(STATION_ID))
                        .thenReturn(Optional.of(testStation));

                // When & Then
                assertThatThrownBy(() -> stationService.delete(STATION_ID))
                        .isInstanceOf(AppException.class)
                        .satisfies(ex -> {
                            AppException appEx = (AppException) ex;
                            assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.STATION_NOT_OWNED);
                        });

                verify(stationRepository, never()).save(any());
            }
        }

        @Test
        @DisplayName("Should throw NOT_FOUND when station does not exist")
        void delete_StationNotFound_ThrowsNotFound() {
            // Given
            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(OWNER_ID);
                when(stationRepository.findByIdAndDeletedAtIsNull(STATION_ID))
                        .thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> stationService.delete(STATION_ID))
                        .isInstanceOf(AppException.class)
                        .satisfies(ex -> {
                            AppException appEx = (AppException) ex;
                            assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.STATION_NOT_FOUND);
                        });
            }
        }
    }

    // ==================== UPDATE STATUS TESTS ====================

    @Nested
    @DisplayName("Update Station Status Tests")
    class UpdateStatusTests {

        @Test
        @DisplayName("Should update station status successfully")
        void updateStatus_ValidRequest_UpdatesStatus() {
            // Given
            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(OWNER_ID);
                when(stationRepository.findByIdAndDeletedAtIsNull(STATION_ID))
                        .thenReturn(Optional.of(testStation));
                when(stationRepository.save(any(Station.class))).thenReturn(testStation);
                when(stationDtoConverter.convert(any(Station.class))).thenReturn(testStationResponse);

                // When
                StationResponse result = stationService.updateStatus(STATION_ID, StationStatus.ACTIVE);

                // Then
                assertThat(result).isNotNull();
                verify(stationRepository).save(argThat(station -> station.getStatus() == StationStatus.ACTIVE));
            }
        }

        @Test
        @DisplayName("Should throw FORBIDDEN when user is not the owner")
        void updateStatus_NotOwner_ThrowsForbidden() {
            // Given
            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(OTHER_OWNER_ID);
                when(stationRepository.findByIdAndDeletedAtIsNull(STATION_ID))
                        .thenReturn(Optional.of(testStation));

                // When & Then
                assertThatThrownBy(() -> stationService.updateStatus(STATION_ID, StationStatus.ACTIVE))
                        .isInstanceOf(AppException.class)
                        .satisfies(ex -> {
                            AppException appEx = (AppException) ex;
                            assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.STATION_NOT_OWNED);
                        });
            }
        }

        @Test
        @DisplayName("Should throw NOT_FOUND when station does not exist")
        void updateStatus_StationNotFound_ThrowsNotFound() {
            // Given
            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(OWNER_ID);
                when(stationRepository.findByIdAndDeletedAtIsNull(STATION_ID))
                        .thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> stationService.updateStatus(STATION_ID, StationStatus.ACTIVE))
                        .isInstanceOf(AppException.class)
                        .satisfies(ex -> {
                            AppException appEx = (AppException) ex;
                            assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.STATION_NOT_FOUND);
                        });
            }
        }
    }

    // ==================== EDGE CASE TESTS ====================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should throw exception when update request is null")
        void update_NullRequest_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> stationService.update(STATION_ID, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should handle station with empty imageUrls list")
        void create_EmptyImageUrls_Success() {
            // Given
            CreateStationRequest request = new CreateStationRequest(
                    "New Station",
                    "Description",
                    "New Address",
                    10.123,
                    106.456,
                    List.of(), // Empty image list
                    null); // Added openingHours

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(OWNER_ID);
                when(stationRepository.existsByNameAndOwnerIdAndDeletedAtIsNull(anyString(), anyLong()))
                        .thenReturn(false);
                when(stationRepository.save(any(Station.class))).thenReturn(testStation);
                when(stationDtoConverter.convert(any(Station.class))).thenReturn(testStationResponse);

                // When
                StationResponse result = stationService.create(request);

                // Then
                assertThat(result).isNotNull();
                verify(stationRepository)
                        .save(argThat(station -> station.getImageUrls() != null && station.getImageUrls().isEmpty()));
            }
        }

        @Test
        @DisplayName("Should set status to PENDING when creating new station")
        void create_NewStation_StatusIsPending() {
            // Given
            CreateStationRequest request = new CreateStationRequest(
                    "New Station",
                    "Description",
                    "New Address",
                    10.123,
                    106.456,
                    List.of("https://example.com/image.jpg"),
                    null); // Added openingHours

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(OWNER_ID);
                when(stationRepository.existsByNameAndOwnerIdAndDeletedAtIsNull(anyString(), anyLong()))
                        .thenReturn(false);
                when(stationRepository.save(any(Station.class))).thenReturn(testStation);
                when(stationDtoConverter.convert(any(Station.class))).thenReturn(testStationResponse);

                // When
                stationService.create(request);

                // Then
                verify(stationRepository).save(argThat(station -> station.getStatus() == StationStatus.PENDING));
            }
        }
    }

    // ==================== OPENING HOURS TESTS ====================

    @Nested
    @DisplayName("Opening Hours Tests")
    class OpeningHoursTests {

        @Test
        @DisplayName("Should create station with opening hours")
        void create_WithOpeningHours_Success() {
            // Given
            List<StationOpeningHoursRequest> openingHours = List.of(
                    new StationOpeningHoursRequest(DayOfWeek.MONDAY, LocalTime.of(7, 0), LocalTime.of(22, 0), true),
                    new StationOpeningHoursRequest(DayOfWeek.SUNDAY, null, null, false));

            CreateStationRequest request = new CreateStationRequest(
                    "Station with Hours",
                    "Description",
                    "Address",
                    10.123,
                    106.456,
                    List.of(),
                    openingHours);

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(OWNER_ID);
                when(stationRepository.existsByNameAndOwnerIdAndDeletedAtIsNull(anyString(), anyLong()))
                        .thenReturn(false);
                when(stationRepository.save(any(Station.class))).thenAnswer(i -> {
                    Station s = i.getArgument(0);
                    s.setId(1L);
                    return s;
                });
                when(stationDtoConverter.convert(any(Station.class))).thenReturn(testStationResponse);

                // When
                StationResponse result = stationService.create(request);

                // Then
                assertThat(result).isNotNull();
                verify(stationRepository).save(argThat(station -> station.getOpeningHours() != null &&
                        station.getOpeningHours().size() == 2));
            }
        }

        @Test
        @DisplayName("Should create station without opening hours (24/7 default)")
        void create_WithoutOpeningHours_Success() {
            // Given
            CreateStationRequest request = new CreateStationRequest(
                    "24/7 Station",
                    "Description",
                    "Address",
                    10.123,
                    106.456,
                    List.of(),
                    null);

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(OWNER_ID);
                when(stationRepository.existsByNameAndOwnerIdAndDeletedAtIsNull(anyString(), anyLong()))
                        .thenReturn(false);
                when(stationRepository.save(any(Station.class))).thenReturn(testStation);
                when(stationDtoConverter.convert(any(Station.class))).thenReturn(testStationResponse);

                // When
                StationResponse result = stationService.create(request);

                // Then
                assertThat(result).isNotNull();
                verify(stationRepository).save(
                        argThat(station -> station.getOpeningHours() == null || station.getOpeningHours().isEmpty()));
            }
        }

        @Test
        @DisplayName("Should update station opening hours")
        void update_WithOpeningHours_ReplacesExisting() {
            // Given
            List<StationOpeningHoursRequest> newOpeningHours = List.of(
                    new StationOpeningHoursRequest(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(20, 0), true));

            UpdateStationRequest request = new UpdateStationRequest(
                    "Updated Station",
                    "Updated Description",
                    "Updated Address",
                    10.999,
                    106.999,
                    List.of(),
                    newOpeningHours);

            testStation.setOpeningHours(new ArrayList<>()); // Existing empty list

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(OWNER_ID);
                when(stationRepository.findByIdAndDeletedAtIsNull(STATION_ID))
                        .thenReturn(Optional.of(testStation));
                when(stationRepository.existsByNameAndOwnerIdAndIdNotAndDeletedAtIsNull(
                        anyString(), anyLong(), anyLong())).thenReturn(false);
                when(stationRepository.save(any(Station.class))).thenReturn(testStation);
                when(stationDtoConverter.convert(any(Station.class))).thenReturn(testStationResponse);

                // When
                StationResponse result = stationService.update(STATION_ID, request);

                // Then
                assertThat(result).isNotNull();
                verify(stationRepository).save(argThat(station -> station.getOpeningHours() != null &&
                        station.getOpeningHours().size() == 1));
            }
        }

        @Test
        @DisplayName("Should handle opening hours with isOpen=false (closed day)")
        void create_WithClosedDay_Success() {
            // Given - Sunday is closed
            List<StationOpeningHoursRequest> openingHours = List.of(
                    new StationOpeningHoursRequest(DayOfWeek.SUNDAY, null, null, false));

            CreateStationRequest request = new CreateStationRequest(
                    "Station Closed Sunday",
                    "Description",
                    "Address",
                    10.123,
                    106.456,
                    List.of(),
                    openingHours);

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(OWNER_ID);
                when(stationRepository.existsByNameAndOwnerIdAndDeletedAtIsNull(anyString(), anyLong()))
                        .thenReturn(false);
                when(stationRepository.save(any(Station.class))).thenAnswer(i -> {
                    Station s = i.getArgument(0);
                    s.setId(1L);
                    return s;
                });
                when(stationDtoConverter.convert(any(Station.class))).thenReturn(testStationResponse);

                // When
                StationResponse result = stationService.create(request);

                // Then
                assertThat(result).isNotNull();
                verify(stationRepository).save(argThat(station -> {
                    if (station.getOpeningHours() == null || station.getOpeningHours().isEmpty()) {
                        return false;
                    }
                    var sundayHour = station.getOpeningHours().get(0);
                    return sundayHour.getDayOfWeek() == DayOfWeek.SUNDAY &&
                            Boolean.FALSE.equals(sundayHour.getIsOpen());
                }));
            }
        }
    }

    // ==================== SEARCH TESTS ====================

    @Nested
    @DisplayName("Search Stations Tests")
    class SearchStationsTests {

        @Test
        @DisplayName("searchNearby should return found stations")
        void searchNearby_ValidRequest_ReturnsResults() {
            // Given
            com.project.evgo.station.request.SearchNearbyRequest request = new com.project.evgo.station.request.SearchNearbyRequest(
                    10.0, 106.0, 5.0, 10);

            com.project.evgo.station.internal.StationProjection projection = mock(
                    com.project.evgo.station.internal.StationProjection.class);
            List<com.project.evgo.station.internal.StationProjection> projections = List.of(projection);

            when(stationRepository.findNearByStations(10.0, 106.0, 5000.0, 10))
                    .thenReturn(projections);

            List<com.project.evgo.station.response.StationSearchResult> mockResults = List.of(
                    com.project.evgo.station.response.StationSearchResult.builder().id(1L).name("Station A").build());
            when(stationDtoConverter.convertToSearchResults(projections)).thenReturn(mockResults);

            // When
            List<com.project.evgo.station.response.StationSearchResult> results = stationService.searchNearby(request);

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).name()).isEqualTo("Station A");
            verify(stationRepository).findNearByStations(10.0, 106.0, 5000.0, 10);
            verify(stationDtoConverter).convertToSearchResults(projections);
        }

        @Test
        @DisplayName("searchNearby should throw exception when coordinates are missing")
        void searchNearby_MissingCoordinates_ThrowsException() {
            // Given
            com.project.evgo.station.request.SearchNearbyRequest request = new com.project.evgo.station.request.SearchNearbyRequest(
                    null, null, 5.0, 10);

            // When / Then
            assertThatThrownBy(() -> stationService.searchNearby(request))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);

            verify(stationRepository, never()).findNearByStations(anyDouble(), anyDouble(), anyDouble(), anyInt());
        }

        @Test
        @DisplayName("searchByText should return found stations")
        void searchByText_ValidRequest_ReturnsResults() {
            // Given
            com.project.evgo.station.request.SearchTextRequest request = new com.project.evgo.station.request.SearchTextRequest(
                    "Test", 10.0, 106.0, 10);

            com.project.evgo.station.internal.StationProjection projection = mock(
                    com.project.evgo.station.internal.StationProjection.class);
            List<com.project.evgo.station.internal.StationProjection> projections = List.of(projection);

            when(stationRepository.searchByText("Test", 10.0, 106.0, 10))
                    .thenReturn(projections);

            List<com.project.evgo.station.response.StationSearchResult> mockResults = List.of(
                    com.project.evgo.station.response.StationSearchResult.builder().id(1L).name("Station A").build());
            when(stationDtoConverter.convertToSearchResults(projections)).thenReturn(mockResults);

            // When
            List<com.project.evgo.station.response.StationSearchResult> results = stationService.searchByText(request);

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).name()).isEqualTo("Station A");
            verify(stationRepository).searchByText("Test", 10.0, 106.0, 10);
        }

        @Test
        @DisplayName("searchByText should throw exception when query is empty")
        void searchByText_EmptyQuery_ThrowsException() {
            // Given
            com.project.evgo.station.request.SearchTextRequest request = new com.project.evgo.station.request.SearchTextRequest(
                    "   ", 10.0, 106.0, 10);

            // When / Then
            assertThatThrownBy(() -> stationService.searchByText(request))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);

            verify(stationRepository, never()).searchByText(anyString(), anyDouble(), anyDouble(), anyInt());
        }
    }
}
