package com.project.evgo.charging;

import com.project.evgo.booking.BookingService;
import com.project.evgo.booking.response.BookingResponse;
import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.response.PortResponse;
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
import com.project.evgo.sharedkernel.enums.PortStatus;
import com.project.evgo.sharedkernel.events.SendRemoteStartCommandEvent;
import com.project.evgo.sharedkernel.events.SendRemoteStopCommandEvent;
import com.project.evgo.sharedkernel.exceptions.AppException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.project.evgo.sharedkernel.dto.PageResponse;

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
    private ChargerService chargerService;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
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
        when(sessionRepository.existsByPortIdAndStatusIn(anyLong(), anyList())).thenReturn(false);

        ChargingSession savedSession = new ChargingSession();
        savedSession.setId(10L);
        when(sessionRepository.save(any(ChargingSession.class))).thenReturn(savedSession);

        PortResponse port = new PortResponse();
        port.setId(portId);
        port.setChargerId(5L);
        port.setPortNumber(1);
        when(chargerService.findPortById(portId)).thenReturn(Optional.of(port));

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
    @DisplayName("stopCharging with valid CHARGING session publishes RemoteStop event")
    void stopCharging_ValidRequest_PublishesEvent() {
        Long userId = 1L;
        Long sessionId = 10L;
        StopChargingRequest request = new StopChargingRequest(sessionId);
        
        ChargingSession session = new ChargingSession();
        session.setId(sessionId);
        session.setUserId(userId);
        session.setPortId(100L);
        session.setStatus(ChargingSessionStatus.CHARGING);
        session.setTransactionId(1001); // Has transactionId → should send RemoteStop
        
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        PortResponse port = new PortResponse();
        port.setId(100L);
        port.setChargerId(5L);
        port.setPortNumber(1);
        when(chargerService.findPortById(100L)).thenReturn(Optional.of(port));
        
        service.stopCharging(request, userId);
        
        verify(eventPublisher).publishEvent(any(SendRemoteStopCommandEvent.class));
    }

    @Test
    @DisplayName("stopCharging PREPARING session without transactionId interrupts locally")
    void stopCharging_PreparingNoTransaction_InterruptsLocally() {
        Long userId = 1L;
        Long sessionId = 10L;
        StopChargingRequest request = new StopChargingRequest(sessionId);

        ChargingSession session = new ChargingSession();
        session.setId(sessionId);
        session.setUserId(userId);
        session.setPortId(100L);
        session.setStatus(ChargingSessionStatus.PREPARING);
        session.setTransactionId(null); // No transactionId yet

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        service.stopCharging(request, userId);

        // Should NOT publish RemoteStop event
        verify(eventPublisher, never()).publishEvent(any(SendRemoteStopCommandEvent.class));
        // Should save session as INTERRUPTED
        verify(sessionRepository).save(session);
        assertThat(session.getStatus()).isEqualTo(ChargingSessionStatus.INTERRUPTED);
        assertThat(session.getEndTime()).isNotNull();
        // Should release the port
        verify(chargerService).internalUpdatePortStatus(100L, PortStatus.AVAILABLE);
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

    @Test
    @DisplayName("startCharging when port is not found should throw AppException")
    void startCharging_PortNotFound_ThrowsAppException() {
        Long userId = 1L;
        Long portId = 999L;
        StartChargingRequest request = new StartChargingRequest(null, portId);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        when(invoiceService.hasUnpaidInvoices(userId)).thenReturn(false);
        when(sessionRepository.existsByPortIdAndStatusIn(anyLong(), anyList())).thenReturn(false);

        ChargingSession savedSession = new ChargingSession();
        savedSession.setId(10L);
        when(sessionRepository.save(any(ChargingSession.class))).thenReturn(savedSession);

        // Port resolution returns empty
        when(chargerService.findPortById(portId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.startCharging(request, userId))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND);

        // Redis lock should be cleaned up on failure
        verify(redisTemplate).delete(eq("charging:start:" + userId + ":" + portId));
    }

    @Test
    @DisplayName("stopCharging with SUSPENDED_EV session should publish RemoteStop event")
    void stopCharging_SessionIsSuspendedEV_Success() {
        Long userId = 1L;
        Long sessionId = 10L;
        StopChargingRequest request = new StopChargingRequest(sessionId);

        ChargingSession session = new ChargingSession();
        session.setId(sessionId);
        session.setUserId(userId);
        session.setPortId(100L);
        session.setStatus(ChargingSessionStatus.SUSPENDED_EV);
        session.setTransactionId(1001);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        PortResponse port = new PortResponse();
        port.setId(100L);
        port.setChargerId(5L);
        port.setPortNumber(1);
        when(chargerService.findPortById(100L)).thenReturn(Optional.of(port));

        service.stopCharging(request, userId);

        verify(eventPublisher).publishEvent(any(SendRemoteStopCommandEvent.class));
        // Session should NOT be locally interrupted (it has a transactionId)
        verify(sessionRepository, never()).save(any(ChargingSession.class));
    }

    @Test
    @DisplayName("startCharging with valid booking should succeed")
    void startCharging_ValidBooking_Success() {
        Long userId = 1L;
        Long portId = 100L;
        Long bookingId = 50L;
        StartChargingRequest request = new StartChargingRequest(bookingId, portId);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        when(invoiceService.hasUnpaidInvoices(userId)).thenReturn(false);
        when(sessionRepository.existsByPortIdAndStatusIn(anyLong(), anyList())).thenReturn(false);

        BookingResponse booking = BookingResponse.builder().id(bookingId).userId(userId).build();
        when(bookingService.findById(bookingId)).thenReturn(Optional.of(booking));

        ChargingSession savedSession = new ChargingSession();
        savedSession.setId(10L);
        when(sessionRepository.save(any(ChargingSession.class))).thenReturn(savedSession);

        PortResponse port = PortResponse.builder().id(portId).chargerId(5L).portNumber(1).build();
        when(chargerService.findPortById(portId)).thenReturn(Optional.of(port));

        ChargingSessionResponse response = ChargingSessionResponse.builder().id(10L).build();
        when(converter.convert(savedSession)).thenReturn(response);

        ChargingSessionResponse result = service.startCharging(request, userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("startCharging with unowned booking should throw FORBIDDEN")
    void startCharging_UnownedBooking_ThrowsForbidden() {
        Long userId = 1L;
        Long portId = 100L;
        Long bookingId = 50L;
        StartChargingRequest request = new StartChargingRequest(bookingId, portId);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        when(invoiceService.hasUnpaidInvoices(userId)).thenReturn(false);
        when(sessionRepository.existsByPortIdAndStatusIn(anyLong(), anyList())).thenReturn(false);

        // Booking belongs to user 2
        BookingResponse booking = BookingResponse.builder().id(bookingId).userId(2L).build();
        when(bookingService.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.startCharging(request, userId))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);

        verify(redisTemplate).delete(eq("charging:start:" + userId + ":" + portId));
    }

    @Test
    @DisplayName("startCharging with non-existent booking should throw NOT_FOUND")
    void startCharging_NonExistentBooking_ThrowsNotFound() {
        Long userId = 1L;
        Long portId = 100L;
        Long bookingId = 50L;
        StartChargingRequest request = new StartChargingRequest(bookingId, portId);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        when(invoiceService.hasUnpaidInvoices(userId)).thenReturn(false);
        when(sessionRepository.existsByPortIdAndStatusIn(anyLong(), anyList())).thenReturn(false);

        when(bookingService.findById(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.startCharging(request, userId))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND);

        verify(redisTemplate).delete(eq("charging:start:" + userId + ":" + portId));
    }

    // ===== Tests for findActiveSession (C-2 optimization) =====

    @Test
    @DisplayName("findActiveSession returns session when active session exists")
    void findActiveSession_ActiveExists_ReturnsSession() {
        Long userId = 1L;
        ChargingSession session = new ChargingSession();
        session.setId(10L);
        session.setUserId(userId);
        session.setStatus(ChargingSessionStatus.CHARGING);

        when(sessionRepository.findFirstByUserIdAndStatusIn(eq(userId), anyCollection()))
                .thenReturn(Optional.of(session));

        ChargingSessionResponse response = ChargingSessionResponse.builder().id(10L).build();
        when(converter.convert(Optional.of(session))).thenReturn(Optional.of(response));

        Optional<ChargingSessionResponse> result = service.findActiveSession(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(10L);
        verify(sessionRepository).findFirstByUserIdAndStatusIn(eq(userId), anyCollection());
    }

    @Test
    @DisplayName("findActiveSession returns empty when no active session")
    void findActiveSession_NoActive_ReturnsEmpty() {
        Long userId = 1L;

        when(sessionRepository.findFirstByUserIdAndStatusIn(eq(userId), anyCollection()))
                .thenReturn(Optional.empty());
        when(converter.convert(Optional.empty())).thenReturn(Optional.empty());

        Optional<ChargingSessionResponse> result = service.findActiveSession(userId);

        assertThat(result).isEmpty();
    }

    // ===== Tests for findSessionHistory (C-1 optimization) =====

    @Test
    @DisplayName("findSessionHistory returns paginated completed sessions")
    void findSessionHistory_Completed_ReturnsPaginated() {
        Long userId = 1L;
        ChargingSession session1 = new ChargingSession();
        session1.setId(10L);
        session1.setStatus(ChargingSessionStatus.COMPLETED);
        ChargingSession session2 = new ChargingSession();
        session2.setId(11L);
        session2.setStatus(ChargingSessionStatus.COMPLETED);

        Page<ChargingSession> page = new org.springframework.data.domain.PageImpl<>(
                java.util.List.of(session1, session2),
                org.springframework.data.domain.PageRequest.of(0, 10),
                2
        );

        when(sessionRepository.findByUserIdAndStatus(eq(userId), eq(ChargingSessionStatus.COMPLETED), any(Pageable.class)))
                .thenReturn(page);

        ChargingSessionResponse resp1 = ChargingSessionResponse.builder().id(10L).build();
        ChargingSessionResponse resp2 = ChargingSessionResponse.builder().id(11L).build();
        when(converter.convert(session1)).thenReturn(resp1);
        when(converter.convert(session2)).thenReturn(resp2);

        PageResponse<ChargingSessionResponse> result = service.findSessionHistory(userId, ChargingSessionStatus.COMPLETED, 0, 10);

        assertThat(result.content()).hasSize(2);
        assertThat(result.page()).isEqualTo(0);
        assertThat(result.totalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("findSessionHistory returns empty page when no sessions match")
    void findSessionHistory_NoSessions_ReturnsEmptyPage() {
        Long userId = 1L;

        Page<ChargingSession> emptyPage = new org.springframework.data.domain.PageImpl<>(
                java.util.List.of(),
                org.springframework.data.domain.PageRequest.of(0, 10),
                0
        );

        when(sessionRepository.findByUserIdAndStatus(eq(userId), eq(ChargingSessionStatus.COMPLETED), any(Pageable.class)))
                .thenReturn(emptyPage);

        PageResponse<ChargingSessionResponse> result = service.findSessionHistory(userId, ChargingSessionStatus.COMPLETED, 0, 10);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(0);
    }
}
