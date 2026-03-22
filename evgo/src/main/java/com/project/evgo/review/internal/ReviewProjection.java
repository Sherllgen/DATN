package com.project.evgo.review.internal;

import java.time.LocalDateTime;

/**
 * Projection interface for paginated review query with user info.
 * Maps native query result (JOIN reviews + users) to typed fields.
 * Internal - not accessible by other modules.
 */
public interface ReviewProjection {
    Long getId();
    Long getUserId();
    String getUserName();
    String getUserAvatar();
    Integer getRating();
    String getComment();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}

