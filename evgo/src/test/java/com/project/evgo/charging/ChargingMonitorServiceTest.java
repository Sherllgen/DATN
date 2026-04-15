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

    /**
     * Helper: mock the full price resolution chain and subscribe with portId.
     * Rate is resolved ONCE at subscribe time and cached.
     */
    private void subscribeWithPricingMocks(Long sessionId, Long portId) {
        PortResponse port = PortResponse.builder().id(portId).chargerId(5L).portNumber(1).build();
        when(chargerService.findPortById(portId)).thenReturn(Optional.of(port));

        ChargerResponse charger = ChargerResponse.builder().id(5L).stationId(2L).build();
        when(chargerService.findById(5L)).thenReturn(Optional.of(charger));

        PriceSettingResponse pricing = PriceSettingResponse.builder()
                .chargingRatePerKwh(new BigDecimal("3500"))
                .build();
        when(priceSettingService.getActivePriceSetting(2L)).thenReturn(pricing);

        monitorService.subscribe(sessionId, portId);
    }

    @Nested
    @DisplayName("Subscribe Tests")
    class SubscribeTests {

        @Test
        @DisplayName("Should create and return SseEmitter for valid session")
        void subscribe_ValidSession_ReturnsEmitter() {
            PortResponse port = PortResponse.builder().id(10L).chargerId(5L).portNumber(1).build();
            when(chargerService.findPortById(10L)).thenReturn(Optional.of(port));
            ChargerResponse charger = ChargerResponse.builder().id(5L).stationId(2L).build();
            when(chargerService.findById(5L)).thenReturn(Optional.of(charger));
            PriceSettingResponse pricing = PriceSettingResponse.builder()
                    .chargingRatePerKwh(new BigDecimal("3500")).build();
            when(priceSettingService.getActivePriceSetting(2L)).thenReturn(pricing);

            SseEmitter emitter = monitorService.subscribe(1L, 10L);

            assertThat(emitter).isNotNull();
            // Rate resolution should happen exactly once during subscribe
            verify(chargerService).findPortById(10L);
            verify(chargerService).findById(5L);
            verify(priceSettingService).getActivePriceSetting(2L);
        }

        @Test
        @DisplayName("Should replace existing emitter on reconnect")
        void subscribe_ExistingEmitter_ReplacesPrevious() {
            // First subscribe
            subscribeWithPricingMocks(1L, 10L);
            SseEmitter first = monitorService.subscribe(1L, 10L);

            // Reset mocks for second subscribe
            reset(chargerService, priceSettingService);
            subscribeWithPricingMocks(1L, 10L);

            SseEmitter second = monitorService.subscribe(1L, 10L);

            assertThat(second).isNotNull();
            assertThat(second).isNotSameAs(first);
        }

        @Test
        @DisplayName("Should fall back to DB lookup when using single-arg subscribe")
        void subscribe_SingleArg_LooksUpPortFromDb() {
            when(sessionRepository.findById(1L)).thenReturn(Optional.of(chargingSession));

            PortResponse port = PortResponse.builder().id(10L).chargerId(5L).portNumber(1).build();
            when(chargerService.findPortById(10L)).thenReturn(Optional.of(port));
            ChargerResponse charger = ChargerResponse.builder().id(5L).stationId(2L).build();
            when(chargerService.findById(5L)).thenReturn(Optional.of(charger));
            PriceSettingResponse pricing = PriceSettingResponse.builder()
                    .chargingRatePerKwh(new BigDecimal("3500")).build();
            when(priceSettingService.getActivePriceSetting(2L)).thenReturn(pricing);

            SseEmitter emitter = monitorService.subscribe(1L);

            assertThat(emitter).isNotNull();
            verify(sessionRepository).findById(1L);
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
        @DisplayName("Should use cached rate instead of querying DB on every tick")
        void pushUpdate_ValidMeterValue_UsesCachedRate() {
            // Subscribe (resolves rate once)
            subscribeWithPricingMocks(1L, 10L);

            // Reset interaction counters to prove pushUpdate doesn't re-query
            reset(chargerService, priceSettingService);

            // Mock: session with meterStart=1000 Wh
            when(sessionRepository.findById(1L)).thenReturn(Optional.of(chargingSession));

            // Act: meter now at 3500 Wh → consumed = (3500-1000) / 1000 = 2.5 kWh
            monitorService.pushUpdate(1L, 3500, LocalDateTime.now());

            // Verify: only session DB query, NO pricing queries (rate is cached)
            verify(sessionRepository).findById(1L);
            verifyNoInteractions(chargerService);
            verifyNoInteractions(priceSettingService);
        }

        @Test
        @DisplayName("Should handle session not found gracefully")
        void pushUpdate_SessionNotFound_CompletesEmitter() {
            subscribeWithPricingMocks(1L, 10L);
            reset(chargerService, priceSettingService);

            when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

            monitorService.pushUpdate(1L, 3000, LocalDateTime.now());

            verify(sessionRepository).findById(1L);
        }

        @Test
        @DisplayName("Should send final update using cached rate when session is FINISHING")
        void pushUpdate_SessionFinishing_SendsFinalUpdate() {
            subscribeWithPricingMocks(1L, 10L);
            reset(chargerService, priceSettingService);

            chargingSession.setStatus(ChargingSessionStatus.FINISHING);
            chargingSession.setTotalKwh(new BigDecimal("4.0000"));
            when(sessionRepository.findById(1L)).thenReturn(Optional.of(chargingSession));

            monitorService.pushUpdate(1L, 5000, LocalDateTime.now());

            // Verify: only session query, rate comes from cache
            verify(sessionRepository).findById(1L);
            verifyNoInteractions(chargerService);
            verifyNoInteractions(priceSettingService);
        }

        @Test
        @DisplayName("Should use zero rate when port resolution fails during subscribe")
        void subscribe_PortNotFound_CachesZeroRate() {
            when(chargerService.findPortById(10L)).thenReturn(Optional.empty());

            monitorService.subscribe(1L, 10L);

            // Reset and do a pushUpdate — should use the cached zero rate
            reset(chargerService, priceSettingService);
            when(sessionRepository.findById(1L)).thenReturn(Optional.of(chargingSession));

            monitorService.pushUpdate(1L, 3000, LocalDateTime.now());

            verify(chargerService, never()).findPortById(anyLong());
            verifyNoInteractions(priceSettingService);
        }

        @Test
        @DisplayName("Should fallback to zero rate when pricing service throws exception")
        void resolveChargingRate_PricingServiceFails_ReturnsZero() {
            // Subscribe with a pricing chain that throws on the last step
            PortResponse port = PortResponse.builder().id(10L).chargerId(5L).portNumber(1).build();
            when(chargerService.findPortById(10L)).thenReturn(Optional.of(port));

            ChargerResponse charger = ChargerResponse.builder().id(5L).stationId(2L).build();
            when(chargerService.findById(5L)).thenReturn(Optional.of(charger));

            when(priceSettingService.getActivePriceSetting(2L))
                    .thenThrow(new RuntimeException("DB connection lost"));

            // Subscribe should NOT throw — fallback catches and caches ZERO
            SseEmitter emitter = monitorService.subscribe(1L, 10L);
            assertThat(emitter).isNotNull();

            // Reset and push an update — the cached zero rate should be used
            reset(chargerService, priceSettingService);
            when(sessionRepository.findById(1L)).thenReturn(Optional.of(chargingSession));

            monitorService.pushUpdate(1L, 3000, LocalDateTime.now());

            // Verify: pricing chain NOT called again (zero was cached at subscribe time)
            verifyNoInteractions(chargerService);
            verifyNoInteractions(priceSettingService);
        }
    }
}
