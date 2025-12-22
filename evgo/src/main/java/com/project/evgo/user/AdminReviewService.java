package com.project.evgo.user;

import com.project.evgo.sharedkernel.dto.PageResponse;
import com.project.evgo.user.request.ApprovalRequest;
import com.project.evgo.user.request.RejectionRequest;
import com.project.evgo.user.response.PendingRegistrationResponse;
import com.project.evgo.user.response.RegistrationDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminReviewService {
    PageResponse<PendingRegistrationResponse> getPendingRegistrations(Pageable pageable);
    RegistrationDetailResponse getRegistrationDetail(Long profileId);
    void approveRegistration(Long profileId, ApprovalRequest request);
    void rejectRegistration(Long profileId, RejectionRequest request);
}
