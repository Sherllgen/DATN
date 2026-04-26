package com.project.evgo.notification.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushTokenRepository extends JpaRepository<PushToken, Long> {

    Optional<PushToken> findByDeviceToken(String deviceToken);

    List<PushToken> findAllByUserId(Long userId);

    void deleteByDeviceToken(String deviceToken);
}
