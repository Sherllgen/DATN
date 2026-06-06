package com.project.evgo.charging.internal;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.response.ChargerResponse;
import com.project.evgo.charger.response.PortResponse;
import com.project.evgo.charging.response.ChargingMonitorResponse;
import com.project.evgo.sharedkernel.enums.ChargingSessionStatus;
import com.project.evgo.station.PriceSettingService;
import com.project.evgo.station.response.PriceSettingResponse;

/**
 * Manages Server-Sent Event (SSE) connections for real-time charging session monitoring.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChargingMonitorService {

    // SSE timeout: 30 minutes
    private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L;

    // Heartbeat interval in seconds
    private static final long HEARTBEAT_INTERVAL_SECONDS = 30;

    // Redis key prefix
    private static final String REDIS_METER_KEY_PREFIX = "evgo:charging:meter:";

    private final ChargingSessionRepository sessionRepository;
    private final ChargerService chargerService;
    private final PriceSettingService priceSettingService;
    private final StringRedisTemplate redisTemplate;

    // Thread-safe registry: sessionId → active SseEmitter
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    // Thread-safe cache: sessionId → resolved chargingRatePerKwh
    private final Map<Long, BigDecimal> sessionRateCache = new ConcurrentHashMap<>();

    // Thread-safe registry: sessionId → scheduled heartbeat task
    private final Map<Long, ScheduledFuture<?>> heartbeatTasks = new ConcurrentHashMap<>();

    // Shared single-thread scheduler for all SSE heartbeats
    private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "sse-heartbeat");
        t.setDaemon(true);
        return t;
    });

    /**
     * Subscribe to real-time updates for a charging session.
     */
    public SseEmitter subscribe(Long sessionId, Long portId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        // Resolve and cache the charging rate once at subscription time
        BigDecimal chargingRate = resolveChargingRate(portId);
        sessionRateCache.put(sessionId, chargingRate);
        log.debug("Cached chargingRate={} for sessionId={} (portId={})", chargingRate, sessionId, portId);

        // Remove any existing emitter for this session (reconnect scenario)
        SseEmitter previous = emitters.put(sessionId, emitter);
        if (previous != null) {
            log.info("Replacing existing SSE emitter for sessionId={}", sessionId);
            try {
                previous.complete();
            } catch (Exception e) {
                log.debug("Error completing previous emitter for sessionId={}: {}", sessionId, e.getMessage());
            }
        }

        // Start periodic heartbeat to keep the connection alive through proxies
        startHeartbeat(sessionId);

        // Push initial state immediately so the client doesn't wait for the first MeterValues
        sessionRepository.findById(sessionId).ifPresent(session -> {
            String redisKey = REDIS_METER_KEY_PREFIX + sessionId;
            String cachedMeterStr = redisTemplate.opsForValue().get(redisKey);
            Integer currentMeter = null;
            if (cachedMeterStr != null) {
                try {
                    currentMeter = Integer.parseInt(cachedMeterStr);
                } catch (NumberFormatException ignored) {}
            } else if (session.getStatus() == ChargingSessionStatus.CHARGING) {
                currentMeter = session.getMeterStart();
            }
            log.info("Pushing initial state for sessionId={} upon subscription: status={}, meter={}",
                    sessionId, session.getStatus(), currentMeter);
            pushUpdate(session, currentMeter, LocalDateTime.now());
        });

        // Cleanup callback: runs on normal completion, timeout, or error.
        // Evicts the emitter, cached rate, and cancels the heartbeat task.
        Runnable cleanup = () -> {
            boolean removed = emitters.remove(sessionId, emitter);
            if (removed) {
                sessionRateCache.remove(sessionId);
                cancelHeartbeat(sessionId);
                log.info("SSE emitter cleaned up for sessionId={}. Rate cache evicted, heartbeat cancelled.", sessionId);
            } else {
                log.debug("Old SSE emitter completion callback ignored for sessionId={} (already replaced)", sessionId);
            }
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError((Throwable throwable) -> {
            cleanup.run();
            log.debug("SSE emitter error for sessionId={}: {}", sessionId, throwable.getMessage());
        });

        log.info("SSE client subscribed to sessionId={}", sessionId);
        return emitter;
    }

    /**
     * Overloaded subscribe for backward compatibility.
     */
    public SseEmitter subscribe(Long sessionId) {
        Optional<ChargingSession> optionalSession = sessionRepository.findById(sessionId);
        Long portId = optionalSession.map(ChargingSession::getPortId).orElse(null);
        return subscribe(sessionId, portId);
    }

    /**
     * Returns the latest known meter snapshot for a session.
     */
    public ChargingMonitorResponse getLatestMeterSnapshot(Long sessionId) {
        ChargingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new com.project.evgo.sharedkernel.exceptions.AppException(
                        com.project.evgo.sharedkernel.enums.ErrorCode.SESSION_NOT_FOUND));

        BigDecimal consumedKwh;
        Integer currentMeterWh = null;

        if (session.getTotalKwh() != null) {
            // Session already completed — totalKwh from StopTransaction is authoritative
            consumedKwh = session.getTotalKwh();
        } else {
            // Session in progress — read last known meter value from Redis
            String redisKey = REDIS_METER_KEY_PREFIX + sessionId;
            String cachedMeterStr = redisTemplate.opsForValue().get(redisKey);

            if (cachedMeterStr != null) {
                try {
                    currentMeterWh = Integer.parseInt(cachedMeterStr);
                    Integer meterStart = session.getMeterStart() != null ? session.getMeterStart() : 0;
                    int consumedWh = currentMeterWh < meterStart
                            ? currentMeterWh               // session-relative measurement
                            : currentMeterWh - meterStart; // absolute measurement
                    consumedKwh = BigDecimal.valueOf(consumedWh)
                            .divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP);
                    log.debug("Polling snapshot for sessionId={}: meterWh={} from Redis, consumedKwh={}",
                            sessionId, currentMeterWh, consumedKwh);
                } catch (NumberFormatException e) {
                    log.warn("Invalid meter value in Redis for sessionId={}: '{}'", sessionId, cachedMeterStr);
                    consumedKwh = BigDecimal.ZERO;
                }
            } else {
                // No MeterValues received yet (session just started or Redis evicted)
                log.debug("No Redis meter value for sessionId={}. Defaulting consumedKwh=0.", sessionId);
                consumedKwh = BigDecimal.ZERO;
            }
        }

        BigDecimal chargingRatePerKwh = getCachedRate(sessionId, session.getPortId());
        BigDecimal estimatedCost = consumedKwh.multiply(chargingRatePerKwh)
                .setScale(0, RoundingMode.HALF_UP);

        return ChargingMonitorResponse.builder()
                .status(session.getStatus())
                .consumedKwh(consumedKwh)
                .estimatedCost(estimatedCost)
                .currentMeterValue(currentMeterWh)
                .chargingRatePerKwh(chargingRatePerKwh)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Push a real-time meter update to the subscribed SSE client.
     */
    public void pushUpdate(ChargingSession session, Integer currentMeterValue, LocalDateTime timestamp) {
        Long sessionId = session.getId();
        SseEmitter emitter = emitters.get(sessionId);
        if (emitter == null) {
            log.debug("No SSE subscriber for sessionId={}. Skipping push.", sessionId);
            return;
        }

        try {

            // If session is no longer active, send final status and complete
            if (session.getStatus() != ChargingSessionStatus.CHARGING
                    && session.getStatus() != ChargingSessionStatus.SUSPENDED_EV
                    && session.getStatus() != ChargingSessionStatus.SUSPENDED_EVSE) {
                log.info("Session {} is no longer active (status={}). Sending final update and completing SSE.",
                        sessionId, session.getStatus());
                sendFinalUpdate(sessionId, session, emitter);
                return;
            }

            // Calculate consumed energy
            Integer meterStart = session.getMeterStart() != null ? session.getMeterStart() : 0;
            Integer safeMeterValue = currentMeterValue != null ? currentMeterValue : meterStart;
            
            BigDecimal consumedWh;
            if (safeMeterValue < meterStart) {
                // Defensive check: If the reported meter value is less than the start meter,
                // the charge point is likely sending session-relative measurements.
                consumedWh = BigDecimal.valueOf(safeMeterValue);
            } else {
                consumedWh = BigDecimal.valueOf(safeMeterValue - meterStart);
            }
            
            BigDecimal consumedKwh = consumedWh.divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP);

            // Read cached rate (resolved once at subscribe time) — zero DB queries here
            BigDecimal chargingRatePerKwh = getCachedRate(sessionId, session.getPortId());
            BigDecimal estimatedCost = consumedKwh.multiply(chargingRatePerKwh)
                    .setScale(0, RoundingMode.HALF_UP);

            ChargingMonitorResponse response = ChargingMonitorResponse.builder()
                    .status(session.getStatus())
                    .consumedKwh(consumedKwh)
                    .estimatedCost(estimatedCost)
                    .currentMeterValue(currentMeterValue)
                    .chargingRatePerKwh(chargingRatePerKwh)
                    .timestamp(timestamp != null ? timestamp : LocalDateTime.now())
                    .build();

            synchronized (emitter) {
                emitter.send(SseEmitter.event()
                        .name("meter-update")
                        .data(response));
            }

            log.debug("Pushed meter update to sessionId={}: consumedKwh={}, estimatedCost={}",
                    sessionId, consumedKwh, estimatedCost);

        } catch (IOException e) {
            log.warn("Failed to send SSE event for sessionId={}. Client likely disconnected: {}",
                    sessionId, e.getMessage());
            completeEmitter(sessionId, emitter);
        } catch (Exception e) {
            log.error("Unexpected error pushing SSE update for sessionId={}: {}", sessionId, e.getMessage(), e);
            completeEmitter(sessionId, emitter);
        }
    }

    // ========================================================================
    // Heartbeat Management
    // ========================================================================

    /**
     * Start a periodic heartbeat for the given session.
     */
    private void startHeartbeat(Long sessionId) {
        cancelHeartbeat(sessionId); // Cancel any previous heartbeat for this session

        ScheduledFuture<?> task = heartbeatScheduler.scheduleAtFixedRate(() -> {
            SseEmitter emitter = emitters.get(sessionId);
            if (emitter == null) {
                // Emitter gone — cancel this heartbeat task to free the slot
                cancelHeartbeat(sessionId);
                return;
            }
            try {
                synchronized (emitter) {
                    emitter.send(SseEmitter.event().comment("heartbeat"));
                }
            } catch (IOException e) {
                log.debug("Heartbeat failed for sessionId={} (client likely disconnected). Cleaning up.", sessionId);
                completeEmitter(sessionId, emitter);
            } catch (Exception e) {
                log.debug("Heartbeat error for sessionId={}: {}", sessionId, e.getMessage());
            }
        }, HEARTBEAT_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);

        heartbeatTasks.put(sessionId, task);
        log.debug("Started heartbeat for sessionId={} (interval={}s)", sessionId, HEARTBEAT_INTERVAL_SECONDS);
    }

    /**
     * Cancel and remove the heartbeat task for the given session.
     */
    private void cancelHeartbeat(Long sessionId) {
        ScheduledFuture<?> task = heartbeatTasks.remove(sessionId);
        if (task != null) {
            task.cancel(false); // Don't interrupt if currently running
            log.debug("Cancelled heartbeat for sessionId={}", sessionId);
        }
    }

    // ========================================================================
    // Rate Cache
    // ========================================================================

    /**
     * Retrieve the cached charging rate for a session.
     */
    private BigDecimal getCachedRate(Long sessionId, Long portId) {
        BigDecimal cachedRate = sessionRateCache.get(sessionId);
        if (cachedRate != null) {
            return cachedRate;
        }

        // Defensive fallback: cache miss (shouldn't happen in normal flow)
        log.warn("Rate cache miss for sessionId={}. Resolving from DB (portId={}).", sessionId, portId);
        BigDecimal resolvedRate = resolveChargingRate(portId);
        sessionRateCache.put(sessionId, resolvedRate);
        return resolvedRate;
    }

    /**
     * Resolve the charging rate per kWh.
     */
    private BigDecimal resolveChargingRate(Long portId) {
        try {
            Optional<PortResponse> optionalPort = chargerService.findPortById(portId);
            if (optionalPort.isEmpty()) {
                log.warn("Port {} not found. Defaulting chargingRate to 0.", portId);
                return BigDecimal.ZERO;
            }

            Long chargerId = optionalPort.get().getChargerId();
            Optional<ChargerResponse> optionalCharger = chargerService.findById(chargerId);
            if (optionalCharger.isEmpty()) {
                log.warn("Charger {} not found. Defaulting chargingRate to 0.", chargerId);
                return BigDecimal.ZERO;
            }

            Long stationId = optionalCharger.get().getStationId();
            PriceSettingResponse pricing = priceSettingService.getActivePriceSetting(stationId);
            return pricing.chargingRatePerKwh();

        } catch (Exception e) {
            log.warn("Failed to resolve chargingRate for portId={}: {}", portId, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    // ========================================================================
    // SSE Emitter Lifecycle
    // ========================================================================

    /**
     * Send a final status update before completing the SSE connection.
     */
    private void sendFinalUpdate(Long sessionId, ChargingSession session, SseEmitter emitter) {
        try {
            BigDecimal consumedKwh = session.getTotalKwh() != null ? session.getTotalKwh() : BigDecimal.ZERO;
            BigDecimal chargingRatePerKwh = getCachedRate(sessionId, session.getPortId());
            BigDecimal estimatedCost = consumedKwh.multiply(chargingRatePerKwh).setScale(0, RoundingMode.HALF_UP);

            ChargingMonitorResponse response = ChargingMonitorResponse.builder()
                    .status(session.getStatus())
                    .consumedKwh(consumedKwh)
                    .estimatedCost(estimatedCost)
                    .currentMeterValue(null)
                    .timestamp(LocalDateTime.now())
                    .build();

            synchronized (emitter) {
                emitter.send(SseEmitter.event()
                        .name("session-ended")
                        .data(response));
            }
        } catch (IOException e) {
            log.debug("Failed to send final update for sessionId={}: {}", sessionId, e.getMessage());
        } finally {
            completeEmitter(sessionId, emitter);
        }
    }

    /**
     * Safely complete and remove an emitter.
     */
    private void completeEmitter(Long sessionId, SseEmitter emitter) {
        boolean removed = emitters.remove(sessionId, emitter);
        if (removed) {
            sessionRateCache.remove(sessionId);
            cancelHeartbeat(sessionId);
        }
        try {
            emitter.complete();
        } catch (Exception e) {
            log.debug("Error completing emitter for sessionId={}: {}", sessionId, e.getMessage());
        }
    }
}
