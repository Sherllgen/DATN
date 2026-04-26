package com.project.evgo.notification.internal;

import com.project.evgo.notification.PushTokenService;
import com.project.evgo.notification.request.RegisterPushTokenRequest;
import com.project.evgo.notification.response.PushTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PushTokenServiceImpl implements PushTokenService {

    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    private final PushTokenRepository repository;
    private final PushTokenDtoConverter converter;
    private final WebClient webClient;

    @Override
    @Transactional
    public PushTokenResponse registerToken(Long userId, RegisterPushTokenRequest request) {
        Optional<PushToken> existingOpt = repository.findByDeviceToken(request.getDeviceToken());

        if (existingOpt.isPresent()) {
            PushToken existing = existingOpt.get();
            // If the token already exists but belongs to a different user, update the owner
            if (!existing.getUserId().equals(userId)) {
                existing.setUserId(userId);
                existing.setDeviceType(request.getDeviceType());
                repository.save(existing);
            }
            return converter.convert(existing);
        }

        PushToken newToken = new PushToken();
        newToken.setUserId(userId);
        newToken.setDeviceToken(request.getDeviceToken());
        newToken.setDeviceType(request.getDeviceType());

        PushToken saved = repository.save(newToken);
        return converter.convert(saved);
    }

    @Override
    @Transactional
    public void unregisterToken(String deviceToken) {
        repository.deleteByDeviceToken(deviceToken);
    }

    @Override
    public List<PushTokenResponse> getAllTokensForUser(Long userId) {
        return converter.convert(repository.findAllByUserId(userId));
    }

    @Override
    public void sendPushNotification(Long userId, String title, String body) {
        List<PushToken> tokens = repository.findAllByUserId(userId);
        if (tokens.isEmpty()) {
            log.warn("No device tokens found for user ID: {}", userId);
            return;
        }

        for (PushToken pt : tokens) {
            dispatchExpoPush(pt.getDeviceToken(), title, body);
        }
    }

    /**
     * Sends a single push notification to the Expo push service.
     * Ref: https://docs.expo.dev/push-notifications/sending-notifications/
     *
     * @param expoPushToken the Expo push token of the target device (format: ExponentPushToken[...])
     * @param title         notification title
     * @param body          notification body text
     */
    private void dispatchExpoPush(String expoPushToken, String title, String body) {
        Map<String, Object> payload = Map.of(
                "to", expoPushToken,
                "title", title,
                "body", body,
                "sound", "default"
        );

        try {
            String response = webClient.post()
                    .uri(EXPO_PUSH_URL)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Accept-Encoding", "gzip, deflate")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Expo push notification sent to token {}. Response: {}", expoPushToken, response);
        } catch (WebClientResponseException ex) {
            log.error("Expo push API error for token {}: status={}, body={}",
                    expoPushToken, ex.getStatusCode(), ex.getResponseBodyAsString());
        } catch (Exception ex) {
            log.error("Failed to send push notification to token {}: {}", expoPushToken, ex.getMessage());
        }
    }
}
