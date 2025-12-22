package com.project.evgo.user.internal;

import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.user.ActivationTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivationTokenServiceImpl implements ActivationTokenService {

    private final ActivationTokenRepository activationTokenRepository;

    @Value("${app.activation.token.expiry-hours:24}")
    private int tokenExpiryHours;

    @Override
    @Transactional
    public String generateActivationToken(User user) {
        String token = UUID.randomUUID().toString();

        ActivationToken activationToken = new ActivationToken();
        activationToken.setToken(token);
        activationToken.setUserId(user.getId());
        activationToken.setEmail(user.getEmail());
        activationToken.setExpiresAt(LocalDateTime.now().plusHours(tokenExpiryHours));
        activationToken.setUsed(false);

        activationTokenRepository.save(activationToken);

        log.info("Activation token generated for user: {}", user.getEmail());
        return token;
    }

    @Override
    @Transactional
    public void validateAndActivateAccount(String token) {
        ActivationToken activationToken = activationTokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN,
                        "Invalid or expired activation token"));

        if (activationToken.isUsed()) {
            throw new AppException(ErrorCode.TOKEN_ALREADY_USED,
                    "This activation token has already been used");
        }

        if (activationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED,
                    "Activation token has expired");
        }

        activationToken.setUsed(true);
        activationToken.setUsedAt(LocalDateTime.now());
        activationTokenRepository.save(activationToken);

        log.info("Activation token validated for user ID: {}", activationToken.getUserId());
    }

    @Override
    @Transactional
    public void deleteToken(String token) {
        activationTokenRepository.deleteByToken(token);
        log.info("Activation token deleted: {}", token);
    }
}

