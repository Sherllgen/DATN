package com.project.evgo.charging;

import com.project.evgo.booking.BookingService;
import com.project.evgo.booking.response.BookingResponse;
import com.project.evgo.charging.internal.ChargingSession;
import com.project.evgo.charging.internal.ChargingSessionDtoConverter;
import com.project.evgo.charging.internal.ChargingSessionRepository;
import com.project.evgo.charging.internal.ChargingServiceImpl;
import com.project.evgo.charging.request.StartChargingRequest;
import com.project.evgo.charging.request.StopChargingRequest;
import com.project.evgo.charging.response.ChargingSessionResponse;
import com.project.evgo.payment.InvoiceService;
import com.project.evgo.sharedkernel.enums.ChargingSessionStatus;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChargingServiceTest {

    @Mock
    private ChargingSessionRepository sessionRepository;
    @Mock
    private ChargingSessionDtoConverter converter;
    @Mock
    private InvoiceService invoiceService;
    @Mock
    private BookingService bookingService;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ChargingServiceImpl service;

    @Test
    @DisplayName("startCharging with valid request should return response")
    void startCharging_ValidRequest_ReturnsResponse() {
        Long userId = 1L;
        Long portId = 100L;
        StartChargingRequest request = new StartChargingRequest(null, portId);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        when(invoiceService.hasUnpaidInvoices(userId)).thenReturn(false);

        ChargingSession savedSession = new ChargingSession();
        savedSession.setId(10L);
        when(sessionRepository.save(any(ChargingSession.class))).thenReturn(savedSession);

        ChargingSessionResponse response = new ChargingSessionResponse();
        response.setId(10L);
        when(converter.convert(savedSession)).thenReturn(response);

        ChargingSessionResponse result = service.startCharging(request, userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        verify(sessionRepository).save(any(ChargingSession.class));
        verify(eventPublisher).publishEvent(any(SendRemoteStartCommandEvent.class));
    }

    @Test
    @DisplayName("startCharging with unpaid invoice should throw AppException")
    void startCharging_UnpaidInvoice_ThrowsAppException() {
        Long userId = 1L;
        StartChargingRequest request = new StartChargingRequest(null, 100L);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        when(invoiceService.hasUnpaidInvoices(userId)).thenReturn(true);

        assertThatThrownBy(() -> service.startCharging(request, userId))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNPAID_INVOICE_EXISTS);
    }
    
    @Test
    @DisplayName("startCharging with spam request should throw AppException")
    void startCharging_SpamRequest_ThrowsAppException() {
        Long userId = 1L;
        StartChargingRequest request = new StartChargingRequest(null, 100L);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false);

        assertThatThrownBy(() -> service.startCharging(request, userId))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);
    }
    
    @Test
    @DisplayName("stopCharging with valid request publishes event")
    void stopCharging_ValidRequest_PublishesEvent() {
        Long userId = 1L;
        Long sessionId = 10L;
        StopChargingRequest request = new StopChargingRequest(sessionId);
        
        ChargingSession session = new ChargingSession();
        session.setId(sessionId);
        session.setUserId(userId);
        session.setStatus(ChargingSessionStatus.CHARGING);
        
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        
        service.stopCharging(request, userId);
        
        verify(eventPublisher).publishEvent(any(SendRemoteStopCommandEvent.class));
    }
    
    @Test
    @DisplayName("stopCharging when not owned throws AppException")
    void stopCharging_NotOwned_ThrowsAppException() {
        Long userId = 1L;
        Long sessionId = 10L;
        StopChargingRequest request = new StopChargingRequest(sessionId);
        
        ChargingSession session = new ChargingSession();
        session.setId(sessionId);
        session.setUserId(2L); // Different user
        
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        
        assertThatThrownBy(() -> service.stopCharging(request, userId))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_NOT_OWNED);
    }
    
    @Test
    @DisplayName("stopCharging when invalid status throws AppException")
    void stopCharging_InvalidStatus_ThrowsAppException() {
        Long userId = 1L;
        Long sessionId = 10L;
        StopChargingRequest request = new StopChargingRequest(sessionId);
        
        ChargingSession session = new ChargingSession();
        session.setId(sessionId);
        session.setUserId(userId);
        session.setStatus(ChargingSessionStatus.COMPLETED); // Already completed
        
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        
        assertThatThrownBy(() -> service.stopCharging(request, userId))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_SESSION_STATUS);
    }
}
