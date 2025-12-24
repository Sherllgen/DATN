package com.project.evgo.user.request;

import jakarta.validation.constraints.Min;

public record PaginationRequest(
        @Min(value = 0, message = "Page number must be 0 or greater")
        Integer page,

        @Min(value = 1, message = "Page size must be at least 1")
        Integer size,

        String sortBy,
        String sortDirection
) {
    public PaginationRequest {
        page = page != null ? page : 0;
        size = size != null ? size : 10;
        sortBy = sortBy != null ? sortBy : "submittedAt";
        sortDirection = sortDirection != null ? sortDirection : "DESC";
    }
}
