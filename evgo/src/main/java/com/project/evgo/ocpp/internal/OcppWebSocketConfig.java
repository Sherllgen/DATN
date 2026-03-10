package com.project.evgo.ocpp.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

/**
 * WebSocket configuration for OCPP 1.6J.
 * <p>
 * Registers the {@link OcppWebSocketHandler} at the path {@code /ocpp/*}
 * with the OCPP 1.6 sub-protocol.
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class OcppWebSocketConfig implements WebSocketConfigurer {

    private final OcppWebSocketHandler ocppWebSocketHandler;
    private final OcppHandshakeInterceptor ocppHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        DefaultHandshakeHandler handshakeHandler = new DefaultHandshakeHandler();
        handshakeHandler.setSupportedProtocols("ocpp1.6");

        registry.addHandler(ocppWebSocketHandler, "/ocpp/*")
                .setHandshakeHandler(handshakeHandler)
                .addInterceptors(ocppHandshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
