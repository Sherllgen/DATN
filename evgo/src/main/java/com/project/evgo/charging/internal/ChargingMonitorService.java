package com.project.evgo.charging.internal;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
 * <p>
 * Maintains a thread-safe registry of active {@link SseEmitter} instances keyed by sessionId.
 * When a MeterValues update arrives, calculates consumedKwh and estimatedCost, then pushes
 * the data to the subscribed mobile client.
 * <p>
 * <b>Performance Optimization — Rate Caching [S-2]:</b>
 * The charging rate per kWh is resolved ONCE when a client subscribes via {@link #subscribe(Long, Long)}
 * and cached in a JVM-local {@link ConcurrentHashMap}. Subsequent {@link #pushUpdate} calls read the
 * cached rate instead of performing 3–4 DB queries (port → charger → station → priceSetting) per tick.
 * The cache entry is automatically evicted when the emitter completes, times out, or errors out.
 * <p>
 * <b>Architectural Limitation — Horizontal Scaling [S-3]:</b>
 * Both the emitter registry ({@code emitters}) and the rate cache ({@code sessionRateCache}) are
 * JVM-local {@link ConcurrentHashMap} instances. This means that SSE connections are pinned to a single
 * application instance. In a horizontally scaled production deployment with multiple instances behind
 * a load balancer, a MeterValues event arriving at instance A cannot push to a client connected via
 * SSE on instance B. To support horizontal scaling, this should be refactored to use
 * <b>Redis Pub/Sub</b>: each instance subscribes to a Redis channel for SSE events, and the instance
 * holding the client's emitter delivers the update. Sticky sessions (e.g., via IP hash or cookie)
 * can serve as an interim mitigation.
 * <p>
 * Connection lifecycle:
 * <ul>
 *   <li>{@link #subscribe(Long, Long)} — resolves + caches rate, creates emitter, registers cleanup callbacks</li>
 *   <li>{@link #pushUpdate(Long, Integer, LocalDateTime)} — calculates cost from cached rate and sends update</li>
 *   <li>Cleanup happens automatically via onCompletion/onTimeout/onError callbacks, which also evict the cached rate</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChargingMonitorService {

    /** SSE timeout: 30 minutes (in milliseconds). */
    private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L;

    private final ChargingSessionRepository sessionRepository;
    private final ChargerService chargerService;
    private final PriceSettingService priceSettingService;

    /** Thread-safe registry: sessionId → active SseEmitter. */
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * Thread-safe cache: sessionId → resolved chargingRatePerKwh.
     * <p>
     * Populated once during {@link #subscribe(Long, Long)} and evicted on emitter cleanup.
     * Avoids 3–4 DB queries per MeterValues tick.
     */
    private final Map<Long, BigDecimal> sessionRateCache = new ConcurrentHashMap<>();

    /**
     * Subscribe to real-time updates for a charging session.
     * <p>
     * Resolves the charging rate per kWh ONCE by navigating
     * {@code portId → chargerId → stationId → activePriceSetting}, then caches the result
     * for the lifetime of this SSE connection. Creates an {@link SseEmitter} with a 30-minute
     * timeout and registers cleanup callbacks that evict both the emitter and the cached rate.
     *
     * @param sessionId the charging session ID
     * @param portId    the port ID used to resolve the charging rate
     * @return the SSE emitter for the client to consume
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

        emitter.onCompletion(() -> {
            emitters.remove(sessionId, emitter);
            sessionRateCache.remove(sessionId);
            log.debug("SSE emitter completed for sessionId={}. Rate cache evicted.", sessionId);
        });

        emitter.onTimeout(() -> {
            emitters.remove(sessionId, emitter);
            sessionRateCache.remove(sessionId);
            log.debug("SSE emitter timed out for sessionId={}. Rate cache evicted.", sessionId);
        });

        emitter.onError((Throwable throwable) -> {
            emitters.remove(sessionId, emitter);
            sessionRateCache.remove(sessionId);
            log.debug("SSE emitter error for sessionId={}: {}. Rate cache evicted.",
                    sessionId, throwable.getMessage());
        });

        log.info("SSE client subscribed to sessionId={}", sessionId);
        return emitter;
    }

    /**
     * Overloaded subscribe for backward compatibility and cases where portId is not known at call site.
     * <p>
     * Resolves the portId from the session in the database. Prefer {@link #subscribe(Long, Long)}
     * when the portId is already available to avoid an extra DB lookup.
     *
     * @param sessionId the charging session ID
     * @return the SSE emitter for the client to consume
     */
    public SseEmitter subscribe(Long sessionId) {
        Optional<ChargingSession> optionalSession = sessionRepository.findById(sessionId);
        Long portId = optionalSession.map(ChargingSession::getPortId).orElse(null);
        return subscribe(sessionId, portId);
    }

    /**
     * Push a real-time meter update to the subscribed SSE client.
     * <p>
     * Calculates:
     * <ul>
     *   <li>consumedKwh = (currentMeterValue - meterStart) / 1000</li>
     *   <li>estimatedCost = consumedKwh × chargingRatePerKwh (from cache)</li>
     * </ul>
     * The charging rate is read from the {@code sessionRateCache} (populated during subscribe),
     * <p>
     * If no emitter is registered for the session (no client connected), silently returns.
     *
     * @param sessionId         the charging session ID
     * @param currentMeterValue the current meter reading in Wh from the charge point
     * @param timestamp         the timestamp of the meter sample
     */
    public void pushUpdate(Long sessionId, Integer currentMeterValue, LocalDateTime timestamp) {
        SseEmitter emitter = emitters.get(sessionId);
        if (emitter == null) {
            log.debug("No SSE subscriber for sessionId={}. Skipping push.", sessionId);
            return;
        }

        try {
            // Fetch session from DB
            Optional<ChargingSession> optionalSession = sessionRepository.findById(sessionId);
            if (optionalSession.isEmpty()) {
                log.warn("Session {} not found in DB. Completing SSE emitter.", sessionId);
                completeEmitter(sessionId, emitter);
                return;
            }

            ChargingSession session = optionalSession.get();

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
            BigDecimal consumedWh = BigDecimal.valueOf(safeMeterValue - meterStart);
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

            emitter.send(SseEmitter.event()
                    .name("meter-update")
                    .data(response));

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

    /**
     * Retrieve the cached charging rate for a session.
     * Falls back to resolving from DB if the cache entry is missing (defensive).
     *
     * @param sessionId the charging session ID
     * @param portId    the port ID (used for fallback resolution)
     * @return the charging rate per kWh
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
     * Resolve the charging rate per kWh by navigating:
     * portId → chargerId → stationId → active PriceSetting.
     * <p>
     * This method performs 3 DB queries. It should only be called ONCE per session
     * (during subscribe or on cache miss). The result is cached in {@code sessionRateCache}.
     *
     * @param portId the port ID from the charging session
     * @return charging rate per kWh, or BigDecimal.ZERO if resolution fails
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

    /**
     * Send a final status update before completing the SSE connection.
     * Uses the cached rate if available; falls back to DB resolution otherwise.
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

            emitter.send(SseEmitter.event()
                    .name("session-ended")
                    .data(response));
        } catch (IOException e) {
            log.debug("Failed to send final update for sessionId={}: {}", sessionId, e.getMessage());
        } finally {
            completeEmitter(sessionId, emitter);
        }
    }

    /**
     * Safely complete and remove an emitter and its cached rate from the registries.
     */
    private void completeEmitter(Long sessionId, SseEmitter emitter) {
        emitters.remove(sessionId, emitter);
        sessionRateCache.remove(sessionId);
        try {
            emitter.complete();
        } catch (Exception e) {
            log.debug("Error completing emitter for sessionId={}: {}", sessionId, e.getMessage());
        }
    }
}
