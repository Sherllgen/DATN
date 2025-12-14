package com.project.evgo.complaint.request;

import com.project.evgo.sharedkernel.enums.ComplaintStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating complaint status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateComplaintStatusRequest {

    @NotNull(message = "Status is required")
    private ComplaintStatus status;

    private String adminNote;
}
