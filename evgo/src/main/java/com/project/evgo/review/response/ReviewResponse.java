package com.project.evgo.review.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for review information.
 * Public API - accessible by other modules.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;
    private String userName;
    private String userAvatar;
    private Integer rating;
    private String comment;
    private String createdAt;
    private String updatedAt;
    private Boolean isOwner;
}
