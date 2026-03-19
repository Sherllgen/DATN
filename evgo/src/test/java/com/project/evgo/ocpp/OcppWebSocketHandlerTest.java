package com.project.evgo.ocpp;

import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.response.ChargerResponse;
import com.project.evgo.ocpp.internal.*;
import com.project.evgo.ocpp.internal.OcppActionHandlerImpl.BootNotificationHandler;
import com.project.evgo.ocpp.internal.OcppActionHandlerImpl.HeartbeatHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OcppWebSocketHandler with action routing.
 * Note: chargePointId in WebSocket URL is now the charger's database ID (Long).
 */
@ExtendWith(MockitoExtension.class)
class OcppWebSocketHandlerTest {

    private OcppWebSocketHandler handler;
    private OcppSessionManager sessionManager;
    private OcppMessageParser messageParser;
    private OcppActionRouter actionRouter;
    private PendingCommandManager pendingCommandManager;

    @Mock
    private ChargerService chargerService;

    // Using numeric IDs since we now use charger DB ID
    private static final String CP_ID = "42";
    private static final Long CP_ID_LONG = 42L;

    @BeforeEach
    void setUp() {
        sessionManager = new OcppSessionManager();
        messageParser = new OcppMessageParser();
        pendingCommandManager = new PendingCommandManager();

        BootNotificationHandler bootHandler = new BootNotificationHandler(chargerService);
        HeartbeatHandler heartbeatHandler = new HeartbeatHandler(chargerService);
        actionRouter = new OcppActionRouter(List.of(bootHandler, heartbeatHandler));

        handler = new OcppWebSocketHandler(sessionManager, messageParser, actionRouter, pendingCommandManager, chargerService);
    }

