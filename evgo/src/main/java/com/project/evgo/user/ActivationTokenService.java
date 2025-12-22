package com.project.evgo.user;

import com.project.evgo.user.internal.User;

public interface ActivationTokenService {
    String generateActivationToken(User user);
    void validateAndActivateAccount(String token);
    void deleteToken(String token);
}
