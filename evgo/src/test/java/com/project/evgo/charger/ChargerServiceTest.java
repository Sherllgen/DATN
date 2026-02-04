package com.project.evgo.charger;

import com.project.evgo.charger.internal.Charger;
import com.project.evgo.charger.internal.ChargerDtoConverter;
import com.project.evgo.charger.internal.ChargerRepository;
import com.project.evgo.charger.internal.ChargerServiceImpl;
import com.project.evgo.charger.internal.Port;
import com.project.evgo.charger.internal.PortRepository;
import com.project.evgo.charger.request.CreateChargerRequest;
import com.project.evgo.charger.request.CreatePortRequest;
import com.project.evgo.charger.response.ChargerResponse;
import com.project.evgo.charger.response.PortResponse;
import com.project.evgo.sharedkernel.enums.ChargerStatus;
import com.project.evgo.sharedkernel.enums.ConnectorType;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.enums.PortStatus;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.station.StationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ChargerService.
 */
@ExtendWith(MockitoExtension.class)
class ChargerServiceTest {

    @InjectMocks
    private ChargerServiceImpl chargerService;

    @Mock
    private ChargerRepository chargerRepository;

    @Mock
    private PortRepository portRepository;

    @Mock
    private StationService stationService;

    @Mock
    private ChargerDtoConverter converter;

    private static final Long STATION_ID = 100L;
    private static final Long CHARGER_ID = 200L;
    private static final Long PORT_ID = 300L;

    private Charger testCharger;
    private Port testPort;
    private ChargerResponse testChargerResponse;
    private PortResponse testPortResponse;

    @BeforeEach
    void setUp() {
        testCharger = new Charger();
        testCharger.setId(CHARGER_ID);
        testCharger.setName("Test Charger");
        testCharger.setMaxPower(50.0);
        testCharger.setConnectorType(ConnectorType.VINFAST_STD);
        testCharger.setStatus(ChargerStatus.AVAILABLE);
        testCharger.setStationId(STATION_ID);
        testCharger.setCreatedAt(LocalDateTime.now());

        testPort = new Port();
        testPort.setId(PORT_ID);
        testPort.setPortNumber(1);
        testPort.setStatus(PortStatus.AVAILABLE);
        testPort.setCharger(testCharger);

        testChargerResponse = ChargerResponse.builder()
                .id(CHARGER_ID)
                .name("Test Charger")
                .maxPower(50.0)
                .connectorType(ConnectorType.VINFAST_STD)
                .status(ChargerStatus.AVAILABLE)
                .stationId(STATION_ID)
                .build();

        testPortResponse = PortResponse.builder()
                .id(PORT_ID)
                .portNumber(1)
                .status(PortStatus.AVAILABLE)
                .build();
    }

    // ==================== CREATE CHARGER TESTS ====================

    @Nested
    @DisplayName("Create Charger Tests")
    class CreateChargerTests {

        @Test
        @DisplayName("Should create charger successfully with valid request")
        void createCharger_ValidRequest_ReturnsChargerResponse() {
            // Given
            CreateChargerRequest request = new CreateChargerRequest();
            request.setName("New Charger");
            request.setPowerOutput(50.0);
            request.setStationId(STATION_ID);

            doNothing().when(stationService).verifyOwnership(STATION_ID);
            when(chargerRepository.save(any(Charger.class))).thenReturn(testCharger);
            when(converter.toChargerResponse(any(Charger.class))).thenReturn(testChargerResponse);

            // When
            ChargerResponse result = chargerService.createCharger(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Test Charger");
            verify(chargerRepository).save(any(Charger.class));
        }

        @Test
        @DisplayName("Should throw FORBIDDEN when user is not station owner")
        void createCharger_NotOwner_ThrowsForbidden() {
            // Given
            CreateChargerRequest request = new CreateChargerRequest();
            request.setName("New Charger");
            request.setPowerOutput(50.0);
            request.setStationId(STATION_ID);

            doThrow(new AppException(ErrorCode.STATION_NOT_OWNED))
                    .when(stationService).verifyOwnership(STATION_ID);

            // When & Then
            assertThatThrownBy(() -> chargerService.createCharger(request))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.STATION_NOT_OWNED);
                    });

