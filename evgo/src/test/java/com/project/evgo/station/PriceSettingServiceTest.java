package com.project.evgo.station;

import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.station.internal.PriceSetting;
import com.project.evgo.station.internal.PriceSettingDtoConverter;
import com.project.evgo.station.internal.PriceSettingRepository;
import com.project.evgo.station.internal.PriceSettingServiceImpl;
import com.project.evgo.station.request.CreatePriceSettingRequest;
import com.project.evgo.station.response.PriceSettingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PriceSettingService.
 * Tests are written BEFORE implementation (TDD approach).
 */
@ExtendWith(MockitoExtension.class)
class PriceSettingServiceTest {

    @InjectMocks
    private PriceSettingServiceImpl priceSettingService;

    @Mock
    private PriceSettingRepository priceSettingRepository;

    @Mock
    private PriceSettingDtoConverter priceSettingDtoConverter;

    @Mock
    private StationService stationService;

    private static final Long STATION_ID = 100L;
    private static final Long PRICE_SETTING_ID = 300L;

    private PriceSetting testPriceSetting;
    private PriceSettingResponse testPriceSettingResponse;

    @BeforeEach
    void setUp() {
        testPriceSetting = new PriceSetting();
        testPriceSetting.setId(PRICE_SETTING_ID);
        testPriceSetting.setStationId(STATION_ID);
        testPriceSetting.setVersion(1);
        testPriceSetting.setChargingRatePerKwh(new BigDecimal("3000"));
        testPriceSetting.setBookingFee(new BigDecimal("5000"));
        testPriceSetting.setIdlePenaltyPerMinute(new BigDecimal("500"));
        testPriceSetting.setGracePeriodMinutes(30);
        testPriceSetting.setIsActive(true);
        testPriceSetting.setNotes("Initial pricing");
        testPriceSetting.setEffectiveFrom(LocalDateTime.now());
        testPriceSetting.setCreatedAt(LocalDateTime.now());

        testPriceSettingResponse = PriceSettingResponse.builder()
                .id(PRICE_SETTING_ID)
                .stationId(STATION_ID)
                .version(1)
                .chargingRatePerKwh(new BigDecimal("3000"))
                .bookingFee(new BigDecimal("5000"))
                .idlePenaltyPerMinute(new BigDecimal("500"))
                .gracePeriodMinutes(30)
                .isActive(true)
                .notes("Initial pricing")
                .effectiveFrom(testPriceSetting.getEffectiveFrom())
                .createdAt(testPriceSetting.getCreatedAt())
                .build();
    }

    // ==================== CREATE PRICE SETTING TESTS ====================

    @Nested
    @DisplayName("Create Price Setting Tests")
    class CreatePriceSettingTests {

        @Test
        @DisplayName("Should create first pricing version successfully")
        void createPriceSetting_FirstVersion_ReturnsVersion1() {
            // Given
            CreatePriceSettingRequest request = new CreatePriceSettingRequest(
                    new BigDecimal("3000"),
                    new BigDecimal("5000"),
                    new BigDecimal("500"),
                    30,
                    "Initial pricing",
                    LocalDateTime.now());

            doNothing().when(stationService).verifyOwnership(STATION_ID);
            when(priceSettingRepository.findTopByStationIdOrderByVersionDesc(STATION_ID))
                    .thenReturn(Optional.empty()); // No existing version
            when(priceSettingRepository.save(any(PriceSetting.class))).thenReturn(testPriceSetting);
            when(priceSettingDtoConverter.convert(any(PriceSetting.class))).thenReturn(testPriceSettingResponse);

            // When
            PriceSettingResponse result = priceSettingService.createPriceSetting(STATION_ID, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.version()).isEqualTo(1);
            assertThat(result.isActive()).isTrue();
            verify(priceSettingRepository).deactivateAllByStationId(STATION_ID);
            verify(priceSettingRepository).save(argThat(ps -> ps.getVersion() == 1 && ps.getIsActive()));
        }

