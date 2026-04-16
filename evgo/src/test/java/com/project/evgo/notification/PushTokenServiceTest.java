package com.project.evgo.notification;

import com.project.evgo.notification.internal.PushToken;
import com.project.evgo.notification.internal.PushTokenDtoConverter;
import com.project.evgo.notification.internal.PushTokenRepository;
import com.project.evgo.notification.internal.PushTokenServiceImpl;
import com.project.evgo.notification.request.RegisterPushTokenRequest;
import com.project.evgo.notification.response.PushTokenResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PushTokenServiceTest {

    @Mock
    private PushTokenRepository repository;

    @Mock
    private PushTokenDtoConverter converter;

    @Mock
    private WebClient webClient;

    @InjectMocks
    private PushTokenServiceImpl service;

    // --- registerToken ---

    @Test
    @DisplayName("Should save and return response when registering new token")
    void registerToken_ValidRequest_ReturnsResponse() {
        // Given
        Long userId = 1L;
        RegisterPushTokenRequest request = new RegisterPushTokenRequest("token123", "ios");
        PushToken savedEntity = new PushToken();
        savedEntity.setId(1L);
        savedEntity.setUserId(userId);
        savedEntity.setDeviceToken("token123");

        PushTokenResponse expectedResponse = PushTokenResponse.builder()
                .id(1L)
                .userId(userId)
                .deviceToken("token123")
                .deviceType("ios")
                .build();

        when(repository.findByDeviceToken("token123")).thenReturn(Optional.empty());
        when(repository.save(any(PushToken.class))).thenReturn(savedEntity);
        when(converter.convert(savedEntity)).thenReturn(expectedResponse);

        // When
        PushTokenResponse response = service.registerToken(userId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getDeviceToken()).isEqualTo("token123");
        verify(repository).save(any(PushToken.class));
    }

    @Test
    @DisplayName("Should return existing token if already registered by the same user")
    void registerToken_AlreadyExists_ReturnsExistingResponse() {
        // Given
        Long userId = 1L;
        RegisterPushTokenRequest request = new RegisterPushTokenRequest("token123", "ios");
        PushToken existingEntity = new PushToken();
        existingEntity.setId(1L);
        existingEntity.setUserId(userId);
        existingEntity.setDeviceToken("token123");
        existingEntity.setDeviceType("ios");

        PushTokenResponse expectedResponse = PushTokenResponse.builder()
                .id(1L)
                .userId(userId)
                .deviceToken("token123")
                .deviceType("ios")
                .build();

        when(repository.findByDeviceToken("token123")).thenReturn(Optional.of(existingEntity));
        when(converter.convert(existingEntity)).thenReturn(expectedResponse);

        // When
        PushTokenResponse response = service.registerToken(userId, request);

        // Then
        assertThat(response).isNotNull();
        verify(repository, never()).save(any(PushToken.class));
    }

    @Test
    @DisplayName("Should reassign token to new user when token belongs to a different user")
    void registerToken_ExistingTokenDifferentUser_UpdatesOwner() {
        // Given
        Long originalUserId = 1L;
        Long newUserId = 2L;
        RegisterPushTokenRequest request = new RegisterPushTokenRequest("token123", "android");

        PushToken existingEntity = new PushToken();
        existingEntity.setId(1L);
        existingEntity.setUserId(originalUserId); // owned by different user
        existingEntity.setDeviceToken("token123");
        existingEntity.setDeviceType("ios");

        PushTokenResponse expectedResponse = PushTokenResponse.builder()
                .id(1L)
                .userId(newUserId)
                .deviceToken("token123")
                .deviceType("android")
                .build();

        when(repository.findByDeviceToken("token123")).thenReturn(Optional.of(existingEntity));
        when(repository.save(existingEntity)).thenReturn(existingEntity);
        when(converter.convert(existingEntity)).thenReturn(expectedResponse);

        // When
        PushTokenResponse response = service.registerToken(newUserId, request);

        // Then
        assertThat(response.getUserId()).isEqualTo(newUserId);
        verify(repository).save(existingEntity);
    }

    // --- unregisterToken ---

    @Test
    @DisplayName("Should delete token successfully when token exists")
    void unregisterToken_Exists_DeletesSuccessfully() {
        // Given
        String deviceToken = "token123";
        doNothing().when(repository).deleteByDeviceToken(deviceToken);

        // When
        service.unregisterToken(deviceToken);

        // Then
        verify(repository).deleteByDeviceToken(deviceToken);
    }

    // --- sendPushNotification ---

    @Test
    @DisplayName("Should log warning and not call Expo API when no tokens found for user")
    void sendPushNotification_NoTokens_DoesNotCallExpoApi() {
        // Given
        Long userId = 1L;
        when(repository.findAllByUserId(userId)).thenReturn(Collections.emptyList());

        // When
        service.sendPushNotification(userId, "Test", "Hello");

        // Then
        verify(repository).findAllByUserId(userId);
        // WebClient should NOT be used when no tokens found
        verifyNoInteractions(webClient);
    }

    @Test
    @DisplayName("Should call Expo push API for each registered token")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void sendPushNotification_WithTokens_CallsExpoApiForEachToken() {
        // Given
        Long userId = 1L;

        PushToken token1 = new PushToken();
        token1.setDeviceToken("ExponentPushToken[aaaa]");
        token1.setUserId(userId);

        PushToken token2 = new PushToken();
        token2.setDeviceToken("ExponentPushToken[bbbb]");
        token2.setUserId(userId);

        when(repository.findAllByUserId(userId)).thenReturn(List.of(token1, token2));

        // Mock the WebClient fluent chain using raw types to avoid wildcard capture issues
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("{\"data\":[{\"status\":\"ok\"}]}"));

        // When
        service.sendPushNotification(userId, "Notif Title", "Notif Body");

        // Then
        verify(webClient, times(2)).post(); // called once per token
    }

    @Test
    @DisplayName("Should gracefully handle Expo API error without throwing exception")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void sendPushNotification_ExpoApiError_HandlesGracefully() {
        // Given
        Long userId = 1L;

        PushToken token = new PushToken();
        token.setDeviceToken("ExponentPushToken[aaaa]");
        token.setUserId(userId);

        when(repository.findAllByUserId(userId)).thenReturn(List.of(token));

        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(new RuntimeException("Connection refused")));

        // When / Then: should NOT throw
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> service.sendPushNotification(userId, "Title", "Body")
        );
    }
}