            verify(chargerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw NOT_FOUND when station does not exist")
        void createCharger_StationNotFound_ThrowsNotFound() {
            // Given
            CreateChargerRequest request = new CreateChargerRequest();
            request.setName("New Charger");
            request.setPowerOutput(50.0);
            request.setStationId(STATION_ID);

            doThrow(new AppException(ErrorCode.STATION_NOT_FOUND))
                    .when(stationService).verifyOwnership(STATION_ID);

            // When & Then
            assertThatThrownBy(() -> chargerService.createCharger(request))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.STATION_NOT_FOUND);
                    });
        }
    }

    // ==================== UPDATE CHARGER TESTS ====================

    @Nested
    @DisplayName("Update Charger Tests")
    class UpdateChargerTests {

        @Test
        @DisplayName("Should update charger successfully when owner is correct")
        void updateCharger_ValidRequest_UpdatesCharger() {
            // Given
            when(chargerRepository.findById(CHARGER_ID))
                    .thenReturn(Optional.of(testCharger));
            doNothing().when(stationService).verifyOwnership(STATION_ID);
            when(chargerRepository.save(any(Charger.class))).thenReturn(testCharger);
            when(converter.toChargerResponse(any(Charger.class))).thenReturn(testChargerResponse);

            // When
            ChargerResponse result = chargerService.updateCharger(CHARGER_ID, "Updated Name", 100.0,
                    ConnectorType.IEC_TYPE_2);

            // Then
            assertThat(result).isNotNull();
            verify(chargerRepository).save(any(Charger.class));
        }

        @Test
        @DisplayName("Should throw FORBIDDEN when user is not station owner")
        void updateCharger_NotOwner_ThrowsForbidden() {
            // Given
            when(chargerRepository.findById(CHARGER_ID))
                    .thenReturn(Optional.of(testCharger));
            doThrow(new AppException(ErrorCode.STATION_NOT_OWNED))
                    .when(stationService).verifyOwnership(STATION_ID);

            // When & Then
            assertThatThrownBy(
                    () -> chargerService.updateCharger(CHARGER_ID, "Updated", 100.0, ConnectorType.VINFAST_STD))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.CHARGER_NOT_OWNED);
                    });

            verify(chargerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw NOT_FOUND when charger does not exist")
        void updateCharger_ChargerNotFound_ThrowsNotFound() {
            // Given
            when(chargerRepository.findById(CHARGER_ID))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(
                    () -> chargerService.updateCharger(CHARGER_ID, "Updated", 100.0, ConnectorType.VINFAST_STD))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.CHARGER_NOT_FOUND);
                    });
        }
    }

    // ==================== DELETE CHARGER TESTS ====================

    @Nested
    @DisplayName("Delete Charger Tests")
    class DeleteChargerTests {

        @Test
        @DisplayName("Should delete charger successfully when owner is correct")
        void deleteCharger_ValidOwner_DeletesCharger() {
            // Given
            when(chargerRepository.findById(CHARGER_ID))
                    .thenReturn(Optional.of(testCharger));
            doNothing().when(stationService).verifyOwnership(STATION_ID);

            // When
            chargerService.deleteCharger(CHARGER_ID);

            // Then
            verify(chargerRepository).delete(testCharger);
        }

        @Test
        @DisplayName("Should throw FORBIDDEN when user is not station owner")
        void deleteCharger_NotOwner_ThrowsForbidden() {
            // Given
            when(chargerRepository.findById(CHARGER_ID))
                    .thenReturn(Optional.of(testCharger));
            doThrow(new AppException(ErrorCode.STATION_NOT_OWNED))
                    .when(stationService).verifyOwnership(STATION_ID);

            // When & Then
            assertThatThrownBy(() -> chargerService.deleteCharger(CHARGER_ID))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.CHARGER_NOT_OWNED);
                    });

            verify(chargerRepository, never()).delete(any());
        }
    }

    // ==================== CREATE PORT TESTS ====================

    @Nested
    @DisplayName("Create Port Tests")
    class CreatePortTests {

        @Test
        @DisplayName("Should create port successfully with valid request")
        void createPort_ValidRequest_ReturnsPortResponse() {
            // Given
            CreatePortRequest request = new CreatePortRequest();
            request.setPortNumber(1);
            request.setChargerId(CHARGER_ID);

            when(chargerRepository.findById(CHARGER_ID))
                    .thenReturn(Optional.of(testCharger));
            doNothing().when(stationService).verifyOwnership(STATION_ID);
            when(portRepository.save(any(Port.class))).thenReturn(testPort);
            when(converter.toPortResponse(any(Port.class))).thenReturn(testPortResponse);

            // When
            PortResponse result = chargerService.createPort(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPortNumber()).isEqualTo(1);
            verify(portRepository).save(any(Port.class));
        }

        @Test
        @DisplayName("Should throw FORBIDDEN when user is not charger's station owner")
        void createPort_ChargerNotOwned_ThrowsForbidden() {
            // Given
            CreatePortRequest request = new CreatePortRequest();
            request.setPortNumber(1);
            request.setChargerId(CHARGER_ID);

            when(chargerRepository.findById(CHARGER_ID))
                    .thenReturn(Optional.of(testCharger));
            doThrow(new AppException(ErrorCode.STATION_NOT_OWNED))
                    .when(stationService).verifyOwnership(STATION_ID);

            // When & Then
            assertThatThrownBy(() -> chargerService.createPort(request))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.CHARGER_NOT_OWNED);
                    });

            verify(portRepository, never()).save(any());
        }
    }

    // ==================== UPDATE PORT TESTS ====================

    @Nested
    @DisplayName("Update Port Tests")
    class UpdatePortTests {

        @Test
        @DisplayName("Should update port status successfully when owner is correct")
        void updatePortStatus_ValidOwner_UpdatesStatus() {
            // Given
            when(portRepository.findById(PORT_ID))
                    .thenReturn(Optional.of(testPort));
            doNothing().when(stationService).verifyOwnership(STATION_ID);
            when(portRepository.save(any(Port.class))).thenReturn(testPort);
            when(converter.toPortResponse(any(Port.class))).thenReturn(testPortResponse);

            // When
            PortResponse result = chargerService.updatePortStatus(PORT_ID, PortStatus.CHARGING);

            // Then
            assertThat(result).isNotNull();
            verify(portRepository).save(any(Port.class));
        }

        @Test
        @DisplayName("Should throw FORBIDDEN when user is not port's charger's station owner")
        void updatePortStatus_NotOwner_ThrowsForbidden() {
            // Given
            when(portRepository.findById(PORT_ID))
                    .thenReturn(Optional.of(testPort));
            doThrow(new AppException(ErrorCode.STATION_NOT_OWNED))
                    .when(stationService).verifyOwnership(STATION_ID);

            // When & Then
            assertThatThrownBy(() -> chargerService.updatePortStatus(PORT_ID, PortStatus.CHARGING))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.CHARGER_NOT_OWNED);
                    });

            verify(portRepository, never()).save(any());
        }
    }

    // ==================== DELETE PORT TESTS ====================

    @Nested
    @DisplayName("Delete Port Tests")
    class DeletePortTests {

        @Test
        @DisplayName("Should delete port successfully when owner is correct")
        void deletePort_ValidOwner_DeletesPort() {
            // Given
            when(portRepository.findById(PORT_ID))
                    .thenReturn(Optional.of(testPort));
            doNothing().when(stationService).verifyOwnership(STATION_ID);

            // When
            chargerService.deletePort(PORT_ID);

            // Then
            verify(portRepository).delete(testPort);
        }

        @Test
        @DisplayName("Should throw NOT_FOUND when port does not exist")
        void deletePort_PortNotFound_ThrowsNotFound() {
            // Given
            when(portRepository.findById(PORT_ID))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> chargerService.deletePort(PORT_ID))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.PORT_NOT_FOUND);
                    });
        }
    }

    // ==================== GET CHARGERS TESTS ====================

    @Nested
    @DisplayName("Get Chargers Tests")
    class GetChargersTests {

        @Test
        @DisplayName("Should return chargers for a specific station")
        void getChargersByStation_ReturnsOnlyStationChargers() {
            // Given
            List<Charger> chargers = List.of(testCharger);
            when(chargerRepository.findByStationId(STATION_ID)).thenReturn(chargers);
            when(converter.toChargerResponse(chargers)).thenReturn(List.of(testChargerResponse));

            // When
            List<ChargerResponse> result = chargerService.findByStationId(STATION_ID);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStationId()).isEqualTo(STATION_ID);
        }
    }
}
