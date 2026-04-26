package com.project.evgo.notification.internal;

import com.project.evgo.notification.response.PushTokenResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PushTokenDtoConverter {

    public PushTokenResponse convert(PushToken from) {
        if (from == null) {
            return null;
        }
        return PushTokenResponse.builder()
                .id(from.getId())
                .userId(from.getUserId())
                .deviceToken(from.getDeviceToken())
                .deviceType(from.getDeviceType())
                .createdAt(from.getCreatedAt())
                .updatedAt(from.getUpdatedAt())
                .build();
    }

    public List<PushTokenResponse> convert(List<PushToken> fromList) {
        return fromList.stream().map(this::convert).toList();
    }

    public Optional<PushTokenResponse> convert(Optional<PushToken> fromOpt) {
        return fromOpt.map(this::convert);
    }
}
