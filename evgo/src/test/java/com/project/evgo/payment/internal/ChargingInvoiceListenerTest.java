package com.project.evgo.payment.internal;

import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.response.ChargerResponse;
import com.project.evgo.charger.response.PortResponse;
import com.project.evgo.sharedkernel.enums.InvoicePurpose;
import com.project.evgo.sharedkernel.enums.InvoiceStatus;
import com.project.evgo.sharedkernel.events.ChargingSessionCompletedEvent;
import com.project.evgo.station.PriceSettingService;
import com.project.evgo.station.response.PriceSettingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChargingInvoiceListenerTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ChargerService chargerService;

    @Mock
    private PriceSettingService priceSettingService;

    @InjectMocks
    private ChargingInvoiceListener listener;

    @Captor
    private ArgumentCaptor<Invoice> invoiceCaptor;

    private ChargingSessionCompletedEvent validEvent;
    private final Long sessionId = 100L;
    private final Long portId = 200L;
    private final Long chargerId = 300L;
    private final Long stationId = 400L;
    private final Long userId = 500L;
    private final BigDecimal totalKwh = BigDecimal.valueOf(15.5);

    @BeforeEach
    void setUp() {
        validEvent = new ChargingSessionCompletedEvent(sessionId, userId, portId, totalKwh, "Local", null);
    }

    @Test
    void onChargingSessionCompleted_shouldCreateInvoice() {
        // Arrange
        when(invoiceRepository.findByChargingSessionId(sessionId)).thenReturn(Optional.empty());

        PortResponse portResponse = new PortResponse();
        portResponse.setId(portId);
        portResponse.setChargerId(chargerId);

        ChargerResponse chargerResponse = new ChargerResponse();
        chargerResponse.setId(chargerId);
        chargerResponse.setStationId(stationId);

        when(chargerService.findPortById(portId)).thenReturn(Optional.of(portResponse));
        when(chargerService.findById(chargerId)).thenReturn(Optional.of(chargerResponse));

        PriceSettingResponse priceSetting = PriceSettingResponse.builder()
                .id(1L)
                .stationId(stationId)
                .chargingRatePerKwh(BigDecimal.valueOf(3000))
                .build();
        when(priceSettingService.getActivePriceSetting(stationId)).thenReturn(priceSetting);

        // Act
        listener.onChargingSessionCompleted(validEvent);

        // Assert
        verify(invoiceRepository).save(invoiceCaptor.capture());
        Invoice savedInvoice = invoiceCaptor.getValue();
        
        assertThat(savedInvoice.getChargingSessionId()).isEqualTo(sessionId);
        assertThat(savedInvoice.getUserId()).isEqualTo(userId);
        // 15.5 * 3000 = 46500.00
        assertThat(savedInvoice.getTotalCost()).isEqualByComparingTo("46500.00");
        assertThat(savedInvoice.getPurpose()).isEqualTo(InvoicePurpose.CHARGING_SESSION);
        assertThat(savedInvoice.getStatus()).isEqualTo(InvoiceStatus.PENDING);
        assertThat(savedInvoice.getNumber()).startsWith("INV-CHG-");
    }

    @Test
    void onChargingSessionCompleted_shouldSkipIfInvoiceExists() {
        // Arrange
        when(invoiceRepository.findByChargingSessionId(sessionId)).thenReturn(Optional.of(new Invoice()));

        // Act
        listener.onChargingSessionCompleted(validEvent);

        // Assert
        verify(chargerService, never()).findPortById(any());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void onChargingSessionCompleted_shouldSkipIfPortNotFound() {
        // Arrange
        when(invoiceRepository.findByChargingSessionId(sessionId)).thenReturn(Optional.empty());
        when(chargerService.findPortById(portId)).thenReturn(Optional.empty());

        // Act
        listener.onChargingSessionCompleted(validEvent);

        // Assert
        verify(chargerService, never()).findById(any());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void onChargingSessionCompleted_shouldSkipIfChargerNotFound() {
        // Arrange
        when(invoiceRepository.findByChargingSessionId(sessionId)).thenReturn(Optional.empty());
        
        PortResponse portResponse = new PortResponse();
        portResponse.setId(portId);
        portResponse.setChargerId(chargerId);
        
        when(chargerService.findPortById(portId)).thenReturn(Optional.of(portResponse));
        when(chargerService.findById(chargerId)).thenReturn(Optional.empty());

        // Act
        listener.onChargingSessionCompleted(validEvent);

        // Assert
        verify(priceSettingService, never()).getActivePriceSetting(any());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void onChargingSessionCompleted_shouldSkipIfPriceSettingThrowsException() {
        // Arrange
        when(invoiceRepository.findByChargingSessionId(sessionId)).thenReturn(Optional.empty());

        PortResponse portResponse = new PortResponse();
        portResponse.setId(portId);
        portResponse.setChargerId(chargerId);

        ChargerResponse chargerResponse = new ChargerResponse();
        chargerResponse.setId(chargerId);
        chargerResponse.setStationId(stationId);

        when(chargerService.findPortById(portId)).thenReturn(Optional.of(portResponse));
        when(chargerService.findById(chargerId)).thenReturn(Optional.of(chargerResponse));
        when(priceSettingService.getActivePriceSetting(stationId)).thenThrow(new RuntimeException("No active price"));

        // Act
        listener.onChargingSessionCompleted(validEvent);

        // Assert
        verify(invoiceRepository, never()).save(any());
    }
}
