package com.project.evgo.sharedkernel.events;

/**
 * Event requesting a push notification to be sent to a user.
 *
 * @param userId the target user
 * @param title  notification title
 * @param body   notification body text
 */
public record SendPushNotificationEvent(Long userId, String title, String body) {
}