        @Test
        @DisplayName("Should create subsequent version and deactivate previous")
        void createPriceSetting_SubsequentVersion_IncrementsVersion() {
            // Given
            CreatePriceSettingRequest request = new CreatePriceSettingRequest(
                    new BigDecimal("3500"),
                    new BigDecimal("5000"),
                    new BigDecimal("600"),
                    25,
                    "Price increase",
                    null);

            PriceSetting existingVersion = new PriceSetting();
            existingVersion.setVersion(2);

            PriceSettingResponse expectedResponse = PriceSettingResponse.builder()
                    .version(3)
                    .isActive(true)
                    .build();

            doNothing().when(stationService).verifyOwnership(STATION_ID);
            when(priceSettingRepository.findTopByStationIdOrderByVersionDesc(STATION_ID))
                    .thenReturn(Optional.of(existingVersion));
            when(priceSettingRepository.save(any(PriceSetting.class))).thenAnswer(invocation -> {
                PriceSetting saved = invocation.getArgument(0);
                saved.setId(301L);
                return saved;
            });
            when(priceSettingDtoConverter.convert(any(PriceSetting.class))).thenReturn(expectedResponse);

            // When
            PriceSettingResponse result = priceSettingService.createPriceSetting(STATION_ID, request);

            // Then
            assertThat(result).isNotNull();
            verify(priceSettingRepository).deactivateAllByStationId(STATION_ID);
            verify(priceSettingRepository).save(argThat(ps -> ps.getVersion() == 3 && ps.getIsActive()));
        }

        @Test
        @DisplayName("Should use default grace period when not specified")
        void createPriceSetting_NoGracePeriod_UsesDefault30() {
            // Given
            CreatePriceSettingRequest request = new CreatePriceSettingRequest(
                    new BigDecimal("3000"),
                    null,
                    null,
                    null, // No grace period specified
                    null,
                    null);

            doNothing().when(stationService).verifyOwnership(STATION_ID);
            when(priceSettingRepository.findTopByStationIdOrderByVersionDesc(STATION_ID))
                    .thenReturn(Optional.empty());
            when(priceSettingRepository.save(any(PriceSetting.class))).thenReturn(testPriceSetting);
            when(priceSettingDtoConverter.convert(any(PriceSetting.class))).thenReturn(testPriceSettingResponse);

            // When
            priceSettingService.createPriceSetting(STATION_ID, request);

            // Then
            verify(priceSettingRepository).save(argThat(ps -> ps.getGracePeriodMinutes() == 30));
        }

