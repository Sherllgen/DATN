package com.project.evgo.notification;

import com.project.evgo.notification.request.RegisterPushTokenRequest;
import com.project.evgo.notification.response.PushTokenResponse;

import java.util.List;

public interface PushTokenService {

    PushTokenResponse registerToken(Long userId, RegisterPushTokenRequest request);

    void unregisterToken(String deviceToken);

    List<PushTokenResponse> getAllTokensForUser(Long userId);

    void sendPushNotification(Long userId, String title, String body);
}
