package com.project.evgo.user;

import com.project.evgo.user.request.RegistrationRequest;
import com.project.evgo.user.response.RegistrationResponse;

public interface RegistrationService {
    RegistrationResponse submitRegistration(RegistrationRequest request);
}
