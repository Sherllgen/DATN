package com.project.evgo.notification.internal;

import com.project.evgo.notification.response.NotificationResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Converter for Notification entity to DTO.
 */
@Component
public class NotificationDtoConverter {

    public NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    public List<NotificationResponse> toResponseList(List<Notification> notifications) {
        return notifications.stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<NotificationResponse> toResponse(Optional<Notification> notification) {
        return notification.map(this::toResponse);
    }
}
