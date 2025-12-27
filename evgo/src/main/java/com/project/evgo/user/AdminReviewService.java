package com.project.evgo.user;

import com.project.evgo.sharedkernel.dto.PageResponse;
import com.project.evgo.sharedkernel.enums.StationOwnerStatus;
import com.project.evgo.user.request.RejectionRequest;
import com.project.evgo.user.response.RegistrationAdminResponse;
import com.project.evgo.user.response.RegistrationDetailResponse;
import org.springframework.data.domain.Pageable;

public interface AdminReviewService {
    PageResponse<RegistrationAdminResponse> getRegistrations(StationOwnerStatus status, Pageable pageable);
    RegistrationDetailResponse getRegistrationDetail(Long profileId);
    void approveRegistration(Long profileId);
    void rejectRegistration(Long profileId, RejectionRequest request);
    void markRegistrationUnderReview(Long profileId);
}
