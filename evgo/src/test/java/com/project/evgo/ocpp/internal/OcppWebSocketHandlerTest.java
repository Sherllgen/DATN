package com.project.evgo.ocpp.internal;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.response.PortResponse;
import com.project.evgo.sharedkernel.enums.PortStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.util.Optional;

import com.project.evgo.ocpp.OcppCall;
import com.project.evgo.ocpp.OcppCallError;
import com.project.evgo.ocpp.OcppCallResult;
import com.project.evgo.ocpp.OcppErrorCode;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OcppWebSocketHandlerTest {

    @Mock
    private OcppSessionManager sessionManager;

    @Mock
    private OcppMessageParser messageParser;

    @Mock
    private OcppActionRouter actionRouter;

    @Mock
    private PendingCommandManager pendingCommandManager;

    @Mock
    private ChargerService chargerService;

    @Mock
    private WebSocketSession session;

    @InjectMocks
    private OcppWebSocketHandler handler;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(session.getUri()).thenReturn(new URI("ws://localhost:8080/ocpp/1"));
    }

    @Test
    void testAfterConnectionEstablished() throws Exception {
        handler.afterConnectionEstablished(session);
        verify(sessionManager).registerSession("1", session);
    }

    @Test
    void testHandleTextMessage_OcppCall_Handled() throws Exception {
        TextMessage message = new TextMessage("[2,\"123\",\"BootNotification\",{}]");
        OcppCall call = new OcppCall("123", "BootNotification", JsonNodeFactory.instance.objectNode());
        OcppCallResult result = new OcppCallResult("123", JsonNodeFactory.instance.objectNode());

        when(messageParser.parse(anyString())).thenReturn(call);
        when(actionRouter.route("1", call)).thenReturn(Optional.of(result));
        when(messageParser.serialize(result)).thenReturn("[3,\"123\",{}]");

        handler.handleTextMessage(session, message);

        verify(session).sendMessage(any(TextMessage.class));
    }

    @Test
    void testHandleTextMessage_OcppCall_NotImplemented() throws Exception {
        TextMessage message = new TextMessage("[2,\"123\",\"UnknownAction\",{}]");
        OcppCall call = new OcppCall("123", "UnknownAction", JsonNodeFactory.instance.objectNode());
        OcppCallError error = new OcppCallError("123", "NotImplemented", "Action not supported", JsonNodeFactory.instance.objectNode());

        when(messageParser.parse(anyString())).thenReturn(call);
        when(actionRouter.route("1", call)).thenReturn(Optional.empty());
        when(actionRouter.createNotImplementedError(call)).thenReturn(error);
        when(messageParser.serialize(error)).thenReturn("[4,\"123\",\"NotImplemented\",\"Action not supported\",{}]");

        handler.handleTextMessage(session, message);

        verify(session).sendMessage(any(TextMessage.class));
    }

    @Test
    void testHandleTextMessage_OcppCallResult_ReserveNow_Accepted() throws Exception {
        TextMessage message = new TextMessage("[3,\"123\",{\"status\":\"Accepted\"}]");
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("status", "Accepted");
        OcppCallResult callResult = new OcppCallResult("123", payload);

        when(messageParser.parse(anyString())).thenReturn(callResult);
        
        PendingCommandManager.PendingCommand cmd = new PendingCommandManager.PendingCommand("ReserveNow", "1", 1);
        when(pendingCommandManager.pop("123")).thenReturn(cmd);

        PortResponse port = new PortResponse();
        port.setId(100L);
        port.setPortNumber(1);
        port.setChargerId(1L);
        port.setStatus(PortStatus.AVAILABLE);
        
        when(chargerService.findPortByChargerIdAndPortNumber(anyLong(), eq(1))).thenReturn(Optional.of(port));

        handler.handleTextMessage(session, message);

        verify(chargerService).internalUpdatePortStatus(100L, PortStatus.RESERVED);
    }

    @Test
    void testHandleTextMessage_OcppCallResult_ReserveNow_Rejected() throws Exception {
        TextMessage message = new TextMessage("[3,\"123\",{\"status\":\"Rejected\"}]");
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("status", "Rejected");
        OcppCallResult callResult = new OcppCallResult("123", payload);

        when(messageParser.parse(anyString())).thenReturn(callResult);
        
        PendingCommandManager.PendingCommand cmd = new PendingCommandManager.PendingCommand("ReserveNow", "1", 1);
        when(pendingCommandManager.pop("123")).thenReturn(cmd);

        handler.handleTextMessage(session, message);

        verify(chargerService, never()).internalUpdatePortStatus(anyLong(), any());
    }

    @Test
    void testHandleTextMessage_OcppProtocolException() throws Exception {
        TextMessage message = new TextMessage("Invalid JSON");
        
        when(messageParser.parse(anyString())).thenThrow(new OcppProtocolException(OcppErrorCode.FORMATION_VIOLATION, "Invalid format"));
        when(messageParser.serialize(any(OcppCallError.class))).thenReturn("[4,\"\",\"FormatViolation\",\"Invalid format\",{}]");

        handler.handleTextMessage(session, message);

        verify(session).sendMessage(any(TextMessage.class));
    }

    @Test
    void testAfterConnectionClosed() throws Exception {
        handler.afterConnectionClosed(session, CloseStatus.NORMAL);
        verify(sessionManager).removeSession("1");
    }

    @Test
    void testHandleTransportError() throws Exception {
        handler.handleTransportError(session, new RuntimeException("Test Error"));
        verify(sessionManager).removeSession("1");
    }

    @Test
    void testExtractChargePointId_NoUri() {
        when(session.getUri()).thenReturn(null);
        assertThrows(IllegalStateException.class, () -> handler.afterConnectionEstablished(session));
    }
}
