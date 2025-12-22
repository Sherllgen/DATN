package com.project.evgo.user.request;

import jakarta.validation.constraints.NotBlank;

public record ApprovalRequest(
        @NotBlank(message = "Approval message is required")
        String approvalMessage
) {}
