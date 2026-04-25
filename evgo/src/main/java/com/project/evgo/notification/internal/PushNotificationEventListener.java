package com.project.evgo.notification.internal;

import com.project.evgo.sharedkernel.events.SendPushNotificationEvent;
import com.project.evgo.notification.PushTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Listens to {@link SendPushNotificationEvent} published by other modules
 * (e.g., BookingScheduler) and delegates the actual push delivery to
 * {@link PushTokenService}.
 * <p>
 * Uses {@code @ApplicationModuleListener} to guarantee correct
 * transaction/async boundaries as defined by Spring Modulith.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PushNotificationEventListener {

    private final PushTokenService pushTokenService;

    /**
     * Handles incoming push notification events.
     * Looks up push tokens for the target user and dispatches the notification
     * to their registered devices via the Expo Push API.
     *
     * @param event the push notification event containing userId, title, and body
     */
    @ApplicationModuleListener
    public void onSendPushNotification(SendPushNotificationEvent event) {
        log.info("Received SendPushNotificationEvent: userId={}, title='{}'",
                event.userId(), event.title());

        pushTokenService.sendPushNotification(
                event.userId(),
                event.title(),
                event.body()
        );
    }
}
