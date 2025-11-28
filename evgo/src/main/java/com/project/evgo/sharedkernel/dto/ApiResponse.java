package com.project.evgo.sharedkernel.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class ApiResponse<T> {
    private Integer status;
    private String message;
    private T data;

    public ApiResponse(Integer status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