    private WebSocketSession createMockSession(String chargePointId) throws Exception {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getUri()).thenReturn(new URI("/ocpp/" + chargePointId));
        return session;
    }

    // ==================== CONNECTION TESTS ====================

    @Nested
    @DisplayName("Connection Lifecycle Tests")
    class ConnectionLifecycleTests {

        @Test
        @DisplayName("Should register session when connection is established with valid URI")
        void afterConnectionEstablished_ValidUri_RegistersSession() throws Exception {
            WebSocketSession session = createMockSession(CP_ID);

            handler.afterConnectionEstablished(session);

            assertThat(sessionManager.getSession(CP_ID)).isEqualTo(session);
        }

        @Test
        @DisplayName("Should remove session when connection is closed")
        void afterConnectionClosed_RemovesSession() throws Exception {
            WebSocketSession session = createMockSession(CP_ID);
            handler.afterConnectionEstablished(session);
            assertThat(sessionManager.getSession(CP_ID)).isNotNull();

            handler.afterConnectionClosed(session, CloseStatus.NORMAL);

            assertThat(sessionManager.getSession(CP_ID)).isNull();
        }

        @Test
        @DisplayName("Should track multiple charge points simultaneously")
        void afterConnectionEstablished_MultipleChargePoints_TracksAll() throws Exception {
            WebSocketSession session1 = createMockSession("42");
            WebSocketSession session2 = createMockSession("43");

            handler.afterConnectionEstablished(session1);
            handler.afterConnectionEstablished(session2);

            assertThat(sessionManager.getSession("42")).isEqualTo(session1);
            assertThat(sessionManager.getSession("43")).isEqualTo(session2);
            assertThat(sessionManager.getAllChargePointIds()).containsExactlyInAnyOrder("42", "43");
        }
    }

    // ==================== BOOT NOTIFICATION TESTS ====================

    @Nested
    @DisplayName("BootNotification Tests")
    class BootNotificationTests {

        @Test
        @DisplayName("Should accept BootNotification for registered charge point")
        void bootNotification_RegisteredCharger_ReturnsAccepted() throws Exception {
            WebSocketSession session = createMockSession(CP_ID);
            handler.afterConnectionEstablished(session);

            when(chargerService.processBootNotification(eq(CP_ID_LONG), eq("TestVendor"), eq("TestModel"),
                    any(), any()))
                    .thenReturn(Optional.of(ChargerResponse.builder().id(CP_ID_LONG).build()));

            String callJson = "[2,\"msg-001\",\"BootNotification\","
                    + "{\"chargePointVendor\":\"TestVendor\",\"chargePointModel\":\"TestModel\","
                    + "\"chargePointSerialNumber\":\"SN-001\",\"firmwareVersion\":\"1.0\"}]";
            handler.handleMessage(session, new TextMessage(callJson));

            ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
            verify(session).sendMessage(captor.capture());
            String response = captor.getValue().getPayload();
            assertThat(response).startsWith("[3,\"msg-001\"");
            assertThat(response).contains("\"Accepted\"");
            assertThat(response).contains("\"interval\"");
            assertThat(response).contains("\"currentTime\"");
        }

        @Test
        @DisplayName("Should reject BootNotification for unknown charge point")
        void bootNotification_UnknownCharger_ReturnsRejected() throws Exception {
            WebSocketSession session = createMockSession("999");
            handler.afterConnectionEstablished(session);

            when(chargerService.processBootNotification(eq(999L), anyString(), anyString(),
                    any(), any()))
                    .thenReturn(Optional.empty());

            String callJson = "[2,\"msg-002\",\"BootNotification\","
                    + "{\"chargePointVendor\":\"Vendor\",\"chargePointModel\":\"Model\"}]";
            handler.handleMessage(session, new TextMessage(callJson));

            ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
            verify(session).sendMessage(captor.capture());
            String response = captor.getValue().getPayload();
            assertThat(response).contains("\"Rejected\"");
        }

        @Test
        @DisplayName("Should reject BootNotification with missing vendor")
        void bootNotification_MissingVendor_ReturnsRejected() throws Exception {
            WebSocketSession session = createMockSession(CP_ID);
            handler.afterConnectionEstablished(session);

            String callJson = "[2,\"msg-003\",\"BootNotification\","
                    + "{\"chargePointModel\":\"TestModel\"}]";
            handler.handleMessage(session, new TextMessage(callJson));

            ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
            verify(session).sendMessage(captor.capture());
            String response = captor.getValue().getPayload();
            assertThat(response).contains("\"Rejected\"");
            verify(chargerService, never()).processBootNotification(anyLong(), anyString(), anyString(),
                    any(), any());
        }
    }

    // ==================== HEARTBEAT TESTS ====================

    @Nested
    @DisplayName("Heartbeat Tests")
    class HeartbeatTests {

        @Test
        @DisplayName("Should return currentTime for Heartbeat")
        void heartbeat_ValidCall_ReturnsCurrentTime() throws Exception {
            WebSocketSession session = createMockSession(CP_ID);
            handler.afterConnectionEstablished(session);

            String callJson = "[2,\"hb-001\",\"Heartbeat\",{}]";
            handler.handleMessage(session, new TextMessage(callJson));

            ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
            verify(session).sendMessage(captor.capture());
            String response = captor.getValue().getPayload();
            assertThat(response).startsWith("[3,\"hb-001\"");
            assertThat(response).contains("\"currentTime\"");
            verify(chargerService).updateHeartbeat(CP_ID_LONG);
        }
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should send CallError when receiving invalid JSON")
        void handleTextMessage_InvalidJson_SendsCallError() throws Exception {
            WebSocketSession session = createMockSession(CP_ID);
            handler.afterConnectionEstablished(session);

            handler.handleMessage(session, new TextMessage("not valid json"));

            ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
            verify(session).sendMessage(captor.capture());
            String response = captor.getValue().getPayload();
            assertThat(response).startsWith("[4,");
            assertThat(response).contains("FormationViolation");
        }

        @Test
        @DisplayName("Should send NotImplemented error for unknown action")
        void handleTextMessage_UnknownAction_SendsNotImplemented() throws Exception {
            WebSocketSession session = createMockSession(CP_ID);
            handler.afterConnectionEstablished(session);

            String callJson = "[2,\"msg-005\",\"UnknownAction\",{}]";
            handler.handleMessage(session, new TextMessage(callJson));

            ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
            verify(session).sendMessage(captor.capture());
            String response = captor.getValue().getPayload();
            assertThat(response).startsWith("[4,");
            assertThat(response).contains("NotImplemented");
        }

        @Test
        @DisplayName("Should send CallError when receiving unknown message type ID")
        void handleTextMessage_UnknownMessageType_SendsCallError() throws Exception {
            WebSocketSession session = createMockSession(CP_ID);
            handler.afterConnectionEstablished(session);

            handler.handleMessage(session, new TextMessage("[9,\"msg-001\",\"Heartbeat\",{}]"));

            ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
            verify(session).sendMessage(captor.capture());
            String response = captor.getValue().getPayload();
            assertThat(response).startsWith("[4,");
        }
    }
}
