package com.project.evgo.charging.internal;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.project.evgo.booking.BookingService;
import com.project.evgo.booking.response.BookingResponse;
import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.response.PortResponse;
import com.project.evgo.charging.ChargingService;
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

/**
 * Implementation of ChargingService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChargingServiceImpl implements ChargingService {

    private final ChargingSessionRepository sessionRepository;
    private final ChargingSessionDtoConverter converter;
    private final InvoiceService invoiceService;
    private final BookingService bookingService;
    private final ChargerService chargerService;
    private final StringRedisTemplate redisTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Optional<ChargingSessionResponse> findById(Long id) {
        return converter.convert(sessionRepository.findById(id));
    }

    @Override
    public List<ChargingSessionResponse> findByUserId(Long userId) {
        return converter.convert(sessionRepository.findByUserId(userId));
    }

    @Override
    @Transactional
    public ChargingSessionResponse startCharging(StartChargingRequest request, Long userId) {
        String redisKey = "charging:start:" + userId + ":" + request.getPortId();
        Boolean isAbsent = redisTemplate.opsForValue().setIfAbsent(redisKey, "LOCKED", Duration.ofSeconds(10));
        if (Boolean.FALSE.equals(isAbsent)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Please wait before trying again");
        }

        try {
            if (invoiceService.hasUnpaidInvoices(userId)) {
                throw new AppException(ErrorCode.UNPAID_INVOICE_EXISTS);
            }

            boolean activeSessionExists = sessionRepository.existsByPortIdAndStatusIn(
                    request.getPortId(),
                    List.of(ChargingSessionStatus.PREPARING, ChargingSessionStatus.CHARGING)
            );

            if (activeSessionExists) {
                throw new AppException(ErrorCode.SESSION_ALREADY_EXISTS);
            }

            if (request.getBookingId() != null) {
                BookingResponse booking = bookingService.findById(request.getBookingId())
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
                
                if (!booking.getUserId().equals(userId)) {
                    throw new AppException(ErrorCode.FORBIDDEN);
                }
            }

            ChargingSession session = new ChargingSession();
            session.setUserId(userId);
            session.setPortId(request.getPortId());
            session.setBookingId(request.getBookingId());
            session.setStatus(ChargingSessionStatus.PREPARING);

            session = sessionRepository.save(session);

            // Resolve portId → chargePointId (chargerId) + connectorId (portNumber)
            PortResponse port = chargerService.findPortById(request.getPortId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Port not found"));
            String chargePointId = port.getChargerId().toString();
            Integer connectorId = port.getPortNumber();
            String idTag = userId.toString();

            eventPublisher.publishEvent(new SendRemoteStartCommandEvent(
                    session.getId(), chargePointId, connectorId, idTag));

            return converter.convert(session);
        } catch (Exception e) {
            redisTemplate.delete(redisKey);
            throw e;
        }
    }

    @Override
    @Transactional
    public void stopCharging(StopChargingRequest request, Long userId) {
        ChargingSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));

        if (!session.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.SESSION_NOT_OWNED);
        }

        if (session.getStatus() != ChargingSessionStatus.CHARGING
                && session.getStatus() != ChargingSessionStatus.PREPARING
                && session.getStatus() != ChargingSessionStatus.SUSPENDED_EV) {
            throw new AppException(ErrorCode.INVALID_SESSION_STATUS);
        }

        if (session.getTransactionId() == null) {
            log.info("Cancelling PREPARING session {} as transactionId is null. User requested stop before physical transaction started.", session.getId());
            session.setStatus(ChargingSessionStatus.INTERRUPTED);
            session.setEndTime(LocalDateTime.now());
            sessionRepository.save(session);
            chargerService.internalUpdatePortStatus(session.getPortId(), PortStatus.AVAILABLE);
            return;
        }

        // Resolve chargePointId from the session's portId
        PortResponse port = chargerService.findPortById(session.getPortId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Port not found"));
        String chargePointId = port.getChargerId().toString();

        eventPublisher.publishEvent(new SendRemoteStopCommandEvent(
                session.getId(), chargePointId, session.getTransactionId(), "User Requested"));
    }
}
