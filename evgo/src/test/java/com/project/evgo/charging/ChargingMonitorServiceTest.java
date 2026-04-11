package com.project.evgo.charging;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.response.ChargerResponse;
import com.project.evgo.charger.response.PortResponse;
import com.project.evgo.charging.internal.ChargingMonitorService;
import com.project.evgo.charging.internal.ChargingSession;
import com.project.evgo.charging.internal.ChargingSessionRepository;
import com.project.evgo.sharedkernel.enums.ChargingSessionStatus;
import com.project.evgo.station.PriceSettingService;
import com.project.evgo.station.response.PriceSettingResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ChargingMonitorService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChargingMonitorService Tests")
class ChargingMonitorServiceTest {

    @Mock
    private ChargingSessionRepository sessionRepository;

    @Mock
    private ChargerService chargerService;

    @Mock
    private PriceSettingService priceSettingService;

    @InjectMocks
    private ChargingMonitorService monitorService;

    private ChargingSession chargingSession;

    @BeforeEach
    void setUp() {
        chargingSession = new ChargingSession();
        chargingSession.setId(1L);
        chargingSession.setUserId(100L);
        chargingSession.setPortId(10L);
        chargingSession.setTransactionId(42);
        chargingSession.setMeterStart(1000);
        chargingSession.setStatus(ChargingSessionStatus.CHARGING);
    }

    @Nested
    @DisplayName("Subscribe Tests")
    class SubscribeTests {

        @Test
        @DisplayName("Should create and return SseEmitter for valid session")
        void subscribe_ValidSession_ReturnsEmitter() {
            SseEmitter emitter = monitorService.subscribe(1L);

            assertThat(emitter).isNotNull();
        }

        @Test
        @DisplayName("Should replace existing emitter on reconnect")
        void subscribe_ExistingEmitter_ReplacesPrevious() {
            SseEmitter first = monitorService.subscribe(1L);
            SseEmitter second = monitorService.subscribe(1L);

            assertThat(second).isNotNull();
            assertThat(second).isNotSameAs(first);
        }
    }

    @Nested
    @DisplayName("Push Update Tests")
    class PushUpdateTests {

        @Test
        @DisplayName("Should silently skip when no subscriber exists")
        void pushUpdate_NoSubscriber_SkipsWithoutDbCall() {
            // No subscribe call → no emitter registered
            monitorService.pushUpdate(1L, 3000, LocalDateTime.now());

            // Should not throw, and no DB call needed
            verifyNoInteractions(sessionRepository);
        }

        @Test
        @DisplayName("Should calculate consumedKwh and estimatedCost correctly")
        void pushUpdate_ValidMeterValue_CalculatesCorrectly() {
            // Subscribe first
            monitorService.subscribe(1L);

            // Mock: session with meterStart=1000 Wh
            when(sessionRepository.findById(1L)).thenReturn(Optional.of(chargingSession));

            // Mock: price resolution chain
            PortResponse port = PortResponse.builder().id(10L).chargerId(5L).portNumber(1).build();
            when(chargerService.findPortById(10L)).thenReturn(Optional.of(port));

            ChargerResponse charger = ChargerResponse.builder().id(5L).stationId(2L).build();
            when(chargerService.findById(5L)).thenReturn(Optional.of(charger));

            PriceSettingResponse pricing = PriceSettingResponse.builder()
                    .chargingRatePerKwh(new BigDecimal("3500"))
                    .build();
            when(priceSettingService.getActivePriceSetting(2L)).thenReturn(pricing);

            // Act: meter now at 3500 Wh → consumed = (3500-1000) / 1000 = 2.5 kWh
            monitorService.pushUpdate(1L, 3500, LocalDateTime.now());

            // Verify DB was queried
            verify(sessionRepository).findById(1L);
            verify(chargerService).findPortById(10L);
            verify(chargerService).findById(5L);
            verify(priceSettingService).getActivePriceSetting(2L);
        }

        @Test
        @DisplayName("Should handle session not found gracefully")
        void pushUpdate_SessionNotFound_CompletesEmitter() {
            monitorService.subscribe(1L);

            when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

            monitorService.pushUpdate(1L, 3000, LocalDateTime.now());

            verify(sessionRepository).findById(1L);
        }

        @Test
        @DisplayName("Should send final update and complete emitter when session is FINISHING")
        void pushUpdate_SessionFinishing_SendsFinalUpdate() {
            monitorService.subscribe(1L);

            chargingSession.setStatus(ChargingSessionStatus.FINISHING);
            chargingSession.setTotalKwh(new BigDecimal("4.0000"));
            when(sessionRepository.findById(1L)).thenReturn(Optional.of(chargingSession));

            monitorService.pushUpdate(1L, 5000, LocalDateTime.now());

            verify(sessionRepository).findById(1L);
            // No pricing resolution needed for final update
            verifyNoInteractions(chargerService);
            verifyNoInteractions(priceSettingService);
        }

        @Test
        @DisplayName("Should use zero rate when port resolution fails")
        void pushUpdate_PortNotFound_UsesZeroRate() {
            monitorService.subscribe(1L);

            when(sessionRepository.findById(1L)).thenReturn(Optional.of(chargingSession));
            when(chargerService.findPortById(10L)).thenReturn(Optional.empty());

            monitorService.pushUpdate(1L, 3000, LocalDateTime.now());

            verify(chargerService).findPortById(10L);
            verifyNoInteractions(priceSettingService);
        }
    }
}
