package com.project.evgo.station;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.project.evgo.sharedkernel.dto.PageResponse;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.enums.StationStatus;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.station.internal.Station;
import com.project.evgo.station.internal.StationAdminServiceImpl;
import com.project.evgo.station.internal.StationDtoConverter;
import com.project.evgo.station.internal.StationRepository;
import com.project.evgo.station.response.StationResponse;

@ExtendWith(MockitoExtension.class)
class StationAdminServiceImplTest {

    @InjectMocks
    private StationAdminServiceImpl stationAdminService;

    @Mock
    private StationRepository stationRepository;

    @Mock
    private StationDtoConverter converter;

    private static final Long STATION_ID = 1L;
    private Station testStation;
    private StationResponse testResponse;

    @BeforeEach
    void setUp() {
        testStation = new Station();
        testStation.setId(STATION_ID);
        testStation.setStatus(StationStatus.PENDING);

        testResponse = StationResponse.builder().id(STATION_ID).status(StationStatus.PENDING).build();
    }

    @Nested
    @DisplayName("getAllStations")
    class GetAllStationsTests {

        @Test
        @DisplayName("Should return all stations when status is null")
        void getAllStations_StatusNull_ReturnsAllStations() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Station> page = new PageImpl<>(List.of(testStation));
            when(stationRepository.findByDeletedAtIsNull(pageable)).thenReturn(page);
            when(converter.toResponse(testStation)).thenReturn(testResponse);

            PageResponse<StationResponse> response = stationAdminService.getAllStations(null, pageable);

            assertThat(response.content()).hasSize(1);
            verify(stationRepository).findByDeletedAtIsNull(pageable);
        }

        @Test
        @DisplayName("Should return stations by status when status is provided")
        void getAllStations_WithStatus_ReturnsFilteredStations() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Station> page = new PageImpl<>(List.of(testStation));
            when(stationRepository.findByStatusAndDeletedAtIsNull(StationStatus.PENDING, pageable)).thenReturn(page);
            when(converter.toResponse(testStation)).thenReturn(testResponse);

            PageResponse<StationResponse> response = stationAdminService.getAllStations(StationStatus.PENDING, pageable);

            assertThat(response.content()).hasSize(1);
            verify(stationRepository).findByStatusAndDeletedAtIsNull(StationStatus.PENDING, pageable);
        }
    }

    @Nested
    @DisplayName("getStationById")
    class GetStationByIdTests {

        @Test
        @DisplayName("Should return station response when station exists")
        void getStationById_Exists_ReturnsStation() {
            when(stationRepository.findByIdAndDeletedAtIsNull(STATION_ID)).thenReturn(Optional.of(testStation));
            when(converter.toResponse(testStation)).thenReturn(testResponse);

            StationResponse response = stationAdminService.getStationById(STATION_ID);

            assertThat(response.id()).isEqualTo(STATION_ID);
        }

        @Test
        @DisplayName("Should throw AppException when station not found")
        void getStationById_NotFound_ThrowsException() {
            when(stationRepository.findByIdAndDeletedAtIsNull(STATION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stationAdminService.getStationById(STATION_ID))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STATION_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Status Modification Tests")
    class StatusModificationTests {

        @BeforeEach
        void setupExistingStation() {
            when(stationRepository.findByIdAndDeletedAtIsNull(STATION_ID)).thenReturn(Optional.of(testStation));
        }

        @Test
        @DisplayName("approveStation should set status to ACTIVE")
        void approveStation_Valid_Success() {
            stationAdminService.approveStation(STATION_ID);

            assertThat(testStation.getStatus()).isEqualTo(StationStatus.ACTIVE);
            verify(stationRepository).save(testStation);
        }

        @Test
        @DisplayName("approveStation should throw exception if already approved or not pending")
        void approveStation_NotPending_ThrowsException() {
            testStation.setStatus(StationStatus.ACTIVE);

            assertThatThrownBy(() -> stationAdminService.approveStation(STATION_ID))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STATION_ALREADY_APPROVED);
        }

        @Test
        @DisplayName("rejectStation should set status to SUSPENDED")
        void rejectStation_Valid_Success() {
            stationAdminService.rejectStation(STATION_ID, "Incomplete info");

            assertThat(testStation.getStatus()).isEqualTo(StationStatus.SUSPENDED);
            verify(stationRepository).save(testStation);
        }

        @Test
        @DisplayName("rejectStation should throw exception if not pending")
        void rejectStation_NotPending_ThrowsException() {
            testStation.setStatus(StationStatus.ACTIVE);

            assertThatThrownBy(() -> stationAdminService.rejectStation(STATION_ID, "Reason"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STATION_INVALID_STATUS_CHANGE);
        }

        @Test
        @DisplayName("suspendStation should set status to SUSPENDED for active station")
        void suspendStation_Valid_Success() {
            testStation.setStatus(StationStatus.ACTIVE);
            
            stationAdminService.suspendStation(STATION_ID, "Violation");

            assertThat(testStation.getStatus()).isEqualTo(StationStatus.SUSPENDED);
            verify(stationRepository).save(testStation);
        }

        @Test
        @DisplayName("suspendStation should throw exception if already suspended")
        void suspendStation_AlreadySuspended_ThrowsException() {
            testStation.setStatus(StationStatus.SUSPENDED);

            assertThatThrownBy(() -> stationAdminService.suspendStation(STATION_ID, "Reason"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STATION_ALREADY_SUSPENDED);
        }

        @Test
        @DisplayName("suspendStation should throw exception if pending")
        void suspendStation_Pending_ThrowsException() {
            testStation.setStatus(StationStatus.PENDING);

            assertThatThrownBy(() -> stationAdminService.suspendStation(STATION_ID, "Reason"))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STATION_INVALID_STATUS_CHANGE);
        }

        @Test
        @DisplayName("unsuspendStation should set status to ACTIVE")
        void unsuspendStation_Valid_Success() {
            testStation.setStatus(StationStatus.SUSPENDED);

            stationAdminService.unsuspendStation(STATION_ID);

            assertThat(testStation.getStatus()).isEqualTo(StationStatus.ACTIVE);
            verify(stationRepository).save(testStation);
        }

        @Test
        @DisplayName("unsuspendStation should throw exception if not suspended")
        void unsuspendStation_NotSuspended_ThrowsException() {
            testStation.setStatus(StationStatus.ACTIVE);

            assertThatThrownBy(() -> stationAdminService.unsuspendStation(STATION_ID))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STATION_INVALID_STATUS_CHANGE);
        }
    }
}
