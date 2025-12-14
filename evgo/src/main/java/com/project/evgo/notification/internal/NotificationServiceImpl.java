package com.project.evgo.notification.internal;

import com.project.evgo.notification.NotificationService;
import com.project.evgo.notification.response.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of NotificationService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationDtoConverter converter;

    @Override
    public Optional<NotificationResponse> findById(Long id) {
        return converter.toResponse(notificationRepository.findById(id));
    }

    @Override
    public List<NotificationResponse> findByUserId(Long userId) {
        return converter.toResponseList(
                notificationRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @Override
    public List<NotificationResponse> findUnreadByUserId(Long userId) {
        return converter.toResponseList(
                notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId));
    }

    @Override
    public long countUnreadByUserId(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
}
