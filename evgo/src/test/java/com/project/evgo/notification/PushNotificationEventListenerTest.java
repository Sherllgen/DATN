package com.project.evgo.notification;

import com.project.evgo.sharedkernel.events.SendPushNotificationEvent;
import com.project.evgo.notification.internal.PushNotificationEventListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PushNotificationEventListenerTest {

    @Mock
    private PushTokenService pushTokenService;

    @InjectMocks
    private PushNotificationEventListener listener;

    // ── Happy path ──────────────────────────────────────────

    @Test
    @DisplayName("Should forward push notification to PushTokenService when event received")
    void onSendPushNotification_ValidEvent_CallsService() {
        // Given
        Long userId = 42L;
        String title = "Charging Session Ending Soon";
        String body = "Your charging session will end in 15 minutes.";
        SendPushNotificationEvent event = new SendPushNotificationEvent(userId, title, body);

        // When
        listener.onSendPushNotification(event);

        // Then
        verify(pushTokenService).sendPushNotification(userId, title, body);
    }

    @Test
    @DisplayName("Should handle event with different userId correctly")
    void onSendPushNotification_DifferentUserId_CallsServiceWithCorrectId() {
        // Given
        Long userId = 999L;
        String title = "Booking Confirmed";
        String body = "Your booking has been confirmed.";
        SendPushNotificationEvent event = new SendPushNotificationEvent(userId, title, body);

        // When
        listener.onSendPushNotification(event);

        // Then
        verify(pushTokenService).sendPushNotification(999L, "Booking Confirmed", "Your booking has been confirmed.");
    }

    // ── Edge cases ──────────────────────────────────────────

    @Test
    @DisplayName("Should not throw when PushTokenService handles missing token gracefully")
    void onSendPushNotification_NoTokensForUser_DoesNotThrow() {
        // Given — service.sendPushNotification logs warning but doesn't throw
        Long userId = 123L;
        SendPushNotificationEvent event = new SendPushNotificationEvent(
                userId, "Test", "Body");
        doNothing().when(pushTokenService).sendPushNotification(userId, "Test", "Body");

        // When
        listener.onSendPushNotification(event);

        // Then
        verify(pushTokenService).sendPushNotification(userId, "Test", "Body");
    }

    @Test
    @DisplayName("Should propagate exception when PushTokenService throws")
    void onSendPushNotification_ServiceThrows_PropagatesException() {
        // Given
        Long userId = 1L;
        SendPushNotificationEvent event = new SendPushNotificationEvent(
                userId, "Error Test", "Body");
        doThrow(new RuntimeException("Expo API unreachable"))
                .when(pushTokenService).sendPushNotification(userId, "Error Test", "Body");

        // When / Then
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> listener.onSendPushNotification(event));
    }

    @Test
    @DisplayName("Should call service exactly once per event")
    void onSendPushNotification_SingleEvent_InvokesServiceOnce() {
        // Given
        SendPushNotificationEvent event = new SendPushNotificationEvent(
                1L, "Title", "Body");

        // When
        listener.onSendPushNotification(event);

        // Then
        verify(pushTokenService, times(1)).sendPushNotification(1L, "Title", "Body");
        verifyNoMoreInteractions(pushTokenService);
    }
}
