package com.project.evgo.notification.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushTokenResponse {
    
    private Long id;
    
    private Long userId;
    
    private String deviceToken;
    
    private String deviceType;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
