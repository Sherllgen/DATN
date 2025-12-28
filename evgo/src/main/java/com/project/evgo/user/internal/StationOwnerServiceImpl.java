package com.project.evgo.user.internal;

import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.user.StationOwnerService;
import com.project.evgo.user.request.TrackingRequest;
import com.project.evgo.user.response.TrackingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StationOwnerServiceImpl implements StationOwnerService {

    private final StationOwnerProfileRepository stationOwnerProfileRepository;

    @Override
    public TrackingResponse getStatus(TrackingRequest request) {
        if (request.email() != null && !request.email().isEmpty()) {
        Optional<StationOwnerProfile> profile = stationOwnerProfileRepository.findByContactEmail(request.email());
            if (profile.isPresent()) {
                return TrackingResponse.builder()
                        .status(profile.get().getStatus())
                        .message("Your registration status is: " + profile.get().getStatus())
                        .build();
            } else {
                throw new AppException(ErrorCode.NOT_FOUND, "Profile not found for the provided email");
            }
        } else if (request.registrationCode() != null && !request.registrationCode().isEmpty()) {
            Optional<StationOwnerProfile> profile = stationOwnerProfileRepository.findByRegistrationCode(request.registrationCode());
            if (profile.isPresent()) {
                return TrackingResponse.builder()
                        .status(profile.get().getStatus())
                        .message("Your registration status is: " + profile.get().getStatus())
                        .build();
            } else {
                throw new AppException(ErrorCode.NOT_FOUND, "Profile not found for the provided registration code");
            }
        } else {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Either email or registration code must be provided");
        }
    }
}