        @Test
        @DisplayName("Should throw exception when station not found")
        void createPriceSetting_StationNotFound_ThrowsNotFound() {
            // Given
            CreatePriceSettingRequest request = new CreatePriceSettingRequest(
                    new BigDecimal("3000"), null, null, null, null, null);

            doThrow(new AppException(ErrorCode.STATION_NOT_FOUND))
                    .when(stationService).verifyOwnership(STATION_ID);

            // When & Then
            assertThatThrownBy(() -> priceSettingService.createPriceSetting(STATION_ID, request))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.STATION_NOT_FOUND);
                    });

            verify(priceSettingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when user is not station owner")
        void createPriceSetting_NotOwner_ThrowsForbidden() {
            // Given
            CreatePriceSettingRequest request = new CreatePriceSettingRequest(
                    new BigDecimal("3000"), null, null, null, null, null);

            doThrow(new AppException(ErrorCode.STATION_NOT_OWNED))
                    .when(stationService).verifyOwnership(STATION_ID);

            // When & Then
            assertThatThrownBy(() -> priceSettingService.createPriceSetting(STATION_ID, request))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.STATION_NOT_OWNED);
                    });

            verify(priceSettingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when charging rate is zero or negative")
        void createPriceSetting_ZeroChargingRate_ThrowsException() {
            // Given
            CreatePriceSettingRequest request = new CreatePriceSettingRequest(
                    BigDecimal.ZERO, null, null, null, null, null);

            doNothing().when(stationService).verifyOwnership(STATION_ID);

            // When & Then
            assertThatThrownBy(() -> priceSettingService.createPriceSetting(STATION_ID, request))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.INVALID_PRICE_VALUE);
                    });

            verify(priceSettingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when charging rate is negative")
        void createPriceSetting_NegativeChargingRate_ThrowsException() {
            // Given
            CreatePriceSettingRequest request = new CreatePriceSettingRequest(
                    new BigDecimal("-100"), null, null, null, null, null);

            doNothing().when(stationService).verifyOwnership(STATION_ID);

            // When & Then
            assertThatThrownBy(() -> priceSettingService.createPriceSetting(STATION_ID, request))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.INVALID_PRICE_VALUE);
                    });
        }

        @Test
        @DisplayName("Should throw exception when booking fee is negative")
        void createPriceSetting_NegativeBookingFee_ThrowsException() {
            // Given
            CreatePriceSettingRequest request = new CreatePriceSettingRequest(
                    new BigDecimal("3000"),
                    new BigDecimal("-100"), // Negative booking fee
                    null, null, null, null);

            doNothing().when(stationService).verifyOwnership(STATION_ID);

            // When & Then
            assertThatThrownBy(() -> priceSettingService.createPriceSetting(STATION_ID, request))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.INVALID_PRICE_VALUE);
                    });
        }

        @Test
        @DisplayName("Should throw exception when idle penalty is negative")
        void createPriceSetting_NegativePenalty_ThrowsException() {
            // Given
            CreatePriceSettingRequest request = new CreatePriceSettingRequest(
                    new BigDecimal("3000"),
                    null,
                    new BigDecimal("-500"), // Negative penalty
                    null, null, null);

            doNothing().when(stationService).verifyOwnership(STATION_ID);

            // When & Then
            assertThatThrownBy(() -> priceSettingService.createPriceSetting(STATION_ID, request))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.INVALID_PRICE_VALUE);
                    });
        }

        @Test
        @DisplayName("Should throw exception when request is null")
        void createPriceSetting_NullRequest_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> priceSettingService.createPriceSetting(STATION_ID, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should allow zero grace period")
        void createPriceSetting_ZeroGracePeriod_Success() {
            // Given
            CreatePriceSettingRequest request = new CreatePriceSettingRequest(
                    new BigDecimal("3000"),
                    null,
                    new BigDecimal("500"),
                    0, // Zero grace period - penalty starts immediately
                    "No grace period",
                    null);

            doNothing().when(stationService).verifyOwnership(STATION_ID);
            when(priceSettingRepository.findTopByStationIdOrderByVersionDesc(STATION_ID))
                    .thenReturn(Optional.empty());
            when(priceSettingRepository.save(any(PriceSetting.class))).thenReturn(testPriceSetting);
            when(priceSettingDtoConverter.convert(any(PriceSetting.class))).thenReturn(testPriceSettingResponse);

            // When
            priceSettingService.createPriceSetting(STATION_ID, request);

            // Then
            verify(priceSettingRepository).save(argThat(ps -> ps.getGracePeriodMinutes() == 0));
        }
    }

    // ==================== GET ACTIVE PRICE SETTING TESTS ====================

    @Nested
    @DisplayName("Get Active Price Setting Tests")
    class GetActivePriceSettingTests {

        @Test
        @DisplayName("Should return active pricing for a station")
        void getActivePriceSetting_Exists_ReturnsPricing() {
            // Given
            when(priceSettingRepository.findByStationIdAndIsActiveTrue(STATION_ID))
                    .thenReturn(Optional.of(testPriceSetting));
            when(priceSettingDtoConverter.convert(testPriceSetting)).thenReturn(testPriceSettingResponse);

            // When
            PriceSettingResponse result = priceSettingService.getActivePriceSetting(STATION_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isActive()).isTrue();
            assertThat(result.chargingRatePerKwh()).isEqualTo(new BigDecimal("3000"));
        }

        @Test
        @DisplayName("Should throw exception when no active pricing exists")
        void getActivePriceSetting_NotExists_ThrowsNotFound() {
            // Given
            when(priceSettingRepository.findByStationIdAndIsActiveTrue(STATION_ID))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> priceSettingService.getActivePriceSetting(STATION_ID))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.PRICE_SETTING_NOT_FOUND);
                    });
        }
    }

    // ==================== GET PRICING HISTORY TESTS ====================

    @Nested
    @DisplayName("Get Pricing History Tests")
    class GetPricingHistoryTests {

        @Test
        @DisplayName("Should return pricing history ordered by version desc")
        void getPricingHistory_HasHistory_ReturnsOrderedList() {
            // Given
            PriceSetting v1 = new PriceSetting();
            v1.setVersion(1);
            v1.setIsActive(false);

            PriceSetting v2 = new PriceSetting();
            v2.setVersion(2);
            v2.setIsActive(true);

            List<PriceSetting> history = List.of(v2, v1); // Desc order
            List<PriceSettingResponse> responses = List.of(
                    PriceSettingResponse.builder().version(2).isActive(true).build(),
                    PriceSettingResponse.builder().version(1).isActive(false).build());

            doNothing().when(stationService).verifyOwnership(STATION_ID);
            when(priceSettingRepository.findByStationIdOrderByVersionDesc(STATION_ID))
                    .thenReturn(history);
            when(priceSettingDtoConverter.convert(history)).thenReturn(responses);

            // When
            List<PriceSettingResponse> result = priceSettingService.getPricingHistory(STATION_ID);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).version()).isEqualTo(2);
            assertThat(result.get(1).version()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return empty list when no pricing history")
        void getPricingHistory_NoHistory_ReturnsEmptyList() {
            // Given
            doNothing().when(stationService).verifyOwnership(STATION_ID);
            when(priceSettingRepository.findByStationIdOrderByVersionDesc(STATION_ID))
                    .thenReturn(List.of());
            when(priceSettingDtoConverter.convert(List.<PriceSetting>of()))
                    .thenReturn(List.of());

            // When
            List<PriceSettingResponse> result = priceSettingService.getPricingHistory(STATION_ID);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception when user is not owner")
        void getPricingHistory_NotOwner_ThrowsForbidden() {
            // Given
            doThrow(new AppException(ErrorCode.STATION_NOT_OWNED))
                    .when(stationService).verifyOwnership(STATION_ID);

            // When & Then
            assertThatThrownBy(() -> priceSettingService.getPricingHistory(STATION_ID))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.STATION_NOT_OWNED);
                    });
        }
    }

    // ==================== CALCULATE IDLE FEE TESTS ====================

    @Nested
    @DisplayName("Calculate Idle Fee Tests")
    class CalculateIdleFeeTests {

        @Test
        @DisplayName("Should return zero fee when within grace period")
        void calculateIdleFee_WithinGracePeriod_ReturnsZero() {
            // Given - 20 minutes overstay, 30 minutes grace
            when(priceSettingRepository.findByStationIdAndIsActiveTrue(STATION_ID))
                    .thenReturn(Optional.of(testPriceSetting));

            // When
            BigDecimal fee = priceSettingService.calculateIdleFee(STATION_ID, 20);

            // Then
            assertThat(fee).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should return zero fee at exactly grace period boundary")
        void calculateIdleFee_ExactlyAtGrace_ReturnsZero() {
            // Given - 30 minutes overstay, 30 minutes grace = 0 billable
            when(priceSettingRepository.findByStationIdAndIsActiveTrue(STATION_ID))
                    .thenReturn(Optional.of(testPriceSetting));

            // When
            BigDecimal fee = priceSettingService.calculateIdleFee(STATION_ID, 30);

            // Then
            assertThat(fee).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should calculate fee after grace period")
        void calculateIdleFee_AfterGracePeriod_CalculatesFee() {
            // Given - 45 minutes overstay, 30 minutes grace = 15 billable * 500 = 7500
            when(priceSettingRepository.findByStationIdAndIsActiveTrue(STATION_ID))
                    .thenReturn(Optional.of(testPriceSetting));

            // When
            BigDecimal fee = priceSettingService.calculateIdleFee(STATION_ID, 45);

            // Then
            assertThat(fee).isEqualByComparingTo(new BigDecimal("7500"));
        }

        @Test
        @DisplayName("Should calculate fee starting at 1 minute after grace")
        void calculateIdleFee_OneMinuteAfterGrace_CalculatesOneMinute() {
            // Given - 31 minutes overstay, 30 minutes grace = 1 billable * 500 = 500
            when(priceSettingRepository.findByStationIdAndIsActiveTrue(STATION_ID))
                    .thenReturn(Optional.of(testPriceSetting));

            // When
            BigDecimal fee = priceSettingService.calculateIdleFee(STATION_ID, 31);

            // Then
            assertThat(fee).isEqualByComparingTo(new BigDecimal("500"));
        }

        @Test
        @DisplayName("Should return zero when penalty rate is null")
        void calculateIdleFee_NullPenaltyRate_ReturnsZero() {
            // Given
            PriceSetting noPenalty = new PriceSetting();
            noPenalty.setIdlePenaltyPerMinute(null);
            noPenalty.setGracePeriodMinutes(30);

            when(priceSettingRepository.findByStationIdAndIsActiveTrue(STATION_ID))
                    .thenReturn(Optional.of(noPenalty));

            // When
            BigDecimal fee = priceSettingService.calculateIdleFee(STATION_ID, 60);

            // Then
            assertThat(fee).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should return zero when penalty rate is zero")
        void calculateIdleFee_ZeroPenaltyRate_ReturnsZero() {
            // Given
            PriceSetting zeroPenalty = new PriceSetting();
            zeroPenalty.setIdlePenaltyPerMinute(BigDecimal.ZERO);
            zeroPenalty.setGracePeriodMinutes(30);

            when(priceSettingRepository.findByStationIdAndIsActiveTrue(STATION_ID))
                    .thenReturn(Optional.of(zeroPenalty));

            // When
            BigDecimal fee = priceSettingService.calculateIdleFee(STATION_ID, 60);

            // Then
            assertThat(fee).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should calculate fee with zero grace period (penalty starts immediately)")
        void calculateIdleFee_ZeroGracePeriod_ChargingFromMinuteOne() {
            // Given - 10 minutes overstay, 0 grace = 10 billable * 500 = 5000
            PriceSetting zeroGrace = new PriceSetting();
            zeroGrace.setIdlePenaltyPerMinute(new BigDecimal("500"));
            zeroGrace.setGracePeriodMinutes(0);

            when(priceSettingRepository.findByStationIdAndIsActiveTrue(STATION_ID))
                    .thenReturn(Optional.of(zeroGrace));

            // When
            BigDecimal fee = priceSettingService.calculateIdleFee(STATION_ID, 10);

            // Then
            assertThat(fee).isEqualByComparingTo(new BigDecimal("5000"));
        }

        @Test
        @DisplayName("Should throw exception when no active pricing exists")
        void calculateIdleFee_NoPricing_ThrowsNotFound() {
            // Given
            when(priceSettingRepository.findByStationIdAndIsActiveTrue(STATION_ID))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> priceSettingService.calculateIdleFee(STATION_ID, 45))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.PRICE_SETTING_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("Should return zero fee when overstay is zero minutes")
        void calculateIdleFee_ZeroOverstay_ReturnsZero() {
            // Given
            when(priceSettingRepository.findByStationIdAndIsActiveTrue(STATION_ID))
                    .thenReturn(Optional.of(testPriceSetting));

            // When
            BigDecimal fee = priceSettingService.calculateIdleFee(STATION_ID, 0);

            // Then
            assertThat(fee).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}
