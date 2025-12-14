package com.project.evgo.config.security;

import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.enums.UserStatus;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.user.internal.User;
import com.project.evgo.user.internal.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom UserDetailsService that loads user from database.
 * Supports loading by email, phone number, or user ID.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by username (email or phone number).
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .or(() -> userRepository.findByPhoneNumber(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return buildUserDetails(user);
    }

    /**
     * Load user by ID (used by JWT filter).
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));

        return buildUserDetails(user);
    }

    private CustomUserDetails buildUserDetails(User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList());

        boolean enabled = user.getStatus() == UserStatus.ACTIVE;

        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getPassword(),
                enabled,
                user.isEmailVerified(),
                user.isPhoneVerified(),
                roles);
    }
}
