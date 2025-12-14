package com.project.evgo.notification;

import com.project.evgo.notification.response.NotificationResponse;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for notification management.
 * Public API - accessible by other modules.
 */
public interface NotificationService {

    Optional<NotificationResponse> findById(Long id);

    List<NotificationResponse> findByUserId(Long userId);

    List<NotificationResponse> findUnreadByUserId(Long userId);

    long countUnreadByUserId(Long userId);
}
