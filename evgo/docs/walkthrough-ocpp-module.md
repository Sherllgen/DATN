# Walkthrough — OCPP 1.6J Protocol Module

## Overview

Dedicated Spring Modulith module (`com.project.evgo.ocpp`) implementing OCPP 1.6J Central System over WebSocket: message parsing, session tracking, strategy-based action routing, and handlers for BootNotification, Heartbeat, and StatusNotification.

> **Domain mapping:** `chargePointId` in OCPP = **Charger ID** (database PK) in EV-Go. Each Charger opens its own WebSocket at `ws://host:port/ocpp/{chargerId}`. OCPP Connectors map to **Ports**.

---

## Implemented Actions

| Action | Handler | Behavior |
|--------|---------|----------|
| **BootNotification** | `BootNotificationHandler` | Validates vendor/model → looks up charger by ID → updates metadata (vendor, model, serial, firmware) & status → publishes `ChargePointBootedEvent` → returns `Accepted` with `currentTime` + `interval: 300s`. Returns `Rejected` if charger not found or missing required fields. |
| **Heartbeat** | `HeartbeatHandler` | Updates `lastHeartbeat` timestamp → returns `currentTime` |
| **StatusNotification** | `StatusNotificationHandler` | Logs connector status (connectorId, status, errorCode) → returns `{}` |

---

## Architecture

### Action Routing (Strategy Pattern)

```
WebSocketSession
    → OcppWebSocketHandler (parse message)
        → OcppActionRouter (lookup by action name)
            → OcppActionHandler impl (BootNotification / Heartbeat / StatusNotification)
```

- `OcppActionHandler` — strategy interface: `getAction()` + `handle(chargePointId, call)`
- `OcppActionRouter` — collects all `@Component` handlers via Spring injection, indexes by action name
- Unknown actions → `CALLERROR` with `NotImplemented`

### Cross-Module Communication

```
ocpp → charger (via ChargerService public API)
charger → * (via ChargePointBootedEvent)
```

- `ocpp` module calls `ChargerService.processBootNotification()` and `updateHeartbeat()` directly (synchronous)
- `charger` module publishes `ChargePointBootedEvent(Long chargerId)` for other modules to react

---

## Module Structure

```
ocpp/
├── package-info.java               # @ApplicationModule, depends on sharedkernel + charger
├── OcppMessage.java                # PUBLIC: Sealed interface
├── OcppCall.java                   # PUBLIC: CALL record (typeId=2)
├── OcppCallResult.java             # PUBLIC: CALLRESULT record (typeId=3)
├── OcppCallError.java              # PUBLIC: CALLERROR record (typeId=4)
├── OcppErrorCode.java              # PUBLIC: 10 standard OCPP error codes
└── internal/
    ├── OcppMessageParser.java      # JSON ↔ OcppMessage conversion
    ├── OcppSessionManager.java     # ConcurrentHashMap<chargePointId, session>
    ├── OcppWebSocketHandler.java   # Routes CALLs to action handlers
    ├── OcppWebSocketConfig.java    # Registers handler at /ocpp/*
    ├── OcppProtocolException.java  # Protocol-level runtime exception
    ├── OcppActionHandler.java      # Strategy interface
    ├── OcppActionRouter.java       # Auto-discovers & routes to handlers
    ├── BootNotificationHandler.java
    ├── HeartbeatHandler.java
    └── StatusNotificationHandler.java

charger/ (modified)
├── ChargePointBootedEvent.java     # PUBLIC: Spring event record
├── ChargerService.java            # +processBootNotification(), +updateHeartbeat()
└── internal/
    └── ChargerServiceImpl.java    # Implementation + event publishing
```

---

## Charger API Changes (for OCPP support)

| Change | Details |
|--------|---------|
| Removed `ocppIdentity` field | Using charger database ID directly as OCPP identity |
| `CreateChargerRequest` | Added `@NotNull connectorType` (was hardcoded) |
| `updateCharger()` | Refactored: accepts `UpdateChargerRequest` object instead of individual params |
| Flyway V3 | Added OCPP metadata columns, dropped `ocpp_identity` |

---

## OCPP Message Frame (Reference)

```json
CALL:       [2, "<messageId>", "<action>", {<payload>}]
CALLRESULT: [3, "<messageId>", {<payload>}]
CALLERROR:  [4, "<messageId>", "<errorCode>", "<errorDescription>", {<errorDetails>}]
```

---

## Dependencies

```
ocpp → sharedkernel (ErrorCode)
ocpp → charger     (ChargerService public API)
```

---

## Test Results

| Test Class | Tests | Status |
|------------|-------|--------|
| `OcppMessageParserTest` | 13 | ✅ All pass |
| `OcppWebSocketHandlerTest` | 10 | ✅ All pass |
| `ChargerServiceTest` | 16 | ✅ All pass |
| `ChargerControllerTest` | 13 | ✅ All pass |
| **Total** | **52** | ✅ |

### Key Test Scenarios

- **BootNotification:** Accepted (registered CP), Rejected (unknown CP), Rejected (missing vendor/model)
- **Heartbeat:** Returns `currentTime`, calls `updateHeartbeat()`
- **StatusNotification:** Logs and returns `{}`
- **Error handling:** Invalid JSON → `FormationViolation`, unknown action → `NotImplemented`, unknown message type → `CALLERROR`

---

## Simulator Setup

Use [OCPP CP Simulator](https://shiv3.github.io/ocpp-cp-simulator/):
- **Central System URL:** `ws://localhost:8081/ocpp/`
- **Charge Point Identity:** `{chargerId}` (e.g., `32` = charger DB ID)

> ⚠️ Do NOT put the chargerId in both the URL and the Identity field — the simulator concatenates them.
