package com.project.evgo.payment.internal;

import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.response.ChargerResponse;
import com.project.evgo.charger.response.PortResponse;
import com.project.evgo.sharedkernel.enums.InvoicePurpose;
import com.project.evgo.sharedkernel.enums.InvoiceStatus;
import com.project.evgo.sharedkernel.events.CableUnpluggedEvent;
import com.project.evgo.station.PriceSettingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdleFeeListenerTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ChargerService chargerService;

    @Mock
    private PriceSettingService priceSettingService;

    @InjectMocks
    private IdleFeeListener idleFeeListener;

    @Captor
    private ArgumentCaptor<Invoice> invoiceCaptor;

    private CableUnpluggedEvent validEvent;
    private final Long sessionId = 100L;
    private final Long portId = 200L;
    private final Long chargerId = 300L;
    private final Long stationId = 400L;
    private final Long userId = 500L;

    @BeforeEach
    void setUp() {
        LocalDateTime idleStartTime = LocalDateTime.of(2023, 1, 1, 10, 0);
        LocalDateTime unpluggedTime = LocalDateTime.of(2023, 1, 1, 10, 30); // 30 minutes idle
        validEvent = new CableUnpluggedEvent(sessionId, portId, userId, idleStartTime, unpluggedTime);
    }

    @Test
    void onCableUnplugged_shouldCreateInvoice_whenIdleFeeGreaterThanZero() {
        // Arrange
        PortResponse portResponse = new PortResponse();
        portResponse.setId(portId);
        portResponse.setChargerId(chargerId);

        ChargerResponse chargerResponse = new ChargerResponse();
        chargerResponse.setId(chargerId);
        chargerResponse.setStationId(stationId);

        when(chargerService.findPortById(portId)).thenReturn(Optional.of(portResponse));
        when(chargerService.findById(chargerId)).thenReturn(Optional.of(chargerResponse));
        when(priceSettingService.calculateIdleFee(stationId, 30)).thenReturn(BigDecimal.valueOf(15000));

        // Act
        idleFeeListener.onCableUnplugged(validEvent);

        // Assert
        verify(invoiceRepository).save(invoiceCaptor.capture());
        Invoice savedInvoice = invoiceCaptor.getValue();
        assertThat(savedInvoice.getChargingSessionId()).isEqualTo(sessionId);
        assertThat(savedInvoice.getUserId()).isEqualTo(userId);
        assertThat(savedInvoice.getTotalCost()).isEqualByComparingTo("15000");
        assertThat(savedInvoice.getPurpose()).isEqualTo(InvoicePurpose.IDLE_FEE);
        assertThat(savedInvoice.getStatus()).isEqualTo(InvoiceStatus.PENDING);
        assertThat(savedInvoice.getNumber()).startsWith("INV-IDLE-");
    }

    @Test
    void onCableUnplugged_shouldNotCreateInvoice_whenTimesAreNull() {
        // Arrange
        CableUnpluggedEvent invalidEvent = new CableUnpluggedEvent(sessionId, portId, userId, null, null);

        // Act
        idleFeeListener.onCableUnplugged(invalidEvent);

        // Assert
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void onCableUnplugged_shouldNotCreateInvoice_whenIdleMinutesIsZeroOrNegative() {
        // Arrange
        LocalDateTime idleStartTime = LocalDateTime.of(2023, 1, 1, 10, 30);
        LocalDateTime unpluggedTime = LocalDateTime.of(2023, 1, 1, 10, 0); // Negative idle time
        CableUnpluggedEvent noIdleEvent = new CableUnpluggedEvent(sessionId, portId, userId, idleStartTime, unpluggedTime);

        // Act
        idleFeeListener.onCableUnplugged(noIdleEvent);

        // Assert
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void onCableUnplugged_shouldNotCreateInvoice_whenPortNotFound() {
        // Arrange
        when(chargerService.findPortById(portId)).thenReturn(Optional.empty());

        // Act
        idleFeeListener.onCableUnplugged(validEvent);

        // Assert
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void onCableUnplugged_shouldNotCreateInvoice_whenChargerNotFound() {
        // Arrange
        PortResponse portResponse = new PortResponse();
        portResponse.setId(portId);
        portResponse.setChargerId(chargerId);

        when(chargerService.findPortById(portId)).thenReturn(Optional.of(portResponse));
        when(chargerService.findById(chargerId)).thenReturn(Optional.empty());

        // Act
        idleFeeListener.onCableUnplugged(validEvent);

        // Assert
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void onCableUnplugged_shouldNotCreateInvoice_whenCalculateIdleFeeThrowsException() {
        // Arrange
        PortResponse portResponse = new PortResponse();
        portResponse.setId(portId);
        portResponse.setChargerId(chargerId);

        ChargerResponse chargerResponse = new ChargerResponse();
        chargerResponse.setId(chargerId);
        chargerResponse.setStationId(stationId);

        when(chargerService.findPortById(portId)).thenReturn(Optional.of(portResponse));
        when(chargerService.findById(chargerId)).thenReturn(Optional.of(chargerResponse));
        when(priceSettingService.calculateIdleFee(stationId, 30)).thenThrow(new RuntimeException("Pricing error"));

        // Act
        idleFeeListener.onCableUnplugged(validEvent);

        // Assert
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void onCableUnplugged_shouldNotCreateInvoice_whenIdleFeeIsZero() {
        // Arrange
        PortResponse portResponse = new PortResponse();
        portResponse.setId(portId);
        portResponse.setChargerId(chargerId);

        ChargerResponse chargerResponse = new ChargerResponse();
        chargerResponse.setId(chargerId);
        chargerResponse.setStationId(stationId);

        when(chargerService.findPortById(portId)).thenReturn(Optional.of(portResponse));
        when(chargerService.findById(chargerId)).thenReturn(Optional.of(chargerResponse));
        when(priceSettingService.calculateIdleFee(stationId, 30)).thenReturn(BigDecimal.ZERO);

        // Act
        idleFeeListener.onCableUnplugged(validEvent);

        // Assert
        verify(invoiceRepository, never()).save(any());
    }
}
